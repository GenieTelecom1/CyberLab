package com.cyberlab.service;

import com.cyberlab.database.AuthCodeRepository;
import com.cyberlab.database.UserRepository;
import com.cyberlab.model.User;
import com.cyberlab.service.AuthException.Field;
import com.cyberlab.service.AuthException.Type;
import com.cyberlab.util.PasswordUtil;
import com.cyberlab.util.SessionManager;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    private static final String PG_UNIQUE_VIOLATION = "23505";
    private static final String T_VERIFICATION = "VERIFICATION";
    private static final String T_RESET = "RESET";
    private static final int CODE_MAX_TENTATIVES = 5;
    private static final Duration VALIDITE_VERIFICATION = Duration.ofMinutes(15);
    private static final Duration VALIDITE_RESET = Duration.ofMinutes(10);
    private static final Duration DELAI_RENVOI = Duration.ofSeconds(60);
    private static final SecureRandom RANDOM = new SecureRandom();

    private static volatile EmailSender emailSender = EmailSenders.fromConfig();
    private final UserRepository users = new UserRepository();
    private final AuthCodeRepository codes = new AuthCodeRepository();

    /** Remplace l'envoi par défaut (console) par un vrai envoi SMTP. À appeler une fois au démarrage. */
    public static void setEmailSender(EmailSender sender) {
        emailSender = sender;
    }

    // ------------------------------------------------------------------ inscription

    /** Crée le compte (non vérifié) et envoie le code de vérification par e-mail. */
    public void register(String nom, String email, String password, String confirm) throws AuthException {
        String n = Validator.nom(nom);
        String e = Validator.email(email);
        Validator.password(password);
        Validator.confirmation(password, confirm);
        String hash = PasswordUtil.hash(password);
        try {
            User user;
            try {
                user = users.insert(n, e, hash);
            } catch (SQLException ex) {
                if (!PG_UNIQUE_VIOLATION.equals(ex.getSQLState())) throw ex;
                Optional<UserRepository.Credentials> existant = users.findCredentialsByEmail(e);
                if (existant.isEmpty() || existant.get().user().emailVerifie()) {
                    throw new AuthException(Type.EMAIL_DEJA_UTILISE, Field.EMAIL,
                            "Cet e-mail est déjà utilisé.");
                }
                // Compte jamais vérifié : on remplace nom et mot de passe par la nouvelle saisie,
                // pour qu'on ne puisse pas réserver l'adresse d'une autre personne avec son propre mot de passe.
                user = existant.get().user();
                users.updateNomEtHash(user.id(), n, hash);
            }
            envoyerCode(user, T_VERIFICATION);
        } catch (SQLException ex) {
            throw technique(ex);
        }
    }

    // ------------------------------------------------------------------ connexion

    public User login(String email, String password) throws AuthException {
        String e = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (e.isEmpty() || Validator.isBlank(password)) {
            throw new AuthException(Type.CHAMPS_VIDES, Field.AUCUN,
                    "Veuillez saisir votre e-mail et votre mot de passe.");
        }
        LoginAttemptLimiter.verifier(e);
        try {
            Optional<UserRepository.Credentials> found = users.findCredentialsByEmail(e);
            // Même temps de calcul que l'e-mail existe ou non (évite de deviner les comptes existants)
            String hash = found.isPresent() ? found.get().hash() : Factice.HASH;
            boolean ok = PasswordUtil.verify(password, hash) && found.isPresent();
            if (!ok) {
                LoginAttemptLimiter.enregistrerEchec(e);
                throw new AuthException(Type.IDENTIFIANTS_INCORRECTS, Field.PASSWORD,
                        "E-mail ou mot de passe incorrect.");
            }
            LoginAttemptLimiter.reinitialiser(e);
            User user = found.get().user();
            if (!user.emailVerifie()) {
                try {
                    envoyerCode(user, T_VERIFICATION);
                } catch (AuthException ignore) {
                    LOG.log(Level.INFO, "Nouveau code non envoyé : {0}", ignore.getType());
                }
                throw new AuthException(Type.EMAIL_NON_VERIFIE, Field.AUCUN,
                        "Votre e-mail n'est pas encore vérifié.");
            }
            SessionManager.login(user);
            return user;
        } catch (SQLException ex) {
            throw technique(ex);
        }
    }

    public void logout() {
        SessionManager.logout();
    }

    // ------------------------------------------------------------------ vérification de l'e-mail

    public User verifyEmail(String email, String code) throws AuthException {
        String e = Validator.email(email);
        String c = Validator.code(code);
        try {
            User user = trouver(e).orElseThrow(AuthService::codeInvalide);
            controlerCode(user.id(), T_VERIFICATION, c);
            users.markEmailVerified(user.id());
            User verifie = new User(user.id(), user.nom(), user.email(), true, user.dateCreation());
            SessionManager.login(verifie);
            return verifie;
        } catch (SQLException ex) {
            throw technique(ex);
        }
    }

    public void resendVerification(String email) throws AuthException {
        String e = Validator.email(email);
        try {
            Optional<User> user = trouver(e);
            if (user.isPresent() && !user.get().emailVerifie()) {
                envoyerCode(user.get(), T_VERIFICATION);
            }
        } catch (SQLException ex) {
            throw technique(ex);
        }
    }

    // ------------------------------------------------------------------ mot de passe oublié

    /** Répond toujours de la même façon, que le compte existe ou non (pas de fuite d'information). */
    public void requestPasswordReset(String email) throws AuthException {
        String e = Validator.email(email);
        try {
            Optional<User> user = trouver(e);
            if (user.isPresent()) {
                try {
                    envoyerCode(user.get(), T_RESET);
                } catch (AuthException ex) {
                    if (ex.getType() != Type.TROP_DE_TENTATIVES) throw ex;
                }
            }
        } catch (SQLException ex) {
            throw technique(ex);
        }
    }

    public void resetPassword(String email, String code, String newPassword, String confirm)
            throws AuthException {
        String e = Validator.email(email);
        String c = Validator.code(code);
        Validator.password(newPassword);                 // validé avant de consommer le code
        Validator.confirmation(newPassword, confirm);
        try {
            User user = trouver(e).orElseThrow(AuthService::codeInvalide);
            controlerCode(user.id(), T_RESET, c);
            users.updatePasswordHash(user.id(), PasswordUtil.hash(newPassword));
            users.markEmailVerified(user.id());           // il a prouvé qu'il possède l'adresse
            LoginAttemptLimiter.reinitialiser(e);
        } catch (SQLException ex) {
            throw technique(ex);
        }
    }

    // ------------------------------------------------------------------ outils internes

    private Optional<User> trouver(String emailNormalise) throws SQLException {
        return users.findCredentialsByEmail(emailNormalise).map(UserRepository.Credentials::user);
    }

    private static AuthException codeInvalide() {
        return new AuthException(Type.CODE_INVALIDE, Field.CODE, "Code invalide ou expiré.");
    }

    private void envoyerCode(User user, String type) throws SQLException, AuthException {
        verifierDelaiRenvoi(user.id(), type);
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        Instant maintenant = Instant.now();
        Duration validite = T_RESET.equals(type) ? VALIDITE_RESET : VALIDITE_VERIFICATION;
        codes.invalidateAll(user.id(), type);
        codes.insert(user.id(), type, PasswordUtil.hash(code), maintenant, maintenant.plus(validite));

        String sujet = T_RESET.equals(type)
                ? "CyberLab - Réinitialisation de votre mot de passe"
                : "CyberLab - Vérification de votre adresse e-mail";
        String corps = "Bonjour " + user.nom() + ",\n\n"
                + "Votre code CyberLab est : " + code + "\n"
                + "Il expire dans " + validite.toMinutes() + " minutes.\n\n"
                + "Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.";
        try {
            emailSender.send(user.email(), sujet, corps);
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Envoi de l'e-mail impossible", ex);
            throw new AuthException(Type.ERREUR_TECHNIQUE, Field.AUCUN,
                    "L'e-mail n'a pas pu être envoyé. Réessayez dans un instant.", ex);
        }
    }

    private void verifierDelaiRenvoi(long userId, String type) throws SQLException, AuthException {
        Optional<Instant> derniere = codes.lastCreation(userId, type);
        if (derniere.isPresent()) {
            Instant autorise = derniere.get().plus(DELAI_RENVOI);
            Instant maintenant = Instant.now();
            if (maintenant.isBefore(autorise)) {
                long reste = Duration.between(maintenant, autorise).toSeconds() + 1;
                throw new AuthException(Type.TROP_DE_TENTATIVES, Field.AUCUN,
                        "Patientez " + reste + " secondes avant de demander un nouveau code.");
            }
        }
    }

    private void controlerCode(long userId, String type, String code) throws SQLException, AuthException {
        Optional<AuthCodeRepository.Code> actif = codes.findLatestActive(userId, type);
        if (actif.isEmpty()) {
            throw new AuthException(Type.CODE_EXPIRE, Field.CODE,
                    "Code expiré ou déjà utilisé. Demandez un nouveau code.");
        }
        AuthCodeRepository.Code c = actif.get();
        if (Instant.now().isAfter(c.expireLe())) {
            codes.markUsed(c.id());
            throw new AuthException(Type.CODE_EXPIRE, Field.CODE, "Ce code a expiré. Demandez un nouveau code.");
        }
        if (c.tentatives() >= CODE_MAX_TENTATIVES) {
            codes.markUsed(c.id());
            throw new AuthException(Type.TROP_DE_TENTATIVES, Field.CODE,
                    "Trop d'essais. Demandez un nouveau code.");
        }
        if (!PasswordUtil.verify(code, c.hash())) {
            codes.incrementAttempts(c.id());
            int restant = CODE_MAX_TENTATIVES - c.tentatives() - 1;
            throw new AuthException(Type.CODE_INVALIDE, Field.CODE, restant > 0
                    ? "Code incorrect. Il vous reste " + restant + " essai(s)."
                    : "Code incorrect. Demandez un nouveau code.");
        }
        codes.markUsed(c.id());
    }

    /** Traduit une erreur SQL en message clair ; le détail va dans le journal. */
    static AuthException technique(SQLException ex) {
        LOG.log(Level.WARNING, "Erreur base de données, SQLState=" + ex.getSQLState(), ex);
        String etat = ex.getSQLState() == null ? "" : ex.getSQLState();
        String message;
        if (etat.isEmpty() || etat.startsWith("08")) {
            message = "Impossible de joindre la base de données. Vérifiez votre connexion Internet.";
        } else if (etat.startsWith("28")) {
            message = "Accès à la base refusé : vérifiez la configuration (db.properties).";
        } else if (etat.startsWith("42")) {
            message = "La base n'est pas à jour (migration SQL manquante ?). Contactez le Membre 1.";
        } else {
            message = "Une erreur est survenue côté base de données. Réessayez dans un instant.";
        }
        return new AuthException(Type.ERREUR_TECHNIQUE, Field.AUCUN, message, ex);
    }

    /** Hash factice, calculé à la première utilisation seulement. */
    private static final class Factice {
        static final String HASH = PasswordUtil.hash("mot-de-passe-factice-1");
    }
}

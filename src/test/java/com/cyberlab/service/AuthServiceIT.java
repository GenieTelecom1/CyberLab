package com.cyberlab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.cyberlab.TestDb;
import com.cyberlab.database.DatabaseConnection;
import com.cyberlab.database.UserRepository;
import com.cyberlab.model.User;
import com.cyberlab.util.SessionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests d'intégration (J9) : service + repository + vraie base. Ignorés sans configuration.
 * Chaque test crée un utilisateur à e-mail unique et le supprime ensuite, même en cas d'échec.
 * Le code de vérification est récupéré en remplaçant l'envoi d'e-mails par une boîte de test.
 */
class AuthServiceIT {

    private static final String MDP = "Secret123";
    private static final Pattern CODE = Pattern.compile("est : (\\d{6})");
    private static final List<String[]> BOITE = new CopyOnWriteArrayList<>(); // {destinataire, corps}

    private final AuthService auth = new AuthService();
    private final UserService users = new UserService();
    private final UserRepository repo = new UserRepository();
    private String email;

    @BeforeAll
    static void boiteDeReception() {
        AuthService.setEmailSender((to, sujet, corps) -> BOITE.add(new String[] {to, corps}));
    }

    @AfterAll
    static void fermer() {
        AuthService.setEmailSender(new ConsoleEmailSender());
        DatabaseConnection.close();
    }

    @BeforeEach
    void preparer() {
        assumeTrue(TestDb.isConfigured(), "base non configurée : test ignoré");
        email = "it-" + UUID.randomUUID() + "@test.com";
    }

    @AfterEach
    void nettoyer() throws Exception {
        SessionManager.logout();
        if (email != null && TestDb.isConfigured()) {
            repo.findCredentialsByEmail(email).ifPresent(c -> {
                try {
                    repo.delete(c.user().id());
                } catch (Exception ignore) {
                    // nettoyage au mieux
                }
            });
        }
    }

    // ---------- outils ----------

    private String dernierCode() {
        for (int i = BOITE.size() - 1; i >= 0; i--) {
            if (BOITE.get(i)[0].equals(email)) {
                Matcher m = CODE.matcher(BOITE.get(i)[1]);
                if (m.find()) return m.group(1);
            }
        }
        throw new AssertionError("Aucun code reçu pour " + email);
    }

    /** Inscription + vérification par code : l'utilisateur est alors connecté. */
    private User inscrireEtVerifier() throws Exception {
        auth.register("Test IT", email, MDP, MDP);
        return auth.verifyEmail(email, dernierCode());
    }

    // ---------- critère d'acceptation (4.1 du cahier des charges) ----------

    @Test
    void criterieAcceptation() throws Exception {
        auth.register("Test IT", email, MDP, MDP);                  // 1. créer le compte
        assertFalse(SessionManager.isLoggedIn(), "pas de session avant la vérification de l'e-mail");
        User cree = auth.verifyEmail(email, dernierCode());         //    vérifier l'e-mail
        assertTrue(SessionManager.isLoggedIn());
        auth.logout();                                              // 2. se déconnecter
        assertFalse(SessionManager.isLoggedIn());
        DatabaseConnection.close();                                 // « redémarrage » de l'application
        User retrouve = auth.login(email.toUpperCase(), MDP);       // 3. se reconnecter
        assertEquals(cree.id(), retrouve.id());
        assertEquals(email, retrouve.email());
        assertTrue(SessionManager.isLoggedIn());
    }

    @Test
    void hashEnBaseEstUnHashBcrypt() throws Exception {
        auth.register("Test IT", email, MDP, MDP);
        long id = repo.findCredentialsByEmail(email).orElseThrow().user().id();
        String hash = repo.findHashById(id).orElseThrow();
        assertNotEquals(MDP, hash);
        assertTrue(hash.startsWith("$2a$12$"));
        assertEquals(60, hash.length());
    }

    @Test
    void lAdresseNonVerifieeNePeutPasSeConnecter() throws Exception {
        auth.register("Test IT", email, MDP, MDP);
        AuthException e = assertThrows(AuthException.class, () -> auth.login(email, MDP));
        assertEquals(AuthException.Type.EMAIL_NON_VERIFIE, e.getType());
        assertFalse(SessionManager.isLoggedIn());
    }

    @Test
    void mauvaisCodeDeVerificationEstRefuse() throws Exception {
        auth.register("Test IT", email, MDP, MDP);
        String vrai = dernierCode();
        String faux = vrai.equals("111111") ? "222222" : "111111";
        AuthException e = assertThrows(AuthException.class, () -> auth.verifyEmail(email, faux));
        assertEquals(AuthException.Type.CODE_INVALIDE, e.getType());
        assertFalse(SessionManager.isLoggedIn());
    }

    @Test
    void mauvaisMotDePasseEstRefuse() throws Exception {
        inscrireEtVerifier();
        auth.logout();
        AuthException e = assertThrows(AuthException.class, () -> auth.login(email, "Mauvais123"));
        assertEquals(AuthException.Type.IDENTIFIANTS_INCORRECTS, e.getType());
        assertFalse(SessionManager.isLoggedIn());
    }

    @Test
    void emailDejaUtiliseEstRefuse() throws Exception {
        inscrireEtVerifier();
        AuthException e = assertThrows(AuthException.class,
                () -> auth.register("Autre", email.toUpperCase(), MDP, MDP));
        assertEquals(AuthException.Type.EMAIL_DEJA_UTILISE, e.getType());
    }

    @Test
    void injectionSqlNeContournePasLaConnexion() {
        AuthException e = assertThrows(AuthException.class, () -> auth.login("' OR '1'='1' --", "x"));
        assertEquals(AuthException.Type.IDENTIFIANTS_INCORRECTS, e.getType());
        assertFalse(SessionManager.isLoggedIn());
    }

    /**
     * Deux inscriptions simultanées avec le même e-mail : c'est la contrainte UNIQUE de la base qui
     * tranche. Un compte non vérifié peut être repris par la seconde inscription, donc on vérifie
     * qu'il n'existe qu'UNE ligne et qu'aucune erreur technique inattendue ne remonte.
     */
    @Test
    void deuxInscriptionsSimultaneesNeCreentQuUnCompte() throws Exception {
        Callable<Throwable> tache = () -> {
            try {
                new AuthService().register("Course", email, MDP, MDP);
                return null;
            } catch (AuthException e) {
                return e;
            }
        };
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<Throwable>> resultats = pool.invokeAll(List.of(tache, tache));
        pool.shutdown();
        List<Throwable> erreurs = new ArrayList<>();
        for (Future<Throwable> f : resultats) {
            Throwable t = f.get();
            if (t != null) erreurs.add(t);
        }
        for (Throwable t : erreurs) {
            AuthException ae = (AuthException) t;
            assertNotEquals(AuthException.Type.ERREUR_TECHNIQUE, ae.getType(), ae.getMessage());
        }
        long lignes = repo.findAll().stream().filter(u -> u.email().equals(email)).count();
        assertEquals(1, lignes, "Une seule ligne doit exister pour cet e-mail");
    }

    // ---------- UserService ----------

    @Test
    void suppressionDeCompteEffaceLaSession() throws Exception {
        User u = inscrireEtVerifier();
        AuthException e = assertThrows(AuthException.class, () -> users.deleteAccount(u.id(), "Mauvais123"));
        assertEquals(AuthException.Type.IDENTIFIANTS_INCORRECTS, e.getType());
        assertTrue(SessionManager.isLoggedIn());

        users.deleteAccount(u.id(), MDP);
        assertFalse(SessionManager.isLoggedIn());
        assertTrue(repo.findById(u.id()).isEmpty());
    }

    @Test
    void changementDeMotDePasse() throws Exception {
        User u = inscrireEtVerifier();
        AuthException e = assertThrows(AuthException.class,
                () -> users.changePassword(u.id(), "Mauvais123", "Nouveau456", "Nouveau456"));
        assertEquals(AuthException.Type.ANCIEN_MOT_DE_PASSE_INCORRECT, e.getType());

        users.changePassword(u.id(), MDP, "Nouveau456", "Nouveau456");
        auth.logout();
        assertThrows(AuthException.class, () -> auth.login(email, MDP));
        assertEquals(u.id(), auth.login(email, "Nouveau456").id());
    }

    @Test
    void changementDeNomMetLaSessionAJour() throws Exception {
        User u = inscrireEtVerifier();
        users.updateNom(u.id(), "  Nouveau   Nom ");
        assertEquals("Nouveau Nom", SessionManager.getCurrentUser().nom());
        assertEquals("Nouveau Nom", users.getUser(u.id()).nom());
        AuthException e = assertThrows(AuthException.class, () -> users.getUser(-1));
        assertEquals(AuthException.Type.UTILISATEUR_INTROUVABLE, e.getType());
    }
}

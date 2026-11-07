package com.cyberlab.service;

import com.cyberlab.database.UserRepository;
import com.cyberlab.model.User;
import com.cyberlab.service.AuthException.Field;
import com.cyberlab.service.AuthException.Type;
import com.cyberlab.util.PasswordUtil;
import com.cyberlab.util.SessionManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Opérations sur les comptes (lecture, nom, mot de passe, suppression).
 * Les règles de validation sont dans {@link Validator} ; les erreurs SQL sont traduites
 * par {@link AuthService#technique(SQLException)}.
 */
public class UserService {

    private final UserRepository repo = new UserRepository();

    private static AuthException introuvable() {
        return new AuthException(Type.UTILISATEUR_INTROUVABLE, Field.AUCUN, "Utilisateur introuvable.");
    }

    public User getUser(long id) throws AuthException {
        try {
            return repo.findById(id).orElseThrow(UserService::introuvable);
        } catch (SQLException ex) {
            throw AuthService.technique(ex);
        }
    }

    /** Renvoie les e-mails de tout le monde : pour la démonstration ou les tests, pas pour un écran. */
    public List<User> listUsers() throws AuthException {
        try {
            return repo.findAll();
        } catch (SQLException ex) {
            throw AuthService.technique(ex);
        }
    }

    public User updateNom(long id, String nom) throws AuthException {
        String n = Validator.nom(nom);
        try {
            if (!repo.updateNom(id, n)) throw introuvable();
            User updated = repo.findById(id).orElseThrow(UserService::introuvable);
            User current = SessionManager.getCurrentUser();
            if (current != null && current.id() == id) {
                SessionManager.login(updated); // garde la session à jour
            }
            return updated;
        } catch (SQLException ex) {
            throw AuthService.technique(ex);
        }
    }

    public void changePassword(long id, String ancien, String nouveau, String confirm)
            throws AuthException {
        if (Validator.isBlank(ancien) || Validator.isBlank(nouveau) || Validator.isBlank(confirm)) {
            throw new AuthException(Type.CHAMPS_VIDES, Field.AUCUN, "Tous les champs sont obligatoires.");
        }
        Validator.password(nouveau);
        Validator.confirmation(nouveau, confirm);
        try {
            String hash = repo.findHashById(id).orElseThrow(UserService::introuvable);
            if (!PasswordUtil.verify(ancien, hash)) {
                throw new AuthException(Type.ANCIEN_MOT_DE_PASSE_INCORRECT, Field.AUCUN,
                        "L'ancien mot de passe est incorrect.");
            }
            if (!repo.updatePasswordHash(id, PasswordUtil.hash(nouveau))) throw introuvable();
        } catch (SQLException ex) {
            throw AuthService.technique(ex);
        }
    }

    /** Supprime le compte (et, en cascade, ses scores, sa progression et ses codes). */
    public void deleteAccount(long id, String password) throws AuthException {
        if (Validator.isBlank(password)) {
            throw new AuthException(Type.CHAMPS_VIDES, Field.PASSWORD,
                    "Saisissez votre mot de passe pour confirmer.");
        }
        try {
            String hash = repo.findHashById(id).orElseThrow(UserService::introuvable);
            if (!PasswordUtil.verify(password, hash)) {
                throw new AuthException(Type.IDENTIFIANTS_INCORRECTS, Field.PASSWORD,
                        "Mot de passe incorrect.");
            }
            if (!repo.delete(id)) throw introuvable();
            User current = SessionManager.getCurrentUser();
            if (current != null && current.id() == id) SessionManager.logout();
        } catch (SQLException ex) {
            throw AuthService.technique(ex);
        }
    }
}

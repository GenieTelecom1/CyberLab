package com.cyberlab.util;

import com.cyberlab.model.User;
import com.cyberlab.service.AuthException;

public final class SessionManager {

    private static volatile User currentUser;

    private SessionManager() {}

    public static void login(User user) { currentUser = user; }
    public static void logout() { currentUser = null; }
    public static User getCurrentUser() { return currentUser; }
    public static boolean isLoggedIn() { return currentUser != null; }


    /** Alias de logout(). */
    public static void clear() { logout(); }

    /** Identifiant de l'utilisateur connecté (0 si personne). Préférer getCurrentUser().id(). */
    public static int getCurrentUserId() {
        User u = currentUser;
        return u == null ? 0 : (int) u.id();
    }

    /** Nom affiché dans le message de bienvenue. */
    public static String getCurrentUserName() {
        User u = currentUser;
        return u == null ? "Invité" : u.nom();
    }

    /** Utilisateur connecté, ou AuthException si personne ne l'est. */
    public static User requireUser() throws AuthException {
        User u = currentUser;
        if (u == null) {
            throw new AuthException(AuthException.Type.NON_CONNECTE, AuthException.Field.AUCUN,
                    "Veuillez vous connecter.");
        }
        return u;
    }
}

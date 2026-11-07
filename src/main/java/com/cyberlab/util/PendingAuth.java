package com.cyberlab.util;

/** Données transmises d'un écran d'authentification au suivant (e-mail en cours, message à afficher). */
public final class PendingAuth {

    private static volatile String email;
    private static volatile String flash;

    private PendingAuth() {}

    public static String getEmail() { return email; }
    public static void setEmail(String e) { email = e; }

    public static void setFlash(String message) { flash = message; }

    /** Renvoie le message en attente puis l'efface (affiché une seule fois). */
    public static String consumeFlash() {
        String f = flash;
        flash = null;
        return f;
    }

    public static void clear() {
        email = null;
        flash = null;
    }
}

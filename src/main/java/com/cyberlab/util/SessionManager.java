package com.cyberlab.util;

/**
 * STUB TEMPORAIRE (J3) : permet à M2 de tester son module seul.
 * L'implémentation réelle est convenue avec M1/M6 ; ne pas livrer ce fichier
 * s'il existe déjà une version officielle.
 */
public final class SessionManager {
    private static int currentUserId = 1;
    private static String currentUserName = "Hacker";

    private SessionManager() { }

    public static int getCurrentUserId() { return currentUserId; }
    public static String getCurrentUserName() { return currentUserName; }

    public static void login(int id, String name) { currentUserId = id; currentUserName = name; }
    public static void clear() { currentUserId = 0; currentUserName = ""; }
}

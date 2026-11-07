package com.cyberlab;

import javafx.application.Application;

/**
 * Point d'entrée à utiliser dans VS Code (Run). Une classe qui n'étend pas Application
 * évite l'erreur « JavaFX runtime components are missing ».
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(AuthDemoApp.class, args);
    }
}

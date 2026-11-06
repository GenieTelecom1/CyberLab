package com.cyberlab.demo;

import com.cyberlab.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/** Lanceur de TEST pour M2 uniquement (le vrai MainApp appartient à M6). Ne pas livrer. */
public class DashboardDemoApp extends Application {
    @Override
    public void start(Stage stage) {
        SceneManager.init(stage);
        SceneManager.showShell();
    }
    public static void main(String[] args) { launch(args); }
}

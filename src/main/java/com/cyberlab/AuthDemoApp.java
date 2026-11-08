package com.cyberlab;

import com.cyberlab.database.DatabaseConnection;
import com.cyberlab.service.AuthService;
import com.cyberlab.service.EmailSenders;
import com.cyberlab.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/** Application de test des écrans d'authentification (à lancer via Launcher). */
public class AuthDemoApp extends Application {

    @Override
    public void start(Stage stage) {
        AuthService.setEmailSender(EmailSenders.fromConfig()); // SMTP si configuré, sinon console (dev)
        DatabaseConnection.warmUp();
        stage.setTitle("CyberLab");
        stage.setMinWidth(900);
        stage.setMinHeight(640);
        SceneManager.init(stage);
        SceneManager.switchTo("login");
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseConnection.close();
    }
}

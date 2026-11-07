package com.example.demo1;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.InputStream;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader();

            // Recherche du fichier FXML dans le dossier resources
            InputStream fxmlStream = getClass().getResourceAsStream("/phishing_detector.fxml");
            if (fxmlStream == null) {
                fxmlStream = getClass().getResourceAsStream("phishing_detector.fxml");
            }

            if (fxmlStream == null) {
                System.err.println("❌ Erreur : Le fichier phishing_detector.fxml est introuvable dans resources !");
                return;
            }

            Parent root = loader.load(fxmlStream);
            primaryStage.setTitle("CyberLab - Détecteur de Phishing");
            primaryStage.setScene(new Scene(root, 900, 700));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
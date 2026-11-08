package com.cyberlab.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class ResultatController {

    @FXML private Label scoreLabel;
    @FXML private Label messageLabel;
    @FXML private Button btnRecommencer;
    @FXML private Button btnDashboard;

    private int scoreFinal = 0;
    private int totalQuestions = 0;

    // Méthode pour initialiser les résultats passés depuis le QuizController
    public void setResults(int score, int total) {
        this.scoreFinal = score;
        this.totalQuestions = total;

        if (scoreLabel != null) {
            scoreLabel.setText(scoreFinal + " / " + totalQuestions);
        }

        if (messageLabel != null) {
            double pourcentage = (total > 0) ? ((double) score / total) * 100 : 0;
            if (pourcentage >= 80) {
                messageLabel.setText("Excellent travail ! Maîtrise parfaite de la sécurité.");
            } else if (pourcentage >= 50) {
                messageLabel.setText("Bien joué, mais quelques révisions s'imposent !");
            } else {
                messageLabel.setText("Score faible. Reprenez les cours pour vous améliorer.");
            }
        }
    }

    @FXML
    private void handleRecommencer(ActionEvent event) {
        // Rediriger vers le quiz ou fermer la vue actuelle
        fermerFenetre(event);
    }

    @FXML
    private void handleRetourDashboard(ActionEvent event) {
        // Logique de retour au dashboard principal
        fermerFenetre(event);
    }

    private void fermerFenetre(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
package com.cyberlab.controller;

import com.cyberlab.model.PasswordStrengthResult;
import com.cyberlab.service.PasswordService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class PasswordCheckerController {

    @FXML private PasswordField txtPassword;
    @FXML private ProgressBar progressScore;
    @FXML private Label lblNiveau;
    @FXML private Label lblScorePercent;
    @FXML private VBox vboxCriteres;
    @FXML private VBox vboxConseils;

    private final PasswordService passwordService = new PasswordService();

    @FXML
    public void initialize() {
        // Écouteur en temps réel sur la saisie du mot de passe
        txtPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            analyserMotDePasse(newValue);
        });
    }

    private void analyserMotDePasse(String password) {
        PasswordStrengthResult result = passwordService.evaluerMotDePasse(password);

        double progress = result.getScore() / 100.0;
        progressScore.setProgress(progress);

        lblNiveau.setText("Force : " + result.getNiveau());
        lblNiveau.setTextFill(Color.web(result.getCouleurHex()));
        lblScorePercent.setText(result.getScore() + " / 100");

        vboxCriteres.getChildren().clear();
        for (String critere : result.getCriteresValides()) {
            Label lbl = new Label("✓ " + critere);
            lbl.setTextFill(Color.web("#2ECC71"));
            vboxCriteres.getChildren().add(lbl);
        }

        vboxConseils.getChildren().clear();
        for (String conseil : result.getConseilsAmelioration()) {
            Label lbl = new Label("• " + conseil);
            lbl.setTextFill(Color.web("#E67E22"));
            vboxConseils.getChildren().add(lbl);
        }
    }
}

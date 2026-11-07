package com.cyberlab.controller;

import com.cyberlab.service.AuthException;
import com.cyberlab.service.AuthService;
import com.cyberlab.service.Validator;
import com.cyberlab.util.FormUtil;
import com.cyberlab.util.PendingAuth;
import com.cyberlab.util.SceneManager;
import com.cyberlab.util.UiTask;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/** Vérification de l'adresse e-mail par code à 6 chiffres. */
public class VerifyController {

    private static final int DELAI_RENVOI_S = 60;

    @FXML private Label infoLabel;
    @FXML private TextField codeField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button verifyButton;
    @FXML private Hyperlink resendLink;

    private final AuthService auth = new AuthService();
    private String email;
    private Timeline compteARebours;

    @FXML
    private void initialize() {
        FormUtil.digitsOnly(codeField, Validator.CODE_LENGTH);
        FormUtil.showMessage(errorLabel, "");
        FormUtil.showMessage(successLabel, PendingAuth.consumeFlash());

        email = PendingAuth.getEmail();
        if (email == null) {
            // arrivée sans e-mail en cours : retour à la connexion
            Platform.runLater(() -> SceneManager.switchTo("login"));
            return;
        }
        infoLabel.setText("Nous avons envoyé un code à 6 chiffres à " + email + ".");
        demarrerCompteARebours(DELAI_RENVOI_S);
    }

    private void demarrerCompteARebours(int secondes) {
        arreterCompteARebours();
        int[] restant = {secondes};
        resendLink.setDisable(true);
        resendLink.setText("Renvoyer le code (" + restant[0] + " s)");
        compteARebours = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            restant[0]--;
            if (restant[0] <= 0) {
                resendLink.setDisable(false);
                resendLink.setText("Renvoyer le code");
                compteARebours.stop();
            } else {
                resendLink.setText("Renvoyer le code (" + restant[0] + " s)");
            }
        }));
        compteARebours.setCycleCount(Timeline.INDEFINITE);
        compteARebours.play();
    }

    private void arreterCompteARebours() {
        if (compteARebours != null) compteARebours.stop();
    }

    @FXML
    private void onVerify() {
        if (verifyButton.isDisabled() || email == null) return;
        FormUtil.clear(codeField);
        FormUtil.showMessage(errorLabel, "");
        FormUtil.showMessage(successLabel, "");
        verifyButton.setDisable(true);

        String code = codeField.getText();
        String adresse = email;

        UiTask.run(
            () -> auth.verifyEmail(adresse, code),
            user -> {
                arreterCompteARebours();
                PendingAuth.clear();
                SceneManager.switchTo("dashboard");
            },
            erreur -> {
                FormUtil.showMessage(errorLabel, UiTask.messageFor(erreur));
                FormUtil.setInvalid(codeField, true);
                codeField.requestFocus();
                if (erreur instanceof AuthException ae && ae.getType() == AuthException.Type.CODE_INVALIDE) {
                    codeField.selectAll();
                }
            },
            () -> verifyButton.setDisable(false));
    }

    @FXML
    private void onResend() {
        if (email == null) return;
        FormUtil.showMessage(errorLabel, "");
        FormUtil.showMessage(successLabel, "");
        String adresse = email;
        UiTask.run(
            () -> {
                auth.resendVerification(adresse);
                return null;
            },
            ok -> {
                FormUtil.showMessage(successLabel, "Un nouveau code vient d'être envoyé.");
                demarrerCompteARebours(DELAI_RENVOI_S);
            },
            erreur -> FormUtil.showMessage(errorLabel, UiTask.messageFor(erreur)),
            () -> { });
    }

    @FXML
    private void onBack() {
        arreterCompteARebours();
        PendingAuth.clear();
        SceneManager.switchTo("login");
    }
}

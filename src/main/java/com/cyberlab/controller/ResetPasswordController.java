package com.cyberlab.controller;

import com.cyberlab.service.AuthException;
import com.cyberlab.service.AuthService;
import com.cyberlab.service.Validator;
import com.cyberlab.util.FormUtil;
import com.cyberlab.util.PendingAuth;
import com.cyberlab.util.SceneManager;
import com.cyberlab.util.UiTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

/** Saisie du code reçu par e-mail et du nouveau mot de passe. */
public class ResetPasswordController {

    @FXML private Label infoLabel;
    @FXML private TextField codeField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button togglePassword;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button resetButton;

    private final AuthService auth = new AuthService();
    private String email;

    @FXML
    private void initialize() {
        FormUtil.digitsOnly(codeField, Validator.CODE_LENGTH);
        FormUtil.bindReveal(passwordField, passwordVisible, togglePassword);
        FormUtil.showMessage(errorLabel, "");
        FormUtil.showMessage(successLabel, PendingAuth.consumeFlash());
        FormUtil.applyStrength(strengthBar, strengthLabel, "");
        passwordField.textProperty().addListener((obs, ancien, nouveau) ->
                FormUtil.applyStrength(strengthBar, strengthLabel, nouveau));

        email = PendingAuth.getEmail();
        if (email == null) {
            Platform.runLater(() -> SceneManager.switchTo("forgot"));
            return;
        }
        infoLabel.setText("Saisissez le code envoyé à " + email + " et choisissez un nouveau mot de passe.");
    }

    private Node champPour(AuthException.Field f) {
        return switch (f) {
            case CODE -> codeField;
            case PASSWORD -> passwordField;
            case CONFIRM -> confirmField;
            default -> null;
        };
    }

    @FXML
    private void onReset() {
        if (resetButton.isDisabled() || email == null) return;
        FormUtil.clear(codeField, passwordField, confirmField);
        FormUtil.showMessage(errorLabel, "");
        FormUtil.showMessage(successLabel, "");
        resetButton.setDisable(true);

        String adresse = email;
        String code = codeField.getText();
        String password = passwordField.getText();
        String confirm = confirmField.getText();

        UiTask.run(
            () -> {
                auth.resetPassword(adresse, code, password, confirm);
                return null;
            },
            ok -> {
                PendingAuth.setEmail(adresse); // pré-remplit l'e-mail sur l'écran de connexion
                PendingAuth.setFlash("Mot de passe modifié. Vous pouvez maintenant vous connecter.");
                SceneManager.switchTo("login");
            },
            erreur -> {
                FormUtil.showMessage(errorLabel, UiTask.messageFor(erreur));
                if (erreur instanceof AuthException ae) {
                    Node champ = champPour(ae.getField());
                    if (champ != null) {
                        FormUtil.setInvalid(champ, true);
                        champ.requestFocus();
                    }
                }
            },
            () -> resetButton.setDisable(false));
    }

    /** « Je n'ai pas reçu de code » : retour à la demande. */
    @FXML
    private void onNoCode() {
        SceneManager.switchTo("forgot");
    }

    @FXML
    private void onBack() {
        PendingAuth.clear();
        SceneManager.switchTo("login");
    }
}

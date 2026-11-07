package com.cyberlab.controller;

import com.cyberlab.service.AuthException;
import com.cyberlab.service.AuthService;
import com.cyberlab.service.Validator;
import com.cyberlab.util.FormUtil;
import com.cyberlab.util.PendingAuth;
import com.cyberlab.util.SceneManager;
import com.cyberlab.util.UiTask;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button togglePassword;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;

    private final AuthService auth = new AuthService();

    private interface Verification { void executer() throws AuthException; }

    @FXML
    private void initialize() {
        FormUtil.limit(nomField, Validator.NOM_MAX);
        FormUtil.limit(emailField, Validator.EMAIL_MAX);
        FormUtil.bindReveal(passwordField, passwordVisible, togglePassword);
        FormUtil.showMessage(errorLabel, "");
        FormUtil.applyStrength(strengthBar, strengthLabel, "");

        passwordField.textProperty().addListener((obs, ancien, nouveau) ->
                FormUtil.applyStrength(strengthBar, strengthLabel, nouveau));

        verifierALaSortie(nomField, () -> Validator.nom(nomField.getText()));
        verifierALaSortie(emailField, () -> Validator.email(emailField.getText()));
        verifierALaSortie(passwordField, () -> Validator.password(passwordField.getText()));
    }

    private void verifierALaSortie(TextInputControl champ, Verification v) {
        champ.focusedProperty().addListener((obs, avait, a) -> {
            if (avait && !a && !champ.getText().isEmpty()) {
                try {
                    v.executer();
                    FormUtil.setInvalid(champ, false);
                    FormUtil.showMessage(errorLabel, "");
                } catch (AuthException e) {
                    FormUtil.setInvalid(champ, true);
                    FormUtil.showMessage(errorLabel, e.getMessage());
                }
            }
        });
    }

    private Node champPour(AuthException.Field f) {
        return switch (f) {
            case NOM -> nomField;
            case EMAIL -> emailField;
            case PASSWORD -> passwordField;
            case CONFIRM -> confirmField;
            default -> null;
        };
    }

    @FXML
    private void onRegister() {
        if (registerButton.isDisabled()) return;
        FormUtil.clear(nomField, emailField, passwordField, confirmField);
        FormUtil.showMessage(errorLabel, "");
        registerButton.setDisable(true);

        String nom = nomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirm = confirmField.getText();

        UiTask.run(
            () -> {
                auth.register(nom, email, password, confirm);
                return null;
            },
            ok -> {
                PendingAuth.setEmail(email.trim().toLowerCase(Locale.ROOT));
                PendingAuth.setFlash("Compte créé. Un code de vérification vient de vous être envoyé.");
                SceneManager.switchTo("verify");
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
            () -> registerButton.setDisable(false));
    }

    @FXML
    private void onGoToLogin() {
        SceneManager.switchTo("login");
    }
}

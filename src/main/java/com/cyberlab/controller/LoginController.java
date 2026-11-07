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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button togglePassword;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button loginButton;

    private final AuthService auth = new AuthService();

    @FXML
    private void initialize() {
        FormUtil.limit(emailField, Validator.EMAIL_MAX);
        FormUtil.bindReveal(passwordField, passwordVisible, togglePassword);
        FormUtil.showMessage(errorLabel, "");
        FormUtil.showMessage(successLabel, PendingAuth.consumeFlash());
        String prefill = PendingAuth.getEmail();
        if (prefill != null) {
            emailField.setText(prefill);
            PendingAuth.clear();
        }
    }

    @FXML
    private void onLogin() {
        if (loginButton.isDisabled()) return;
        FormUtil.clear(emailField, passwordField);
        FormUtil.showMessage(errorLabel, "");
        FormUtil.showMessage(successLabel, "");
        loginButton.setDisable(true);

        String email = emailField.getText();
        String password = passwordField.getText();

        UiTask.run(
            () -> auth.login(email, password),
            user -> {
                passwordField.clear();
                SceneManager.switchTo("dashboard");
            },
            erreur -> {
                if (erreur instanceof AuthException ae && ae.getType() == AuthException.Type.EMAIL_NON_VERIFIE) {
                    // compte non vérifié : un nouveau code vient d'être envoyé
                    PendingAuth.setEmail(email.trim().toLowerCase(Locale.ROOT));
                    PendingAuth.setFlash("Votre e-mail n'est pas encore vérifié. Un code vient de vous être envoyé.");
                    SceneManager.switchTo("verify");
                    return;
                }
                FormUtil.showMessage(errorLabel, UiTask.messageFor(erreur));
                if (erreur instanceof AuthException ex && ex.getField() == AuthException.Field.PASSWORD) {
                    FormUtil.setInvalid(passwordField, true);
                    passwordField.clear();
                }
            },
            () -> loginButton.setDisable(false));
    }

    @FXML
    private void onForgot() {
        PendingAuth.setEmail(emailField.getText());
        SceneManager.switchTo("forgot");
    }

    @FXML
    private void onGoToRegister() {
        SceneManager.switchTo("register");
    }
}

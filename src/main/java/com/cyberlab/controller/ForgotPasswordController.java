package com.cyberlab.controller;

import com.cyberlab.service.Validator;
import com.cyberlab.service.AuthService;
import com.cyberlab.util.FormUtil;
import com.cyberlab.util.PendingAuth;
import com.cyberlab.util.SceneManager;
import com.cyberlab.util.UiTask;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Demande de code pour réinitialiser un mot de passe oublié. */
public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Label errorLabel;
    @FXML private Button sendButton;

    private final AuthService auth = new AuthService();

    @FXML
    private void initialize() {
        FormUtil.limit(emailField, Validator.EMAIL_MAX);
        FormUtil.showMessage(errorLabel, "");
        String prefill = PendingAuth.getEmail();
        if (prefill != null) emailField.setText(prefill);
    }

    @FXML
    private void onSend() {
        if (sendButton.isDisabled()) return;
        FormUtil.clear(emailField);
        FormUtil.showMessage(errorLabel, "");
        sendButton.setDisable(true);

        String email = emailField.getText();

        UiTask.run(
            () -> {
                auth.requestPasswordReset(email);
                return null;
            },
            ok -> {
                PendingAuth.setEmail(email.trim().toLowerCase(Locale.ROOT));
                PendingAuth.setFlash("Si un compte existe pour cette adresse, un code vient d'être envoyé.");
                SceneManager.switchTo("reset");
            },
            erreur -> {
                FormUtil.showMessage(errorLabel, UiTask.messageFor(erreur));
                FormUtil.setInvalid(emailField, true);
            },
            () -> sendButton.setDisable(false));
    }

    @FXML
    private void onBack() {
        PendingAuth.clear();
        SceneManager.switchTo("login");
    }
}

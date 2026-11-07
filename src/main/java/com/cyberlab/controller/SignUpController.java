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
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

/**
 * Écran d'inscription simple (signup.fxml), version J6 + correctif B4.
 * L'écran principal est register.fxml (RegisterController) ; ce contrôleur reste cohérent avec lui :
 * après l'inscription, le compte doit être vérifié par code e-mail avant la première connexion.
 */
public class SignUpController {

    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;
    @FXML private Button signUpButton;

    private final AuthService authService = new AuthService();

    private interface Verification { void executer() throws AuthException; }

    @FXML
    private void initialize() {
        FormUtil.limit(nomField, Validator.NOM_MAX);
        FormUtil.limit(emailField, Validator.EMAIL_MAX);
        FormUtil.limit(passwordField, 128);
        FormUtil.limit(confirmField, 128);

        verifierALaSortie(nomField, () -> Validator.nom(nomField.getText()));
        verifierALaSortie(emailField, () -> Validator.email(emailField.getText()));
        verifierALaSortie(passwordField, () -> Validator.password(passwordField.getText()));
        FormUtil.showMessage(errorLabel, "");
    }

    /** Valide un champ quand on le quitte : réponse immédiate, sans appel réseau. */
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
    private void onSignUp() {
        if (signUpButton.isDisabled()) return; // B4 : un second Entrée pendant l'appel est ignoré
        FormUtil.clear(nomField, emailField, passwordField, confirmField);
        FormUtil.showMessage(errorLabel, "");
        signUpButton.setDisable(true);

        String nom = nomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirm = confirmField.getText();

        UiTask.run(
            () -> {
                authService.register(nom, email, password, confirm);
                return null;
            },
            ok -> {
                passwordField.clear();
                confirmField.clear();
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
            () -> signUpButton.setDisable(false));
    }

    @FXML
    private void onGoToLogin() {
        SceneManager.switchTo("login");
    }
}

package com.cyberlab.controller;
import com.cyberlab.service.AuthException;
import com.cyberlab.service.AuthService;
import com.cyberlab.util.SceneManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
public class SignUpController {
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;
    @FXML private Button signUpButton;
    private final AuthService authService = new AuthService();
    @FXML
    private void onSignUp() {
        errorLabel.setText("");
        signUpButton.setDisable(true);
        String nom = nomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirm = confirmField.getText();
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                authService.register(nom, email, password, confirm);
                return null;
                 }
        };
        task.setOnSucceeded(ev -> {
            passwordField.clear();
            confirmField.clear();
            signUpButton.setDisable(false);
            SceneManager.switchTo("dashboard");
        });
        task.setOnFailed(ev -> {
            signUpButton.setDisable(false);
            Throwable t = task.getException();
            errorLabel.setText(t instanceof AuthException ? t.getMessage() : 
"Erreur inattendue.");
        });
        Thread th = new Thread(task, "signup-task");
        th.setDaemon(true);
        th.start();
    }
    @FXML
    private void onGoToLogin() {
        SceneManager.switchTo("login");
    }
}
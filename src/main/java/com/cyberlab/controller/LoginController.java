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

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    private final AuthService authService = new AuthService();
    @FXML
    private void onLogin() {
        errorLabel.setText("");
        loginButton.setDisable(true);
        String email = emailField.getText();
        String password = passwordField.getText();
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                authService.login(email, password);
                return null;
            }
        };
        task.setOnSucceeded(ev -> {
            passwordField.clear();
            loginButton.setDisable(false);
            SceneManager.switchTo("dashboard");
        });
        task.setOnFailed(ev -> {
            loginButton.setDisable(false);
            Throwable t = task.getException();
            errorLabel.setText(t instanceof AuthException ? t.getMessage() : "Erreur inattendue.");
        });
        Thread th = new Thread(task, "login-task");
        th.setDaemon(true);
        th.start();
    }
    @FXML

     private void onGoToSignUp() {
        SceneManager.switchTo("signup");
    }
}
package com.cyberlab.util;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;

/** Petits utilitaires communs aux formulaires d'authentification. */
public final class FormUtil {

    private static final String INVALID = "invalid";

    private FormUtil() {}

    // ---------- état d'erreur des champs ----------

    public static void setInvalid(Node node, boolean invalid) {
        node.getStyleClass().remove(INVALID);
        if (invalid) node.getStyleClass().add(INVALID);
    }

    public static void clear(Node... nodes) {
        for (Node n : nodes) setInvalid(n, false);
    }

    /** Affiche un message dans un Label (masqué et sans place réservée si le message est vide). */
    public static void showMessage(Label label, String message) {
        boolean visible = message != null && !message.isEmpty();
        label.setText(visible ? message : "");
        label.setVisible(visible);
        label.setManaged(visible);
    }

    // ---------- saisie ----------

    /** Limite le nombre de caractères d'un champ. */
    public static void limit(TextInputControl field, int max) {
        field.setTextFormatter(new TextFormatter<String>(
                c -> c.getControlNewText().length() <= max ? c : null));
    }

    /** N'accepte que des chiffres, 6 au maximum (code reçu par e-mail). */
    public static void digitsOnly(TextInputControl field, int max) {
        field.setTextFormatter(new TextFormatter<String>(
                c -> c.getControlNewText().matches("[0-9]{0," + max + "}") ? c : null));
    }

    /** Bouton « Afficher / Masquer » pour un champ mot de passe. */
    public static void bindReveal(PasswordField password, TextField visible, Button toggle) {
        visible.textProperty().bindBidirectional(password.textProperty());
        visible.setVisible(false);
        visible.setManaged(false);
        // le style d'erreur du champ masqué est recopié sur le champ visible
        password.getStyleClass().addListener((ListChangeListener<String>) ch -> {
            visible.getStyleClass().remove(INVALID);
            if (password.getStyleClass().contains(INVALID)) visible.getStyleClass().add(INVALID);
        });
        toggle.setOnAction(ev -> {
            boolean show = !visible.isVisible();
            visible.setVisible(show);
            visible.setManaged(show);
            password.setVisible(!show);
            password.setManaged(!show);
            toggle.setText(show ? "Masquer" : "Afficher");
            (show ? visible : password).requestFocus();
        });
    }

    // ---------- force du mot de passe ----------

    /** Score de 0 (très faible) à 4 (excellent). */
    public static int strength(String p) {
        if (p == null || p.isEmpty()) return 0;
        int s = 0;
        if (p.length() >= 8) s++;
        if (p.length() >= 12) s++;
        if (p.chars().anyMatch(Character::isUpperCase) && p.chars().anyMatch(Character::isLowerCase)) s++;
        if (p.chars().anyMatch(Character::isDigit)) s++;
        if (p.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch))) s++;
        return Math.min(s, 4);
    }

    public static void applyStrength(ProgressBar bar, Label label, String password) {
        int s = strength(password);
        String[] noms = {"Très faible", "Faible", "Moyen", "Bon", "Excellent"};
        bar.setProgress(s / 4.0);
        bar.getStyleClass().removeAll("strength-weak", "strength-medium", "strength-strong");
        bar.getStyleClass().add(s <= 1 ? "strength-weak" : s == 2 ? "strength-medium" : "strength-strong");
        label.setText(password == null || password.isEmpty() ? "" : noms[s]);
    }
}

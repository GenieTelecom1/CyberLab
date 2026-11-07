package com.example.demo1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PasswordService {

    /**
     * Évalue la robustesse d'un mot de passe en temps réel.
     * RÈGLE DE SÉCURITÉ : Aucun mot de passe n'est jamais stocké en BDD !
     */
    public PasswordResult evaluerMotDePasse(String mdp) {
        if (mdp == null || mdp.isEmpty()) {
            return new PasswordResult(
                    0,
                    "TRÈS FAIBLE",
                    "#E74C3C",
                    new ArrayList<>(),
                    Arrays.asList("Saisissez un mot de passe"),
                    0
            );
        }

        int score = 0;
        List<String> valides = new ArrayList<>();
        List<String> conseils = new ArrayList<>();

        // 1. Longueur
        if (mdp.length() >= 12) {
            score += 30;
            valides.add("Longueur suffisante (>= 12 caractères)");
        } else if (mdp.length() >= 8) {
            score += 15;
            valides.add("Longueur moyenne (>= 8 caractères)");
            conseils.add("Visez au moins 12 caractères pour une sécurité optimale");
        } else {
            conseils.add("Mot de passe trop court (moins de 8 caractères)");
        }

        // 2. Majuscules
        if (mdp.matches(".*[A-Z].*")) {
            score += 15;
            valides.add("Contient des majuscules (A-Z)");
        } else {
            conseils.add("Ajoutez au moins une lettre majuscule");
        }

        // 3. Minuscules
        if (mdp.matches(".*[a-z].*")) {
            score += 15;
            valides.add("Contient des minuscules (a-z)");
        } else {
            conseils.add("Ajoutez au moins une lettre minuscule");
        }

        // 4. Chiffres
        if (mdp.matches(".*[0-9].*")) {
            score += 20;
            valides.add("Contient des chiffres (0-9)");
        } else {
            conseils.add("Ajoutez au moins un chiffre");
        }

        // 5. Caractères spéciaux
        if (mdp.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            score += 20;
            valides.add("Contient des symboles spéciaux (!@#$...)");
        } else {
            conseils.add("Ajoutez au moins un symbole spécial (!@#$...)");
        }

        // Déterminer le niveau et la couleur
        String niveau;
        String couleur;
        if (score >= 85) {
            niveau = "TRÈS FORT";
            couleur = "#2ECC71"; // Vert
        } else if (score >= 65) {
            niveau = "FORT";
            couleur = "#27AE60"; // Vert foncé
        } else if (score >= 40) {
            niveau = "MOYEN";
            couleur = "#F39C12"; // Orange
        } else {
            niveau = "FAIBLE";
            couleur = "#E74C3C"; // Rouge
        }

        double tempsCrackSecondes = Math.pow(94, mdp.length()) / 1e10;

        return new PasswordResult(score, niveau, couleur, valides, conseils, tempsCrackSecondes);
    }
}


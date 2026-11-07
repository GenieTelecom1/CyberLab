package com.cyberlab.service;

import com.cyberlab.model.PasswordStrengthResult;
import java.util.ArrayList;
import java.util.List;

public class PasswordService {

    public PasswordStrengthResult evaluerMotDePasse(String motDePasse) {
        List<String> valides = new ArrayList<>();
        List<String> conseils = new ArrayList<>();
        int score = 0;

        if (motDePasse.length() >= 8) { score += 20; valides.add("Au moins 8 caractères"); }
        else conseils.add("Utilisez au moins 8 caractères");

        if (motDePasse.matches(".*[A-Z].*")) { score += 20; valides.add("Majuscule"); }
        else conseils.add("Ajoutez une majuscule");

        if (motDePasse.matches(".*[a-z].*")) { score += 20; valides.add("Minuscule"); }
        else conseils.add("Ajoutez une minuscule");

        if (motDePasse.matches(".*\\d.*")) { score += 20; valides.add("Chiffre"); }
        else conseils.add("Ajoutez un chiffre");

        if (motDePasse.matches(".*[^a-zA-Z0-9].*")) { score += 20; valides.add("Caractère spécial"); }
        else conseils.add("Ajoutez un caractère spécial");

        String niveau;
        String couleur;
        if (score < 40)       { niveau = "FAIBLE";    couleur = "#e74c3c"; }
        else if (score < 70)  { niveau = "MOYEN";     couleur = "#f39c12"; }
        else if (score < 100) { niveau = "FORT";      couleur = "#2ecc71"; }
        else                  { niveau = "TRÈS FORT"; couleur = "#27ae60"; }

        double tempsCrack = Math.pow(2, motDePasse.length()) / 1_000_000.0;

        return new PasswordStrengthResult(score, niveau, couleur, valides, conseils, tempsCrack);
    }
}
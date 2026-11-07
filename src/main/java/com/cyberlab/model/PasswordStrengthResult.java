package com.cyberlab.model;

import java.util.List;

public class PasswordStrengthResult {
    private final int score;
    private final String niveau;
    private final String couleurHex;
    private final List<String> criteresValides;
    private final List<String> conseilsAmelioration;
    private final double tempsEstimationCrack;

    public PasswordStrengthResult(int score, String niveau, String couleurHex,
                                  List<String> criteresValides,
                                  List<String> conseilsAmelioration,
                                  double tempsEstimationCrack) {
        this.score = score;
        this.niveau = niveau;
        this.couleurHex = couleurHex;
        this.criteresValides = criteresValides;
        this.conseilsAmelioration = conseilsAmelioration;
        this.tempsEstimationCrack = tempsEstimationCrack;
    }

    public int getScore() { return score; }
    public String getNiveau() { return niveau; }
    public String getCouleurHex() { return couleurHex; }
    public List<String> getCriteresValides() { return criteresValides; }
    public List<String> getConseilsAmelioration() { return conseilsAmelioration; }
    public double getTempsEstimationCrack() { return tempsEstimationCrack; }
}
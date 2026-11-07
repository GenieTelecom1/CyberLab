package com.example.demo1;

import java.util.List;

public class PasswordResult {
    private int score; // 0 à 100
    private String niveau; // FAIBLE, MOYEN, FORT, TRÈS FORT
    private String couleurHex;
    private List<String> criteresValides;
    private List<String> conseilsAmelioration;
    private double tempsEstimationCrack;

    public PasswordResult(int score, String niveau, String couleurHex,
                          List<String> criteresValides, List<String> conseilsAmelioration, double tempsEstimationCrack) {
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
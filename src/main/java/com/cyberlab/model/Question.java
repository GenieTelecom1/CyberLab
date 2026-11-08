package com.cyberlab.model;
public class Question {
    private int id;
    private int courseId;
    private String texte;
    private String choixA;
    private String choixB;
    private String choixC;
    private String choixD;
    private String bonneReponse;

    public Question(int id, int courseId, String texte, String choixA, String choixB, String choixC, String choixD, String bonneReponse) {
        this.id = id;
        this.courseId = courseId;
        this.texte = texte;
        this.choixA = choixA;
        this.choixB = choixB;
        this.choixC = choixC;
        this.choixD = choixD;
        this.bonneReponse = bonneReponse;
    }

    // Getters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getTexte() { return texte; }
    public void setTexte(String texte) { this.texte = texte; }

    public String getChoixA() { return choixA; }
    public void setChoixA(String choixA) { this.choixA = choixA; }

    public String getChoixB() { return choixB; }
    public void setChoixB(String choixB) { this.choixB = choixB; }

    public String getChoixC() { return choixC; }
    public void setChoixC(String choixC) { this.choixC = choixC; }

    public String getChoixD() { return choixD; }
    public void setChoixD(String choixD) { this.choixD = choixD; }

    public String getBonneReponse() { return bonneReponse; }
    public void setBonneReponse(String bonneReponse) { this.bonneReponse = bonneReponse; }
}

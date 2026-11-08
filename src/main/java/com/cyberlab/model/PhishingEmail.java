package com.cyberlab.model;

import java.util.List;

public class PhishingEmail {
    private int id;
    private String expediteur;
    private String objet;
    private String corps;
    private boolean estFrauduleux;
    private List<String> indicesSuspects;
    private String conseilSecurite;

    public PhishingEmail() {}

    public PhishingEmail(int id, String expediteur, String objet, String corps,
                         boolean estFrauduleux, List<String> indicesSuspects, String conseilSecurite) {
        this.id = id;
        this.expediteur = expediteur;
        this.objet = objet;
        this.corps = corps;
        this.estFrauduleux = estFrauduleux;
        this.indicesSuspects = indicesSuspects;
        this.conseilSecurite = conseilSecurite;
    }

    public int getId() { return id; }
    public String getExpediteur() { return expediteur; }
    public String getObjet() { return objet; }
    public String getCorps() { return corps; }
    public boolean isEstFrauduleux() { return estFrauduleux; }
    public List<String> getIndicesSuspects() { return indicesSuspects; }
    public String getConseilSecurite() { return conseilSecurite; }
}

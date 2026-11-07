package com.cyberlab.model;

/**
 * Un cours (ligne de la table "courses" de Supabase).
 * Colonnes : id, titre, description, contenu (Markdown), ordre.
 */
public class Course {

    private int id;
    private String titre;
    private String description;
    private String contenu;
    private int ordre;

    public Course() {
    }

    public Course(int id, String titre, String description, String contenu, int ordre) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.contenu = contenu;
        this.ordre = ordre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    @Override
    public String toString() {
        return titre;
    }
}

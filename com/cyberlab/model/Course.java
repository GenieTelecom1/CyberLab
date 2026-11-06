package com.cyberlab.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Un cours de cybersécurité (table Courses).
 * Colonnes : id, titre, description, contenu, ordre.
 *
 * Le champ contenu est découpé en parties : chaque partie commence par une ligne
 * "## Titre de la partie", suivie de son texte.
 */
public class Course {

    /** Une partie d'un cours. */
    public static class Section {
        private final String titre;
        private final String texte;

        public Section(String titre, String texte) {
            this.titre = titre;
            this.texte = texte;
        }

        public String getTitre() { return titre; }
        public String getTexte() { return texte; }

        /** Temps de lecture estimé en minutes (environ 180 mots par minute, minimum 1). */
        public int getReadingMinutes() {
            return readingMinutes(texte);
        }
    }

    static int readingMinutes(String text) {
        if (text == null || text.isBlank()) {
            return 1;
        }
        int words = text.trim().split("\\s+").length;
        return Math.max(1, (int) Math.round(words / 180.0));
    }

    private final int id;
    private final String titre;
    private final String description;
    private final String contenu;
    private final int ordre;
    private List<Section> sections;

    public Course(int id, String titre, String description, String contenu, int ordre) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.contenu = contenu;
        this.ordre = ordre;
    }

    public int getId() { return id; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public String getContenu() { return contenu; }
    public int getOrdre() { return ordre; }

    /** Temps de lecture estimé de tout le cours, en minutes. */
    public int getReadingMinutes() {
        return readingMinutes(contenu);
    }

    /** Parties du cours, dans l'ordre. Au moins une partie si le contenu n'est pas vide. */
    public List<Section> getSections() {
        if (sections == null) {
            sections = parseSections(contenu);
        }
        return sections;
    }

    private static List<Section> parseSections(String contenu) {
        List<Section> result = new ArrayList<>();
        if (contenu == null || contenu.isBlank()) {
            return result;
        }
        String currentTitle = null;
        StringBuilder text = new StringBuilder();
        for (String line : contenu.split("\\R")) {
            if (line.startsWith("## ")) {
                if (currentTitle != null) {
                    result.add(new Section(currentTitle, text.toString().trim()));
                }
                currentTitle = line.substring(3).trim();
                text.setLength(0);
            } else if (currentTitle != null) {
                text.append(line).append('\n');
            }
        }
        if (currentTitle != null) {
            result.add(new Section(currentTitle, text.toString().trim()));
        }
        if (result.isEmpty()) {
            result.add(new Section("Contenu", contenu.trim()));
        }
        return result;
    }

    @Override
    public String toString() {
        return titre;
    }
}

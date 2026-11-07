package com.cyberlab.service;

import com.cyberlab.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Base de connaissances de l'assistant : le contenu des 6 cours (table courses, Supabase).
 *
 * Chaque cours (markdown) est decoupe en sections (## 1.1 ..., ## 2.3 ...). Quand l'utilisateur pose
 * une question, on classe les sections par pertinence (BM25 simplifie) et on renvoie les meilleures.
 * Ces sections servent soit de contexte pour Gemini, soit de reponse directe en mode hors ligne.
 *
 * Le chargement se fait une seule fois (cache en memoire).
 */
public class CourseKnowledgeService {

    /** Un morceau de cours : une section. */
    public static final class Chunk {
        public final String course;   // ex : "Phishing"
        public final String section;  // ex : "3.1 Indices dans un email"
        public final String text;     // texte nettoye, lisible

        private final Map<String, Integer> termFreq = new HashMap<>();
        private final Set<String> headingTerms = new HashSet<>();
        private int length;

        Chunk(String course, String section, String text) {
            this.course = course;
            this.section = section;
            this.text = text;
        }

        /** Libelle court pour citer la source. */
        public String label() {
            return course + " - " + section;
        }
    }

    /** Un resultat de recherche. */
    public static final class Hit {
        public final Chunk chunk;
        public final double score;

        Hit(Chunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }
    }

    private static final double K1 = 1.5;
    private static final double B = 0.75;
    private static final int SPLIT_LONG_SECTION_AT = 3000;

    /** "c'est quoi X", "qu'est-ce que X", "what is X"... : on favorise les sections de definition. */
    private static final Pattern DEFINITION_INTENT =
            Pattern.compile("cest quoi|quest|what is|definition|definir|signifie|veut dire");
    /** Sections peu utiles pour repondre (recapitulatifs, checklists) : leur score est reduit. */
    private static final Pattern GENERIC_HEADING =
            Pattern.compile("(?i)^(en r[eé]sum[eé]|partie \\d.*|.*checklist.*|.*je sais faire.*|.*exercice pratique.*)$");

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    private static final Set<String> STOPWORDS = new HashSet<>(List.of(
            "le", "la", "les", "un", "une", "des", "du", "de", "d", "l", "et", "ou", "en", "au", "aux", "a",
            "ce", "ca", "cela", "cest", "qui", "que", "quoi", "quel", "quelle", "quels", "quelles",
            "est", "sont", "etre", "fait", "faire", "comment", "pourquoi", "quand", "dans", "sur", "pour",
            "par", "avec", "sans", "il", "elle", "on", "je", "tu", "nous", "vous", "ils", "mon", "ma",
            "mes", "ton", "ta", "son", "sa", "ses", "se", "si", "ne", "pas", "plus", "peut", "peux", "puis",
            "dit", "dis", "explique", "expliquer", "moi", "donne", "definir", "signifie",
            "the", "is", "are", "what", "how", "why", "of", "to", "in", "and", "or", "an", "does", "do", "me",
            "tell", "explain", "about"));

    /** Quelques equivalences pour que la question (fr/en) retrouve les termes des cours. */
    private static final Map<String, String> SYNONYMS = Map.ofEntries(
            Map.entry("password", "passe"),
            Map.entry("passwords", "passe"),
            Map.entry("mdp", "passe"),
            Map.entry("hameconnage", "phishing"),
            Map.entry("rancongiciel", "ransomware"),
            Map.entry("rancon", "ransomware"),
            Map.entry("engineering", "ingenierie"),
            Map.entry("firewall", "feu"),
            Map.entry("2fa", "mfa"),
            Map.entry("multifacteur", "mfa"),
            Map.entry("trojan", "troie"),
            Map.entry("worm", "ver"),
            Map.entry("spyware", "spyware"),
            Map.entry("backup", "sauvegarde"),
            Map.entry("backups", "sauvegarde"),
            Map.entry("encryption", "chiffrement"),
            Map.entry("cryptage", "chiffrement"),
            Map.entry("fort", "bon"),
            Map.entry("forts", "bon"),
            Map.entry("solide", "bon"),
            Map.entry("solides", "bon"),
            Map.entry("robuste", "bon"),
            Map.entry("robustes", "bon"));

    private final List<Chunk> chunks = new ArrayList<>();
    private final Map<String, Integer> docFreq = new HashMap<>();
    private double avgLength = 1;
    private boolean loaded = false;
    private String loadError = null;

    // ------------------------------------------------------------------ chargement

    /** Charge les cours depuis Supabase (une seule fois). */
    public synchronized void ensureLoaded() {
        if (loaded) return;
        List<String[]> courses = new ArrayList<>();
        String sql = "SELECT titre, contenu FROM courses ORDER BY ordre";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                courses.add(new String[]{rs.getString("titre"), rs.getString("contenu")});
            }
            loadError = null;
        } catch (SQLException | RuntimeException e) {
            loadError = e.getMessage();
            System.err.println("CourseKnowledgeService : chargement des cours impossible : " + e.getMessage());
            return; // on reessaiera a la prochaine question
        }
        load(courses);
    }

    /** Construit l'index a partir de {titre, contenu markdown}. Public pour pouvoir etre teste sans base. */
    public synchronized void load(List<String[]> courses) {
        chunks.clear();
        docFreq.clear();
        for (String[] c : courses) {
            if (c[1] != null && !c[1].isBlank()) {
                chunks.addAll(split(c[0] == null ? "Cours" : c[0], c[1]));
            }
        }
        long totalLength = 0;
        for (Chunk ch : chunks) {
            for (String term : tokenize(ch.text)) {
                ch.termFreq.merge(term, 1, Integer::sum);
                ch.length++;
            }
            for (String t : tokenize(ch.course + " " + ch.section)) ch.headingTerms.add(t);
            totalLength += ch.length;
            for (String term : ch.termFreq.keySet()) docFreq.merge(term, 1, Integer::sum);
        }
        avgLength = chunks.isEmpty() ? 1 : Math.max(1, (double) totalLength / chunks.size());
        loaded = !chunks.isEmpty();
    }

    public synchronized boolean isReady() {
        return loaded;
    }

    public synchronized String getLoadError() {
        return loadError;
    }

    // ------------------------------------------------------------------ decoupage

    private static List<Chunk> split(String courseTitle, String markdown) {
        List<Chunk> result = new ArrayList<>();
        String section = "Introduction"; // dernier titre # ou ##
        String heading = section;        // libelle du morceau en cours
        StringBuilder body = new StringBuilder();
        boolean inCode = false;

        for (String raw : markdown.replace("\r\n", "\n").split("\n")) {
            String line = raw;
            if (line.trim().startsWith("```")) {
                inCode = !inCode;
                continue;
            }
            boolean isH1 = !inCode && line.startsWith("# ");
            boolean isH2 = !inCode && line.startsWith("## ");
            boolean isH3 = !inCode && line.startsWith("### ");

            if (isH1 || isH2) {
                flush(result, courseTitle, heading, body);
                section = line.replaceFirst("^#+\\s*", "").trim();
                heading = section;
                continue;
            }
            if (isH3 && body.length() > SPLIT_LONG_SECTION_AT) {
                // section tres longue : on coupe, mais on garde le titre de la section parente
                flush(result, courseTitle, heading, body);
                heading = section + " > " + line.replaceFirst("^#+\\s*", "").trim();
                continue;
            }
            String cleaned = cleanLine(line, isH3);
            if (cleaned != null) body.append(cleaned).append('\n');
        }
        flush(result, courseTitle, heading, body);
        return result;
    }

    private static void flush(List<Chunk> out, String course, String heading, StringBuilder body) {
        String text = body.toString().replaceAll("\n{3,}", "\n\n").trim();
        body.setLength(0);
        if (text.length() >= 60) { // ignore les titres vides
            out.add(new Chunk(course, heading, text));
        }
    }

    /** Retire le balisage markdown pour que le texte soit lisible dans une bulle de chat. */
    private static String cleanLine(String line, boolean isH3) {
        String t = line.trim();
        if (t.isEmpty()) return "";
        if (t.startsWith("![")) return null;                       // images
        if (t.matches("^\\|?[\\s:|-]+\\|?$") && t.contains("-")) return null; // separateur de tableau
        if (t.startsWith("|")) {                                    // ligne de tableau
            t = t.replaceAll("^\\||\\|$", "").trim().replaceAll("\\s*\\|\\s*", "  -  ");
        }
        if (isH3) t = "> " + t.replaceFirst("^#+\\s*", "").trim();
        t = t.replaceFirst("^>\\s*", "");
        t = t.replace("- [ ] ", "[ ] ").replace("- [x] ", "[x] ");
        t = t.replaceAll("\\*\\*(.+?)\\*\\*", "$1").replaceAll("`([^`]+)`", "$1");
        t = t.replaceFirst("^- ", "- ");
        return t;
    }

    // ------------------------------------------------------------------ recherche

    /** Les sections les plus pertinentes pour la question (au plus maxResults, eventuellement aucune). */
    public synchronized List<Hit> search(String question, int maxResults) {
        if (!loaded || question == null) return List.of();
        List<String> queryTerms = new ArrayList<>(tokenize(question));
        if (queryTerms.isEmpty()) return List.of();
        if (DEFINITION_INTENT.matcher(normalize(question)).find()) {
            queryTerms.add("definition");
        }

        int n = chunks.size();
        List<Hit> hits = new ArrayList<>();
        for (Chunk ch : chunks) {
            double score = 0;
            int matched = 0;
            Set<String> seen = new HashSet<>();
            for (String q : queryTerms) {
                if (!seen.add(q)) continue;
                Integer tf = ch.termFreq.get(q);
                Integer df = docFreq.get(q);
                if (df == null) continue;
                double idf = Math.log(1 + (n - df + 0.5) / (df + 0.5));
                if (tf != null) {
                    matched++;
                    score += idf * (tf * (K1 + 1)) / (tf + K1 * (1 - B + B * ch.length / avgLength));
                }
                if (ch.headingTerms.contains(q)) {
                    score += idf * 1.5; // le titre de section compte plus que le corps
                    if (tf == null) matched++;
                }
            }
            boolean generic = GENERIC_HEADING.matcher(ch.section).matches()
                    || ch.section.equalsIgnoreCase(ch.course); // simple page d'intro du cours
            if (matched > 0 && generic) score *= 0.6;
            if (matched > 0) hits.add(new Hit(ch, score));
        }
        if (hits.isEmpty()) return List.of();

        hits.sort(Comparator.comparingDouble((Hit h) -> h.score).reversed());
        double best = hits.get(0).score;
        List<Hit> out = new ArrayList<>();
        for (Hit h : hits) {
            if (out.size() >= maxResults) break;
            if (h.score >= best * 0.45) out.add(h);
        }
        return out;
    }

    // ------------------------------------------------------------------ texte

    /** Minuscules, sans accents ni apostrophes ; "3-2-1" devient "321" pour rester un seul mot. */
    static String normalize(String text) {
        String s = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        s = DIACRITICS.matcher(s).replaceAll("");
        s = s.replace("'", "").replace("’", "");
        return s.replaceAll("(\\d)-(\\d)-(\\d)", "$1$2$3");
    }

    static List<String> tokenize(String text) {
        String s = normalize(text);
        List<String> out = new ArrayList<>();
        for (String w : NON_ALNUM.split(s)) {
            if (w.length() < 2 || STOPWORDS.contains(w)) continue;
            w = SYNONYMS.getOrDefault(w, w);
            if (w.length() > 3 && (w.endsWith("s") || w.endsWith("x"))) {
                w = w.substring(0, w.length() - 1); // pluriel simple
            }
            out.add(w);
        }
        return out;
    }
}

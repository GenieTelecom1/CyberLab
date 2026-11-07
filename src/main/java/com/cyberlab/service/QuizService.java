package com.cyberlab.service;

import com.cyberlab.model.Question;
import com.cyberlab.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuizService {
    private List<Question> toutesLesQuestions = new ArrayList<>();
    private List<Question> questionsCourantes = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;

    // Constructeur par défaut (chargement par défaut du cours 1)
    public QuizService() {
        chargerQuestionsDepuisBDD();
        filtrerParCours(1);
    }

    // Constructeur avec ID de cours spécifique
    public QuizService(int courseId) {
        chargerQuestionsDepuisBDD();
        filtrerParCours(courseId);
    }

    private void chargerQuestionsDepuisBDD() {
        String query = "SELECT id, course_id, texte, choix_a, choix_b, choix_c, choix_d, bonne_reponse FROM questions";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                toutesLesQuestions.add(new Question(
                        rs.getInt("id"),
                        rs.getInt("course_id"),
                        rs.getString("texte"),
                        rs.getString("choix_a"),
                        rs.getString("choix_b"),
                        rs.getString("choix_c"),
                        rs.getString("choix_d"),
                        rs.getString("bonne_reponse")
                ));
            }
            System.out.println("Succès : " + toutesLesQuestions.size() + " questions chargées depuis Supabase.");

        } catch (SQLException e) {
            System.err.println("Erreur de connexion à Supabase : " + e.getMessage());
        }

        // Données de secours si la table est vide
        if (toutesLesQuestions.isEmpty()) {
            toutesLesQuestions.add(new Question(1, 1, "Quel protocole chiffre le trafic web ?", "HTTP", "FTP", "HTTPS", "SMTP", "HTTPS"));
            toutesLesQuestions.add(new Question(2, 1, "Que signifie l'acronyme CIA en sécurité ?", "Confidentialité, Intégrité, Disponibilité", "Contrôle, Internet, Accès", "Code, Interface, Application", "Cyber, Info, Action", "Confidentialité, Intégrité, Disponibilité"));
        }
    }

    public void filtrerParCours(int courseId) {
        questionsCourantes = toutesLesQuestions.stream()
                .filter(q -> q.getCourseId() == courseId)
                .collect(Collectors.toList());
        currentIndex = 0;
        score = 0;
    }

    public Question getCurrentQuestion() {
        if (currentIndex < questionsCourantes.size()) {
            return questionsCourantes.get(currentIndex);
        }
        return null;
    }

    public boolean verifierReponse(String reponseUtilisateur) {
        Question q = getCurrentQuestion();
        if (q != null && q.getBonneReponse() != null && q.getBonneReponse().trim().equalsIgnoreCase(reponseUtilisateur.trim())) {
            score++;
            return true;
        }
        return false;
    }

    public boolean passerQuestionSuivante() {
        currentIndex++;
        return currentIndex < questionsCourantes.size();
    }

    public int getCurrentIndex() {
        return this.currentIndex;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return questionsCourantes.size();
    }

    public void resetQuiz() {
        currentIndex = 0;
        score = 0;
    }
}
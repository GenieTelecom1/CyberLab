package com.cyberlab.service;

import com.cyberlab.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Sauvegarde du résultat d'un quiz dans la table quiz_results. */
public class ScoreService {

    private ScoreService() { }

    /** Un cours qui possède au moins une question de quiz. */
    public record CourseChoice(int id, String title, int questionCount) { }

    /** Liste des cours ayant des questions (pour l'écran "Choisissez un cours"). */
    public static List<CourseChoice> listCourses() {
        List<CourseChoice> list = new ArrayList<>();
        String withTitles = "SELECT q.course_id, COALESCE(c.titre, 'Cours ' || q.course_id) AS titre, COUNT(*) AS n "
                + "FROM questions q LEFT JOIN courses c ON c.id = q.course_id "
                + "GROUP BY q.course_id, c.titre ORDER BY q.course_id";
        String simple = "SELECT course_id, 'Cours ' || course_id AS titre, COUNT(*) AS n "
                + "FROM questions GROUP BY course_id ORDER BY course_id";
        for (String sql : new String[]{withTitles, simple}) {
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new CourseChoice(rs.getInt(1), rs.getString(2), rs.getInt(3)));
                }
                if (!list.isEmpty()) return list;
            } catch (SQLException e) {
                System.err.println("Erreur liste des cours : " + e.getMessage());
            }
        }
        return list;
    }

    public static void saveResult(int userId, String username, int courseId, int score, int total) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            String courseName = "Cours " + courseId;
            try (PreparedStatement ps = conn.prepareStatement("SELECT titre FROM courses WHERE id = ?")) {
                ps.setInt(1, courseId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getString(1) != null) courseName = rs.getString(1);
                }
            } catch (SQLException ignored) {
                // table courses différente : on garde "Cours N"
            }

            String sql = "INSERT INTO quiz_results (user_id, username, course_id, course_name, score, total) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, username == null || username.isBlank() ? "Anonyme" : username);
                ps.setInt(3, courseId);
                ps.setString(4, courseName);
                ps.setInt(5, score);
                ps.setInt(6, total);
                ps.executeUpdate();
            }
            System.out.println("Succès : score " + score + "/" + total + " enregistré.");
        } catch (SQLException e) {
            System.err.println("Erreur sauvegarde du score : " + e.getMessage());
        }
    }
}
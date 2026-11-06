package com.cyberlab.service;

import com.cyberlab.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Progression des cours (table Progress).
 * Utilisé par le module Learning, le Dashboard (M2) et le Leaderboard (M4).
 * Seul ce service parle à la base pour la progression.
 *
 * Colonne ajoutée à Progress : parties_terminees (nombre de parties terminées, par défaut 0).
 * Un cours est terminé (termine vrai) quand toutes ses parties sont terminées.
 *
 * SQL standard : fonctionne avec PostgreSQL (Supabase) et SQLite. La colonne "termine" peut être
 * de type BOOLEAN (PostgreSQL de M1) ou INTEGER 0/1 : le type est détecté automatiquement.
 */
public class ProgressService {

    /** true si la colonne termine est un BOOLEAN, false si c'est un entier. Détecté une seule fois. */
    private Boolean termineIsBoolean;

    private boolean termineIsBoolean(Connection conn) throws SQLException {
        if (termineIsBoolean == null) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT termine FROM Progress WHERE 1 = 0");
                 ResultSet rs = ps.executeQuery()) {
                int type = rs.getMetaData().getColumnType(1);
                termineIsBoolean = (type == Types.BOOLEAN || type == Types.BIT);
            }
        }
        return termineIsBoolean;
    }

    private void setTermine(PreparedStatement ps, int index, boolean finished, boolean asBoolean) throws SQLException {
        if (asBoolean) {
            ps.setBoolean(index, finished);
        } else {
            ps.setInt(index, finished ? 1 : 0);
        }
    }

    /**
     * Enregistre qu'un utilisateur a terminé les parties 1 à partNumber d'un cours.
     * Ne fait jamais reculer la progression. Marque le cours terminé quand toutes les parties sont faites.
     */
    public void completePart(int userId, int courseId, int partNumber, int totalParts) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean asBoolean = termineIsBoolean(conn);

            int current = -1;   // -1 = aucune ligne pour ce cours
            String select = "SELECT parties_terminees FROM Progress WHERE user_id = ? AND course_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setInt(1, userId);
                ps.setInt(2, courseId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        current = rs.getInt(1);
                    }
                }
            }

            int newValue = Math.max(Math.max(current, 0), partNumber);   // ne recule jamais
            boolean finished = newValue >= totalParts;

            if (current >= 0) {
                String update = "UPDATE Progress SET parties_terminees = ?, termine = ?, date = CURRENT_TIMESTAMP "
                        + "WHERE user_id = ? AND course_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    ps.setInt(1, newValue);
                    setTermine(ps, 2, finished, asBoolean);
                    ps.setInt(3, userId);
                    ps.setInt(4, courseId);
                    ps.executeUpdate();
                }
            } else {
                String insert = "INSERT INTO Progress (user_id, course_id, termine, parties_terminees, date) "
                        + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, courseId);
                    setTermine(ps, 3, finished, asBoolean);
                    ps.setInt(4, newValue);
                    ps.executeUpdate();
                }
            }
        }
    }

    /** Nombre de parties terminées pour un cours (0 si le cours n'est pas commencé). */
    public int getPartsDone(int userId, int courseId) throws SQLException {
        String sql = "SELECT parties_terminees FROM Progress WHERE user_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("parties_terminees") : 0;
            }
        }
    }

    /** Parties terminées pour chaque cours commencé : course_id -> nombre de parties. */
    public Map<Integer, Integer> getPartsDoneByCourse(int userId) throws SQLException {
        String sql = "SELECT course_id, parties_terminees FROM Progress WHERE user_id = ?";
        Map<Integer, Integer> result = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("course_id"), rs.getInt("parties_terminees"));
                }
            }
        }
        return result;
    }

    public boolean isCourseCompleted(int userId, int courseId) throws SQLException {
        String sql = "SELECT termine FROM Progress WHERE user_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean("termine");
            }
        }
    }

    /** Identifiants des cours terminés par l'utilisateur. */
    public Set<Integer> getCompletedCourseIds(int userId) throws SQLException {
        String sql = "SELECT course_id, termine FROM Progress WHERE user_id = ?";
        Set<Integer> ids = new HashSet<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (rs.getBoolean("termine")) {
                        ids.add(rs.getInt("course_id"));
                    }
                }
            }
        }
        return ids;
    }

    public int getCompletedCount(int userId) throws SQLException {
        return getCompletedCourseIds(userId).size();
    }

    /** Pourcentage de cours terminés (0 à 100), pour le Dashboard et le Leaderboard. */
    public int getProgressPercentage(int userId) throws SQLException {
        int total = countCourses();
        if (total == 0) {
            return 0;
        }
        return Math.round(100f * getCompletedCount(userId) / total);
    }

    private int countCourses() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Courses";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}

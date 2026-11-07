package com.cyberlab.service;

import com.cyberlab.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Statistiques du Dashboard, calculées à partir des vraies données Supabase :
 *   - courses / progress     : liste des cours et cours terminés
 *   - quiz_results           : scores, moyenne, points, rang, série de jours
 *
 * Points = somme, sur chaque cours, du meilleur score de l'utilisateur
 * (c'est le même calcul que la page « Classement »).
 * Niveau : Novice (0-9), Intermédiaire (10-24), Avancé (25-39), Expert (40+).
 */
public class DashboardService {

    public record CourseSummary(int id, String title, String description, double progress, boolean completed) { }
    public record RankEntry(int position, String name, String level, int points) { }

    public record DashboardSnapshot(List<CourseSummary> courses, double averageScore,
                                    String rank, String rankNote, List<RankEntry> leaderboard,
                                    int points, String level, int streakDays) {
        public int completedCount() {
            return (int) courses.stream().filter(CourseSummary::completed).count();
        }
        public int totalCourses() { return courses.size(); }
        /** Progression globale = cours terminés / total (0 si aucun cours). */
        public double progressRatio() {
            return courses.isEmpty() ? 0 : (double) completedCount() / courses.size();
        }
    }

    private static final ZoneId TUNIS = ZoneId.of("Africa/Tunis");

    public static String levelFor(int points) {
        if (points >= 40) return "Expert";
        if (points >= 25) return "Avancé";
        if (points >= 10) return "Intermédiaire";
        return "Novice";
    }

    public DashboardSnapshot getSnapshot(int userId) {
        List<CourseSummary> courses = new ArrayList<>();
        double average = 0;
        int points = 0;
        String rank = "—";
        String rankNote = "Passez un quiz pour être classé";
        List<RankEntry> top = new ArrayList<>();
        int streak = 0;

        try (Connection c = DatabaseConnection.getConnection()) {
            courses = loadCourses(c, userId);
            average = loadAverage(c, userId);
            streak = loadStreak(c, userId);

            // Classement global (somme des meilleurs scores par cours)
            String sql = "SELECT user_id, username, SUM(best) AS total FROM ("
                    + "  SELECT user_id, username, course_id, MAX(score) AS best "
                    + "  FROM quiz_results GROUP BY user_id, username, course_id"
                    + ") t GROUP BY user_id, username ORDER BY total DESC, username ASC";
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                int position = 1;
                int participants = 0;
                int myPosition = -1;
                while (rs.next()) {
                    int total = rs.getInt("total");
                    if (position <= 3) {
                        top.add(new RankEntry(position, rs.getString("username"), levelFor(total), total));
                    }
                    if (rs.getInt("user_id") == userId) {
                        myPosition = position;
                        points = total;
                    }
                    position++;
                    participants++;
                }
                if (myPosition > 0) {
                    rank = "#" + myPosition;
                    rankNote = "Sur " + participants + (participants > 1 ? " participants" : " participant");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur Dashboard : " + e.getMessage());
        }

        return new DashboardSnapshot(courses, average, rank, rankNote, top, points, levelFor(points), streak);
    }

    // ---------- cours + progression ----------

    private List<CourseSummary> loadCourses(Connection c, int userId) {
        List<CourseSummary> list = new ArrayList<>();

        Set<Integer> completed = new HashSet<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT course_id FROM progress WHERE user_id = ? AND termine = TRUE")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) completed.add(rs.getInt(1));
            }
        } catch (Exception e) {
            System.err.println("Erreur progression : " + e.getMessage());
        }

        // Meilleur résultat de quiz par cours (0..1)
        Map<Integer, Double> bestRatio = new HashMap<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT course_id, MAX(score * 1.0 / NULLIF(total, 0)) FROM quiz_results "
                        + "WHERE user_id = ? GROUP BY course_id")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) bestRatio.put(rs.getInt(1), rs.getDouble(2));
            }
        } catch (Exception e) {
            System.err.println("Erreur meilleurs scores : " + e.getMessage());
        }

        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, titre, description FROM courses ORDER BY ordre")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                boolean done = completed.contains(id);
                double progress = done ? 1.0 : Math.min(bestRatio.getOrDefault(id, 0.0), 0.99);
                String desc = rs.getString("description");
                list.add(new CourseSummary(id, rs.getString("titre"), desc == null ? "" : desc, progress, done));
            }
        } catch (Exception e) {
            System.err.println("Erreur liste des cours : " + e.getMessage());
        }
        return list;
    }

    // ---------- score moyen (0..1) ----------

    private double loadAverage(Connection c, int userId) {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT AVG(score * 1.0 / NULLIF(total, 0)) FROM quiz_results WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);   // 0 si aucun quiz (NULL)
            }
        } catch (Exception e) {
            System.err.println("Erreur score moyen : " + e.getMessage());
        }
        return 0;
    }

    // ---------- série de jours consécutifs avec au moins un quiz ----------

    private int loadStreak(Connection c, int userId) {
        String sql = "SELECT DISTINCT (played_at AT TIME ZONE 'Africa/Tunis')::date AS d "
                + "FROM quiz_results WHERE user_id = ? ORDER BY d DESC";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                LocalDate expected = LocalDate.now(TUNIS);
                int streak = 0;
                boolean first = true;
                while (rs.next()) {
                    LocalDate day = rs.getDate("d").toLocalDate();
                    if (first) {
                        first = false;
                        // la série est encore active si le dernier quiz date d'aujourd'hui ou d'hier
                        if (day.equals(expected.minusDays(1))) expected = expected.minusDays(1);
                    }
                    if (day.equals(expected)) {
                        streak++;
                        expected = expected.minusDays(1);
                    } else {
                        break;
                    }
                }
                return streak;
            }
        } catch (Exception e) {
            System.err.println("Erreur série de jours : " + e.getMessage());
            return 0;
        }
    }
}
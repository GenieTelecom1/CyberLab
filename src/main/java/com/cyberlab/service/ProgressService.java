package com.cyberlab.service;

import com.cyberlab.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ProgressService {

    public Set<Integer> getCompletedCourseIds(int userId) throws SQLException {
        String sql = "SELECT course_id FROM progress WHERE user_id = ? AND termine = TRUE";
        Set<Integer> ids = new HashSet<>();

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("course_id"));
                }
            }
        }
        return ids;
    }

    public void markAsCompleted(int userId, int courseId) throws SQLException {
        String update = "UPDATE progress SET termine = TRUE, date = NOW() WHERE user_id = ? AND course_id = ?";
        String insert = "INSERT INTO progress (user_id, course_id, termine, date) VALUES (?, ?, TRUE, NOW())";

        try (Connection c = DatabaseConnection.getConnection()) {
            int updated;

            try (PreparedStatement ps = c.prepareStatement(update)) {
                ps.setInt(1, userId);
                ps.setInt(2, courseId);
                updated = ps.executeUpdate();
            }

            if (updated == 0) {
                try (PreparedStatement ps = c.prepareStatement(insert)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, courseId);
                    ps.executeUpdate();
                }
            }
        }
    }

    public static double percentage(int completed, int total) {
        return total == 0 ? 0 : (completed * 100.0) / total;
    }
}

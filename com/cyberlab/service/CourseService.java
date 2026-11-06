package com.cyberlab.service;

import com.cyberlab.database.DatabaseConnection;
import com.cyberlab.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lecture des cours (table Courses). Seul ce service parle à la base pour les cours.
 */
public class CourseService {

    public List<Course> getAllCourses() throws SQLException {
        String sql = "SELECT id, titre, description, contenu, ordre FROM Courses ORDER BY ordre";
        List<Course> courses = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                courses.add(new Course(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("contenu"),
                        rs.getInt("ordre")));
            }
        }
        return courses;
    }

    public Course getCourseById(int id) throws SQLException {
        String sql = "SELECT id, titre, description, contenu, ordre FROM Courses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Course(
                            rs.getInt("id"),
                            rs.getString("titre"),
                            rs.getString("description"),
                            rs.getString("contenu"),
                            rs.getInt("ordre"));
                }
            }
        }
        return null;
    }
}

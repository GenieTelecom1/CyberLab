package com.cyberlab.service;

import com.cyberlab.model.Course;
import com.cyberlab.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseService {

    public List<Course> getAllCourses() throws SQLException {
        String sql = "SELECT id, titre, description, contenu, ordre FROM courses ORDER BY ordre";
        List<Course> courses = new ArrayList<>();

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
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
}
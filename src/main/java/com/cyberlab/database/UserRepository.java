package com.cyberlab.database;

import com.cyberlab.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Accès SQL à la table users. Seuls les services appellent cette classe. */
public class UserRepository {

    public record Credentials(User user, String hash) {}

    private static final String COLS = "id, nom, email, email_verifie, date_creation";

    private User map(ResultSet rs) throws SQLException {
        return new User(rs.getLong("id"), rs.getString("nom"), rs.getString("email"),
                rs.getBoolean("email_verifie"), rs.getTimestamp("date_creation").toInstant());
    }

    public User insert(String nom, String email, String hash) throws SQLException {
        String sql = "INSERT INTO users (nom, email, mot_de_passe_hash) VALUES (?, ?, ?) RETURNING " + COLS;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, email);
            ps.setString(3, hash);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return map(rs);
            }
        }
    }

    public Optional<Credentials> findCredentialsByEmail(String email) throws SQLException {
        String sql = "SELECT " + COLS + ", mot_de_passe_hash FROM users WHERE email = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Credentials(map(rs), rs.getString("mot_de_passe_hash")));
            }
        }
    }

    public Optional<User> findById(long id) throws SQLException {
        String sql = "SELECT " + COLS + " FROM users WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT " + COLS + " FROM users ORDER BY id";
        List<User> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Le hash ne sort jamais de la couche base / service. */
    public Optional<String> findHashById(long id) throws SQLException {
        String sql = "SELECT mot_de_passe_hash FROM users WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString(1)) : Optional.empty();
            }
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean updateNom(long id, String nom) throws SQLException {
        String sql = "UPDATE users SET nom = ? WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setLong(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean updateNomEtHash(long id, String nom, String hash) throws SQLException {
        String sql = "UPDATE users SET nom = ?, mot_de_passe_hash = ? WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, hash);
            ps.setLong(3, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean updatePasswordHash(long id, String hash) throws SQLException {
        String sql = "UPDATE users SET mot_de_passe_hash = ? WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setLong(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean markEmailVerified(long id) throws SQLException {
        String sql = "UPDATE users SET email_verifie = true WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }
}

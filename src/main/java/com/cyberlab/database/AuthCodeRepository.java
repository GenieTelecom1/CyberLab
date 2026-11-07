package com.cyberlab.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

/** Codes à usage unique (vérification d'e-mail, réinitialisation de mot de passe). */
public class AuthCodeRepository {

    public record Code(long id, String hash, Instant expireLe, int tentatives) {}

    public void insert(long userId, String type, String hash, Instant creation, Instant expireLe)
            throws SQLException {
        String sql = "INSERT INTO auth_codes (user_id, type, code_hash, date_creation, expire_le) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, type);
            ps.setString(3, hash);
            ps.setTimestamp(4, Timestamp.from(creation));
            ps.setTimestamp(5, Timestamp.from(expireLe));
            ps.executeUpdate();
        }
    }

    /** Marque tous les codes en attente de cet utilisateur et de ce type comme utilisés. */
    public void invalidateAll(long userId, String type) throws SQLException {
        String sql = "UPDATE auth_codes SET utilise = true WHERE user_id = ? AND type = ? AND NOT utilise";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, type);
            ps.executeUpdate();
        }
    }

    public Optional<Code> findLatestActive(long userId, String type) throws SQLException {
        String sql = "SELECT id, code_hash, expire_le, tentatives FROM auth_codes "
                + "WHERE user_id = ? AND type = ? AND NOT utilise ORDER BY id DESC LIMIT 1";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Code(rs.getLong("id"), rs.getString("code_hash"),
                        rs.getTimestamp("expire_le").toInstant(), rs.getInt("tentatives")));
            }
        }
    }

    public void incrementAttempts(long id) throws SQLException {
        String sql = "UPDATE auth_codes SET tentatives = tentatives + 1 WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void markUsed(long id) throws SQLException {
        String sql = "UPDATE auth_codes SET utilise = true WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public Optional<Instant> lastCreation(long userId, String type) throws SQLException {
        String sql = "SELECT max(date_creation) FROM auth_codes WHERE user_id = ? AND type = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Timestamp t = rs.getTimestamp(1);
                return t == null ? Optional.empty() : Optional.of(t.toInstant());
            }
        }
    }
}

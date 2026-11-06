package com.cyberlab.database;
import com.cyberlab.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserRepository {
public record Credentials(User user, String hash) {}
private User map(ResultSet rs) throws SQLException {
return new User(rs.getLong("id"), rs.getString("nom"), 
rs.getString("email"),
rs.getTimestamp("date_creation").toInstant());
}
public User insert(String nom, String email, String hash) throws 
SQLException {
String sql = "INSERT INTO users (nom, email, mot_de_passe_hash) VALUES (?, ?, ?) "+ "RETURNING id, nom, email, date_creation";
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
    public Optional<Credentials> findCredentialsByEmail(String email) throws 
SQLException {
        String sql = "SELECT id, nom, email, date_creation, mot_de_passe_hash FROM users WHERE email = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Credentials(map(rs), rs.getString("mot_de_passe_hash")));
            }
        }
    }
}
package com.cyberlab.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbPing {
    public static void main(String[] args) {
        String sql = "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public'";
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            System.out.println("Connexion OK. Tables dans le schéma public : " + rs.getInt(1));
        } catch (Exception e) {
            System.out.println("Echec de la connexion :");
            for (Throwable t = e; t != null; t = t.getCause()) {
                System.out.println("  -> " + t.getClass().getSimpleName() + " : " + t.getMessage());
            }
        } finally {
            DatabaseConnection.close();
        }
    }
}
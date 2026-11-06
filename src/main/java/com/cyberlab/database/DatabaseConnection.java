package com.cyberlab.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private static volatile HikariDataSource dataSource;

    private DatabaseConnection() {}

    private static String config(Properties p, String envName, String key) {
        String env = System.getenv(envName);
        String valeur = (env != null && !env.isBlank()) ? env : p.getProperty(key);
        return valeur == null ? null : valeur.trim();
    }

    public static synchronized void init() {
        if (dataSource != null) return;
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("db.properties")) {
            p.load(in);
        } catch (IOException e) {
            // pas de fichier : on tente les variables d'environnement
        }
        String url = config(p, "CYBERLAB_DB_URL", "db.url");
        String user = config(p, "CYBERLAB_DB_USER", "db.user");
        String password = config(p, "CYBERLAB_DB_PASSWORD", "db.password");
        if (url == null || user == null || password == null) {
            throw new IllegalStateException("Configuration BD manquante (db.properties)");
        }
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(password);
        cfg.setMaximumPoolSize(3);
        cfg.setConnectionTimeout(8000);
        cfg.setInitializationFailTimeout(-1);
        dataSource = new HikariDataSource(cfg);
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) init();
        return dataSource.getConnection();
    }

    /** Ouvre le pool en tâche de fond au démarrage (premier Login plus rapide). */
    public static void warmUp() {
        Thread t = new Thread(() -> {
            try (Connection c = getConnection()) {
                // ouvre la connexion SSL à l'avance
            } catch (Exception e) {
                // l'erreur sera traitée au premier vrai appel
            }
        }, "db-warmup");
        t.setDaemon(true);
        t.start();
    }

    public static synchronized void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}
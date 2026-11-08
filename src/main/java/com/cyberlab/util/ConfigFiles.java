package com.cyberlab.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

/**
 * Retrouve les fichiers de configuration (db.properties, mail.properties). Ordre de recherche :
 * 1. le dossier de travail (développement : racine du projet) ;
 * 2. le dossier indiqué par la variable CYBERLAB_HOME ;
 * 3. %APPDATA%\CyberLab (Windows, application installée) ;
 * 4. ~/.cyberlab (Linux, macOS).
 * Une application installée (.exe) ne s'exécute pas dans le dossier du projet : sans cela,
 * elle ne retrouverait pas sa configuration.
 */
public final class ConfigFiles {

    private ConfigFiles() {}

    public static Optional<Path> find(String nom) {
        return trouver(nom, Path.of("").toAbsolutePath(), System::getenv, System.getProperty("user.home"));
    }

    /** Charge le fichier trouvé ; propriétés vides si aucun fichier (les variables d'environnement prennent le relais). */
    public static Properties load(String nom) {
        Properties p = new Properties();
        find(nom).ifPresent(chemin -> {
            try (InputStream in = Files.newInputStream(chemin)) {
                p.load(in);
            } catch (IOException e) {
                // fichier illisible : traité comme absent
            }
        });
        return p;
    }

    static Optional<Path> trouver(String nom, Path courant, Function<String, String> env, String home) {
        List<Path> candidats = new ArrayList<>();
        candidats.add(courant.resolve(nom));
        String cyberlabHome = env.apply("CYBERLAB_HOME");
        if (cyberlabHome != null && !cyberlabHome.isBlank()) candidats.add(Path.of(cyberlabHome.trim()).resolve(nom));
        String appData = env.apply("APPDATA");
        if (appData != null && !appData.isBlank()) candidats.add(Path.of(appData.trim(), "CyberLab", nom));
        if (home != null && !home.isBlank()) candidats.add(Path.of(home, ".cyberlab", nom));
        return candidats.stream().filter(Files::isRegularFile).findFirst();
    }
}

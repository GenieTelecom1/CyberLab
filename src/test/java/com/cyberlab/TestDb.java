package com.cyberlab;

import java.io.File;

/** Les tests d'intégration sont ignorés s'il n'y a ni db.properties ni variables d'environnement. */
public final class TestDb {

    private TestDb() {}

    public static boolean isConfigured() {
        String env = System.getenv("CYBERLAB_DB_URL");
        return new File("db.properties").exists() || (env != null && !env.isBlank());
    }
}

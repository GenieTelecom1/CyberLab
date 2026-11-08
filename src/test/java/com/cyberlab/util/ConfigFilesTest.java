package com.cyberlab.util;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigFilesTest {

    @Test
    void ordreDeRecherche(@TempDir Path tmp) throws Exception {
        Path courant = Files.createDirectory(tmp.resolve("courant"));
        Path home = Files.createDirectory(tmp.resolve("cyberlabhome"));
        Path appData = Files.createDirectory(tmp.resolve("appdata"));
        Files.createDirectories(appData.resolve("CyberLab"));
        Path userHome = Files.createDirectory(tmp.resolve("user"));
        Files.createDirectories(userHome.resolve(".cyberlab"));
        Map<String, String> env = Map.of("CYBERLAB_HOME", home.toString(), "APPDATA", appData.toString());

        assertTrue(ConfigFiles.trouver("db.properties", courant, env::get, userHome.toString()).isEmpty());

        Path dansUserHome = Files.writeString(userHome.resolve(".cyberlab/db.properties"), "a=1");
        assertEquals(dansUserHome, ConfigFiles.trouver("db.properties", courant, env::get, userHome.toString()).orElseThrow());

        Path dansAppData = Files.writeString(appData.resolve("CyberLab/db.properties"), "a=2");
        assertEquals(dansAppData, ConfigFiles.trouver("db.properties", courant, env::get, userHome.toString()).orElseThrow());

        Path dansHome = Files.writeString(home.resolve("db.properties"), "a=3");
        assertEquals(dansHome, ConfigFiles.trouver("db.properties", courant, env::get, userHome.toString()).orElseThrow());

        Path dansCourant = Files.writeString(courant.resolve("db.properties"), "a=4");
        assertEquals(dansCourant, ConfigFiles.trouver("db.properties", courant, env::get, userHome.toString()).orElseThrow());
    }

    @Test
    void sansVariablesNiFichierRienNEstTrouve(@TempDir Path tmp) {
        assertTrue(ConfigFiles.trouver("mail.properties", tmp, k -> null, null).isEmpty());
    }
}

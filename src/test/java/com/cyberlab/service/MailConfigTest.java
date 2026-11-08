package com.cyberlab.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class MailConfigTest {

    private static Properties props(String... kv) {
        Properties p = new Properties();
        for (int i = 0; i < kv.length; i += 2) p.setProperty(kv[i], kv[i + 1]);
        return p;
    }

    @Test
    void sansHoteLEnvoiNEstPasConfigure() {
        assertTrue(MailConfig.from(new Properties(), k -> null).isEmpty());
    }

    @Test
    void valeursParDefautEtNettoyage() {
        MailConfig c = MailConfig.from(props(
                "mail.host", " smtp.gmail.com ", "mail.user", "a@gmail.com ", "mail.password", "secret "),
                k -> null).orElseThrow();
        assertEquals("smtp.gmail.com", c.host());
        assertEquals(587, c.port());
        assertEquals(MailConfig.Security.STARTTLS, c.security());
        assertEquals("a@gmail.com", c.from());          // par défaut : l'utilisateur
        assertEquals("CyberLab", c.fromName());
        assertEquals("secret", c.password());           // espace final retiré
    }

    @Test
    void lEnvironnementPasseAvantLeFichier() {
        Map<String, String> env = Map.of("CYBERLAB_MAIL_HOST", "smtp.exemple.com",
                "CYBERLAB_MAIL_PORT", "465", "CYBERLAB_MAIL_SECURITY", "SSL",
                "CYBERLAB_MAIL_FROM", "no-reply@exemple.com");
        MailConfig c = MailConfig.from(props("mail.host", "autre.com"), env::get).orElseThrow();
        assertEquals("smtp.exemple.com", c.host());
        assertEquals(465, c.port());
        assertEquals(MailConfig.Security.SSL, c.security());
    }

    @Test
    void refuseLEnvoiNonChiffreVersUnServeurDistant() {
        assertThrows(IllegalStateException.class, () -> MailConfig.from(
                props("mail.host", "smtp.exemple.com", "mail.from", "a@b.com", "mail.security", "none"),
                k -> null));
        MailConfig.from(props("mail.host", "localhost", "mail.from", "a@b.com", "mail.security", "none"),
                k -> null).orElseThrow(); // localhost : accepté
    }

    @Test
    void refuseLesConfigurationsIncoherentes() {
        assertThrows(IllegalStateException.class, () -> MailConfig.from(
                props("mail.host", "h", "mail.from", "a@b.com", "mail.port", "abc"), k -> null));
        assertThrows(IllegalStateException.class, () -> MailConfig.from(
                props("mail.host", "h"), k -> null));                       // pas d'expéditeur
        assertThrows(IllegalStateException.class, () -> MailConfig.from(
                props("mail.host", "h", "mail.user", "a@b.com"), k -> null)); // utilisateur sans mot de passe
        assertThrows(IllegalStateException.class, () -> MailConfig.from(
                props("mail.host", "h", "mail.from", "a@b.com", "mail.security", "bof"), k -> null));
    }

    @Test
    void leMotDePasseNApparaitJamaisDansToString() {
        MailConfig c = MailConfig.from(props("mail.host", "h", "mail.user", "a@b.com",
                "mail.password", "tres-secret"), k -> null).orElseThrow();
        assertFalse(c.toString().contains("tres-secret"));
    }
}

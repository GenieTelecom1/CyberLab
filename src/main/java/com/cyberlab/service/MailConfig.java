package com.cyberlab.service;

import com.cyberlab.util.ConfigFiles;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

/**
 * Configuration de l'envoi d'e-mails (SMTP). Lue dans les variables d'environnement
 * CYBERLAB_MAIL_* (prioritaires) puis dans mail.properties, comme db.properties.
 * Ce fichier contient un secret : il ne doit jamais être publié (voir .gitignore).
 *
 * Clés : mail.host, mail.port (587), mail.user, mail.password, mail.from, mail.from.name (CyberLab),
 * mail.security = starttls (défaut) | ssl | none (none : uniquement pour localhost, tests).
 */
public record MailConfig(String host, int port, String user, String password,
                         String from, String fromName, Security security) {

    public enum Security { STARTTLS, SSL, NONE }

    private static final Set<String> HOTES_LOCAUX = Set.of("localhost", "127.0.0.1", "::1");

    /** Ne jamais afficher le mot de passe (journal, débogage). */
    @Override
    public String toString() {
        return "MailConfig[host=" + host + ", port=" + port + ", user=" + user
                + ", password=***, from=" + from + ", security=" + security + "]";
    }

    /** Configuration réelle : variables d'environnement puis mail.properties. Vide si non configuré. */
    public static Optional<MailConfig> load() {
        return from(ConfigFiles.load("mail.properties"), System::getenv);
    }

    /** Testable : les propriétés et l'environnement sont injectés. */
    static Optional<MailConfig> from(Properties p, Function<String, String> env) {
        String host = valeur(p, env, "HOST", "mail.host");
        if (host == null) return Optional.empty();

        String portTexte = valeur(p, env, "PORT", "mail.port");
        int port;
        try {
            port = portTexte == null ? 587 : Integer.parseInt(portTexte);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("mail.port invalide : " + portTexte);
        }

        String user = valeur(p, env, "USER", "mail.user");
        String password = valeur(p, env, "PASSWORD", "mail.password");
        String from = valeur(p, env, "FROM", "mail.from");
        if (from == null) from = user;
        if (from == null || !from.contains("@")) {
            throw new IllegalStateException("mail.from (ou mail.user) doit être une adresse e-mail.");
        }
        String fromName = valeur(p, env, "FROM_NAME", "mail.from.name");
        if (fromName == null) fromName = "CyberLab";

        String securiteTexte = valeur(p, env, "SECURITY", "mail.security");
        Security securite;
        try {
            securite = securiteTexte == null ? Security.STARTTLS
                    : Security.valueOf(securiteTexte.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("mail.security doit valoir starttls, ssl ou none.");
        }
        if (securite == Security.NONE && !HOTES_LOCAUX.contains(host.toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException(
                    "Envoi non chiffré refusé : mail.security=none n'est autorisé que pour localhost.");
        }
        if (user != null && password == null) {
            throw new IllegalStateException("mail.user est défini mais pas mail.password.");
        }
        return Optional.of(new MailConfig(host, port, user, password, from, fromName, securite));
    }

    private static String valeur(Properties p, Function<String, String> env, String envSuffix, String cle) {
        String v = env.apply("CYBERLAB_MAIL_" + envSuffix);
        if (v == null || v.isBlank()) v = p.getProperty(cle);
        return v == null || v.isBlank() ? null : v.trim();
    }
}

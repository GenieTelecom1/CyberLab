package com.cyberlab.service;

import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Choisit l'envoi d'e-mails au démarrage de l'application :
 * SMTP si une configuration existe, sinon console (développement uniquement).
 * En mode production (CYBERLAB_ENV=prod ou -Dcyberlab.env=prod), l'envoi par console est INTERDIT :
 * il afficherait les codes de vérification à quiconque lit la console.
 */
public final class EmailSenders {

    private static final Logger LOG = Logger.getLogger(EmailSenders.class.getName());

    private EmailSenders() {}

    public static boolean isProduction() {
        String env = System.getenv("CYBERLAB_ENV");
        if (env == null || env.isBlank()) env = System.getProperty("cyberlab.env");
        return env != null && env.trim().toLowerCase(Locale.ROOT).equals("prod");
    }

    public static EmailSender fromConfig() {
        Optional<MailConfig> config = MailConfig.load();
        if (config.isPresent()) {
            LOG.info("Envoi d'e-mails par SMTP via " + config.get().host() + ":" + config.get().port());
            return new SmtpEmailSender(config.get());
        }
        if (isProduction()) {
            throw new IllegalStateException(
                    "Mode production : envoi d'e-mails non configuré. Renseignez mail.properties "
                            + "ou les variables CYBERLAB_MAIL_*.");
        }
        LOG.warning("Envoi d'e-mails non configuré : MODE DÉVELOPPEMENT, les codes s'affichent dans la console.");
        return new ConsoleEmailSender();
    }
}

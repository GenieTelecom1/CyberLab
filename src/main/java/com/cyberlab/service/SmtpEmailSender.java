package com.cyberlab.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Envoi réel des e-mails par SMTP (Jakarta Mail), avec chiffrement obligatoire (STARTTLS ou SSL),
 * vérification du certificat du serveur et délais maximum (l'interface ne reste jamais bloquée).
 * Le contenu du message (qui contient le code) n'est jamais écrit dans le journal.
 */
public class SmtpEmailSender implements EmailSender {

    private static final String DELAI_MS = "10000";

    private final MailConfig config;
    private final Session session;

    public SmtpEmailSender(MailConfig config) {
        this.config = config;
        Properties props = new Properties();
        props.put("mail.smtp.host", config.host());
        props.put("mail.smtp.port", String.valueOf(config.port()));
        props.put("mail.smtp.connectiontimeout", DELAI_MS);
        props.put("mail.smtp.timeout", DELAI_MS);
        props.put("mail.smtp.writetimeout", DELAI_MS);
        switch (config.security()) {
            case STARTTLS -> {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.ssl.checkserveridentity", "true");
            }
            case SSL -> {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.ssl.checkserveridentity", "true");
            }
            case NONE -> { /* localhost uniquement : voir MailConfig */ }
        }
        if (config.user() != null) {
            props.put("mail.smtp.auth", "true");
            this.session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.user(), config.password());
                }
            });
        } else {
            this.session = Session.getInstance(props);
        }
    }

    @Override
    public void send(String to, String subject, String body) throws Exception {
        if (to == null || to.indexOf('\r') >= 0 || to.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("Destinataire invalide");
        }
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(config.from(), config.fromName(), "UTF-8"));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to, true)); // analyse stricte
        msg.setSubject(subject, "UTF-8");
        msg.setText(body, "UTF-8");
        Transport.send(msg);
    }
}

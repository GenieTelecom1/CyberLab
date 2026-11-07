package com.cyberlab.service;

/** Envoi d'e-mails. Deux implémentations : console (développement) et SMTP (voir dossier optional). */
public interface EmailSender {
    void send(String to, String subject, String body) throws Exception;
}

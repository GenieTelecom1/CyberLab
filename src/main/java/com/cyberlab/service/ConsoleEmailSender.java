package com.cyberlab.service;

/**
 * Mode développement : n'envoie rien, affiche le message (donc le code) dans la console.
 * À remplacer par un vrai envoi SMTP pour une utilisation réelle.
 */
public class ConsoleEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        System.out.println("==================== E-MAIL SIMULE ====================");
        System.out.println("A       : " + to);
        System.out.println("Sujet   : " + subject);
        System.out.println(body);
        System.out.println("=======================================================");
    }
}

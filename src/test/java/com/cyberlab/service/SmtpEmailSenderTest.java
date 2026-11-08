package com.cyberlab.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Envoie un vrai message SMTP à un faux serveur local (aucun réseau, aucun compte requis). */
class SmtpEmailSenderTest {

    /** Serveur SMTP minimal : accepte une connexion, renvoie le message reçu. */
    private static CompletableFuture<String> fauxServeur(ServerSocket ss) {
        return CompletableFuture.supplyAsync(() -> {
            try (Socket s = ss.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(s.getOutputStream(), true, StandardCharsets.UTF_8)) {
                StringBuilder recu = new StringBuilder();
                out.print("220 test ESMTP\r\n"); out.flush();
                boolean data = false;
                String ligne;
                while ((ligne = in.readLine()) != null) {
                    if (data) {
                        if (ligne.equals(".")) { data = false; out.print("250 OK\r\n"); out.flush(); }
                        else recu.append(ligne).append('\n');
                        continue;
                    }
                    String cmd = ligne.toUpperCase();
                    recu.append("> ").append(ligne).append('\n');
                    if (cmd.startsWith("EHLO") || cmd.startsWith("HELO")) out.print("250 test\r\n");
                    else if (cmd.startsWith("DATA")) { out.print("354 go\r\n"); data = true; }
                    else if (cmd.startsWith("QUIT")) { out.print("221 bye\r\n"); out.flush(); break; }
                    else out.print("250 OK\r\n");
                    out.flush();
                }
                return recu.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static MailConfig configPour(int port) {
        return new MailConfig("localhost", port, null, null, "no-reply@cyberlab.test", "CyberLab",
                MailConfig.Security.NONE);
    }

    @Test
    void envoieUnMessageComplet() throws Exception {
        try (ServerSocket ss = new ServerSocket(0)) {
            CompletableFuture<String> recu = fauxServeur(ss);
            new SmtpEmailSender(configPour(ss.getLocalPort())).send("alice@test.com",
                    "CyberLab - Vérification de votre adresse e-mail",
                    "Bonjour Élise,\n\nVotre code CyberLab est : 123456\nIl expire dans 15 minutes.");
            String m = recu.get(10, TimeUnit.SECONDS);
            assertTrue(m.contains("MAIL FROM:<no-reply@cyberlab.test>"), m);
            assertTrue(m.contains("RCPT TO:<alice@test.com>"), m);
            assertTrue(m.contains("Subject: "), m);
            assertTrue(m.contains("123456"), m);
            assertTrue(m.contains("From: CyberLab <no-reply@cyberlab.test>"), m);
        }
    }

    @Test
    void refuseUnDestinataireAvecInjectionDEnTete() throws Exception {
        try (ServerSocket ss = new ServerSocket(0)) {
            SmtpEmailSender sender = new SmtpEmailSender(configPour(ss.getLocalPort()));
            assertThrows(IllegalArgumentException.class,
                    () -> sender.send("a@test.com\r\nBcc: pirate@mal.com", "sujet", "corps"));
            assertThrows(Exception.class, () -> sender.send("pas-une-adresse", "sujet", "corps"));
            assertThrows(IllegalArgumentException.class, () -> sender.send(null, "sujet", "corps"));
        }
    }

    @Test
    void uneErreurReseauRemonteUneException() throws Exception {
        int portFerme;
        try (ServerSocket ss = new ServerSocket(0)) { portFerme = ss.getLocalPort(); } // fermé juste après
        SmtpEmailSender sender = new SmtpEmailSender(configPour(portFerme));
        assertThrows(Exception.class, () -> sender.send("a@test.com", "sujet", "corps"));
    }

    @Test
    void enProductionLEnvoiParConsoleEstInterdit() {
        // sans mail.properties ni CYBERLAB_MAIL_HOST, mode dev : console ; avec cyberlab.env=prod : refus
        if (MailConfig.load().isPresent()) return; // machine déjà configurée : rien à vérifier ici
        System.setProperty("cyberlab.env", "prod");
        try {
            if (System.getenv("CYBERLAB_ENV") != null) return;
            assertThrows(IllegalStateException.class, EmailSenders::fromConfig);
        } finally {
            System.clearProperty("cyberlab.env");
        }
        assertInstanceOf(ConsoleEmailSender.class, EmailSenders.fromConfig());
    }
}

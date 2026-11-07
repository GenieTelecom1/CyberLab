package com.cyberlab.service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** Après 5 échecs pour un même e-mail, la connexion est bloquée 30 secondes. */
public final class LoginAttemptLimiter {

    private static final int MAX_ECHECS = 5;
    private static final Duration BLOCAGE = Duration.ofSeconds(30);

    private static final class Etat {
        int echecs;
        Instant bloqueJusqua;
    }

    private static final Map<String, Etat> ETATS = new HashMap<>();

    private LoginAttemptLimiter() {}

    public static synchronized void verifier(String email) throws AuthException {
        Etat e = ETATS.get(email);
        if (e != null && e.bloqueJusqua != null) {
            Instant maintenant = Instant.now();
            if (maintenant.isBefore(e.bloqueJusqua)) {
                long reste = Duration.between(maintenant, e.bloqueJusqua).toSeconds() + 1;
                throw new AuthException(AuthException.Type.TROP_DE_TENTATIVES, AuthException.Field.AUCUN,
                        "Trop de tentatives. Réessayez dans " + reste + " secondes.");
            }
            ETATS.remove(email);
        }
    }

    public static synchronized void enregistrerEchec(String email) {
        Etat e = ETATS.computeIfAbsent(email, k -> new Etat());
        e.echecs++;
        if (e.echecs >= MAX_ECHECS) {
            e.bloqueJusqua = Instant.now().plus(BLOCAGE);
            e.echecs = 0;
        }
    }

    public static synchronized void reinitialiser(String email) {
        ETATS.remove(email);
    }
}

package com.cyberlab.model;

import java.time.Instant;

/** Utilisateur connecté. Ne contient jamais le hash du mot de passe. */
public record User(long id, String nom, String email, boolean emailVerifie, Instant dateCreation) {

    // Getters de compatibilité (PropertyValueFactory, appels getNom()...)
    public long getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
}

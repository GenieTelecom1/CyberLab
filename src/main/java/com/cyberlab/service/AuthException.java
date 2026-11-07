package com.cyberlab.service;

public class AuthException extends Exception {

    public enum Type {
        CHAMPS_VIDES, NOM_INVALIDE, EMAIL_INVALIDE, MOT_DE_PASSE_FAIBLE, MOT_DE_PASSE_TROP_LONG,
        MOTS_DE_PASSE_DIFFERENTS, EMAIL_DEJA_UTILISE, IDENTIFIANTS_INCORRECTS, TROP_DE_TENTATIVES,
        EMAIL_NON_VERIFIE, CODE_INVALIDE, CODE_EXPIRE, UTILISATEUR_INTROUVABLE,
        ANCIEN_MOT_DE_PASSE_INCORRECT, NON_CONNECTE, ERREUR_TECHNIQUE
    }

    /** Champ du formulaire à l'origine de l'erreur (pour le surligner). */
    public enum Field { NOM, EMAIL, PASSWORD, CONFIRM, CODE, AUCUN }

    private final Type type;
    private final Field field;

    public AuthException(Type type, Field field, String message) {
        super(message);
        this.type = type;
        this.field = field;
    }

    public AuthException(Type type, Field field, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.field = field;
    }

    // Constructeurs de J4 et J5, conservés pour que l'ancien code compile toujours
    public AuthException(Type type, String message) {
        this(type, Field.AUCUN, message);
    }

    public AuthException(Type type, String message, Throwable cause) {
        this(type, Field.AUCUN, message, cause);
    }

    public Type getType() { return type; }
    public Field getField() { return field; }
}

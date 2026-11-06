package com.cyberlab.service;
public class AuthException extends Exception {

public enum Type {
CHAMPS_VIDES, EMAIL_INVALIDE, MOT_DE_PASSE_FAIBLE, 
MOTS_DE_PASSE_DIFFERENTS,
EMAIL_DEJA_UTILISE, IDENTIFIANTS_INCORRECTS, ERREUR_TECHNIQUE
}
private final Type type;
public AuthException(Type type, String message) {
    super(message);
this.type = type;
}


public AuthException(Type type, String message, Throwable cause) {
super(message, cause);
this.type = type;
}

public Type getType() { return type; }
}
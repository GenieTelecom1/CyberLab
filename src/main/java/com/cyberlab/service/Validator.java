package com.cyberlab.service;

import com.cyberlab.service.AuthException.Field;
import com.cyberlab.service.AuthException.Type;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Validator {

    public static final int NOM_MIN = 2;
    public static final int NOM_MAX = 100;
    public static final int EMAIL_MAX = 255;
    public static final int PASSWORD_MIN = 8;
    public static final int BCRYPT_MAX_BYTES = 72;
    public static final int CODE_LENGTH = 6;

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+[.][A-Za-z]{2,}$");
    private static final String NOM_SYMBOLES = "' -.";

    private Validator() {}

    public static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** Renvoie le nom nettoyé (espaces superflus retirés). */
    public static String nom(String raw) throws AuthException {
        if (isBlank(raw)) {
            throw new AuthException(Type.CHAMPS_VIDES, Field.NOM, "Le nom est obligatoire.");
        }
        String n = raw.trim().replaceAll(" +", " ");
        if (n.length() < NOM_MIN || n.length() > NOM_MAX) {
            throw new AuthException(Type.NOM_INVALIDE, Field.NOM,
                    "Le nom doit contenir entre " + NOM_MIN + " et " + NOM_MAX + " caractères.");
        }
        if (!Character.isLetter(n.charAt(0))) {
            throw new AuthException(Type.NOM_INVALIDE, Field.NOM, "Le nom doit commencer par une lettre.");
        }
        for (int i = 0; i < n.length(); i++) {
            char ch = n.charAt(i);
            if (!Character.isLetter(ch) && NOM_SYMBOLES.indexOf(ch) < 0) {
                throw new AuthException(Type.NOM_INVALIDE, Field.NOM,
                        "Le nom contient des caractères non autorisés.");
            }
        }
        return n;
    }

    /** Renvoie l'e-mail normalisé (minuscules, sans espaces autour). */
    public static String email(String raw) throws AuthException {
        if (isBlank(raw)) {
            throw new AuthException(Type.CHAMPS_VIDES, Field.EMAIL, "L'e-mail est obligatoire.");
        }
        String e = raw.trim().toLowerCase(Locale.ROOT);
        if (e.length() > EMAIL_MAX || !EMAIL.matcher(e).matches()) {
            throw new AuthException(Type.EMAIL_INVALIDE, Field.EMAIL, "Adresse e-mail invalide.");
        }
        return e;
    }

    public static void password(String p) throws AuthException {
        if (isBlank(p)) {
            throw new AuthException(Type.CHAMPS_VIDES, Field.PASSWORD, "Le mot de passe est obligatoire.");
        }
        boolean lettre = p.chars().anyMatch(Character::isLetter);
        boolean chiffre = p.chars().anyMatch(Character::isDigit);
        if (p.length() < PASSWORD_MIN || !lettre || !chiffre) {
            throw new AuthException(Type.MOT_DE_PASSE_FAIBLE, Field.PASSWORD,
                    "Le mot de passe doit contenir au moins " + PASSWORD_MIN
                            + " caractères, une lettre et un chiffre.");
        }
        if (p.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new AuthException(Type.MOT_DE_PASSE_TROP_LONG, Field.PASSWORD,
                    "Le mot de passe est trop long (" + BCRYPT_MAX_BYTES + " octets maximum).");
        }
    }

    public static void confirmation(String password, String confirm) throws AuthException {
        if (isBlank(confirm)) {
            throw new AuthException(Type.CHAMPS_VIDES, Field.CONFIRM, "Veuillez confirmer le mot de passe.");
        }
        if (!password.equals(confirm)) {
            throw new AuthException(Type.MOTS_DE_PASSE_DIFFERENTS, Field.CONFIRM,
                    "Les mots de passe ne correspondent pas.");
        }
    }

    /** Code à 6 chiffres reçu par e-mail. */
    public static String code(String raw) throws AuthException {
        String c = raw == null ? "" : raw.trim();
        if (c.isEmpty()) {
            throw new AuthException(Type.CHAMPS_VIDES, Field.CODE, "Saisissez le code reçu par e-mail.");
        }
        if (c.length() != CODE_LENGTH || !c.chars().allMatch(Character::isDigit)) {
            throw new AuthException(Type.CODE_INVALIDE, Field.CODE,
                    "Le code contient " + CODE_LENGTH + " chiffres.");
        }
        return c;
    }
}

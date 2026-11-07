package com.cyberlab.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/** Tests unitaires de la validation (sans base de données : rapides et exécutables hors ligne). */
class ValidatorTest {

    private static AuthException.Type erreur(Executable e) {
        return assertThrows(AuthException.class, e).getType();
    }

    @Test
    void nomValideEstNettoye() throws Exception {
        assertEquals("Élise Ben Ali", Validator.nom("   Élise   Ben Ali "));
        assertEquals("Jean-Pierre O'Neil", Validator.nom("Jean-Pierre O'Neil"));
    }

    @Test
    void nomInvalide() {
        assertEquals(AuthException.Type.CHAMPS_VIDES, erreur(() -> Validator.nom("   ")));
        assertEquals(AuthException.Type.NOM_INVALIDE, erreur(() -> Validator.nom("A")));
        assertEquals(AuthException.Type.NOM_INVALIDE, erreur(() -> Validator.nom("A".repeat(101))));
        assertEquals(AuthException.Type.NOM_INVALIDE, erreur(() -> Validator.nom("<script>")));
        assertEquals(AuthException.Type.NOM_INVALIDE, erreur(() -> Validator.nom("1Alice")));
        assertEquals(AuthException.Type.NOM_INVALIDE, erreur(() -> Validator.nom("Ali;DROP")));
    }

    @Test
    void nomLimites() throws Exception {
        assertEquals("Al", Validator.nom("Al"));
        assertEquals(100, Validator.nom("A".repeat(100)).length());
    }

    @Test
    void emailEstNormalise() throws Exception {
        assertEquals("ali@test.com", Validator.email("   Ali@Test.COM "));
    }

    @Test
    void emailInvalide() {
        assertEquals(AuthException.Type.CHAMPS_VIDES, erreur(() -> Validator.email("")));
        assertEquals(AuthException.Type.EMAIL_INVALIDE, erreur(() -> Validator.email("pas-un-email")));
        assertEquals(AuthException.Type.EMAIL_INVALIDE, erreur(() -> Validator.email("a@b")));
        assertEquals(AuthException.Type.EMAIL_INVALIDE, erreur(() -> Validator.email("a b@test.com")));
        assertEquals(AuthException.Type.EMAIL_INVALIDE,
                erreur(() -> Validator.email("a".repeat(250) + "@test.com")));
    }

    /** Bug B2 : toLowerCase() sans langue transforme le I en ı avec une langue système turque. */
    @Test
    void emailIndependantDeLaLangueDuSysteme() throws Exception {
        Locale avant = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"));
            assertEquals("info@test.com", Validator.email("INFO@TEST.COM"));
        } finally {
            Locale.setDefault(avant);
        }
    }

    @Test
    void motDePasse() throws Exception {
        Validator.password("Secret123");
        assertEquals(AuthException.Type.CHAMPS_VIDES, erreur(() -> Validator.password("  ")));
        assertEquals(AuthException.Type.MOT_DE_PASSE_FAIBLE, erreur(() -> Validator.password("Ab1")));
        assertEquals(AuthException.Type.MOT_DE_PASSE_FAIBLE, erreur(() -> Validator.password("abcdefgh")));
        assertEquals(AuthException.Type.MOT_DE_PASSE_FAIBLE, erreur(() -> Validator.password("12345678")));
        assertEquals(AuthException.Type.MOT_DE_PASSE_TROP_LONG,
                erreur(() -> Validator.password("a1".repeat(37))));
    }

    @Test
    void motDePasseCompteEnOctetsPasEnCaracteres() throws Exception {
        Validator.password("Ab1" + "é".repeat(34));      // 37 caractères, 71 octets : accepté
        assertEquals(AuthException.Type.MOT_DE_PASSE_TROP_LONG,
                erreur(() -> Validator.password("Ab1" + "😀".repeat(18)))); // 39 caractères, 75 octets
    }

    @Test
    void confirmation() throws Exception {
        Validator.confirmation("Secret123", "Secret123");
        assertEquals(AuthException.Type.MOTS_DE_PASSE_DIFFERENTS,
                erreur(() -> Validator.confirmation("Secret123", "Secret124")));
        assertEquals(AuthException.Type.CHAMPS_VIDES, erreur(() -> Validator.confirmation("Secret123", "")));
    }

    @Test
    void codeAChiffres() throws Exception {
        assertEquals("012345", Validator.code(" 012345 "));
        assertEquals(AuthException.Type.CHAMPS_VIDES, erreur(() -> Validator.code("")));
        assertEquals(AuthException.Type.CODE_INVALIDE, erreur(() -> Validator.code("12345")));
        assertEquals(AuthException.Type.CODE_INVALIDE, erreur(() -> Validator.code("12a456")));
    }

    @Test
    void limiteLesEssaisDeConnexion() throws Exception {
        String email = "limite-" + System.nanoTime() + "@test.com";
        for (int i = 0; i < 5; i++) LoginAttemptLimiter.enregistrerEchec(email);
        assertEquals(AuthException.Type.TROP_DE_TENTATIVES,
                erreur(() -> LoginAttemptLimiter.verifier(email)));
        LoginAttemptLimiter.reinitialiser(email);
        LoginAttemptLimiter.verifier(email); // ne lève plus d'exception
    }

    @Test
    void lesAnciensConstructeursDeAuthExceptionCompilentToujours() {
        AuthException e = new AuthException(AuthException.Type.CHAMPS_VIDES, "message");
        assertEquals(AuthException.Field.AUCUN, e.getField());
    }
}

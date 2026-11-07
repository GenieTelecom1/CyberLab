package com.cyberlab.service;

import static org.junit.jupiter.api.Assertions.*;

import com.cyberlab.util.PasswordUtil;
import com.cyberlab.util.SessionManager;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires de J4, sans base de données : les validations de AuthService lèvent leur
 * exception avant tout accès réseau, donc ces tests fonctionnent hors ligne.
 */
class AuthTest {

    private final AuthService auth = new AuthService();

    @Test
    void hashDifferentDuMotDePasseEtVerifiable() {
        String hash = PasswordUtil.hash("Secret123");
        assertNotEquals("Secret123", hash);
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(PasswordUtil.verify("Secret123", hash));
        assertFalse(PasswordUtil.verify("autre", hash));
    }

    @Test
    void deuxHashDuMemeMotDePasseSontDifferents() {
        assertNotEquals(PasswordUtil.hash("Secret123"), PasswordUtil.hash("Secret123"));
    }

    @Test
    void hashInvalideNeLevePasDException() {
        assertFalse(PasswordUtil.verify("Secret123", "pas-un-hash"));
    }

    @Test
    void inscriptionAvecChampsVides() {
        AuthException e = assertThrows(AuthException.class, () -> auth.register("", "", "", ""));
        assertEquals(AuthException.Type.CHAMPS_VIDES, e.getType());
    }

    @Test
    void inscriptionAvecEmailInvalide() {
        AuthException e = assertThrows(AuthException.class,
                () -> auth.register("Ali", "pas-un-email", "Secret123", "Secret123"));
        assertEquals(AuthException.Type.EMAIL_INVALIDE, e.getType());
    }

    @Test
    void inscriptionAvecMotDePasseFaible() {
        AuthException e = assertThrows(AuthException.class,
                () -> auth.register("Ali", "ali@test.com", "abc", "abc"));
        assertEquals(AuthException.Type.MOT_DE_PASSE_FAIBLE, e.getType());
    }

    @Test
    void inscriptionAvecConfirmationDifferente() {
        AuthException e = assertThrows(AuthException.class,
                () -> auth.register("Ali", "ali@test.com", "Secret123", "Secret124"));
        assertEquals(AuthException.Type.MOTS_DE_PASSE_DIFFERENTS, e.getType());
    }

    @Test
    void connexionAvecChampsVides() {
        AuthException e = assertThrows(AuthException.class, () -> auth.login("", ""));
        assertEquals(AuthException.Type.CHAMPS_VIDES, e.getType());
    }

    @Test
    void sessionVideApresDeconnexionEtRequireUserLeveUneErreur() {
        auth.logout();
        assertFalse(SessionManager.isLoggedIn());
        assertNull(SessionManager.getCurrentUser());
        AuthException e = assertThrows(AuthException.class, SessionManager::requireUser);
        assertEquals(AuthException.Type.NON_CONNECTE, e.getType());
    }
}

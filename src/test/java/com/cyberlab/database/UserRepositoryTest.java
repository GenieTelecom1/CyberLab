package com.cyberlab.database;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.cyberlab.TestDb;
import com.cyberlab.model.User;
import com.cyberlab.util.PasswordUtil;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

/**
 * Test du CRUD sur la vraie base. Ignoré automatiquement sans db.properties (ni variables
 * d'environnement) : il ne casse donc pas la construction chez un membre sans configuration.
 */
class UserRepositoryTest {

    private final UserRepository repo = new UserRepository();

    @AfterAll
    static void fermer() {
        DatabaseConnection.close();
    }

    @Test
    void crudComplet() throws Exception {
        assumeTrue(TestDb.isConfigured(), "base non configurée : test ignoré");
        String email = "crud-" + UUID.randomUUID() + "@test.com";
        User cree = repo.insert("Test CRUD", email, PasswordUtil.hash("Secret123"));
        try {
            // Create + Read
            assertEquals(email, cree.email());
            assertFalse(cree.emailVerifie());
            assertTrue(repo.existsByEmail(email));
            assertEquals("Test CRUD", repo.findById(cree.id()).orElseThrow().nom());
            assertTrue(repo.findAll().stream().anyMatch(u -> u.id() == cree.id()));
            assertEquals(cree.id(), repo.findCredentialsByEmail(email).orElseThrow().user().id());

            // Update
            assertTrue(repo.updateNom(cree.id(), "Nom modifié"));
            assertEquals("Nom modifié", repo.findById(cree.id()).orElseThrow().nom());
            String nouveauHash = PasswordUtil.hash("Autre1234");
            assertTrue(repo.updatePasswordHash(cree.id(), nouveauHash));
            assertTrue(PasswordUtil.verify("Autre1234", repo.findHashById(cree.id()).orElseThrow()));
            assertTrue(repo.markEmailVerified(cree.id()));
            assertTrue(repo.findById(cree.id()).orElseThrow().emailVerifie());
        } finally {
            // Delete (nettoyage même si un assert échoue)
            assertTrue(repo.delete(cree.id()));
        }
        assertTrue(repo.findById(cree.id()).isEmpty());
        assertFalse(repo.existsByEmail(email));
    }

    @Test
    void emailDejaUtiliseEstRefuseParLaBase() throws Exception {
        assumeTrue(TestDb.isConfigured(), "base non configurée : test ignoré");
        String email = "dup-" + UUID.randomUUID() + "@test.com";
        User cree = repo.insert("Doublon", email, PasswordUtil.hash("Secret123"));
        try {
            java.sql.SQLException ex = assertThrows(java.sql.SQLException.class,
                    () -> repo.insert("Doublon 2", email, PasswordUtil.hash("Secret123")));
            assertEquals("23505", ex.getSQLState());
        } finally {
            repo.delete(cree.id());
        }
    }

    @Test
    void emailEnMajusculesEstRefuseParLaBase() throws Exception {
        assumeTrue(TestDb.isConfigured(), "base non configurée : test ignoré");
        String email = "Maj-" + UUID.randomUUID() + "@test.com";
        java.sql.SQLException ex = assertThrows(java.sql.SQLException.class,
                () -> repo.insert("Majuscules", email, PasswordUtil.hash("Secret123")));
        assertEquals("23514", ex.getSQLState()); // contrainte email_minuscule
    }
}

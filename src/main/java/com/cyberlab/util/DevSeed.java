package com.cyberlab.util;

import com.cyberlab.database.DatabaseConnection;
import com.cyberlab.database.UserRepository;
import java.util.Optional;

/**
 * Outil de développement (à lancer une fois depuis l'IDE, ne fait pas partie de l'application).
 * Crée les comptes de test de J5 avec de vrais hashs BCrypt, déjà vérifiés par e-mail
 * (AuthService.register exige désormais la vérification par code : on passe donc par le repository).
 * Le relancer est sans danger : un compte existant est conservé et simplement marqué vérifié.
 * Mot de passe commun : Test1234 (comptes de test uniquement).
 */
public class DevSeed {

    private static final String MOT_DE_PASSE = "Test1234";

    public static void main(String[] args) {
        UserRepository repo = new UserRepository();
        String[][] comptes = {
                {"Alice Test", "alice@test.com"},
                {"Bob Test", "bob@test.com"},
                {"Chaima Test", "chaima@test.com"}
        };
        try {
            for (String[] c : comptes) {
                Optional<UserRepository.Credentials> existant = repo.findCredentialsByEmail(c[1]);
                long id;
                if (existant.isPresent()) {
                    id = existant.get().user().id();
                    System.out.println(c[1] + " : existe déjà");
                } else {
                    id = repo.insert(c[0], c[1], PasswordUtil.hash(MOT_DE_PASSE)).id();
                    System.out.println("Créé : " + c[1]);
                }
                repo.markEmailVerified(id);
            }
        } catch (Exception e) {
            System.out.println("Echec : " + e.getMessage());
        } finally {
            DatabaseConnection.close();
        }
    }
}

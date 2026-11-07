# J9 – Rapport de tests du module Auth / BD

CyberLab – Membre 1 (Authentification et BD)

## 1. Tests automatiques

Résultats obtenus lors de la mise à jour du projet, sur **PostgreSQL 16 local** (schéma `db/schema.sql`)
via JDBC. À **rejouer sur le projet Supabase** (c'est ce qui valide la vraie base) : `mvn test`.

| Catégorie | Classe | Nombre de tests | Réussis | Échoués | Ignorés |
|-----------|--------|-----------------|---------|---------|---------|
| Automatiques hors ligne | `ValidatorTest` | 12 | 12 | 0 | 0 |
| Automatiques hors ligne | `AuthTest` | 9 | 9 | 0 | 0 |
| Automatiques avec base | `UserRepositoryTest` | 3 | 3 | 0 | 0 |
| Automatiques avec base | `AuthServiceIT` | 11 | 11 | 0 | 0 |

Sans `db.properties` (ni variables `CYBERLAB_DB_*`), les 14 tests avec base sont **ignorés** (voulu).
Surefire est configuré dans le `pom.xml` pour exécuter aussi les classes `*IT`.

`AuthServiceIT` couvre : critère d'acceptation (créer, vérifier, se déconnecter, « redémarrer », se
reconnecter), hash BCrypt en base, connexion refusée tant que l'e-mail n'est pas vérifié, mauvais code,
mauvais mot de passe, e-mail déjà utilisé, injection SQL, inscriptions simultanées, suppression de compte,
changement de mot de passe, changement de nom (session mise à jour).

## 2. Tests manuels et de sécurité

| Catégorie | Nombre de tests | Réussis | Échoués |
|-----------|-----------------|---------|---------|
| Manuels interface (J4, J6) | 19 | à remplir | à remplir |
| Inter-modules (J8) | 14 | à remplir | à remplir |
| Sécurité (S1 à S8) | 8 | à remplir | à remplir |

Contrôles de sécurité : S1 `SELECT email FROM users WHERE mot_de_passe_hash NOT LIKE '$2%';` → 0 ligne ;
S2 console propre ; S3 aucun mot de passe de simulation enregistré ; S4 accès anonyme REST fermé (RLS) ;
S5 injection SQL ; S6 secrets absents des zips ; S7 requêtes préparées partout ; S8 limite d'essais.

## 3. Mesures de performance

| Action | Objectif | Mesuré |
|--------|----------|--------|
| Premier Login après lancement | < 5 s (< 2 s avec `warmUp`) | à remplir |
| Login suivant | < 1,5 s | à remplir |
| Sign Up | < 2 s | à remplir |
| Logout | Instantané | à remplir |

## 4. Registre des bugs du module

« Statut dans le code » = vérifié à la relecture du code lors de la mise à jour ; les colonnes
« Reproduit » et « Corrigé » (reproduction réelle) restent à remplir par le testeur.

| ID | Description | Gravité | Statut dans le code | Reproduit | Corrigé | Test de non-régression |
|----|-------------|---------|---------------------|-----------|---------|------------------------|
| B1 | Erreur d'initialisation du pool sans réseau | Majeur | `setInitializationFailTimeout(-1)` présent dans `DatabaseConnection` | à remplir | à remplir | Test manuel 8 de J6 |
| B2 | Minuscules dépendantes de la langue | Mineur | `Locale.ROOT` dans `Validator.email` et `AuthService.login` | à remplir | à remplir | `ValidatorTest.emailIndependantDeLaLangueDuSysteme` |
| B3 | Espaces dans `db.properties` | Majeur | `.trim()` dans `DatabaseConnection.config` | à remplir | à remplir | Test manuel |
| B4 | Double déclenchement par Entrée | Majeur | Garde `isDisabled()` dans `LoginController`, `RegisterController`, `SignUpController` (ce dernier ajouté lors de la mise à jour) | à remplir | à remplir | Test manuel |
| B5 | Premier appel lent | Mineur | `DatabaseConnection.warmUp()` existe et est appelé dans `AuthDemoApp` ; **à appeler aussi dans `Application.start()` de l'application finale (Membre 6)** | à remplir | à remplir | Chronomètre |
| B6 | Différence de temps e-mail inconnu / mot de passe faux | Mineur | Hash factice `Factice.HASH` dans `AuthService.login` | à remplir | à remplir | Mesure |
| B7 | Débordement du nom dans le Dashboard | Mineur | Fichier du Membre 2 : `setTextOverrun(OverrunStyle.ELLIPSIS)` à demander dans #bugs-and-errors | à remplir | à remplir | Test manuel |

## 5. Non-régression finale

1. `mvn test` (ValidatorTest, AuthTest, UserRepositoryTest, AuthServiceIT) ;
2. tests manuels J4 (9), J6 (10), J8 (14 scénarios) ;
3. contrôles de sécurité S1 à S8 ;
4. mesures de performance.

Aucune ligne de test (`it-`, `crud-`, `dup-`) ne doit rester dans `users` :
`SELECT email FROM users WHERE email ~ '^(it|crud|dup)-';` → 0 ligne.

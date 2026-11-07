# J8 – Rapport de tests d'intégration (authentification et autres modules)

CyberLab – Membre 1 (Authentification et BD)

**Préparation** : `db/cleanup_test_data.sql` → `DevSeed` → `db/seed_test_data.sql`, puis lancer la dernière
version intégrée avec ton `db.properties`. Deux fenêtres : l'application et le Table Editor de Supabase.

> Note : l'inscription demande désormais un code de vérification d'e-mail (affiché dans la console
> avec `ConsoleEmailSender`). Le scénario 8 inclut donc la saisie de ce code.

| # | Scénario | Modules | Résultat attendu | Résultat obtenu |
|---|----------|---------|------------------|-----------------|
| 1 | Se connecter avec alice@test.com | 1, 2 | Dashboard : « Bienvenue Alice Test », 33 %, score moyen 90,0 | à remplir |
| 2 | Ouvrir Learning avec Alice | 3 | Cours 1 et 2 marqués terminés, les 4 autres non | à remplir |
| 3 | Marquer le cours 3 terminé, revenir au Dashboard | 3, 2 | Progression 50 % ; ligne `progress` au nom d'Alice en base | à remplir |
| 4 | Faire un quiz complet (par exemple 4/5) | 4 | Ligne `scores` au nom d'Alice ; historique et leaderboard mis à jour | à remplir |
| 5 | Déconnexion, puis connexion avec bob@test.com | 1, 2, 3, 4 | Dashboard de Bob : 17 % et 60,0 ; aucune donnée d'Alice à l'écran | à remplir |
| 6 | Après déconnexion, tenter d'ouvrir un écran protégé (bouton Retour, menu) | 1, 2 | Retour à l'écran de connexion | à remplir |
| 7 | Saisir un mot de passe dans le Password Checker | 5 | Rien en base, rien dans la console, aucun fichier créé | à remplir |
| 8 | Créer un nouveau compte (Dina), saisir le code de vérification | 1, 2, 3, 4 | Dashboard à 0 %, « aucun quiz » sans erreur, leaderboard sans Dina | à remplir |
| 9 | Couper Internet pendant l'ouverture de Learning, puis du Quiz | 3, 4 | Message clair, interface non gelée, retour possible, reprise après retour du réseau | à remplir |
| 10 | Lancer deux instances de l'application avec deux comptes | 1 à 4 | Chaque instance garde sa propre session et ses propres données | à remplir |
| 11 | Fermer puis relancer l'application | 1, 6 | Écran de connexion (pas de session conservée) ; données toujours en base | à remplir |
| 12 | Supprimer le compte connecté (compte temporaire) | 1 | Session vidée, retour à la connexion, scores et progression supprimés | à remplir |
| 13 | Utiliser l'assistant IA connecté | 6 | Chat fonctionnel, aucune requête sur `users` | à remplir |
| 14 | Changer le nom du compte (si un écran de profil existe) | 1, 2 | Le Dashboard affiche le nouveau nom au chargement suivant | à remplir |

Après la campagne : recharger l'état de départ avec `db/seed_test_data.sql` (il remet aussi à zéro la
progression des comptes de test, donc le cours 3 marqué au scénario 3 est bien retiré).

## Vérification en base (section 5.3 de J8)

Requêtes dans `db/requetes_reference.sql`, bloc « VERIFICATIONS ». Après les scénarios 3 et 4, les
nouvelles lignes doivent porter l'e-mail d'Alice, et aucune ligne ne doit avoir été ajoutée au nom de Bob.

## Audit rapide du code des autres modules (section 6 de J8)

```
grep -rnE "prepareStatement|DriverManager|getConnection" src/main/java/com/cyberlab/controller
grep -rniE "println.*(pass|mot_de_passe|hash)" src/main/java
grep -rn "SELECT \* FROM users" src/main/java
grep -rn "getCurrentUser" src/main/java
```

| Recherche | Attendu | Résultat |
|-----------|---------|----------|
| SQL ou connexion dans un contrôleur | Aucune ligne | à remplir |
| Affichage d'un mot de passe ou d'un hash | Aucune ligne | à remplir |
| `SELECT * FROM users` | Aucune ligne | à remplir |
| `getCurrentUser` | Lecture sur le thread JavaFX, test de `null` | à remplir |

## Bugs trouvés

À poster dans #bugs-and-errors avec le modèle `[BUG-J8-01] Titre court` (module, gravité, étapes,
résultat obtenu / attendu, capture). Gravité : bloquant / majeur / mineur.

| ID | Module | Gravité | Résumé |
|----|--------|---------|--------|
| … | … | … | … |

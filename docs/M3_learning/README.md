# Module Learning (Membre 3)

6 cours de cybersécurité, découpés en parties (61 au total), avec progression par partie et pourcentage par cours.

## Fichiers du module
- `com/cyberlab/model/Course.java` : un cours + ses parties (le champ `contenu` est découpé sur les lignes `## Titre`)
- `com/cyberlab/service/CourseService.java` : lecture des cours (table `courses`)
- `com/cyberlab/service/ProgressService.java` : progression (table `progress`)
- `com/cyberlab/controller/CourseController.java` : liste des cours (cartes, stats, % par cours)
- `com/cyberlab/controller/CourseReaderController.java` : lecture partie par partie
- `src/main/resources/fxml/learning-list.fxml` et `learning-reader.fxml`
- `src/main/resources/css/learning.css` : style du module (classes `lrn-*`, aucun conflit avec `cyberlab.css`)

## Ce dont le module a besoin des autres membres
- **M1** : `DatabaseConnection.getConnection()` : méthode statique qui renvoie un `java.sql.Connection` (lance `SQLException`).
- **M1** : une façon d'obtenir l'utilisateur connecté et son id (`AuthService` ou `SessionManager`).
  Une seule ligne à adapter : `CourseController.currentUserId()`. Si `getId()` renvoie un `long`, ajouter `(int)`.
- **M1** : la base Supabase est déjà prête (tables `courses` avec 6 cours, `progress` avec `parties_terminees`).
  Les scripts sont dans `docs/M3_learning/sql/`. Ne pas modifier les ids 1 à 6 de `courses` (la table `questions` y est liée).

## Ce que le module offre aux autres
- **M2 (Dashboard)** : afficher l'écran Learning avec `FXMLLoader.load(getClass().getResource("/fxml/learning-list.fxml"))`
  puis le mettre dans la zone centrale. Pour la carte « Learning Progress » :
  `new ProgressService().getProgressPercentage(userId)` (0 à 100).
- **M4 (Leaderboard)** : `new ProgressService().getCompletedCount(userId)` = nombre de cours terminés (sur 6).
- **M6 (intégration)** : le module charge son CSS tout seul depuis les FXML.

## Pour tester
1. Se connecter dans l'application avec un utilisateur de la table `users`.
2. Ouvrir Learning, ouvrir un cours, cliquer « Partie terminée, suivante ».
3. Dans Supabase, table `progress` : une ligne apparaît avec `parties_terminees = 1`.

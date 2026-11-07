-- =====================================================================================
-- CyberLab - requetes_reference.sql (Membre 1, J7)
-- Requetes pretes a l'emploi pour les Membres 2, 3 et 4 (sections 3 a 5 du guide J7).
--
-- Comment l'utiliser
--   * Chaque requete est ici en version EXECUTABLE dans le SQL Editor de Supabase, avec les
--     comptes de test (seed_test_data.sql) : les valeurs attendues sont donnees en commentaire.
--   * En Java (PreparedStatement), remplacer le parametre repere par un "?" :
--       [USER]   (SELECT id FROM users WHERE email = 'alice@test.com')   ->  ?  (setLong)
--       [COURS]  (SELECT id FROM courses WHERE ordre = 2)                ->  ?  (setInt)
--       [LIMITE] 10                                                       ->  ?  (setInt)
--   * Les requetes qui ECRIVENT dans la base partagee sont entre /* ... */ : les decommenter
--     une par une pour les tester.
--   * Regles PostgreSQL : noms en minuscules sans guillemets ; WHERE termine (pas = 1) ;
--     100.0 * a / b (evite la division entiere) ; COALESCE sur AVG ; $$ ... $$ pour les textes.
-- =====================================================================================


-- =====================================================================================
-- MEMBRE 3 - Learning
-- =====================================================================================

-- Membre 3 : liste des cours (6 cartes) avec l'etat pour l'utilisateur connecte
-- Attendu pour Alice : 6 lignes, termine = true pour les cours 1 et 2 seulement.
SELECT c.id, c.titre, c.description, COALESCE(p.termine, false) AS termine
FROM courses c
LEFT JOIN progress p ON p.course_id = c.id
                    AND p.user_id = (SELECT id FROM users WHERE email = 'alice@test.com')  -- [USER]
ORDER BY c.ordre;

-- Membre 3 : lecture d'une lecon
SELECT id, titre, contenu, ordre
FROM courses
WHERE id = (SELECT id FROM courses WHERE ordre = 2);                                        -- [COURS]

-- Membre 3 : bouton Suivant (aucune ligne = dernier cours : desactiver le bouton)
SELECT id FROM courses
WHERE ordre > (SELECT ordre FROM courses WHERE id = (SELECT id FROM courses WHERE ordre = 2))  -- [COURS]
ORDER BY ordre ASC LIMIT 1;

-- Membre 3 : bouton Precedent (aucune ligne = premier cours : desactiver le bouton)
SELECT id FROM courses
WHERE ordre < (SELECT ordre FROM courses WHERE id = (SELECT id FROM courses WHERE ordre = 2))  -- [COURS]
ORDER BY ordre DESC LIMIT 1;

-- Membre 3 : « Marquer comme termine » (cree la ligne ou la met a jour ; jamais de doublon)
-- ECRIT dans la base. Decommenter pour tester ; rejouer seed_test_data.sql ensuite.
/*
INSERT INTO progress (user_id, course_id, termine)
VALUES ((SELECT id FROM users WHERE email = 'alice@test.com'),                              -- [USER]
        (SELECT id FROM courses WHERE ordre = 3),                                           -- [COURS]
        true)
ON CONFLICT (user_id, course_id) DO UPDATE SET termine = true, date = now();
*/

-- Membre 3 : pourcentage de progression (Dashboard et Leaderboard)
-- Attendu : Alice 33, Bob 17, Chaima 0.
SELECT COALESCE(ROUND(100.0 * (SELECT COUNT(*) FROM progress
                               WHERE user_id = (SELECT id FROM users WHERE email = 'alice@test.com')  -- [USER]
                                 AND termine)
                / NULLIF((SELECT COUNT(*) FROM courses), 0)), 0) AS pourcentage;

-- Membre 3 : remplir le contenu d'un cours (texte long avec apostrophes) : guillemets dollar
-- ECRIT dans la base. Decommenter pour tester.
/*
UPDATE courses
SET contenu = $$Le phishing (hameçonnage) est une technique d'attaque ...
On l'utilise pour voler des identifiants.$$
WHERE ordre = 2;
*/


-- =====================================================================================
-- MEMBRE 4 - Quiz et Leaderboard
-- =====================================================================================

-- Membre 4 : inserer des questions (le cours est retrouve par son ordre : aucun id en dur)
-- ECRIT dans la base. Doubler les apostrophes (l''utilisateur). bonne_reponse : A, B, C ou D.
/*
INSERT INTO questions (course_id, texte, choix_a, choix_b, choix_c, choix_d, bonne_reponse)
SELECT c.id, v.texte, v.ca, v.cb, v.cc, v.cd, v.bonne
FROM courses c,
(VALUES
  ('Que signifie CIA en cybersécurité ?',
   'Confidentialité, Intégrité, Disponibilité', 'Contrôle, Identité, Accès',
   'Chiffrement, Intégrité, Authentification', 'Aucune de ces réponses', 'A'),
  ('Quelle propriété garantit que les données ne sont pas modifiées ?',
   'Confidentialité', 'Intégrité', 'Disponibilité', 'Anonymat', 'B')
) AS v(texte, ca, cb, cc, cd, bonne)
WHERE c.ordre = 1;
*/

-- Membre 4 : charger les questions d'un quiz (5 questions, ordre aleatoire)
-- Pour un ordre fixe : ORDER BY id.
SELECT id, texte, choix_a, choix_b, choix_c, choix_d, bonne_reponse
FROM questions
WHERE course_id = (SELECT id FROM courses WHERE ordre = 1)                                  -- [COURS]
ORDER BY random()
LIMIT 5;                                                                                    -- [LIMITE]

-- Membre 4 : enregistrer le score final (le calcul se fait en Java ; la base verifie 0 <= score <= total)
-- ECRIT dans la base. Decommenter pour tester ; rejouer seed_test_data.sql ensuite.
/*
INSERT INTO scores (user_id, course_id, score, total)
VALUES ((SELECT id FROM users WHERE email = 'alice@test.com'),                              -- [USER]
        (SELECT id FROM courses WHERE ordre = 3),                                           -- [COURS]
        4, 5)
RETURNING id;
*/

-- Membre 4 : historique des resultats d'un utilisateur
-- Attendu : Alice 2 lignes, Bob 1 ligne, Chaima 0 ligne.
SELECT c.titre, s.score, s.total, s.date
FROM scores s JOIN courses c ON c.id = s.course_id
WHERE s.user_id = (SELECT id FROM users WHERE email = 'alice@test.com')                     -- [USER]
ORDER BY s.date DESC;

-- Membre 4 : leaderboard (moyenne en pourcentage, avec rang ; RANK = meme rang en cas d'egalite)
-- Attendu : 1 Alice Test 90.0 (2 quiz) ; 2 Bob Test 60.0 (1 quiz) ; Chaima absente.
-- Le leaderboard n'affiche que le nom, jamais l'e-mail.
SELECT RANK() OVER (ORDER BY AVG(100.0 * s.score / s.total) DESC) AS rang,
       u.nom,
       ROUND(AVG(100.0 * s.score / s.total), 1) AS moyenne,
       COUNT(*) AS nb_quiz
FROM scores s JOIN users u ON u.id = s.user_id
GROUP BY u.id, u.nom
ORDER BY rang, u.nom
LIMIT 10;                                                                                   -- [LIMITE]

-- Membre 4 : variante du leaderboard - seulement le meilleur score de chaque cours
-- (empeche de gonfler sa moyenne en refaisant un quiz facile). Choix = decision n°4 de J2.
WITH meilleur AS (
  SELECT user_id, course_id, MAX(100.0 * score / total) AS pct
  FROM scores GROUP BY user_id, course_id
)
SELECT u.nom, ROUND(AVG(m.pct), 1) AS moyenne, COUNT(*) AS cours_evalues
FROM meilleur m JOIN users u ON u.id = m.user_id
GROUP BY u.id, u.nom
ORDER BY moyenne DESC
LIMIT 10;                                                                                   -- [LIMITE]

-- Membre 4 : position de l'utilisateur connecte dans le classement
-- Attendu : Alice 1, Bob 2, Chaima aucune ligne (absente du classement).
SELECT rang FROM (
  SELECT user_id, RANK() OVER (ORDER BY AVG(100.0 * score / total) DESC) AS rang
  FROM scores GROUP BY user_id
) t
WHERE user_id = (SELECT id FROM users WHERE email = 'alice@test.com');                      -- [USER]


-- =====================================================================================
-- MEMBRE 2 - Dashboard : une seule requete pour toutes les statistiques
-- (en Java, le parametre [USER] apparait deux fois : setLong(1, id) et setLong(2, id))
-- Attendu : Alice 2 / 6 / 90.0 ; Bob 1 / 6 / 60.0 ; Chaima 0 / 6 / 0.
-- =====================================================================================
SELECT (SELECT COUNT(*) FROM progress
        WHERE user_id = (SELECT id FROM users WHERE email = 'alice@test.com') AND termine) AS cours_termines,  -- [USER]
       (SELECT COUNT(*) FROM courses) AS total_cours,
       (SELECT COALESCE(ROUND(AVG(100.0 * score / total), 1), 0) FROM scores
        WHERE user_id = (SELECT id FROM users WHERE email = 'alice@test.com')) AS score_moyen;                 -- [USER]


-- =====================================================================================
-- VERIFICATIONS (checklist J7 / J8)
-- =====================================================================================

-- Chaque cours doit avoir au moins 5 questions
SELECT course_id, COUNT(*) FROM questions GROUP BY course_id ORDER BY course_id;

-- Les donnees appartiennent-elles au bon utilisateur ? (J8, section 5.3)
SELECT u.email, c.ordre, p.termine
FROM progress p
JOIN users u   ON u.id = p.user_id
JOIN courses c ON c.id = p.course_id
ORDER BY u.email, c.ordre;

SELECT u.email, c.ordre, s.score, s.total, s.date
FROM scores s
JOIN users u   ON u.id = s.user_id
JOIN courses c ON c.id = s.course_id
ORDER BY s.date DESC;

-- Securite (J9, S1) : aucun mot de passe en clair -> 0 ligne
SELECT email FROM users WHERE mot_de_passe_hash NOT LIKE '$2%';

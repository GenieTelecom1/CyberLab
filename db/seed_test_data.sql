-- =====================================================================================
-- CyberLab - seed_test_data.sql (Membre 1, J5)
-- Progression et scores d'exemple pour alice@test.com, bob@test.com, chaima@test.com.
-- A executer dans le SQL Editor APRES DevSeed et APRES l'insertion des 6 cours.
-- Rejouable : repart toujours du meme etat (utile apres la campagne de tests J8).
-- Valeurs attendues : Alice 33 % / 90,0 ; Bob 17 % / 60,0 ; Chaima 0 % / aucun quiz.
-- =====================================================================================

-- On repart de zero pour les comptes de test (sinon un cours termine pendant les tests resterait)
DELETE FROM progress WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@test.com');
DELETE FROM scores   WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@test.com');

-- Alice : cours 1 et 2 termines
INSERT INTO progress (user_id, course_id, termine)
SELECT u.id, c.id, true
FROM users u JOIN courses c ON c.ordre IN (1, 2)
WHERE u.email = 'alice@test.com'
ON CONFLICT (user_id, course_id) DO UPDATE SET termine = true, date = now();

-- Bob : cours 1 termine
INSERT INTO progress (user_id, course_id, termine)
SELECT u.id, c.id, true
FROM users u JOIN courses c ON c.ordre = 1
WHERE u.email = 'bob@test.com'
ON CONFLICT (user_id, course_id) DO UPDATE SET termine = true, date = now();

-- Scores
INSERT INTO scores (user_id, course_id, score, total)
SELECT u.id, c.id, 4, 5 FROM users u JOIN courses c ON c.ordre = 1 WHERE u.email = 'alice@test.com';
INSERT INTO scores (user_id, course_id, score, total)
SELECT u.id, c.id, 5, 5 FROM users u JOIN courses c ON c.ordre = 2 WHERE u.email = 'alice@test.com';
INSERT INTO scores (user_id, course_id, score, total)
SELECT u.id, c.id, 3, 5 FROM users u JOIN courses c ON c.ordre = 1 WHERE u.email = 'bob@test.com';

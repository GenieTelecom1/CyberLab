-- =====================================================================================
-- CyberLab - cleanup_test_data.sql (Membre 1, J5)
-- Supprime les comptes de test ; ON DELETE CASCADE supprime aussi leurs scores, leur
-- progression et leurs codes. A lancer avant la demonstration finale.
-- La base est partagee : prevenir l'equipe avant.
-- =====================================================================================
DELETE FROM users WHERE email LIKE '%@test.com';

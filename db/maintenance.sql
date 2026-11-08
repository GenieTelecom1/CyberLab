-- =====================================================================================
-- CyberLab - maintenance.sql (a lancer de temps en temps dans le SQL Editor)
-- =====================================================================================

-- 1. Codes de verification / reinitialisation expires depuis plus d'un jour
DELETE FROM auth_codes WHERE expire_le < now() - interval '1 day';

-- 2. Comptes jamais verifies depuis plus de 7 jours (inscriptions abandonnees ou adresses d'autrui)
--    Verifier d'abord avec le SELECT, puis supprimer.
SELECT id, email, date_creation FROM users
WHERE NOT email_verifie AND date_creation < now() - interval '7 days';
-- DELETE FROM users WHERE NOT email_verifie AND date_creation < now() - interval '7 days';

-- 3. AVANT LA MISE EN PRODUCTION : plus aucun compte de test (mot de passe connu : Test1234)
SELECT email FROM users WHERE email LIKE '%@test.com';
-- DELETE FROM users WHERE email LIKE '%@test.com';

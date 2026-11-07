-- =====================================================================================
-- CyberLab - migration : verification de l'e-mail et codes a usage unique
-- A executer UNE fois sur une base creee avec l'ancien schema.sql (J3, 5 tables).
-- Sans risque si rejoue : tout est protege par IF NOT EXISTS.
-- (Sans cette migration, AuthService affiche « La base n'est pas a jour ».)
-- =====================================================================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verifie BOOLEAN NOT NULL DEFAULT false;

-- Les comptes de test (J5) n'ont pas de vraie boite mail : on les considere comme verifies.
UPDATE users SET email_verifie = true WHERE email LIKE '%@test.com';
-- Pour ne pas bloquer les comptes reels deja crees, decommenter :
-- UPDATE users SET email_verifie = true;

CREATE TABLE IF NOT EXISTS auth_codes (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type          VARCHAR(20) NOT NULL CHECK (type IN ('VERIFICATION', 'RESET')),
    code_hash     VARCHAR(100) NOT NULL,
    date_creation TIMESTAMPTZ NOT NULL DEFAULT now(),
    expire_le     TIMESTAMPTZ NOT NULL,
    utilise       BOOLEAN NOT NULL DEFAULT false,
    tentatives    INT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_auth_codes_user ON auth_codes(user_id, type);

ALTER TABLE auth_codes ENABLE ROW LEVEL SECURITY;

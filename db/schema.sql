-- =====================================================================================
-- CyberLab - schema.sql (Membre 1, J3) - PostgreSQL / Supabase
-- A executer UNE SEULE FOIS dans le SQL Editor de Supabase sur un projet vide.
-- Pour une base qui existe deja (J3 sans verification par e-mail), voir
-- migration_email_verification.sql.
-- Pour tout recommencer : DROP TABLE IF EXISTS auth_codes, progress, scores, questions,
-- courses, users CASCADE;   (efface les donnees de tout le monde : prevenir l'equipe)
-- =====================================================================================

CREATE TABLE users (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom               VARCHAR(100) NOT NULL,
    email             VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(100) NOT NULL,
    email_verifie     BOOLEAN      NOT NULL DEFAULT false,   -- verification par code e-mail
    date_creation     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT email_minuscule CHECK (email = lower(email))
);

CREATE TABLE courses (
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    titre       VARCHAR(150) NOT NULL,
    description TEXT,
    contenu     TEXT,
    ordre       INT NOT NULL UNIQUE
);

CREATE TABLE questions (
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_id     INT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    texte         TEXT NOT NULL,
    choix_a       TEXT NOT NULL,
    choix_b       TEXT NOT NULL,
    choix_c       TEXT NOT NULL,
    choix_d       TEXT NOT NULL,
    bonne_reponse CHAR(1) NOT NULL CHECK (bonne_reponse IN ('A','B','C','D'))
);

CREATE TABLE scores (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id INT    NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    score     INT NOT NULL CHECK (score >= 0),
    total     INT NOT NULL CHECK (total > 0),
    date      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT score_valide CHECK (score <= total)
);

CREATE TABLE progress (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id INT    NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    termine   BOOLEAN NOT NULL DEFAULT false,
    date      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, course_id)
);

-- Codes a usage unique (verification d'e-mail, mot de passe oublie) : voir AuthCodeRepository.
-- Seul le hash BCrypt du code est stocke, jamais le code lui-meme.
CREATE TABLE auth_codes (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type          VARCHAR(20) NOT NULL CHECK (type IN ('VERIFICATION', 'RESET')),
    code_hash     VARCHAR(100) NOT NULL,
    date_creation TIMESTAMPTZ NOT NULL DEFAULT now(),
    expire_le     TIMESTAMPTZ NOT NULL,
    utilise       BOOLEAN NOT NULL DEFAULT false,
    tentatives    INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_scores_user      ON scores(user_id);
CREATE INDEX idx_questions_course ON questions(course_id);
CREATE INDEX idx_auth_codes_user  ON auth_codes(user_id, type);

-- Securite Supabase : l'API REST publique (cle anon) ne doit rien renvoyer.
-- L'application utilise JDBC avec le role postgres, qui contourne RLS.
ALTER TABLE users      ENABLE ROW LEVEL SECURITY;
ALTER TABLE courses    ENABLE ROW LEVEL SECURITY;
ALTER TABLE questions  ENABLE ROW LEVEL SECURITY;
ALTER TABLE scores     ENABLE ROW LEVEL SECURITY;
ALTER TABLE progress   ENABLE ROW LEVEL SECURITY;
ALTER TABLE auth_codes ENABLE ROW LEVEL SECURITY;

-- Donnees de base : les 6 cours du cahier des charges (le Membre 3 remplira "contenu").
INSERT INTO courses (titre, description, contenu, ordre) VALUES
('Introduction à la cybersécurité', 'Principes fondamentaux, triade CIA', 'A compléter', 1),
('Phishing', 'Reconnaître emails et liens frauduleux', 'A compléter', 2),
('Malware', 'Virus, chevaux de Troie, ransomware', 'A compléter', 3),
('Social Engineering', 'Techniques de manipulation des attaquants', 'A compléter', 4),
('Network Security', 'Firewall, VPN, protection des réseaux', 'A compléter', 5),
('Password Security', 'Création et gestion de mots de passe sécurisés', 'A compléter', 6);

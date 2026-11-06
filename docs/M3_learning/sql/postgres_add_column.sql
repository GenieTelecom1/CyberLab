-- CyberLab (M3 - Learning) : colonne ajoutée à la table Progress, version PostgreSQL.
-- À exécuter UNE fois sur la base. Sans cette colonne, l'enregistrement par partie ne fonctionne pas.
-- Sans risque : IF NOT EXISTS ne fait rien si la colonne existe déjà.

ALTER TABLE progress ADD COLUMN IF NOT EXISTS parties_terminees INTEGER NOT NULL DEFAULT 0;

-- La colonne "termine" peut être un BOOLEAN (cas de M1) ou un INTEGER 0/1 : le code Java détecte le type tout seul,
-- il n'y a rien à convertir.

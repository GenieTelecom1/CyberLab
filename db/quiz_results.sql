-- À exécuter UNE SEULE FOIS dans Supabase (SQL Editor)
CREATE TABLE IF NOT EXISTS quiz_results (
    id          SERIAL PRIMARY KEY,
    user_id     INT          NOT NULL,
    username    VARCHAR(100) NOT NULL,
    course_id   INT          NOT NULL,
    course_name VARCHAR(200),
    score       INT          NOT NULL,
    total       INT          NOT NULL,
    played_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_quiz_results_user ON quiz_results(user_id);

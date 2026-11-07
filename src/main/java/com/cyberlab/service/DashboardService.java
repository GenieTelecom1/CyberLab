package com.cyberlab.service;

import java.util.List;

/**
 * Propriétaire : Membre 2 (statistiques générales du Dashboard).
 *
 * J6 : renvoie des DONNÉES DE TEST.
 * J8 / regroupement : remplacer le contenu de getSnapshot() par des appels aux services des autres
 * membres (le contrôleur n'a pas à changer) :
 *   - cours + terminé ?          -> CourseService / ProgressService (M3)
 *   - % de progression           -> ProgressService.calculateProgressPercentage(userId)
 *   - score moyen, rang, top 3   -> QuizService.getLeaderboard() / getUserScoreHistory(userId) (M4)
 * Règle d'arrondi (cf. MAQUETTES.md) : pourcentage arrondi à l'entier le plus proche.
 */
public class DashboardService {

    public record CourseSummary(int id, String title, String description, double progress, boolean completed) { }
    public record RankEntry(int position, String name, String level, int points) { }

    public record DashboardSnapshot(List<CourseSummary> courses, double averageScore,
                                    String rank, String rankNote, List<RankEntry> leaderboard,
                                    int points, String level, int streakDays) {
        public int completedCount() {
            return (int) courses.stream().filter(CourseSummary::completed).count();
        }
        public int totalCourses() { return courses.size(); }
        /** Progression globale = cours terminés / total (0 si aucun cours). */
        public double progressRatio() {
            return courses.isEmpty() ? 0 : (double) completedCount() / courses.size();
        }
    }

    public DashboardSnapshot getSnapshot(int userId) {
        // TODO regroupement : remplacer par les vrais services (voir en-tête)
        return new DashboardSnapshot(
                List.of(
                        new CourseSummary(1, "Introduction à la cybersécurité",
                                "Découvrez les bases de la sécurité informatique.", 1.00, true),
                        new CourseSummary(2, "Hameçonnage (phishing)",
                                "Apprenez à reconnaître les tentatives de phishing.", 1.00, true),
                        new CourseSummary(3, "Logiciels malveillants",
                                "Comprenez virus, chevaux de Troie et ransomware.", 0.20, false),
                        new CourseSummary(4, "Ingénierie sociale",
                                "Découvrez les techniques de manipulation des attaquants.", 0.64, false),
                        new CourseSummary(5, "Sécurité des réseaux",
                                "Protégez vos réseaux avec pare-feu et VPN.", 0.35, false),
                        new CourseSummary(6, "Sécurité des mots de passe",
                                "Créez et gérez des mots de passe sécurisés.", 0.0, false)),
                0.78,
                "#42",
                "Top 15 % cette semaine",
                List.of(new RankEntry(1, "Hacker", "Novice", 1250),
                        new RankEntry(2, "Navi", "Novice", 1120),
                        new RankEntry(3, "CyberHero", "Novice", 980)),
                // TODO : points / niveau / série / tendance ne sont pas définis dans le cahier des charges
                // (formule à valider avec M4 et l'équipe). Valeurs de test pour la maquette.
                6, "Novice", 7);
    }
}

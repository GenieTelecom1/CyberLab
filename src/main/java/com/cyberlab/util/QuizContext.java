package com.cyberlab.util;

/**
 * Petit relais entre l'écran Cours et l'écran Quiz :
 * CourseController y met l'id du cours choisi, QuizController le lit à l'ouverture.
 * Par défaut : cours 1.
 */
public final class QuizContext {

    private static int courseId = 1;

    private QuizContext() { }

    public static int getCourseId() { return courseId; }

    public static void setCourseId(int id) { courseId = id; }
}

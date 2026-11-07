package com.cyberlab.model;

public class Score {
    private int id;
    private int userId;
    private int courseId;
    private int score;
    private String dateObtention;

    public Score(int id, int userId, int courseId, int score, String dateObtention) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.score = score;
        this.dateObtention = dateObtention;
    }

    // Getters
    public int getId() {
        return id;
    }
    public int getUserId() {
        return userId;
    }
    public int getCourseId() {
        return courseId;
    }
    public int getScore() {
        return score;
    }
    public String getDateObtention() {
        return dateObtention;
    }

    // Setters
    public void setScore(int score) {
        this.score = score;
    }
}

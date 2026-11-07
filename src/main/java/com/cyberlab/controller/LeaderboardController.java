package com.cyberlab.controller;

import com.cyberlab.database.DatabaseConnection;
import com.cyberlab.service.ScoreService;
import com.cyberlab.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class LeaderboardController {

    @FXML private TableView<UserScore> leaderboardTable;
    @FXML private TableColumn<UserScore, Integer> rankColumn;
    @FXML private TableColumn<UserScore, String> usernameColumn;
    @FXML private TableColumn<UserScore, Integer> scoreColumn;
    @FXML private TableColumn<UserScore, String> badgeColumn;
    @FXML private ComboBox<String> courseFilter;
    @FXML private Label userRankLabel;
    @FXML private Label bestLabel;

    private static final String ALL_COURSES = "Tous les cours (total des points)";

    private List<ScoreService.CourseChoice> courses = List.of();

    @FXML
    public void initialize() {
        if (leaderboardTable == null) return;

        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        badgeColumn.setCellValueFactory(new PropertyValueFactory<>("badge"));

        courses = ScoreService.listCourses();
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add(ALL_COURSES);
        for (ScoreService.CourseChoice c : courses) items.add(c.title());
        courseFilter.setItems(items);
        courseFilter.getSelectionModel().selectFirst();
        courseFilter.setOnAction(e -> chargerClassement());

        chargerClassement();
    }

    /** 0 = tous les cours, sinon l'id du cours sélectionné. */
    private int selectedCourseId() {
        int idx = courseFilter.getSelectionModel().getSelectedIndex();
        return (idx <= 0 || idx > courses.size()) ? 0 : courses.get(idx - 1).id();
    }

    private void chargerClassement() {
        int courseId = selectedCourseId();

        String query = courseId == 0
                // Total des meilleurs scores de l'utilisateur sur chaque cours
                ? "SELECT username, SUM(best) AS total FROM ("
                + "  SELECT username, course_id, MAX(score) AS best FROM quiz_results GROUP BY username, course_id"
                + ") t GROUP BY username ORDER BY total DESC, username ASC"
                // Meilleur score sur le cours choisi
                : "SELECT username, MAX(score) AS total FROM quiz_results WHERE course_id = ? "
                + "GROUP BY username ORDER BY total DESC, username ASC";

        ObservableList<UserScore> top = FXCollections.observableArrayList();
        String me = SessionManager.getCurrentUserName();
        int myRank = -1, myScore = 0;
        String bestName = null;
        int bestScore = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            if (courseId != 0) ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    String name = rs.getString("username");
                    int total = rs.getInt("total");
                    if (rank == 1) { bestName = name; bestScore = total; }
                    if (rank <= 10) top.add(new UserScore(rank, name, total));
                    if (myRank < 0 && name != null && name.equals(me)) {
                        myRank = rank;
                        myScore = total;
                    }
                    rank++;
                }
            }
            leaderboardTable.setItems(top);

            bestLabel.setText(bestName == null ? ""
                    : "★ Meilleur score : " + bestName + " (" + bestScore + " pts)");

            userRankLabel.setText(myRank > 0
                    ? "Votre rang : #" + myRank + "  •  Votre score : " + myScore
                    : "Passez un quiz pour apparaître dans ce classement.");
        } catch (Exception e) {
            System.err.println("Erreur de chargement du classement : " + e.getMessage());
            userRankLabel.setText("Classement indisponible.");
        }
    }

    public static class UserScore {
        private final int rank;
        private final String username;
        private final int score;

        public UserScore(int rank, String username, int score) {
            this.rank = rank;
            this.username = username;
            this.score = score;
        }

        public int getRank() { return rank; }
        public String getUsername() { return username; }
        public int getScore() { return score; }

        public String getBadge() {
            if (rank == 1) return "★ Meilleur score";
            if (rank <= 3) return "Top 3";
            return "";
        }
    }
}
package com.cyberlab.controller;

import com.cyberlab.database.DatabaseConnection;
import com.cyberlab.service.ScoreService;
import com.cyberlab.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardController {

    @FXML
    private HBox podiumBox;
    @FXML
    private ListView<UserScore> rankList;
    @FXML
    private ComboBox<String> courseFilter;
    @FXML
    private Label userRankLabel;
    @FXML
    private Label bestLabel;

    private static final String ALL_COURSES = "Tous les cours (total des points)";

    private List<ScoreService.CourseChoice> courses = List.of();

    @FXML
    public void initialize() {
        if (rankList == null) return;

        rankList.setCellFactory(lv -> new RankCell());
        rankList.setFixedCellSize(58);

        courses = ScoreService.listCourses();
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add(ALL_COURSES);
        for (ScoreService.CourseChoice c : courses) items.add(c.title());
        courseFilter.setItems(items);
        courseFilter.getSelectionModel().selectFirst();
        courseFilter.setOnAction(e -> chargerClassement());

        chargerClassement();
    }

    private int selectedCourseId() {
        int idx = courseFilter.getSelectionModel().getSelectedIndex();
        return (idx <= 0 || idx > courses.size()) ? 0 : courses.get(idx - 1).id();
    }

    /**
     * nom -> gender. Ken el requête tfashal, el classement yekhdem kif ma houwa (couleur bleu par défaut).
     */
    private Map<String, String> chargerGenres() {
        Map<String, String> m = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT nom, gender FROM users");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                m.put(rs.getString("nom"), rs.getString("gender"));
            }
        } catch (Exception e) {
            System.err.println("Genre indisponible : " + e.getMessage());
        }
        return m;
    }

    private static boolean isFemale(String g) {
        if (g == null) return false;
        String s = g.trim().toLowerCase();
        return s.equals("f") || s.equals("female") || s.equals("femme") || s.equals("fille");
    }

    private void chargerClassement() {
        int courseId = selectedCourseId();

        String query = courseId == 0
                ? "SELECT username, SUM(best) AS total FROM ("
                + "  SELECT username, course_id, MAX(score) AS best FROM quiz_results GROUP BY username, course_id"
                + ") t GROUP BY username ORDER BY total DESC, username ASC"
                : "SELECT username, MAX(score) AS total FROM quiz_results WHERE course_id = ? "
                + "GROUP BY username ORDER BY total DESC, username ASC";

        Map<String, String> genres = chargerGenres();
        List<UserScore> top = new ArrayList<>();
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
                    if (rank == 1) {
                        bestName = name;
                        bestScore = total;
                    }
                    if (rank <= 10) top.add(new UserScore(rank, name, total, isFemale(genres.get(name))));
                    if (myRank < 0 && name != null && name.equals(me)) {
                        myRank = rank;
                        myScore = total;
                    }
                    rank++;
                }
            }

            afficherClassement(top);

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

    /**
     * #1 kbir fou9, w men rang 2 liste b rows.
     */
    private void afficherClassement(List<UserScore> entries) {
        podiumBox.getChildren().clear();
        if (!entries.isEmpty()) podiumBox.getChildren().add(topItem(entries.get(0)));

        List<UserScore> rest = entries.size() > 1 ? entries.subList(1, entries.size()) : List.of();
        rankList.setItems(FXCollections.observableArrayList(rest));
    }

    private VBox topItem(UserScore e) {
        StackPane av = avatar(e, 90);
        StackPane badge = shield(e.isFemale(), 1.3);
        StackPane wrap = new StackPane(av, badge);
        wrap.setPrefSize(90, 90);
        wrap.setMaxSize(90, 90);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(-14, -14, 0, 0));

        Label name = new Label("#01 - " + e.getUsername());
        name.getStyleClass().add("podium-name");
        Label score = new Label(String.valueOf(e.getScore()));
        score.getStyleClass().add("top-score");

        VBox box = new VBox(6, wrap, name, score);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10, 0, 4, 0));
        return box;
    }

    /**
     * Carré rose (fille) ou bleu (garçon) + silhouette.
     */
    private static StackPane avatar(UserScore e, double size) {
        Rectangle bg = new Rectangle(size, size);
        bg.setArcWidth(size * 0.18);
        bg.setArcHeight(size * 0.18);
        bg.getStyleClass().add(e.isFemale() ? "av-pink" : "av-blue");

        SVGPath icon = new SVGPath();
        icon.setContent("M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm0 2c-4 0-8 2-8 5v1h16v-1c0-3-4-5-8-5z");
        icon.getStyleClass().add("av-icon");
        icon.setScaleX(size / 34);
        icon.setScaleY(size / 34);
        return new StackPane(bg, icon);
    }

    private static StackPane shield(boolean female, double scale) {
        SVGPath s = new SVGPath();
        s.setContent("M12 2L4 5v6c0 5 3.4 9.3 8 11 4.6-1.7 8-6 8-11V5l-8-3z");
        s.getStyleClass().addAll("shield", female ? "shield-pink" : "shield-blue");
        Label star = new Label("★");
        star.getStyleClass().add("shield-star");
        StackPane p = new StackPane(s, star);
        p.setScaleX(scale);
        p.setScaleY(scale);
        p.setMaxSize(24, 24);
        return p;
    }

    private static class RankCell extends ListCell<UserScore> {
        @Override
        protected void updateItem(UserScore e, boolean empty) {
            super.updateItem(e, empty);
            if (empty || e == null) {
                setGraphic(null);
                return;
            }

            Label rank = new Label(String.format("%02d", e.getRank()));
            rank.getStyleClass().add("row-rank");

            Label name = new Label(e.getUsername());
            name.getStyleClass().add("row-name");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label score = new Label(String.valueOf(e.getScore()));
            score.getStyleClass().add("row-score");

            HBox row = new HBox(14, rank, avatar(e, 36), name, spacer, score, shield(e.isFemale(), 1.0));
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("lb-row");
            setGraphic(row);
        }
    }

    public static class UserScore {
        private final int rank;
        private final String username;
        private final int score;
        private final boolean female;

        public UserScore(int rank, String username, int score, boolean female) {
            this.rank = rank;
            this.username = username;
            this.score = score;
            this.female = female;
        }

        public int getRank() {
            return rank;
        }

        public String getUsername() {
            return username;
        }

        public int getScore() {
            return score;
        }

        public boolean isFemale() {
            return female;
        }
    }
}
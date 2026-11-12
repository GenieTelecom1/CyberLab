package com.cyberlab.controller;

import com.cyberlab.database.DatabaseConnection;
import com.cyberlab.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryController {

    private static final int MAX_POINTS_ON_CHART = 15;
    private static final String CHART_COLOR = "#C8102E";

    @FXML private TableView<QuizHistory> historyTable;
    @FXML private TableColumn<QuizHistory, String> dateColumn;
    @FXML private TableColumn<QuizHistory, String> courseColumn;
    @FXML private TableColumn<QuizHistory, String> scoreColumn;
    @FXML private LineChart<String, Number> progressChart;
    @FXML private Label summaryLabel;

    @FXML
    public void initialize() {
        if (historyTable != null) {
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            courseColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
            scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
            chargerHistorique();
        }
    }

    private void chargerHistorique() {
        ObservableList<QuizHistory> list = FXCollections.observableArrayList();
        List<String> dates = new ArrayList<>();
        List<Integer> percents = new ArrayList<>();

        String query = "SELECT to_char(played_at AT TIME ZONE 'Africa/Tunis', 'DD/MM HH24:MI') AS d, "
                + "to_char(played_at AT TIME ZONE 'Africa/Tunis', 'DD/MM/YYYY HH24:MI') AS full_d, "
                + "course_name, score, total FROM quiz_results WHERE user_id = ? ORDER BY played_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, SessionManager.getCurrentUserId());
            int count = 0, sumPct = 0;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int score = rs.getInt("score");
                    int total = rs.getInt("total");
                    int pct = total > 0 ? (score * 100) / total : 0;

                    list.add(new QuizHistory(rs.getString("full_d"), rs.getString("course_name"), score + " / " + total));
                    dates.add(rs.getString("d"));
                    percents.add(pct);
                    count++;
                    sumPct += pct;
                }
            }
            historyTable.setItems(list);
            dessinerCourbe(dates, percents);

            summaryLabel.setText(count == 0
                    ? "Aucun quiz passé pour le moment."
                    : count + " quiz passé(s)  •  Moyenne : " + (sumPct / count) + "%");
        } catch (Exception e) {
            System.err.println("Erreur chargement historique : " + e.getMessage());
            if (summaryLabel != null) summaryLabel.setText("Historique indisponible.");
        }
    }

    /** Les listes arrivent du plus récent au plus ancien : on les inverse pour la courbe. */
    private void dessinerCourbe(List<String> dates, List<Integer> percents) {
        if (progressChart == null) return;

        List<String> d = new ArrayList<>(dates);
        List<Integer> p = new ArrayList<>(percents);
        Collections.reverse(d);
        Collections.reverse(p);

        int start = Math.max(0, d.size() - MAX_POINTS_ON_CHART);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = start; i < d.size(); i++) {
            // "#n" rend chaque catégorie unique même si deux quiz ont la même minute
            series.getData().add(new XYChart.Data<>("#" + (i + 1) + "  " + d.get(i), p.get(i)));
        }

        progressChart.getData().clear();
        progressChart.getData().add(series);

        // Couleurs adaptées au thème sombre (le noeud existe après l'affichage)
        Platform.runLater(() -> {
            Node line = series.getNode();
            if (line != null) line.setStyle("-fx-stroke: " + CHART_COLOR + "; -fx-stroke-width: 2px;");
            for (XYChart.Data<String, Number> point : series.getData()) {
                Node n = point.getNode();
                if (n != null) n.setStyle("-fx-background-color: " + CHART_COLOR + ", #ffffff;");
            }
            Node plot = progressChart.lookup(".chart-plot-background");
            if (plot != null) plot.setStyle("-fx-background-color: transparent;");
        });
    }

    public static class QuizHistory {
        private final String date;
        private final String courseName;
        private final String score;

        public QuizHistory(String date, String courseName, String score) {
            this.date = date;
            this.courseName = courseName;
            this.score = score;
        }

        public String getDate() { return date; }
        public String getCourseName() { return courseName; }
        public String getScore() { return score; }
    }
}
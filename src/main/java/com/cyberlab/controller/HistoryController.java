package com.cyberlab.controller;

import com.cyberlab.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class HistoryController {

    @FXML private TableView<QuizHistory> historyTable;
    @FXML private TableColumn<QuizHistory, String> dateColumn;
    @FXML private TableColumn<QuizHistory, String> courseColumn;
    @FXML private TableColumn<QuizHistory, Integer> scoreColumn;

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
        // Exemple de requête (adaptez le nom de votre table d'historique si nécessaire)
        String query = "SELECT date, course_name, score FROM quiz_history ORDER BY date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                list.add(new QuizHistory(
                        rs.getString("date"),
                        rs.getString("course_name"),
                        rs.getInt("score")
                ));
            }
            historyTable.setItems(list);
            System.out.println("Succès : Historique chargé.");
        } catch (Exception e) {
            System.err.println("Erreur chargement historique (table optionnelle) : " + e.getMessage());
        }
    }

    public static class QuizHistory {
        private String date;
        private String courseName;
        private int score;

        public QuizHistory(String date, String courseName, int score) {
            this.date = date;
            this.courseName = courseName;
            this.score = score;
        }

        public String getDate() { return date; }
        public String getCourseName() { return courseName; }
        public int getScore() { return score; }
    }
}
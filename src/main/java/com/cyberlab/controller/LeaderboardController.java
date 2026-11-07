package com.cyberlab.controller;

import com.cyberlab.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class LeaderboardController {

    @FXML private TableView<UserScore> leaderboardTable;
    @FXML private TableColumn<UserScore, Integer> rankColumn;
    @FXML private TableColumn<UserScore, String> usernameColumn;
    @FXML private TableColumn<UserScore, Integer> scoreColumn;
    @FXML private Label userRankLabel;

    @FXML
    public void initialize() {
        if (leaderboardTable != null) {
            rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

            chargerClassement();
        }
    }

    private void chargerClassement() {
        ObservableList<UserScore> list = FXCollections.observableArrayList();
        String query = "SELECT username, score FROM users ORDER BY score DESC LIMIT 10";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int rank = 1;
            while (rs.next()) {
                list.add(new UserScore(rank++, rs.getString("username"), rs.getInt("score")));
            }
            leaderboardTable.setItems(list);
            System.out.println("Succès : Classement chargé.");
        } catch (Exception e) {
            System.err.println("Erreur de chargement du classement : " + e.getMessage());
        }
    }

    // Classe interne modèle pour le TableView du Classement
    public static class UserScore {
        private int rank;
        private String username;
        private int score;

        public UserScore(int rank, String username, int score) {
            this.rank = rank;
            this.username = username;
            this.score = score;
        }

        public int getRank() { return rank; }
        public String getUsername() { return username; }
        public int getScore() { return score; }
    }
}
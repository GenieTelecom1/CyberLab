package com.cyberlab.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import com.cyberlab.util.Icons;
import com.cyberlab.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.LinkedHashMap;
import java.util.Map;

/** Propriétaire : Membre 2. Coque : menu latéral + barre du haut + zone centrale. */
public class MainShellController {

    @FXML private StackPane contentArea;
    @FXML private Label pageTitle;
    @FXML private Button navDashboard, navCourses, navQuiz, navSimulations, navLeaderboard, navChatbot, navLogout;

    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final Map<String, String> titles = new LinkedHashMap<>();

    @FXML
    private void initialize() {
        navButtons.put("dashboard", navDashboard);
        navButtons.put("course_list", navCourses);
        navButtons.put("quiz", navQuiz);
        navButtons.put("phishing_simulation", navSimulations);
        navButtons.put("leaderboard", navLeaderboard);
        navButtons.put("chatbot", navChatbot);
        titles.put("history", "Historique");

        setIcon(navDashboard, Icons.HOME);
        setIcon(navCourses, Icons.BOOK_OPEN);
        setIcon(navQuiz, Icons.HELP_CIRCLE);
        setIcon(navSimulations, Icons.NETWORK);
        setIcon(navLeaderboard, Icons.BAR_CHART);
        setIcon(navChatbot, Icons.MESSAGE);
        setIcon(navLogout, Icons.LOG_OUT);

        titles.put("dashboard", "Tableau de bord");
        titles.put("course_list", "Cours");
        titles.put("quiz", "Quiz");
        titles.put("phishing_simulation", "Simulations");
        titles.put("password_simulation", "Simulations");
        titles.put("leaderboard", "Classement");
        titles.put("chatbot", "Assistant IA");


        SceneManager.registerContentArea(contentArea, this::show);
    }

    private void show(String view) {
        SceneManager.loadInto(view);
        pageTitle.setText(titles.getOrDefault(view, ""));
        navButtons.forEach((key, b) -> {
            b.getStyleClass().remove("nav-active");
            Icons.recolor(b.getGraphic(), Icons.MUTED);
        });
        Button active = navButtons.get(view);
        if (active == null && view.equals("password_simulation")) active = navSimulations;
        if (active != null) {
            active.getStyleClass().add("nav-active");
            Icons.recolor(active.getGraphic(), Icons.CYAN);
        }
    }

    private static void setIcon(Button button, String path) {
        button.setGraphic(Icons.icon(path, 18, Icons.MUTED, null));
        button.setGraphicTextGap(12);
    }

    @FXML private void goDashboard()   { SceneManager.navigateTo("dashboard"); }
    @FXML private void goCourses()     { SceneManager.navigateTo("course_list"); }
    @FXML private void goQuiz()        { SceneManager.navigateTo("quiz"); }
    @FXML private void goSimulations() { SceneManager.navigateTo("phishing_simulation"); }
    @FXML private void goLeaderboard() { SceneManager.navigateTo("leaderboard"); }
    @FXML private void goChatbot()     { SceneManager.navigateTo("chatbot"); }
    @FXML private void logout()        { SceneManager.logout(); }

    @FXML
    private void onLogin() {
        SceneManager.showScene("login");
    }

    @FXML
    private void onSignup() {
        SceneManager.showScene("signup");
    }

}

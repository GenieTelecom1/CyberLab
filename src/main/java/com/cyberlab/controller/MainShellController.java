package com.cyberlab.controller;

import com.cyberlab.util.Icons;
import com.cyberlab.util.SceneManager;
import com.cyberlab.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Propriétaire : Membre 2. Coque : menu latéral + barre du haut + zone centrale. */
public class MainShellController {

    @FXML private TextField searchField;
    @FXML private StackPane contentArea;
    @FXML private Label pageTitle;
    @FXML private Button navDashboard, navCourses, navQuiz, navSimulations, navLeaderboard, navChatbot, navLogout;

    // Barre du haut : visiteur -> Se connecter / S'inscrire ; connecté -> nom + Déconnexion
    @FXML private Button loginButton, signupButton, logoutButton;
    @FXML private Label userLabel;

    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final Map<String, String> titles = new LinkedHashMap<>();

    // FXMLLoader rappelle initialize() à chaque loader.setController(this) (pages À propos / inscription) :
    // ce drapeau évite la boucle infinie (StackOverflowError).
    private boolean started = false;

    @FXML
    private void initialize() {
        if (started) return;
        started = true;

        navButtons.put("dashboard", navDashboard);
        navButtons.put("course_list", navCourses);
        navButtons.put("quiz", navQuiz);
        navButtons.put("phishing_simulation", navSimulations);
        navButtons.put("leaderboard", navLeaderboard);
        navButtons.put("chatbot", navChatbot);

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
        titles.put("history", "Historique");

        refreshAuthUi();
        SceneManager.registerContentArea(contentArea, this::show);
        // Pas de navigation ici : SceneManager.showShell() appelle navigateTo("dashboard"),
        // et show() affiche "À propos" si l'utilisateur n'est pas connecté.
    }

    /** Affiche les bons éléments selon que l'utilisateur est connecté ou visiteur. */
    private void refreshAuthUi() {
        boolean connecte = isLoggedIn();

        setShown(loginButton, !connecte);
        setShown(signupButton, !connecte);

        setShown(logoutButton, connecte);
        setShown(userLabel, connecte);
        setShown(navLogout, connecte);

        // Visiteur : plus de menu latéral ni de recherche -> la page À propos prend toute la largeur.
        setShown(sidebar(), connecte);
        setShown(searchField, connecte);

        if (connecte && userLabel != null) userLabel.setText(SessionManager.getCurrentUserName());
    }

    /** Le menu latéral est le parent direct des boutons de navigation. */
    private Node sidebar() {
        return navDashboard == null ? null : navDashboard.getParent();
    }

    private static void setShown(Node node, boolean shown) {
        if (node == null) return;
        node.setVisible(shown);
        node.setManaged(shown);
    }

    private void show(String view) {
        if (!isLoggedIn()) {          // visiteur : on ne montre que la page À propos
            goAbout();
            return;
        }
        SceneManager.loadInto(view);
        pageTitle.setText(titles.getOrDefault(view, ""));
        refreshAuthUi();
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
        button.setGraphic(Icons.icon(path, 28, Icons.MUTED, null));
        button.setGraphicTextGap(6);
    }

    @FXML
    private void onSearch() {
        if (!requireLogin()) return;
        String query = searchField.getText();
        if (query == null || query.isBlank()) return;
        System.out.println("Recherche : " + query.trim());
        // TODO: aadi lel page Cours w 7ot el filtre
        goCourses();
    }

    @FXML private void goDashboard()   { if (!requireLogin()) return; SceneManager.navigateTo("dashboard"); }
    @FXML private void goCourses()     { if (!requireLogin()) return; SceneManager.navigateTo("course_list"); }
    @FXML private void goQuiz()        { if (!requireLogin()) return; SceneManager.navigateTo("quiz"); }
    @FXML private void goSimulations() { if (!requireLogin()) return; SceneManager.navigateTo("phishing_simulation"); }
    @FXML private void goLeaderboard() { if (!requireLogin()) return; SceneManager.navigateTo("leaderboard"); }
    @FXML private void goChatbot()     { if (!requireLogin()) return; SceneManager.navigateTo("chatbot"); }

    /** Déconnexion : on efface la session et on revient à la coque en mode visiteur. */
    @FXML
    private void logout() {
        SessionManager.logout();
        SceneManager.showShell();
    }

    @FXML
    private void onLogin() {
        SceneManager.showScene("login");
    }

    /** "S'inscrire" : page de présentation (Visiteur / Compte gratuit) avant le vrai formulaire. */
    @FXML
    private void onSignup() {
        goJoin();
    }

    /** Bouton "Créer mon compte gratuit" de la page join.fxml. */
    @FXML
    private void onCreateAccount() {
        SceneManager.showScene("signup");
    }

    private boolean isLoggedIn() {
        return SessionManager.isLoggedIn();
    }

    @FXML
    private void goAbout() {
        loadPage("/fxml/about.fxml", "À propos");
    }

    private void goJoin() {
        loadPage("/fxml/join.fxml", "Créer un compte");
    }

    private void loadPage(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            loader.setController(this);
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);
            pageTitle.setText(title);
            refreshAuthUi();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean requireLogin() {
        if (isLoggedIn()) return true;
        goAbout();
        return false;
    }
}

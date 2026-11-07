package com.cyberlab.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Propriétaire : Membre 2.
 * Contrat de navigation :
 *  - showScene("login")  : remplace toute la scène (Login, Register : écrans autonomes)
 *  - showShell()         : ouvre la coque Dashboard (menu latéral persistant)
 *  - navigateTo("quiz")  : change uniquement le contenu central de la coque
 * Les noms de vues = nom du fichier FXML sans extension (dossier /fxml/).
 */
public final class SceneManager {

    private static final String CSS_PATH = "/css/cyberlab.css";
    private static final String SHELL_FXML = "main_shell";
    private static final double WIDTH = 1280, HEIGHT = 780;

    private static Stage stage;
    private static StackPane contentArea;
    private static Consumer<String> navigationListener;

    private SceneManager() { }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("CyberLab");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
    }

    /** Écrans accessibles sans être connecté (authentification). */
    private static final Set<String> ECRANS_PUBLICS =
            Set.of("login", "signup", "register", "verify", "forgot", "reset");

    /**
     * Point d'entrée de navigation utilisé par les écrans d'authentification (Membre 1).
     * Contrôle d'accès (J8) : personne de connecté => retour à « login » pour tout écran protégé.
     */
    public static void switchTo(String nom) {
        if (!ECRANS_PUBLICS.contains(nom) && !SessionManager.isLoggedIn()) {
            showScene("login");
        } else if (ECRANS_PUBLICS.contains(nom)) {
            showScene(nom);
        } else if ("dashboard".equals(nom)) {
            showShell();
        } else {
            navigateTo(nom);
        }
    }

    /** Écran autonome (login, register) : change toute la scène. */
    public static void showScene(String fxmlName) {
        setRoot(loadView(fxmlName));
    }

    /** Ouvre la coque Dashboard puis affiche l'écran d'accueil. */
    public static void showShell() {
        setRoot(loadView(SHELL_FXML));
        navigateTo("dashboard");
    }

    /** Change le contenu central (et met à jour le menu actif). */
    public static void navigateTo(String viewName) {
        if (navigationListener != null) {
            navigationListener.accept(viewName);
        } else {
            loadInto(viewName);
        }
    }

    /** Appelé par MainShellController pour enregistrer la zone centrale. */
    public static void registerContentArea(StackPane area, Consumer<String> listener) {
        contentArea = area;
        navigationListener = listener;
    }

    /** Charge une vue FXML dans la zone centrale. */
    public static void loadInto(String viewName) {
        if (contentArea == null) return;
        contentArea.getChildren().setAll(loadViewOrPlaceholder(viewName));
    }

    public static void logout() {
        SessionManager.clear();
        contentArea = null;
        navigationListener = null;
        showScene("login");
    }

    // ---------- interne ----------

    private static void setRoot(Parent root) {
        if (stage == null) throw new IllegalStateException("SceneManager.init(stage) n'a pas été appelé");
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, WIDTH, HEIGHT);
            URL css = SceneManager.class.getResource(CSS_PATH);
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        stage.show();
    }

    private static Parent loadView(String name) {
        try {
            URL url = SceneManager.class.getResource("/fxml/" + name + ".fxml");
            if (url == null) throw new IOException("FXML introuvable : " + name);
            return new FXMLLoader(url).load();
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de charger la vue " + name, e);
        }
    }

    private static Node loadViewOrPlaceholder(String name) {
        URL url = SceneManager.class.getResource("/fxml/" + name + ".fxml");
        if (url == null) return placeholder("Écran « " + name + " » en cours de développement");
        try {
            return new FXMLLoader(url).load();
        } catch (IOException e) {
            e.printStackTrace(); // erreur technique : visible en console, message clair à l'écran
            return placeholder("Impossible d'afficher l'écran « " + name + " »");
        }
    }

    private static Node placeholder(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("placeholder-text");
        return new StackPane(l);
    }
}

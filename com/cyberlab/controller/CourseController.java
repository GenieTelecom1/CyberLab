package com.cyberlab.controller;

import com.cyberlab.model.Course;
import com.cyberlab.service.CourseService;
import com.cyberlab.service.ProgressService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Liste des cours sous forme de cartes (learning-list.fxml), avec le pourcentage
 * d'avancement de chaque cours. Aucune requête SQL ici : tout passe par les services.
 */
public class CourseController {

    @FXML private StackPane contentArea;   // zone où s'affiche la page de lecture
    @FXML private ScrollPane listView;     // la vue "liste des cours" (restaurée au retour)
    @FXML private FlowPane cardsPane;
    @FXML private Label progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label statCoursesValue;
    @FXML private Label statPartsValue;
    @FXML private Label statInProgressValue;

    private final CourseService courseService = new CourseService();
    private final ProgressService progressService = new ProgressService();

    private static final double PAGE_PADDING = 24;
    private static final double GAP = 18;
    private static final double MIN_CARD_WIDTH = 320;

    private List<Course> courses;
    private double cardWidth = 340;

    /**
     * UNIQUE point à adapter au code de M1 : id de l'utilisateur connecté.
     * Les deux contrôleurs du module (liste et lecture) passent par cette méthode.
     * Selon ce que M1 écrit, remplace la ligne par exemple par :
     *   AuthService.getCurrentUser().getId()      ou      SessionManager.getCurrentUser().getId()
     * Si getId() renvoie un long, ajoute un cast : (int) ...getId()
     */
    static int currentUserId() {
        return com.cyberlab.service.AuthService.getCurrentUser().getId();
    }

    @FXML
    public void initialize() {
        setupResponsiveCards();
        refresh();
    }

    /** Recharge les cartes, les statistiques et la barre de progression globale. */
    public void refresh() {
        try {
            int userId = currentUserId();
            courses = courseService.getAllCourses();
            Map<Integer, Integer> partsByCourse = progressService.getPartsDoneByCourse(userId);
            Set<Integer> completed = progressService.getCompletedCourseIds(userId);

            int totalParts = 0;
            int doneParts = 0;
            int inProgress = 0;

            cardsPane.getChildren().clear();
            for (Course course : courses) {
                int size = course.getSections().size();
                int done = Math.min(partsByCourse.getOrDefault(course.getId(), 0), size);
                totalParts += size;
                doneParts += done;
                if (done > 0 && done < size) {
                    inProgress++;
                }
                cardsPane.getChildren().add(buildCard(course, done, size));
            }

            int percent = progressService.getProgressPercentage(userId);
            progressBar.setProgress(percent / 100.0);
            progressLabel.setText(percent + " % de progression globale  ·  "
                    + completed.size() + " / " + courses.size() + " cours terminés");
            statCoursesValue.setText(completed.size() + " / " + courses.size());
            statPartsValue.setText(doneParts + " / " + totalParts);
            statInProgressValue.setText(String.valueOf(inProgress));
        } catch (SQLException e) {
            showError("Impossible de charger les cours : " + e.getMessage());
        }
    }

    private Node buildCard(Course course, int partsDone, int totalParts) {
        int percent = totalParts == 0 ? 0 : Math.round(100f * partsDone / totalParts);
        boolean finished = totalParts > 0 && partsDone >= totalParts;
        boolean started = partsDone > 0;

        Label number = new Label(String.format("%02d", course.getOrdre()));
        number.getStyleClass().add("lrn-card-number");

        Label pill = new Label(finished ? "Terminé" : started ? "En cours" : "À commencer");
        pill.getStyleClass().add(finished ? "lrn-pill-done" : started ? "lrn-pill-progress" : "lrn-pill-todo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(8, number, spacer, pill);

        Label title = new Label(course.getTitre());
        title.getStyleClass().add("lrn-card-title");
        title.setWrapText(true);

        Label desc = new Label(course.getDescription());
        desc.getStyleClass().add("lrn-card-text");
        desc.setWrapText(true);

        ProgressBar bar = new ProgressBar(percent / 100.0);
        bar.getStyleClass().add("lrn-progress");
        bar.setMaxWidth(Double.MAX_VALUE);

        Label detail = new Label(percent + " %  ·  " + partsDone + " / " + totalParts + " parties  ·  "
                + "environ " + course.getReadingMinutes() + " min");
        detail.getStyleClass().add("lrn-muted");

        Button open = new Button(finished ? "Relire" : started ? "Continuer" : "Commencer");
        open.getStyleClass().add("lrn-btn-primary");
        open.setOnAction(e -> openCourse(course));

        VBox card = new VBox(10, header, title, desc, bar, detail, open);
        card.getStyleClass().add("lrn-card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(cardWidth);
        card.setMinHeight(230);
        return card;
    }

    /**
     * Les cartes remplissent toute la largeur : on calcule le nombre de colonnes
     * selon la largeur disponible, puis on répartit la largeur entre elles.
     */
    private void setupResponsiveCards() {
        listView.viewportBoundsProperty().addListener((obs, oldBounds, bounds) -> {
            double available = bounds.getWidth() - 2 * PAGE_PADDING;
            if (available <= 0) {
                return;
            }
            int columns = Math.max(1, (int) ((available + GAP) / (MIN_CARD_WIDTH + GAP)));
            double width = (available - GAP * (columns - 1)) / columns - 1;
            if (Math.abs(width - cardWidth) > 1) {
                cardWidth = width;
                for (Node node : cardsPane.getChildren()) {
                    ((VBox) node).setPrefWidth(cardWidth);
                }
            }
        });
    }

    private void openCourse(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/learning-reader.fxml"));
            Node reader = loader.load();
            CourseReaderController controller = loader.getController();
            controller.setCourses(courses, course, this::showList);
            contentArea.getChildren().setAll(reader);
        } catch (IOException e) {
            showError("Impossible d'ouvrir le cours : " + e.getMessage());
        }
    }

    /** Retour à la liste (appelé par la page de lecture). */
    private void showList() {
        contentArea.getChildren().setAll(listView);
        refresh();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}

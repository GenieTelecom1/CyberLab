package com.cyberlab.controller;

import com.cyberlab.model.Course;
import com.cyberlab.service.ProgressService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

/**
 * Page de lecture d'un cours (learning-reader.fxml).
 * Le cours est découpé en parties. Terminer une partie fait avancer le pourcentage du cours ;
 * quand toutes les parties sont terminées, le cours est terminé.
 *
 * Règle : on peut lire les parties déjà terminées et la première partie non terminée,
 * pas celles qui viennent après (partIndex <= partsDone).
 */
public class CourseReaderController {

    @FXML private Label titleLabel;
    @FXML private Label positionLabel;
    @FXML private Button previousCourseButton;
    @FXML private Button nextCourseButton;
    @FXML private Label courseProgressLabel;
    @FXML private ProgressBar courseProgressBar;
    @FXML private VBox tocBox;
    @FXML private Label partTitleLabel;
    @FXML private Label partTextLabel;
    @FXML private Button previousPartButton;
    @FXML private Label statusLabel;
    @FXML private Button completeButton;

    private final ProgressService progressService = new ProgressService();

    private List<Course> courses;
    private Runnable onBack;

    private int courseIndex;
    private Course course;
    private List<Course.Section> sections;
    private int partsDone;   // nombre de parties terminées
    private int partIndex;   // partie affichée (0 = première)

    /** Appelé par CourseController avant l'affichage. */
    public void setCourses(List<Course> courses, Course start, Runnable onBack) {
        this.courses = courses;
        this.onBack = onBack;
        loadCourse(Math.max(0, courses.indexOf(start)));
    }

    private int currentUserId() {
        return CourseController.currentUserId();   // un seul endroit à adapter (voir CourseController)
    }

    private void loadCourse(int index) {
        courseIndex = index;
        course = courses.get(index);
        sections = course.getSections();
        try {
            partsDone = Math.min(progressService.getPartsDone(currentUserId(), course.getId()), sections.size());
        } catch (SQLException e) {
            partsDone = 0;
            showError("Erreur de lecture de la progression : " + e.getMessage());
        }
        // On reprend à la première partie non terminée (ou à la dernière si tout est fini).
        partIndex = Math.min(partsDone, sections.size() - 1);
        render();
    }

    private void render() {
        titleLabel.setText(course.getTitre());
        positionLabel.setText("Cours " + (courseIndex + 1) + " sur " + courses.size());
        previousCourseButton.setDisable(courseIndex == 0);
        nextCourseButton.setDisable(courseIndex == courses.size() - 1);

        int total = sections.size();
        int percent = total == 0 ? 0 : Math.round(100f * partsDone / total);
        courseProgressBar.setProgress(percent / 100.0);
        courseProgressLabel.setText(percent + " %  ·  " + partsDone + " / " + total + " parties terminées");

        buildToc();
        showPart();
    }

    private void buildToc() {
        tocBox.getChildren().clear();
        for (int i = 0; i < sections.size(); i++) {
            boolean done = i < partsDone;
            String mark = done ? "✓" : (i == partsDone ? "•" : "–");
            Button item = new Button(mark + "  " + (i + 1) + ". " + sections.get(i).getTitre());
            item.setMaxWidth(Double.MAX_VALUE);
            item.setWrapText(true);
            item.getStyleClass().add("lrn-toc-item");
            if (i == partIndex) {
                item.getStyleClass().add("lrn-toc-current");
            } else if (done) {
                item.getStyleClass().add("lrn-toc-done");
            }
            item.setDisable(i > partsDone);   // les parties suivantes sont verrouillées
            final int target = i;
            item.setOnAction(e -> {
                partIndex = target;
                buildToc();
                showPart();
            });
            tocBox.getChildren().add(item);
        }
    }

    private void showPart() {
        Course.Section section = sections.get(partIndex);
        boolean isLast = partIndex == sections.size() - 1;
        boolean alreadyDone = partIndex < partsDone;

        partTitleLabel.setText((partIndex + 1) + ". " + section.getTitre());
        partTextLabel.setText(section.getTexte());
        previousPartButton.setDisable(partIndex == 0);

        if (alreadyDone) {
            statusLabel.setText("Partie terminée  ·  environ " + section.getReadingMinutes() + " min de lecture");
            completeButton.setText(isLast ? "Cours terminé ✓" : "Partie suivante →");
            completeButton.setDisable(isLast);
        } else {
            statusLabel.setText("Partie " + (partIndex + 1) + " sur " + sections.size()
                    + "  ·  environ " + section.getReadingMinutes() + " min de lecture");
            completeButton.setText(isLast ? "Terminer le cours ✓" : "Partie terminée, suivante →");
            completeButton.setDisable(false);
        }
    }

    @FXML
    private void onPreviousPart() {
        if (partIndex > 0) {
            partIndex--;
            buildToc();
            showPart();
        }
    }

    /** Termine la partie affichée (si besoin) puis passe à la suivante. */
    @FXML
    private void onComplete() {
        boolean isLast = partIndex == sections.size() - 1;
        if (partIndex >= partsDone) {
            try {
                progressService.completePart(currentUserId(), course.getId(), partIndex + 1, sections.size());
                partsDone = partIndex + 1;
            } catch (SQLException e) {
                showError("Impossible d'enregistrer la progression : " + e.getMessage());
                return;
            }
        }
        if (!isLast) {
            partIndex++;
        }
        render();
    }

    @FXML
    private void onPreviousCourse() {
        if (courseIndex > 0) {
            loadCourse(courseIndex - 1);
        }
    }

    @FXML
    private void onNextCourse() {
        if (courseIndex < courses.size() - 1) {
            loadCourse(courseIndex + 1);
        }
    }

    @FXML
    private void onBack() {
        if (onBack != null) {
            onBack.run();
        }
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}

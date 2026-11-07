package com.cyberlab.controller;

import com.cyberlab.model.Course;
import com.cyberlab.service.CourseService;
import com.cyberlab.database.DatabaseConnection;
import com.cyberlab.service.ProgressService;
import com.cyberlab.util.MarkdownRenderer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.web.WebView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controleur de l'ecran "Learning" : liste des cours a gauche, contenu a droite,
 * navigation Precedent / Suivant, bouton "Marquer comme termine" et barre de progression.
 * Les acces a la base sont faits dans des threads separes pour ne pas bloquer l'interface.
 */
public class CourseController {

    @FXML private ListView<Course> courseList;
    @FXML private WebView contentView;
    @FXML private Label titleLabel;
    @FXML private Label statusLabel;
    @FXML private Label progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button completeButton;

    private final CourseService courseService = new CourseService();
    private final ProgressService progressService = new ProgressService();

    private List<Course> courses = List.of();
    private final Set<Integer> completedIds = new HashSet<>();
    private int userId;

    @FXML
    private void initialize() {
        // TODO membre 1 : remplacer par l'id de l'utilisateur connecte (session du Login).
        userId = DatabaseConnection.devUserId();

        courseList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                if (empty || course == null) {
                    setText(null);
                } else {
                    String done = completedIds.contains(course.getId()) ? "✓  " : "     ";
                    setText(done + course.getOrdre() + ". " + course.getTitre());
                }
            }
        });

        courseList.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> showCourse(selected));

        setNavigationDisabled(true);
        contentView.getEngine().loadContent(MarkdownRenderer.message("Chargement des cours..."));
        loadData();
    }

    private void loadData() {
        Task<Void> task = new Task<>() {
            private List<Course> loadedCourses;
            private Set<Integer> loadedCompleted;

            @Override
            protected Void call() throws Exception {
                loadedCourses = courseService.getAllCourses();
                loadedCompleted = progressService.getCompletedCourseIds(userId);
                return null;
            }

            @Override
            protected void succeeded() {
                courses = loadedCourses;
                completedIds.clear();
                completedIds.addAll(loadedCompleted);
                courseList.setItems(FXCollections.observableArrayList(courses));
                refreshProgress();
                if (courses.isEmpty()) {
                    contentView.getEngine().loadContent(MarkdownRenderer.message("Aucun cours disponible."));
                } else {
                    courseList.getSelectionModel().selectFirst();
                }
            }

            @Override
            protected void failed() {
                showError("Impossible de charger les cours", getException());
            }
        };
        startDaemon(task);
    }

    private void showCourse(Course course) {
        if (course == null) {
            return;
        }
        titleLabel.setText(course.getTitre());
        contentView.getEngine().loadContent(MarkdownRenderer.toHtml(course.getContenu()));
        updateButtons();
    }

    @FXML
    private void onPrevious() {
        int i = courseList.getSelectionModel().getSelectedIndex();
        if (i > 0) {
            courseList.getSelectionModel().select(i - 1);
        }
    }

    @FXML
    private void onNext() {
        int i = courseList.getSelectionModel().getSelectedIndex();
        if (i >= 0 && i < courses.size() - 1) {
            courseList.getSelectionModel().select(i + 1);
        }
    }

    @FXML
    private void onComplete() {
        Course course = courseList.getSelectionModel().getSelectedItem();
        if (course == null) {
            return;
        }
        completeButton.setDisable(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                progressService.markAsCompleted(userId, course.getId());
                return null;
            }

            @Override
            protected void succeeded() {
                completedIds.add(course.getId());
                courseList.refresh();
                refreshProgress();
                updateButtons();
                statusLabel.setText("Cours terminé : " + course.getTitre());
            }

            @Override
            protected void failed() {
                completeButton.setDisable(false);
                showError("Impossible d'enregistrer la progression", getException());
            }
        };
        startDaemon(task);
    }

    private void updateButtons() {
        int i = courseList.getSelectionModel().getSelectedIndex();
        Course course = courseList.getSelectionModel().getSelectedItem();
        prevButton.setDisable(i <= 0);
        nextButton.setDisable(i < 0 || i >= courses.size() - 1);
        boolean done = course != null && completedIds.contains(course.getId());
        completeButton.setDisable(course == null || done);
        completeButton.setText(done ? "✓ Cours terminé" : "Marquer comme terminé");
    }

    private void refreshProgress() {
        int total = courses.size();
        int done = (int) courses.stream().filter(c -> completedIds.contains(c.getId())).count();
        double pct = ProgressService.percentage(done, total);
        progressBar.setProgress(total == 0 ? 0 : pct / 100.0);
        progressLabel.setText(done + " / " + total + " cours terminés (" + Math.round(pct) + " %)");
    }

    private void setNavigationDisabled(boolean disabled) {
        prevButton.setDisable(disabled);
        nextButton.setDisable(disabled);
        completeButton.setDisable(disabled);
    }

    private void showError(String message, Throwable error) {
        String detail = error == null ? "" : error.getMessage();
        statusLabel.setText(message);
        contentView.getEngine().loadContent(MarkdownRenderer.message(message + " : " + detail));
        if (error != null) {
            error.printStackTrace();
        }
    }

    private static void startDaemon(Task<?> task) {
        Thread t = new Thread(task, "cyberlab-db");
        t.setDaemon(true);
        t.start();
    }
}
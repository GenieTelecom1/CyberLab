package com.cyberlab.controller;

import com.cyberlab.model.Question;
import com.cyberlab.service.ProgressService;
import com.cyberlab.service.QuizService;
import com.cyberlab.service.ScoreService;
import com.cyberlab.util.SceneManager;
import com.cyberlab.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QuizController {

    /**
     * Optionnel : depuis l'écran "Cours", mettre QuizController.pendingCourseId = id
     * puis SceneManager.navigateTo("quiz") pour lancer directement le quiz de ce cours.
     */
    public static int pendingCourseId = 0;

    /** Durée maximale pour répondre à une question. */
    private static final int SECONDS_PER_QUESTION = 30;
    /** Pourcentage minimum pour que le cours soit marqué "terminé" dans la progression. */
    private static final int PASS_PERCENT = 50;

    private static final String[] LETTERS = {"A", "B", "C", "D"};
    private static final String STYLE_CORRECT =
            "-fx-background-color: rgba(34,197,94,0.25); -fx-border-color: #22c55e; -fx-text-fill: white;";
    private static final String STYLE_WRONG =
            "-fx-background-color: rgba(239,68,68,0.25); -fx-border-color: #ef4444; -fx-text-fill: white;";

    private final QuizService quizService = new QuizService();

    @FXML private Label questionLabel;
    @FXML private Label progressLabel;
    @FXML private Button btnA, btnB, btnC, btnD, btnSuivant;

    private Button btnRecommencer;
    private Button btnChoisir;
    private Button btnHistorique;
    private VBox courseBox;
    private Button[] choices;
    private Label timerLabel;
    private ProgressBar timerBar;
    private Timeline timer;
    private int remaining;

    private String correctText = "";   // texte de la bonne réponse (indépendant de l'ordre affiché)
    private boolean answered = false;
    private boolean resultSaved = false;
    private int courseId = 1;
    private String courseTitle = null;

    @FXML
    public void initialize() {
        choices = new Button[]{btnA, btnB, btnC, btnD};

        btnRecommencer = new Button("Recommencer ce quiz");
        btnRecommencer.getStyleClass().add("btn-cyan");
        btnRecommencer.setOnAction(e -> recommencer());

        btnChoisir = new Button("Choisir un autre cours");
        btnChoisir.getStyleClass().add("pill-outline");
        btnChoisir.setOnAction(e -> afficherChoixCours());

        btnHistorique = new Button("Voir mon historique");
        btnHistorique.getStyleClass().add("pill-outline");
        btnHistorique.setOnAction(e -> SceneManager.navigateTo("history"));

        courseBox = new VBox(12);
        courseBox.setMaxWidth(650);

        Pane parent = (Pane) btnSuivant.getParent();

        timerLabel = new Label();
        timerBar = new ProgressBar(1);
        timerBar.setMaxWidth(650);
        timerBar.setPrefHeight(8);
        int pos = parent.getChildren().indexOf(questionLabel);
        parent.getChildren().add(Math.max(pos, 0), timerBar);
        parent.getChildren().add(Math.max(pos, 0), timerLabel);
        show(timerLabel, false);
        show(timerBar, false);

        // Quand l'écran est quitté (navigation), on arrête le chronomètre
        questionLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) stopTimer();
        });
        parent.getChildren().add(courseBox);
        parent.getChildren().add(btnRecommencer);
        parent.getChildren().add(btnChoisir);
        parent.getChildren().add(btnHistorique);

        if (pendingCourseId > 0) {
            int id = pendingCourseId;
            pendingCourseId = 0;
            demarrer(id, null);
        } else {
            afficherChoixCours();
        }
    }

    // ---------- Choix du cours ----------

    private void afficherChoixCours() {
        answered = true;
        stopTimer();
        for (Button b : choices) show(b, false);
        show(btnSuivant, false);
        show(btnRecommencer, false);
        show(btnChoisir, false);
        show(btnHistorique, false);

        questionLabel.setText("Choisissez un cours pour commencer le quiz");
        progressLabel.setText("CYBERLAB / QUIZ • Choix du cours");

        List<ScoreService.CourseChoice> cours = ScoreService.listCourses();
        if (cours.isEmpty()) {          // BDD indisponible : quiz par défaut
            demarrer(1, null);
            return;
        }

        courseBox.getChildren().clear();
        for (ScoreService.CourseChoice c : cours) {
            Button b = new Button(c.title() + "   •   " + c.questionCount() + " questions");
            b.getStyleClass().add("pill-outline");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setAlignment(Pos.BASELINE_LEFT);
            b.setOnAction(e -> demarrer(c.id(), c.title()));
            courseBox.getChildren().add(b);
        }
        courseBox.setVisible(true);
        courseBox.setManaged(true);
    }

    private void demarrer(int id, String titre) {
        courseBox.setVisible(false);
        courseBox.setManaged(false);

        courseId = id;
        courseTitle = titre;
        resultSaved = false;
        quizService.filtrerParCours(id);

        if (quizService.getTotalQuestions() == 0) {
            questionLabel.setText("Aucune question pour ce cours pour le moment.");
            show(btnChoisir, true);
            return;
        }
        chargerQuestion();
    }

    // ---------- Questions ----------

    private void chargerQuestion() {
        Question q = quizService.getCurrentQuestion();
        if (q == null) {
            afficherResultat();
            return;
        }

        answered = false;
        correctText = texteBonneReponse(q);

        // Les 4 choix sont mélangés à chaque affichage
        List<String> textes = new ArrayList<>(Arrays.asList(q.getChoixA(), q.getChoixB(), q.getChoixC(), q.getChoixD()));
        Collections.shuffle(textes);

        questionLabel.setText(q.getTexte());
        for (int i = 0; i < choices.length; i++) {
            choices[i].setText(LETTERS[i] + "     " + textes.get(i));
            choices[i].setUserData(textes.get(i));
            choices[i].setStyle("");
            show(choices[i], true);
        }

        boolean derniere = quizService.getCurrentIndex() == quizService.getTotalQuestions() - 1;
        btnSuivant.setText(derniere ? "Voir le résultat →" : "Question Suivante →");
        show(btnSuivant, false);
        show(btnRecommencer, false);
        show(btnChoisir, false);
        show(btnHistorique, false);

        progressLabel.setText("CYBERLAB / QUIZ • " + (courseTitle != null ? courseTitle + " • " : "")
                + "Question " + (quizService.getCurrentIndex() + 1) + " / " + quizService.getTotalQuestions());

        startTimer();
    }

    @FXML
    private void handleReponse(ActionEvent event) {
        if (answered) return;
        answered = true;
        stopTimer();

        Button clicked = (Button) event.getSource();
        Question q = quizService.getCurrentQuestion();
        if (q == null) return;

        boolean ok = false;
        for (int i = 0; i < choices.length; i++) {
            boolean bonne = estBonneReponse((String) choices[i].getUserData());
            if (bonne) choices[i].setStyle(STYLE_CORRECT);
            if (choices[i] == clicked) {
                ok = bonne;
                if (!bonne) clicked.setStyle(STYLE_WRONG);
            }
        }

        quizService.verifierReponse(ok ? q.getBonneReponse() : (String) clicked.getUserData());
        show(btnSuivant, true);
    }

    @FXML
    private void handleSuivant() {
        quizService.passerQuestionSuivante();
        chargerQuestion();
    }

    // ---------- Chronomètre ----------

    private void startTimer() {
        stopTimer();
        remaining = SECONDS_PER_QUESTION;
        updateTimerUi();
        show(timerLabel, true);
        show(timerBar, true);

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remaining--;
            updateTimerUi();
            if (remaining <= 0) onTimeout();
        }));
        timer.setCycleCount(SECONDS_PER_QUESTION);
        timer.play();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void updateTimerUi() {
        String color = remaining <= 10 ? "#ef4444" : "#19D6FF";
        timerLabel.setText("Temps restant : " + Math.max(remaining, 0) + " s");
        timerLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        timerBar.setProgress(Math.max(remaining, 0) / (double) SECONDS_PER_QUESTION);
        timerBar.setStyle("-fx-accent: " + color + ";");
    }

    /** Temps écoulé : la question compte comme fausse, on montre la bonne réponse. */
    private void onTimeout() {
        if (answered) return;
        answered = true;
        stopTimer();

        Question q = quizService.getCurrentQuestion();
        if (q != null) {
            for (int i = 0; i < choices.length; i++) {
                if (estBonneReponse((String) choices[i].getUserData())) {
                    choices[i].setStyle(STYLE_CORRECT);
                }
            }
        }
        timerLabel.setText("Temps écoulé ! Question comptée fausse.");
        timerLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        timerBar.setProgress(0);
        show(btnSuivant, true);
    }

    // ---------- Résultat ----------

    private void afficherResultat() {
        int score = quizService.getScore();
        int total = quizService.getTotalQuestions();
        int pct = total > 0 ? (score * 100) / total : 0;
        stopTimer();
        show(timerLabel, false);
        show(timerBar, false);
        boolean reussi = pct >= PASS_PERCENT;

        String message = pct >= 80 ? "Excellent travail !"
                : pct >= 50 ? "Bien, continuez à vous entraîner."
                : "Révisez les cours puis réessayez.";

        questionLabel.setText("Quiz terminé !\nVotre score : " + score + " / " + total
                + "  (" + pct + "%)\n" + message
                + (reussi ? "\nCours validé dans votre progression." : ""));
        progressLabel.setText("Terminé");

        for (Button b : choices) show(b, false);
        show(btnSuivant, false);
        show(btnRecommencer, true);
        show(btnChoisir, true);
        show(btnHistorique, true);

        if (!resultSaved && total > 0) {
            resultSaved = true;
            final int userId = SessionManager.getCurrentUserId();
            final String username = SessionManager.getCurrentUserName();
            final int cid = courseId;
            Thread t = new Thread(() -> {
                ScoreService.saveResult(userId, username, cid, score, total);
                if (reussi) {
                    try {
                        new ProgressService().markAsCompleted(userId, cid);
                    } catch (Exception ex) {
                        System.err.println("Erreur mise à jour progression : " + ex.getMessage());
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    private void recommencer() {
        quizService.resetQuiz();
        resultSaved = false;
        chargerQuestion();
    }

    /** Le texte de la bonne réponse, que la BDD stocke une lettre (A/B/C/D) ou le texte lui-même. */
    private String texteBonneReponse(Question q) {
        String br = q.getBonneReponse() == null ? "" : q.getBonneReponse().trim();
        String[] originaux = {q.getChoixA(), q.getChoixB(), q.getChoixC(), q.getChoixD()};
        for (int i = 0; i < LETTERS.length; i++) {
            if (br.equalsIgnoreCase(LETTERS[i]) && originaux[i] != null) return originaux[i].trim();
        }
        return br;
    }

    private boolean estBonneReponse(String texte) {
        return texte != null && texte.trim().equalsIgnoreCase(correctText);
    }

    private static void show(Node b, boolean visible) {
        b.setVisible(visible);
        b.setManaged(visible);
    }
}
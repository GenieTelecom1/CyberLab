package com.cyberlab.controller;

import com.cyberlab.model.Question;
import com.cyberlab.service.QuizService;
import com.cyberlab.service.ScoreService;
import com.cyberlab.util.SceneManager;
import com.cyberlab.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class QuizController {

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
    private Button btnHistorique;
    private Button[] choices;
    private boolean answered = false;
    private boolean resultSaved = false;
    private int courseId = 1;

    @FXML
    public void initialize() {
        choices = new Button[]{btnA, btnB, btnC, btnD};

        btnRecommencer = new Button("Recommencer le quiz");
        btnRecommencer.getStyleClass().add("btn-cyan");
        btnRecommencer.setOnAction(e -> recommencer());

        btnHistorique = new Button("Voir mon historique");
        btnHistorique.getStyleClass().add("pill-outline");
        btnHistorique.setOnAction(e -> SceneManager.navigateTo("history"));

        show(btnRecommencer, false);
        show(btnHistorique, false);
        Pane parent = (Pane) btnSuivant.getParent();
        parent.getChildren().add(btnRecommencer);
        parent.getChildren().add(btnHistorique);

        chargerQuestion();
    }

    private void chargerQuestion() {
        Question q = quizService.getCurrentQuestion();
        if (q == null) {
            afficherResultat();
            return;
        }

        courseId = q.getCourseId();
        answered = false;
        String[] textes = {q.getChoixA(), q.getChoixB(), q.getChoixC(), q.getChoixD()};

        questionLabel.setText(q.getTexte());
        for (int i = 0; i < choices.length; i++) {
            choices[i].setText(LETTERS[i] + "     " + textes[i]);
            choices[i].setUserData(textes[i]);
            choices[i].setStyle("");
            show(choices[i], true);
        }

        boolean derniere = quizService.getCurrentIndex() == quizService.getTotalQuestions() - 1;
        btnSuivant.setText(derniere ? "Voir le résultat →" : "Question Suivante →");
        show(btnSuivant, false);
        show(btnRecommencer, false);
        show(btnHistorique, false);

        progressLabel.setText("CYBERLAB / QUIZ • Question "
                + (quizService.getCurrentIndex() + 1) + " / " + quizService.getTotalQuestions());
    }

    @FXML
    private void handleReponse(ActionEvent event) {
        if (answered) return;
        answered = true;

        Button clicked = (Button) event.getSource();
        Question q = quizService.getCurrentQuestion();
        if (q == null) return;

        boolean ok = false;
        for (int i = 0; i < choices.length; i++) {
            boolean bonne = estBonneReponse(q, LETTERS[i], (String) choices[i].getUserData());
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

    private void afficherResultat() {
        int score = quizService.getScore();
        int total = quizService.getTotalQuestions();
        int pct = total > 0 ? (score * 100) / total : 0;

        String message = pct >= 80 ? "Excellent travail !"
                : pct >= 50 ? "Bien, continuez à vous entraîner."
                : "Révisez les cours puis réessayez.";

        questionLabel.setText("Quiz terminé !\nVotre score : " + score + " / " + total
                + "  (" + pct + "%)\n" + message);
        progressLabel.setText("Terminé");

        for (Button b : choices) show(b, false);
        show(btnSuivant, false);
        show(btnRecommencer, true);
        show(btnHistorique, true);

        // Sauvegarde du score (une seule fois par partie), hors du thread JavaFX
        if (!resultSaved && total > 0) {
            resultSaved = true;
            final int userId = SessionManager.getCurrentUserId();
            final String username = SessionManager.getCurrentUserName();
            final int cid = courseId;
            Thread t = new Thread(() -> ScoreService.saveResult(userId, username, cid, score, total));
            t.setDaemon(true);
            t.start();
        }
    }

    private void recommencer() {
        quizService.resetQuiz();
        resultSaved = false;
        chargerQuestion();
    }

    private boolean estBonneReponse(Question q, String lettre, String texte) {
        String br = q.getBonneReponse();
        if (br == null) return false;
        br = br.trim();
        return br.equalsIgnoreCase(lettre) || br.equalsIgnoreCase(texte == null ? "" : texte.trim());
    }

    private static void show(Button b, boolean visible) {
        b.setVisible(visible);
        b.setManaged(visible);
    }
}

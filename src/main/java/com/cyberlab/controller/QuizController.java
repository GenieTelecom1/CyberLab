package com.cyberlab.controller;

import com.cyberlab.model.Question;
import com.cyberlab.service.QuizService;
import com.cyberlab.util.QuizContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class QuizController {

    private QuizService quizService;

    @FXML private Label questionLabel;
    @FXML private Label progressLabel;
    @FXML private Button btnA, btnB, btnC, btnD, btnSuivant;

    @FXML
    public void initialize() {
        // Le cours est choisi sur l'ecran Cours (bouton "Passer le quiz du cours"), cours 1 par defaut.
        quizService = new QuizService(QuizContext.getCourseId());
        chargerQuestion();
    }

    private void chargerQuestion() {
        Question q = quizService.getCurrentQuestion();
        if (q != null) {
            questionLabel.setText(q.getTexte());
            // Intégration des lettres A, B, C, D exigées par le design
            btnA.setText("A     " + q.getChoixA());
            btnB.setText("B     " + q.getChoixB());
            btnC.setText("C     " + q.getChoixC());
            btnD.setText("D     " + q.getChoixD());

            setButtonsEnabled(true);
            if (btnSuivant != null) btnSuivant.setVisible(false);
            if (progressLabel != null) {
                progressLabel.setText("CYBERLAB / QUIZ • Question " + (quizService.getCurrentIndex() + 1) + " / " + quizService.getTotalQuestions());
            }
        } else if (quizService.getTotalQuestions() == 0) {
            // Aucun question pour ce cours dans la base
            questionLabel.setText("Aucune question disponible pour ce cours pour le moment.");
            btnA.setVisible(false); btnB.setVisible(false); btnC.setVisible(false); btnD.setVisible(false);
            if (btnSuivant != null) btnSuivant.setVisible(false);
            if (progressLabel != null) progressLabel.setText("CYBERLAB / QUIZ");
        } else {
            questionLabel.setText("Quiz terminé ! Votre score : " + quizService.getScore() + " / " + quizService.getTotalQuestions());
            btnA.setVisible(false); btnB.setVisible(false); btnC.setVisible(false); btnD.setVisible(false);
            if (btnSuivant != null) btnSuivant.setVisible(false);
            if (progressLabel != null) progressLabel.setText("Terminé");
        }
    }

    @FXML
    private void handleReponse(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        // Extraction du texte brut (en ignorant le préfixe "A     ")
        String texteBrut = clickedButton.getText().substring(6).trim();

        quizService.verifierReponse(texteBrut);
        setButtonsEnabled(false);
        if (btnSuivant != null) btnSuivant.setVisible(true);
    }

    @FXML
    private void handleSuivant() {
        quizService.passerQuestionSuivante();
        chargerQuestion();
    }

    private void setButtonsEnabled(boolean enabled) {
        btnA.setDisable(!enabled);
        btnB.setDisable(!enabled);
        btnC.setDisable(!enabled);
        btnD.setDisable(!enabled);
    }
}

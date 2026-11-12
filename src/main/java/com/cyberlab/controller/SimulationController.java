package com.cyberlab.controller;

import com.cyberlab.model.PhishingEmail;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimulationController {

    @FXML private Label lblEmailCounter;
    @FXML private Label lblScore;
    @FXML private Label lblExpediteur;
    @FXML private Label lblObjet;
    @FXML private TextArea txtCorps;

    @FXML private Button btnLegitime;
    @FXML private Button btnFrauduleux;

    @FXML private VBox paneFeedback;
    @FXML private Label lblResultat;
    @FXML private VBox vboxIndices;
    @FXML private Label lblConseilSecurite;
    @FXML private Button btnSuivant;

    private List<PhishingEmail> listeEmails = new ArrayList<>();
    private int indexCourant = 0;
    private int score = 0;

    @FXML
    public void initialize() {
        chargerEmailsExemples();
        afficherEmailCourant();
    }

    private void chargerEmailsExemples() {
        listeEmails.clear();

        // Email 1 : Phishing Banque
        listeEmails.add(new PhishingEmail(
                1,
                "securite@service-palypal.com",
                "URGENT : Suspension imminente de votre compte",
                "Cher client,\n\nUne tentative d'accès non autorisée a été détectée sur votre compte. Veuillez cliquer immédiatement sur le lien ci-dessous pour confirmer vos informations de carte bancaire, sinon votre compte sera bloqué sous 24h.\n\n[suspicious link removed]",
                true,
                Arrays.asList(
                        "Fausse adresse d'expéditeur (@service-palypal.com au lieu du site officiel)",
                        "Urgence artificielle et menace de blocage sous 24h",
                        "Lien HTTP non sécurisé menant vers un domaine tiers suspect"
                ),
                "Ne cliquez jamais sur un lien vous demandant vos identifiants ou numéros de carte sous la contrainte du temps."
        ));

        // Email 2 : Légitime RH
        listeEmails.add(new PhishingEmail(
                2,
                "rh@entreprise-cyberlab.fr",
                "Planning de la formation Cybersécurité - Semaine 45",
                "Bonjour à tous,\n\nVeuillez trouver sur le portail interne l'emploi du temps pour la session de sensibilisation à venir.\n\nCordialement,\nL'équipe RH",
                false,
                Arrays.asList("Expéditeur interne officiel", "Aucun lien externe ni demande de mot de passe"),
                "Les communications internes officielles ne demandent jamais de saisir des identifiants bancaires ou secrets."
        ));

        // Email 3 : Phishing Faux Colis
        listeEmails.add(new PhishingEmail(
                3,
                "suivi-livraison@chronopost-infoss.net",
                "Votre colis N° 489201 est en attente de paiement (2,99€)",
                "Votre colis n'a pas pu être livré en raison de frais de douane impayés. Réglez 2,99€ avant ce soir pour débloquer la livraison.",
                true,
                Arrays.asList(
                        "Domaine de messagerie contrefait (.infoss.net)",
                        "Demande de petite somme pour tromper la vigilance par carte bancaire"
                ),
                "Vérifiez l'état d'un colis directement sur le site officiel du transporteur avec votre numéro d'envoi."
        ));

        // Email 4 : Légitime Support DSI
        listeEmails.add(new PhishingEmail(
                4,
                "support-it@entreprise-cyberlab.fr",
                "Maintenance planifiée des serveurs ce vendredi à 22h",
                "Bonjour,\n\nUne maintenance de nos serveurs aura lieu ce vendredi soir. Aucune action n'est requise de votre part. Les services seront indisponibles pendant 2h.\n\nL'équipe Support IT",
                false,
                Arrays.asList("Information purement informative", "Aucun lien ou pièce jointe suspecte"),
                "Les messages d'information technique de votre entreprise ne sollicitent aucune réinitialisation de mot de passe."
        ));

        // Email 5 : Phishing Microsoft 365
        listeEmails.add(new PhishingEmail(
                5,
                "no-reply@account-microsoft-security-alert.org",
                "Alerte de sécurité : Connexion détectée depuis la Russie",
                "Une nouvelle connexion à votre compte Microsoft 365 a été détectée depuis Moscou. Si vous n'êtes pas à l'origine de cette connexion, cliquez ici pour sécuriser votre compte.",
                true,
                Arrays.asList(
                        "Extension de domaine suspecte (.org au lieu de [suspicious link removed])",
                        "Intimidation par une fausse localisation géographique pour susciter la panique"
                ),
                "Consultez l'historique des connexions directement depuis les paramètres de votre compte officiel."
        ));

        // Email 6 : Légitime Confirmation de Commande
        listeEmails.add(new PhishingEmail(
                6,
                "commandes@fnac.com",
                "Confirmation de votre commande N° FNAC-982314",
                "Merci pour votre achat ! Votre commande comprenant 1x Casque Audio a bien été enregistrée. Retrouvez votre facture dans votre espace client sur [suspicious link removed].",
                false,
                Arrays.asList("Expéditeur officiel [suspicious link removed]", "Renvoie vers l'espace client sans lien direct suspect"),
                "Les confirmations de commande légitimes comportent votre numéro d'achat exact et renvoient vers votre espace client."
        ));

        // Email 7 : Phishing ANTAI / Amende
        listeEmails.add(new PhishingEmail(
                7,
                "contact@antai-gouv-dossier-amende.fr",
                "Dernier avis avant majoration : Retard de paiement d'amende",
                "Vous avez un retard de paiement pour une infraction au code de la route. Réglez votre amende en ligne sous 48h pour éviter une majoration de 135€.",
                true,
                Arrays.asList(
                        "Domaine frauduleux ne se terminant pas par gouv.fr",
                        "Usurpation d'une administration publique avec menace de majoration financière"
                ),
                "Les sites officiels de l'État français utilisent toujours le domaine strict .gouv.fr."
        ));

        // Email 8 : Légitime Newsletter
        listeEmails.add(new PhishingEmail(
                8,
                "newsletter@cert-ssi.gouv.fr",
                "Bulletin mensuel de sécurité informatique - Édition Novembre",
                "Découvrez notre résumé mensuel des vulnérabilités majeures et les conseils pour protéger vos systèmes d'information.",
                false,
                Arrays.asList("Domaine certifié .gouv.fr", "Diffusion d'informations publiques sans collecte de données"),
                "Les bulletins d'information officiels sont consultables sans avoir à saisir vos identifiants personnels."
        ));
    }

    private void afficherEmailCourant() {
        PhishingEmail email = listeEmails.get(indexCourant);

        lblEmailCounter.setText("Email " + (indexCourant + 1) + " sur " + listeEmails.size());
        lblScore.setText("Score : " + score + " / " + (indexCourant > 0 ? indexCourant : 0));

        lblExpediteur.setText(email.getExpediteur());
        lblObjet.setText(email.getObjet());
        txtCorps.setText(email.getCorps());

        btnLegitime.setDisable(false);
        btnFrauduleux.setDisable(false);

        paneFeedback.setVisible(false);
        paneFeedback.setManaged(false);
    }

    @FXML
    private void handleLegitimeAction(ActionEvent event) {
        traiterChoixUtilisateur(false);
    }

    @FXML
    private void handleFrauduleuxAction(ActionEvent event) {
        traiterChoixUtilisateur(true);
    }

    private void traiterChoixUtilisateur(boolean choixUtilisateurEstFrauduleux) {
        PhishingEmail email = listeEmails.get(indexCourant);
        boolean estCorrect = (choixUtilisateurEstFrauduleux == email.isEstFrauduleux());

        if (estCorrect) {
            score++;
        }
        lblScore.setText("Score : " + score + " / " + (indexCourant + 1));

        btnLegitime.setDisable(true);
        btnFrauduleux.setDisable(true);

        if (estCorrect) {
            lblResultat.setText("✓ Bonne réponse !");
            lblResultat.setTextFill(Color.web("#10B981"));
            paneFeedback.setStyle("-fx-background-color: rgba(16,185,129,0.12); -fx-background-radius: 14; -fx-border-color: #10B981; -fx-border-radius: 14; -fx-padding: 16;");
        } else {
            lblResultat.setText("✘ Mauvaise réponse ! Cet email était " + (email.isEstFrauduleux() ? "FRAUDULEUX" : "LÉGITIME") + ".");
            lblResultat.setTextFill(Color.web("#EF4444"));
            paneFeedback.setStyle("-fx-background-color: rgba(239,68,68,0.12); -fx-background-radius: 14; -fx-border-color: #EF4444; -fx-border-radius: 14; -fx-padding: 16;");
        }
        vboxIndices.getChildren().clear();
        Label headerIndices = new Label("Indices suspects :");
        headerIndices.setStyle("-fx-font-weight: bold; -fx-text-fill: #C8102E;");
        vboxIndices.getChildren().add(headerIndices);

        if (email.getIndicesSuspects() != null && !email.getIndicesSuspects().isEmpty()) {
            for (String indice : email.getIndicesSuspects()) {
                Label lblIndice = new Label("  • " + indice);
                lblIndice.setTextFill(Color.web("#1F2937"));
                vboxIndices.getChildren().add(lblIndice);
            }
        }

        lblConseilSecurite.setText("À retenir : " + email.getConseilSecurite());

        paneFeedback.setManaged(true);
        paneFeedback.setVisible(true);

        if (indexCourant == listeEmails.size() - 1) {
            btnSuivant.setText("Voir le bilan final ➔");
        } else {
            btnSuivant.setText("Email suivant ➔");
        }
    }

    @FXML
    private void handleSuivantAction(ActionEvent event) {
        if (indexCourant < listeEmails.size() - 1) {
            indexCourant++;
            afficherEmailCourant();
        } else {
            afficherBilanFinal();
        }
    }

    private void afficherBilanFinal() {
        lblEmailCounter.setText("Simulation terminée !");
        lblScore.setText("Score final : " + score + " / " + listeEmails.size());
        lblExpediteur.setText("CyberLab - Bilan du test");
        lblObjet.setText("Résultat global de votre détection");

        double pourcentage = ((double) score / listeEmails.size()) * 100;
        txtCorps.setText(String.format(
                "Félicitations, vous avez terminé la simulation Phishing Detector !\n\n" +
                        "Votre score final : %d sur %d (%.0f%% de bonnes réponses).\n\n" +
                        "Continuez à appliquer ces réflexes au quotidien pour vous protéger contre le phishing !",
                score, listeEmails.size(), pourcentage
        ));

        btnLegitime.setDisable(true);
        btnFrauduleux.setDisable(true);

        paneFeedback.setVisible(false);
        paneFeedback.setManaged(false);
    }
}
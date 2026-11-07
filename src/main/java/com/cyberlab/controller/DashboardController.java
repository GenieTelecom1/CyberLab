package com.cyberlab.controller;

import com.cyberlab.service.DashboardService;
import com.cyberlab.service.DashboardService.CourseSummary;
import com.cyberlab.service.DashboardService.DashboardSnapshot;
import com.cyberlab.service.DashboardService.RankEntry;
import com.cyberlab.util.Icons;
import com.cyberlab.util.SceneManager;
import com.cyberlab.util.SessionManager;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.List;

/**
 * Propriétaire : Membre 2.
 * Le contrôleur ne contient aucune donnée ni SQL : il affiche ce que renvoie DashboardService.
 * Carte « Cours » : 2 cours à la fois, qui défilent automatiquement (6 s) ou avec les flèches.
 */
public class DashboardController {

    private static final int PAGE_SIZE = 2;
    private static final Duration ROTATION_DELAY = Duration.seconds(6);

    @FXML private Label welcomeName, modulesDone, avgScoreLabel, rankLabel, rankNote;
    @FXML private Label pointsLabel, levelLabel, streakLabel;
    @FXML private ProgressBar progressBar, avgScoreBar;
    @FXML private ImageView heroArt;
    @FXML private Node heroCard;
    @FXML private VBox coursesCard, leaderboardBox;
    @FXML private HBox coursesPage, pageDots;
    @FXML private StackPane ringHolder, starHolder, coursesIconHolder, simsIconHolder,
            rankingIconHolder, phishIconHolder, pwdIconHolder,
            statIcon1, statIcon2, statIcon3, rankIconHolder;

    private final DashboardService dashboardService = new DashboardService();

    private List<CourseSummary> courses = List.of();
    private int pageIndex = 0;
    private Timeline autoRotate;
    private FadeTransition pageFade;

    @FXML
    private void initialize() {
        welcomeName.setText(SessionManager.getCurrentUserName());
        DashboardSnapshot data = dashboardService.getSnapshot(SessionManager.getCurrentUserId());

        setupIcons();

        // bandeau
        pointsLabel.setText(data.points() + (data.points() > 1 ? " points" : " point"));
        levelLabel.setText(data.level());
        streakLabel.setText(data.streakDays() + (data.streakDays() > 1 ? " jours d'affilée" : " jour d'affilée"));
        roundHeroArt();

        // statistiques
        double ratio = data.progressRatio();   // arrondi à l'entier le plus proche à l'affichage
        buildProgressRing(ratio);
        modulesDone.setText(data.completedCount() + " / " + data.totalCourses() + " cours terminés");
        avgScoreLabel.setText(Math.round(data.averageScore() * 100) + " %");
        rankLabel.setText(data.rank());
        rankNote.setText(data.rankNote());

        buildLeaderboard(data.leaderboard());
        setupCourseCarousel(data.courses());

        // animations d'entrée
        fadeIn(heroCard, 0);
        animateBar(progressBar, ratio, 100);
        animateBar(avgScoreBar, data.averageScore(), 200);
    }

    // ---------- icônes ----------

    private void setupIcons() {
        coursesIconHolder.getChildren().setAll(Icons.icon(Icons.BOOK_OPEN, 26, Icons.CYAN, null));
        simsIconHolder.getChildren().setAll(Icons.icon(Icons.NETWORK, 24, Icons.CYAN, null));
        rankingIconHolder.getChildren().setAll(Icons.icon(Icons.TROPHY, 26, Icons.CYAN, null));
        starHolder.getChildren().setAll(Icons.icon(Icons.STAR, 32, Icons.VIOLET, null));
        rankIconHolder.getChildren().setAll(Icons.icon(Icons.TROPHY, 34, Icons.CYAN, null));
        statIcon1.getChildren().setAll(Icons.icon(Icons.BAR_CHART, 20, Icons.CYAN, null));
        statIcon2.getChildren().setAll(Icons.icon(Icons.TROPHY, 20, Icons.VIOLET, null));
        statIcon3.getChildren().setAll(Icons.icon(Icons.TRENDING_UP, 24, Icons.CYAN, null));
        phishIconHolder.getChildren().setAll(Icons.icon(Icons.MAIL, 40, Icons.VIOLET, null));
        pwdIconHolder.getChildren().setAll(Icons.icon(Icons.SHIELD_CHECK, 40, Icons.CYAN, null));
    }

    // ---------- anneau de progression ----------

    private void buildProgressRing(double ratio) {
        final double size = 72, center = size / 2, radius = 30;

        Circle track = new Circle(center, center, radius);
        track.setFill(Color.TRANSPARENT);
        track.setStroke(Color.web("#25304a"));
        track.setStrokeWidth(7);

        Arc arc = new Arc(center, center, radius, radius, 90, 0);   // départ en haut
        arc.setType(ArcType.OPEN);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Icons.CYAN);
        arc.setStrokeWidth(7);
        arc.setStrokeLineCap(StrokeLineCap.ROUND);

        Pane ring = new Pane(track, arc);
        ring.setPrefSize(size, size);
        ring.setMinSize(size, size);
        ring.setMaxSize(size, size);

        Label percent = new Label(Math.round(ratio * 100) + " %");
        percent.getStyleClass().add("ring-text");

        ringHolder.getChildren().setAll(ring, percent);

        if (ratio > 0) {
            Timeline t = new Timeline(new KeyFrame(Duration.millis(900),
                    new KeyValue(arc.lengthProperty(), -360 * ratio)));   // sens horaire
            t.setDelay(Duration.millis(150));
            t.play();
        }
    }

    // ---------- bandeau ----------

    /** Coins arrondis sur l'illustration du bandeau. */
    private void roundHeroArt() {
        Image img = heroArt.getImage();
        if (img == null || img.getHeight() == 0) return;
        double h = heroArt.getFitHeight();
        double w = img.getWidth() * h / img.getHeight();
        Rectangle clip = new Rectangle(w, h);
        clip.setArcWidth(28);
        clip.setArcHeight(28);
        heroArt.setClip(clip);
    }

    // ---------- carrousel de cours (2 par page) ----------

    private void setupCourseCarousel(List<CourseSummary> list) {
        courses = list;
        showPage(0, false);
        if (pageCount() <= 1) return;

        autoRotate = new Timeline(new KeyFrame(ROTATION_DELAY, e -> showPage(pageIndex + 1, true)));
        autoRotate.setCycleCount(Animation.INDEFINITE);
        autoRotate.play();

        coursesCard.setOnMouseEntered(e -> autoRotate.pause());   // pause au survol
        coursesCard.setOnMouseExited(e -> autoRotate.play());
        coursesCard.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) autoRotate.stop();               // écran quitté : on arrête le minuteur
        });
    }

    private int pageCount() {
        return (courses.size() + PAGE_SIZE - 1) / PAGE_SIZE;
    }

    private void showPage(int index, boolean animate) {
        int count = pageCount();
        if (count == 0) return;
        pageIndex = ((index % count) + count) % count;
        final int target = pageIndex;

        if (pageFade != null) pageFade.stop();
        if (animate) {
            pageFade = new FadeTransition(Duration.millis(220), coursesPage);
            pageFade.setToValue(0);
            pageFade.setOnFinished(e -> fillPage(target));
            pageFade.play();
        } else {
            fillPage(target);
        }
    }

    private void fillPage(int page) {
        coursesPage.getChildren().clear();
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, courses.size());
        for (int i = start; i < end; i++) {
            VBox card = courseCard(courses.get(i), i, (i - start) * 90);
            HBox.setHgrow(card, Priority.ALWAYS);
            card.setPrefWidth(10);                 // les 2 cartes se partagent la largeur à égalité
            card.setMaxWidth(Double.MAX_VALUE);
            coursesPage.getChildren().add(card);
        }
        buildDots();
        fadeIn(coursesPage, 0);
    }

    private void buildDots() {
        pageDots.getChildren().clear();
        for (int i = 0; i < pageCount(); i++) {
            Region dot = new Region();
            dot.getStyleClass().add(i == pageIndex ? "dot-active" : "dot");
            final int target = i;
            dot.setOnMouseClicked(e -> { showPage(target, true); restartTimer(); });
            pageDots.getChildren().add(dot);
        }
    }

    private void restartTimer() {
        if (autoRotate != null) autoRotate.playFromStart();
    }

    private VBox courseCard(CourseSummary c, int globalIndex, int delayMs) {
        Label title = new Label(c.title());
        title.getStyleClass().add("course-title-big");
        title.setWrapText(true);
        title.setMinHeight(24);

        Label desc = new Label(c.description());
        desc.getStyleClass().add("course-desc");
        desc.setWrapText(true);
        desc.setMinHeight(40);

        ProgressBar bar = new ProgressBar(0);
        bar.setMaxWidth(Double.MAX_VALUE);
        animateBar(bar, c.progress(), delayMs + 150);

        // statut écrit en toutes lettres (pas seulement une couleur)
        Label pct = new Label(Math.round(c.progress() * 100) + " %" + (c.completed() ? " · Terminé" : ""));
        pct.getStyleClass().add("course-pct");

        String action = c.completed() ? "Revoir  →" : (c.progress() <= 0 ? "Commencer  →" : "Continuer  →");
        Button go = new Button(action);
        go.getStyleClass().add("btn-cyan");
        go.setOnAction(e -> SceneManager.navigateTo("course_list"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(pct, spacer, go);
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(8, title, desc, bar, footer);
        body.setStyle("-fx-padding: 14 16 16 16;");

        VBox card = new VBox(courseBanner(globalIndex), body);
        card.getStyleClass().add("course-card");
        // coins arrondis : on découpe la carte entière
        Rectangle clip = new Rectangle();
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        card.setClip(clip);
        return card;
    }

    /** Bannière illustrée : image fournie pour les 2 premiers cours, sinon dégradé + grande icône. */
    private StackPane courseBanner(int globalIndex) {
        final double height = 108;
        StackPane banner = new StackPane();
        banner.setMinHeight(height);
        banner.setPrefHeight(height);
        banner.setMaxHeight(height);

        String imagePath = globalIndex < 2 ? "/images/course_banner_" + (globalIndex + 1) + ".png" : null;
        var stream = imagePath == null ? null : getClass().getResourceAsStream(imagePath);
        if (stream != null) {
            Image img = new Image(stream);
            ImageView view = new ImageView(img);
            view.setPreserveRatio(false);
            view.setFitHeight(height);
            banner.widthProperty().addListener((obs, o, w) -> coverImage(view, img, w.doubleValue(), height));
            banner.getChildren().add(view);
            StackPane.setAlignment(view, Pos.CENTER);
        } else {
            banner.getStyleClass().addAll("course-banner", "banner-" + (globalIndex % 4));
            banner.getChildren().add(Icons.icon(Icons.COURSE_ICONS[globalIndex % Icons.COURSE_ICONS.length],
                    50, Icons.CYAN, Color.web("#00D2FF", 0.18)));
        }
        return banner;
    }

    /** Remplit la bannière sans déformer l'image (recadrage centré). */
    private static void coverImage(ImageView view, Image img, double w, double h) {
        if (w <= 0) return;
        double imgRatio = img.getWidth() / img.getHeight();
        double boxRatio = w / h;
        double vw = img.getWidth(), vh = img.getHeight();
        if (imgRatio > boxRatio) vw = vh * boxRatio; else vh = vw / boxRatio;
        view.setViewport(new Rectangle2D((img.getWidth() - vw) / 2, (img.getHeight() - vh) / 2, vw, vh));
        view.setFitWidth(w);
        view.setFitHeight(h);
    }

    @FXML private void nextCourses()     { showPage(pageIndex + 1, true); restartTimer(); }
    @FXML private void previousCourses() { showPage(pageIndex - 1, true); restartTimer(); }

    // ---------- classement ----------

    private void buildLeaderboard(List<RankEntry> entries) {
        leaderboardBox.getChildren().clear();
        if (entries.isEmpty()) {
            Label empty = new Label("Aucun quiz terminé pour le moment");
            empty.getStyleClass().add("muted-small");
            leaderboardBox.getChildren().add(empty);
            return;
        }
        for (RankEntry r : entries) {
            Label medal = new Label(String.valueOf(r.position()));
            medal.getStyleClass().addAll("rank-badge", switch (r.position()) {
                case 1 -> "rank-gold";
                case 2 -> "rank-silver";
                case 3 -> "rank-bronze";
                default -> "rank-other";
            });
            String n = r.name().trim();
            Label avatar = new Label(n.substring(0, Math.min(2, n.length())).toUpperCase());
            avatar.getStyleClass().addAll("avatar-small", r.position() == 1 ? "avatar-blue" : "avatar-dark");
            Label name = new Label(r.name());
            name.getStyleClass().add("card-title");
            Label level = new Label(r.level());
            level.getStyleClass().add("muted-tiny");
            VBox who = new VBox(1, name, level);
            Label points = new Label(r.points() + " pts");
            points.getStyleClass().add("rank-score");
            Label trend = new Label("↑");   // tendance : valeur de test
            trend.getStyleClass().add("trend-up");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox row = new HBox(12, medal, avatar, who, spacer, points, trend);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("rank-row");
            leaderboardBox.getChildren().add(row);
        }
    }

    // ---------- animations ----------

    private static void animateBar(ProgressBar bar, double target, int delayMs) {
        bar.setProgress(0);
        Timeline t = new Timeline(new KeyFrame(Duration.millis(700),
                new KeyValue(bar.progressProperty(), target)));
        t.setDelay(Duration.millis(delayMs));
        t.play();
    }

    private static void fadeIn(Node node, int delayMs) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(400), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(delayMs));
        ft.play();
    }

    // ---------- accès rapide ----------
    @FXML private void goCourses()     { SceneManager.navigateTo("course_list"); }
    @FXML private void goQuiz()        { SceneManager.navigateTo("quiz"); }
    @FXML private void goSimulations() { SceneManager.navigateTo("phishing_simulation"); }
    @FXML private void goPhishing()    { SceneManager.navigateTo("phishing_simulation"); }
    @FXML private void goPassword()    { SceneManager.navigateTo("password_simulation"); }
    @FXML private void goLeaderboard() { SceneManager.navigateTo("leaderboard"); }
}

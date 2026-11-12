package com.cyberlab.util;

import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Propriétaire : Membre 2. Icônes vectorielles (grille 24x24, style « contour », type Feather, licence MIT).
 * Usage : Icons.icon(Icons.MAIL, 32, Icons.VIOLET, null)  -> StackPane carré de 32 px.
 */
public final class Icons {

    public static final Color CYAN = Color.web("#C8102E");
    public static final Color VIOLET = Color.web("#E11D48");
    public static final Color ORANGE = Color.web("#F97316");
    public static final Color GOLD = Color.web("#FBBF24");
    public static final Color MUTED = Color.web("#6B7280");

    public static final String SHIELD_PLUS = "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z M12 8v6 M9 11h6";
    public static final String MAIL = "M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z M22 6l-10 7L2 6";
    public static final String BUG = "M9 7a3 3 0 0 1 6 0v1H9z M7 10h10v4a5 5 0 0 1-10 0z M3 11h4 M17 11h4 M4 19l3-2 M20 19l-3-2 M12 10v9";
    public static final String USERS = "M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M9 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8z M23 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75";
    public static final String WIFI = "M5 12.55a11 11 0 0 1 14.08 0 M1.42 9a16 16 0 0 1 21.16 0 M8.53 16.11a6 6 0 0 1 6.95 0 M12 20h.01";
    public static final String KEY = "M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4";
    public static final String STAR = "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z";
    public static final String BOOKMARK_X = "M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z M10 8l4 4 M14 8l-4 4";
    public static final String TROPHY = "M6 9H4.5a2.5 2.5 0 0 1 0-5H6 M18 9h1.5a2.5 2.5 0 0 0 0-5H18 M4 22h16 M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22 M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22 M18 2H6v7a6 6 0 0 0 12 0V2z";

    /** Icônes des 6 cours, dans l'ordre du cahier des charges. */
    public static final String SHIELD_CHECK = "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z M9 12l2 2 4-4";
    public static final String BOOK_OPEN = "M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z";
    public static final String NETWORK = "M9 2h6v6H9z M2 16h6v6H2z M16 16h6v6h-6z M12 8v4 M5 16v-4h14v4";
    public static final String BAR_CHART = "M18 20V10 M12 20V4 M6 20v-6";
    public static final String TRENDING_UP = "M23 6l-9.5 9.5-5-5L1 18 M17 6h6v6";
    public static final String LOG_OUT = "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4 M16 17l5-5-5-5 M21 12H9";
    public static final String HOME = "M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z M9 22V12h6v10";
    public static final String HELP_CIRCLE = "M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20z M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3 M12 17h.01";
    public static final String MESSAGE = "M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z";

    public static final String[] COURSE_ICONS = {SHIELD_PLUS, MAIL, BUG, USERS, WIFI, KEY};

    private Icons() { }

    /** Change la couleur de contour d'une icône créée par {@link #icon}. */
    public static void recolor(javafx.scene.Node icon, Color stroke) {
        if (icon instanceof StackPane box && !box.getChildren().isEmpty()
                && box.getChildren().get(0) instanceof Group g
                && !g.getChildren().isEmpty() && g.getChildren().get(0) instanceof SVGPath p) {
            p.setStroke(stroke);
        }
    }

    /** @param fill couleur de remplissage (null = contour seul) */
    public static StackPane icon(String path, double size, Color stroke, Color fill) {
        SVGPath p = new SVGPath();
        p.setContent(path);
        p.setFill(fill == null ? Color.TRANSPARENT : fill);
        p.setStroke(stroke);
        p.setStrokeWidth(1.8);
        p.setStrokeLineCap(StrokeLineCap.ROUND);
        p.setStrokeLineJoin(StrokeLineJoin.ROUND);
        Group g = new Group(p);
        double scale = size / 24.0;
        g.setScaleX(scale);
        g.setScaleY(scale);
        StackPane box = new StackPane(g);
        box.setMinSize(size, size);
        box.setPrefSize(size, size);
        box.setMaxSize(size, size);
        return box;
    }
}

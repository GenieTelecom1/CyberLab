package com.cyberlab.util;

import com.cyberlab.service.AuthException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/** Exécute un appel base de données hors du thread JavaFX, puis rend la main à l'interface. */
public final class UiTask {

    private static final Logger LOG = Logger.getLogger(UiTask.class.getName());

    private UiTask() {}

    /**
     * @param work      travail à faire en tâche de fond (appel au service)
     * @param onSuccess exécuté sur le thread JavaFX en cas de succès
     * @param onError   exécuté sur le thread JavaFX en cas d'erreur
     * @param onFinally exécuté sur le thread JavaFX dans tous les cas (réactiver le bouton)
     */
    public static <T> void run(Callable<T> work, Consumer<T> onSuccess,
                               Consumer<Throwable> onError, Runnable onFinally) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(ev -> {
            onFinally.run();
            onSuccess.accept(task.getValue());
        });
        task.setOnFailed(ev -> {
            onFinally.run();
            onError.accept(task.getException());
        });
        Thread th = new Thread(task, "cyberlab-db");
        th.setDaemon(true);
        th.start();
    }

    /** Message à afficher à l'utilisateur pour n'importe quelle erreur. */
    public static String messageFor(Throwable t) {
        if (t instanceof AuthException) {
            return t.getMessage();
        }
        LOG.log(Level.SEVERE, "Erreur inattendue", t);
        if (t instanceof IllegalStateException) {
            return "Configuration de la base manquante : vérifiez db.properties.";
        }
        return "Une erreur inattendue est survenue.";
    }
}

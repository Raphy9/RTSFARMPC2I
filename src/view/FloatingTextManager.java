package src.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Gestionnaire des textes flottants avec un thread de mise à jour dédié.
 * Utilisé pour afficher des indicateurs temporaires (ex: "+10 PO") qui montent et disparaissent.
 */
public class FloatingTextManager {
    // Couleurs thématiques pour la cohérence visuelle (Or et Expérience)
    public static final Color COIN_COLOR = new Color(255, 220, 0);
    public static final Color EXP_COLOR = new Color(175, 90, 255);

    // Paramètres d'animation par défaut
    private static final int DEFAULT_FPS = 30;           // Fluidité de l'animation
    private static final int DEFAULT_DURATION_MS = 900;  // Temps avant disparition (0.9s)
    private static final float DEFAULT_RISE_PX = 24f;     // Distance de montée en pixels

    private final Object lock = new Object();            // Verrou pour la synchronisation des listes
    private final List<FloatingText> texts = new ArrayList<>(); // Liste des textes actifs
    private final int fps;
    private final int durationMs;
    private final float risePx;

    private volatile Runnable repaintCallback; // Fonction de rappel pour redessiner la vue (Global)
    private Thread thread;                     // Thread gérant le cycle de vie des textes

    public FloatingTextManager() {
        this(DEFAULT_FPS, DEFAULT_DURATION_MS, DEFAULT_RISE_PX);
    }

    public FloatingTextManager(int fps, int durationMs, float risePx) {
        this.fps = Math.max(1, fps);
        this.durationMs = Math.max(100, durationMs);
        this.risePx = Math.max(1f, risePx);
    }

    /** Définit l'action à exécuter (repaint) pour mettre à jour l'affichage graphique */
    public void setRepaintCallback(Runnable repaintCallback) {
        this.repaintCallback = repaintCallback;
    }

    /** Démarre le thread de nettoyage/mise à jour */
    public void start() {
        if (thread != null && thread.isAlive()) return;
        thread = new Thread(this::runLoop, "FloatingTextThread");
        thread.setDaemon(true); // S'arrête automatiquement si l'application ferme
        thread.start();
    }

    /** Arrête proprement le thread */
    public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    /**
     * Ajoute un nouveau texte flottant à l'écran.
     * @param text Le message à afficher.
     * @param screenX Position X en pixels écran.
     * @param screenY Position Y en pixels écran.
     * @param color Couleur du texte.
     */
    public void addText(String text, int screenX, int screenY, Color color) {
        if (text == null || text.isEmpty()) return;
        if (color == null) color = Color.WHITE;

        FloatingText ft = new FloatingText(text, screenX, screenY, color, System.currentTimeMillis());

        synchronized (lock) {
            texts.add(ft);
        }

        // Force un rafraîchissement immédiat pour voir le texte apparaître
        Runnable cb = repaintCallback;
        if (cb != null) {
            SwingUtilities.invokeLater(cb);
        }
    }

    /** Raccourci pour ajouter un gain d'argent */
    public void addMoney(int amount, int screenX, int screenY) {
        addText("+" + amount + "PO", screenX, screenY, COIN_COLOR);
    }

    /** Raccourci pour ajouter un gain d'expérience */
    public void addExp(int amount, int screenX, int screenY) {
        addText("+" + amount + "XP", screenX, screenY, EXP_COLOR);
    }

    /** Supprime tous les textes en cours */
    public void clear() {
        synchronized (lock) {
            texts.clear();
        }
    }

    /**
     * Dessine les textes flottants actifs sur le contexte graphique.
     * Appelé généralement dans le paintComponent de la classe Global.
     */
    public void draw(Graphics2D g) {
        long now = System.currentTimeMillis();
        List<FloatingText> snapshot;

        synchronized (lock) {
            if (texts.isEmpty()) return;
            snapshot = new ArrayList<>(texts); // Copie pour éviter les ConcurrentModificationException
        }

        Graphics2D g2 = (Graphics2D) g.create();
        // Améliore la qualité du texte
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (GameFonts.MINECRAFT_FONT != null) {
            g2.setFont(GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 18f));
        } else {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
        }

        for (FloatingText ft : snapshot) {
            // Calcul de la progression de l'animation (0.0 à 1.0)
            float progress = Math.min(1f, (now - ft.startTimeMs) / (float) durationMs);
            float alpha = 1f - progress; // Le texte devient transparent avec le temps

            int drawX = ft.x;
            int drawY = Math.round(ft.y - (risePx * progress)); // Le texte monte

            // Dessin de l'ombre portée pour une meilleure lisibilité
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(new Color(0, 0, 0, 160));
            g2.drawString(ft.text, drawX + 1, drawY + 1);

            // Dessin du texte principal
            g2.setColor(ft.color);
            g2.drawString(ft.text, drawX, drawY);
        }
        g2.dispose();
    }

    /**
     * Boucle de fond qui vérifie quels textes ont expiré et demande le rafraîchissement.
     */
    private void runLoop() {
        int sleepMs = Math.max(1, 1000 / fps);
        try {
            while (!Thread.currentThread().isInterrupted()) {
                boolean hasActive = false;
                long now = System.currentTimeMillis();

                synchronized (lock) {
                    Iterator<FloatingText> it = texts.iterator();
                    while (it.hasNext()) {
                        FloatingText ft = it.next();
                        // Suppression si la durée de vie est dépassée
                        if (now - ft.startTimeMs >= durationMs) {
                            it.remove();
                        } else {
                            hasActive = true;
                        }
                    }
                }

                // Si des textes bougent encore, on demande à redessiner la vue
                if (hasActive && repaintCallback != null) {
                    SwingUtilities.invokeLater(repaintCallback);
                }

                Thread.sleep(sleepMs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Structure de données interne représentant un texte individuel.
     */
    private static class FloatingText {
        private final String text;
        private final int x;
        private final int y;
        private final Color color;
        private final long startTimeMs;

        private FloatingText(String text, int x, int y, Color color, long startTimeMs) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
            this.startTimeMs = startTimeMs;
        }
    }
}
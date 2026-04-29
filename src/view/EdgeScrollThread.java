package src.view;

import src.model.Camera;

import javax.swing.*;
import java.awt.*;

/**
 * Thread dédié à l'edge-scrolling (défilement par les bords).
 * Il surveille en permanence la position de la souris par rapport aux dimensions de la vue
 * et déplace la caméra de manière fluide sans impacter les performances de l'affichage principal.
 */
public class EdgeScrollThread {
    // --- Dépendances ---
    private final Camera camera;      // La caméra à manipuler
    private final Global globalView; // La vue de référence pour les coordonnées souris

    // --- Paramètres de défilement (volatiles pour la sécurité entre threads) ---
    private volatile int fps;           // Fréquence de mise à jour (ex: 60)
    private volatile int edgeThreshold; // Taille de la zone réactive au bord (ex: 20 pixels)
    private volatile float edgeSpeed;   // Vitesse de déplacement

    // --- Gestion du HUD et des menus ---
    private volatile int rightSidebarWidth = 0; // Ignore le bord droit si un menu est ouvert (ex: Shop)
    private volatile java.util.List<Rectangle> ignoredRegions = new java.util.ArrayList<>(); // Zones HUD cliquables
    private volatile boolean overlayActive = false; // Désactive le scroll si un popup central est ouvert
    private volatile boolean enabled = true;        // Interrupteur général

    private Thread thread; // Instance du thread d'exécution

    public EdgeScrollThread(Camera camera, Global globalView, int fps, int edgeThreshold, float edgeSpeed) {
        this.camera = camera;
        this.globalView = globalView;
        this.fps = Math.max(1, fps);
        this.edgeThreshold = Math.max(1, edgeThreshold);
        this.edgeSpeed = edgeSpeed;
    }

    /** Démarre le thread s'il n'est pas déjà actif. */
    public synchronized void start() {
        if (thread != null && thread.isAlive()) return;
        thread = new Thread(this::runLoop, "EdgeScrollThread");
        thread.setDaemon(true); // Le thread s'arrêtera automatiquement si l'app se ferme
        thread.start();
    }

    /** Boucle principale de détection. */
    private void runLoop() {
        try {
            int sleepMs = Math.max(1, 1000 / this.fps); // Calcul de l'intervalle selon les FPS
            while (!Thread.currentThread().isInterrupted()) {

                // Conditions pour autoriser le scroll : activé + caméra présente + pas de menu ouvert
                if (enabled && camera != null && !overlayActive) {

                    // Sécurité : Ne scroller que si le joueur utilise activement la fenêtre du jeu
                    try {
                        java.awt.Window w = SwingUtilities.getWindowAncestor(globalView);
                        if (w == null || !w.isFocused()) {
                            Thread.sleep(sleepMs);
                            continue;
                        }
                    } catch (Throwable t) {}

                    try {
                        // Récupération de la position de la souris sur l'écran
                        PointerInfo pi = MouseInfo.getPointerInfo();
                        if (pi != null) {
                            Point mouseOnScreen = pi.getLocation();

                            // Conversion de la position écran vers la position locale dans le jeu
                            Point gLocal = new Point(mouseOnScreen);
                            SwingUtilities.convertPointFromScreen(gLocal, globalView);

                            int gvW = globalView.getWidth();
                            int gvH = globalView.getHeight();

                            // Vérifie que la souris est bien à l'intérieur de la zone de jeu
                            if (gLocal.x >= 0 && gLocal.y >= 0 && gLocal.x < gvW && gLocal.y < gvH) {
                                int mouseX = gLocal.x;
                                int mouseY = gLocal.y;

                                // Vérification : la souris est-elle sur un bouton HUD ou une zone à ignorer ?
                                boolean inIgnoredRegion = false;
                                for (Rectangle ir : this.ignoredRegions) {
                                    if (ir != null && ir.contains(mouseX, mouseY)) {
                                        inIgnoredRegion = true;
                                        break;
                                    }
                                }

                                if (!inIgnoredRegion) {
                                    // Calcul de la zone de jeu effective (ex: sans le menu latéral droit)
                                    int effectiveW = Math.max(0, gvW - rightSidebarWidth);

                                    if (mouseX < effectiveW) {
                                        float dx = 0f, dy = 0f;

                                        // Détection bords gauche/droite
                                        if (mouseX < edgeThreshold) dx = -edgeSpeed;
                                        else if (mouseX > effectiveW - edgeThreshold && mouseX < effectiveW) dx = edgeSpeed;

                                        // Détection bords haut/bas
                                        if (mouseY < edgeThreshold) dy = -edgeSpeed;
                                        else if (mouseY > gvH - edgeThreshold) dy = edgeSpeed;

                                        // Si un mouvement est détecté, on déplace la caméra
                                        if (dx != 0f || dy != 0f) {
                                            camera.move(dx, dy);
                                            // On demande un rafraîchissement graphique immédiat
                                            SwingUtilities.invokeLater(globalView::repaint);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Throwable t) {
                        t.printStackTrace(); // Évite que le thread crash sur une erreur de conversion
                    }
                }
                Thread.sleep(sleepMs); // Attend le prochain "cycle"
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Sortie propre du thread
        }
    }

    /** Arrête le thread proprement. */
    public synchronized void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    // --- Getters & Setters ---
    public void setRightSidebarWidth(int widthPx) { this.rightSidebarWidth = Math.max(0, widthPx); }
    public void setIgnoredRegions(java.util.List<Rectangle> r) { this.ignoredRegions = r; }
    public void setOverlayActive(boolean active) { this.overlayActive = active; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setEdgeThreshold(int t) { this.edgeThreshold = Math.max(1, t); }
    public void setEdgeSpeed(float s) { this.edgeSpeed = s; }
    public void setFps(int fps) { this.fps = Math.max(1, fps); }
}
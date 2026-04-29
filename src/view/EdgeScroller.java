package src.view;

import src.model.Camera;
import java.awt.*;

/**
 * EdgeScroller léger : wrapper autour d'un EdgeScrollThread dédié.
 * Cette classe sert de pont pour manipuler le thread de défilement automatique
 * (quand la souris approche des bords de l'écran).
 * Elle garde une API compatible avec l'ancien usage dans `Display` pour éviter de casser le code existant.
 */
public class EdgeScroller {
    // Le véritable moteur de calcul du défilement qui tourne en arrière-plan
    private final EdgeScrollThread thread;

    /**
     * Constructeur principal utilisé dans la classe Display.
     *
     * @param frame         La fenêtre du jeu (non utilisée ici mais conservée pour la compatibilité)
     * @param layeredPane   Le panneau de couches (non utilisé ici)
     * @param camera        La caméra du modèle à déplacer
     * @param globalView    La vue graphique pour récupérer la position de la souris
     * @param fps           Nombre de rafraîchissements par seconde du défilement
     * @param edgeThreshold Zone de détection en pixels depuis le bord
     * @param edgeSpeed     Vitesse de déplacement de la caméra
     */
    public EdgeScroller(java.awt.Frame frame, javax.swing.JLayeredPane layeredPane, Camera camera, Global globalView,
                        int fps, int edgeThreshold, float edgeSpeed) {
        // Initialisation et démarrage immédiat du thread de calcul
        this.thread = new EdgeScrollThread(camera, globalView, fps, edgeThreshold, edgeSpeed);
        this.thread.start();
    }

    /**
     * Ancien constructeur minimal.
     * Appelle le constructeur principal avec des paramètres nuls pour la frame et le pane.
     */
    public EdgeScroller(Camera camera, Global globalView, int fps, int edgeThreshold, float edgeSpeed) {
        this(null, null, camera, globalView, fps, edgeThreshold, edgeSpeed);
    }

    // --- Méthodes de délégation vers le Thread ---

    /** Arrête définitivement le thread de défilement */
    public void stop() { this.thread.stop(); }

    /** Définit une marge à droite (utile si un panneau latéral est ouvert) pour décaler le bord réactif */
    public void setRightSidebarWidth(int w) { this.thread.setRightSidebarWidth(w); }

    /** Permet de définir des zones (comme des boutons HUD) où le défilement ne doit pas s'activer */
    public void setIgnoredRegions(java.util.List<Rectangle> r) { this.thread.setIgnoredRegions(r); }

    /** Indique si un menu transparent/overlay est actif pour modifier le comportement */
    public void setOverlayActive(boolean active) { this.thread.setOverlayActive(active); }

    /** Active ou désactive totalement le défilement (ex: pendant une cinématique ou un menu pause) */
    public void setEnabled(boolean enabled) { this.thread.setEnabled(enabled); }

    /** Ajuste la sensibilité (distance du bord à partir de laquelle la caméra bouge) */
    public void setEdgeThreshold(int t) { this.thread.setEdgeThreshold(t); }

    /** Ajuste la vitesse de déplacement */
    public void setEdgeSpeed(float s) { this.thread.setEdgeSpeed(s); }

    /** Modifie le taux de rafraîchissement du calcul */
    public void setFps(int f) { this.thread.setFps(f); }
}
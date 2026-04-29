package src.view;

import src.model.Camera;

import java.awt.*;

/**
 * EdgeScroller leger : wrapper autour d'un EdgeScrollThread dedie.
 * Garde une API compatible avec l'ancien usage dans `Display`.
 */
public class EdgeScroller {
    private final EdgeScrollThread thread;

    // Constructeur compatible avec l'appel existant dans Display
    public EdgeScroller(java.awt.Frame frame, javax.swing.JLayeredPane layeredPane, Camera camera, Global globalView,
                       int fps, int edgeThreshold, float edgeSpeed) {
        // Le thread n'a besoin que de la camera et de la vue globale
        this.thread = new EdgeScrollThread(camera, globalView, fps, edgeThreshold, edgeSpeed);
        this.thread.start();
    }

    // Ancien constructeur minimal (au cas où)
    public EdgeScroller(Camera camera, Global globalView, int fps, int edgeThreshold, float edgeSpeed) {
        this(null, null, camera, globalView, fps, edgeThreshold, edgeSpeed);
    }

    public void stop() { this.thread.stop(); }
    public void setRightSidebarWidth(int w) { this.thread.setRightSidebarWidth(w); }
    public void setIgnoredRegions(java.util.List<Rectangle> r) { this.thread.setIgnoredRegions(r); }
    public void setOverlayActive(boolean active) { this.thread.setOverlayActive(active); }
    public void setEnabled(boolean enabled) { this.thread.setEnabled(enabled); }
    public void setEdgeThreshold(int t) { this.thread.setEdgeThreshold(t); }
    public void setEdgeSpeed(float s) { this.thread.setEdgeSpeed(s); }
    public void setFps(int f) { this.thread.setFps(f); }
}

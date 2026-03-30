package src.view;

import src.model.Camera;

import javax.swing.*;
import java.awt.*;

/**
 * Thread dédié à l'edge-scrolling. Gère en exclusivité la détection de position de la souris
 * et le déplacement de la caméra à intervalle fixe.
 *
 * Usage : instancier en fournissant les composants nécessaires, appeler start() et stop().
 */
public class EdgeScrollThread {
    private final Camera camera;
    private final Global globalView;
    private volatile int fps;
    private volatile int edgeThreshold;
    private volatile float edgeSpeed;

    private volatile int rightSidebarWidth = 0;
    private volatile Rectangle ignoredRegion = null;
    private volatile boolean overlayActive = false; // true quand un menu/popup est ouvert
    private volatile boolean enabled = true;

    private Thread thread;

    public EdgeScrollThread(Camera camera, Global globalView, int fps, int edgeThreshold, float edgeSpeed) {
        this.camera = camera;
        this.globalView = globalView;
        this.fps = Math.max(1, fps);
        this.edgeThreshold = Math.max(1, edgeThreshold);
        this.edgeSpeed = edgeSpeed;
    }

    public synchronized void start() {
        if (thread != null && thread.isAlive()) return;
        thread = new Thread(this::runLoop, "EdgeScrollThread");
        thread.setDaemon(true);
        thread.start();
    }

    private void runLoop() {
        try {
            int sleepMs = Math.max(1, 1000 / this.fps);
            while (!Thread.currentThread().isInterrupted()) {
                if (enabled && camera != null && !overlayActive) {
                    try {
                        PointerInfo pi = MouseInfo.getPointerInfo();
                        if (pi != null) {
                            Point mouseOnScreen = pi.getLocation();
                            // Convert to coordinates relative to globalView
                            Point gLocal = new Point(mouseOnScreen);
                            SwingUtilities.convertPointFromScreen(gLocal, globalView);
                            int gvW = globalView.getWidth();
                            int gvH = globalView.getHeight();
                            if (gLocal.x >= 0 && gLocal.y >= 0 && gLocal.x < gvW && gLocal.y < gvH) {
                                int mouseX = gLocal.x;
                                int mouseY = gLocal.y;
                                // ignore explicit region
                                Rectangle ir = this.ignoredRegion;
                                if (ir == null || !ir.contains(mouseX, mouseY)) {
                                    int effectiveW = Math.max(0, gvW - rightSidebarWidth);
                                    if (mouseX < effectiveW) {
                                        float dx = 0f, dy = 0f;
                                        if (mouseX < edgeThreshold) dx = -edgeSpeed;
                                        else if (mouseX > effectiveW - edgeThreshold && mouseX < effectiveW) dx = edgeSpeed;

                                        if (mouseY < edgeThreshold) dy = -edgeSpeed;
                                        else if (mouseY > gvH - edgeThreshold) dy = edgeSpeed;

                                        if (dx != 0f || dy != 0f) {
                                            camera.move(dx, dy);
                                            SwingUtilities.invokeLater(globalView::repaint);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Throwable t) {
                        // don't kill the thread on unexpected exceptions
                        t.printStackTrace();
                    }
                }
                Thread.sleep(sleepMs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    // API de configuration (thread-safe via volatile fields)
    public void setRightSidebarWidth(int widthPx) { this.rightSidebarWidth = Math.max(0, widthPx); }
    public void setIgnoredRegion(Rectangle r) { this.ignoredRegion = r; }
    public void setOverlayActive(boolean active) { this.overlayActive = active; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setEdgeThreshold(int t) { this.edgeThreshold = Math.max(1, t); }
    public void setEdgeSpeed(float s) { this.edgeSpeed = s; }
    public void setFps(int fps) { this.fps = Math.max(1, fps); }
}

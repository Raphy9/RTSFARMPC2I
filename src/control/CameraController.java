package src.control;

import src.model.Camera;
import src.view.Global;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseMotionAdapter;

public class CameraController extends MouseMotionAdapter {
    private Camera camera;
    private Global global;

    private final float speed = 0.2f; // Vitesse de déplacement de la caméra
    private final float edgeThreshold = 50f; // Distance du bord pour déclencher le déplacement

    public CameraController(Camera camera, Global global) {
        this.camera = camera;
        this.global = global;
    }


    /**
     * Méthode appelée lorsque la souris est déplacée. Si la souris est proche des bords de l'écran,
     * la caméra se déplace dans la direction correspondante. La méthode vérifie les coordonnées
     * de la souris par rapport à la taille de la vue globale pour déterminer si elle est proche des bords et ajuste
     * la position de la caméra en conséquence. Après le déplacement, la vue globale est rafraîchie pour refléter les changements.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseMoved(java.awt.event.MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int width = global.getWidth();
        int height = global.getHeight();

        if (x < edgeThreshold) {
            camera.move(-speed, 0);
        } else if (x > width - 50) {
            camera.move(speed, 0);
        }

        if (y < edgeThreshold) {
            camera.move(0, -speed);
        } else if (y > height - 50) {
            camera.move(0, speed);
        }
        global.repaint();
    }
}

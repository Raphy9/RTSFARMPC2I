package src.control;

import src.model.Camera;
import src.view.Global;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/** Controleur pour déplacer la caméra avec les flèches du clavier
 * Permet de se déplacer dans le monde en déplaçant la caméra
 */
public class CameraController extends KeyAdapter {
    // Le constructeur reçoit la caméra pour pouvoir la déplacer, et la display pour pouvoir la repaint après chaque déplacement
    private Camera camera;
    private Global global;

    // La vitesse de déplacement de la caméra, à ajuster selon les besoins
    private final float speed = 0.2f;

    /** Le constructeur reçoit la caméra pour pouvoir la déplacer, et la display pour pouvoir la repaint après chaque déplacement
     * @param camera la caméra à déplacer
     * @param global la display pour repaint après chaque déplacement
     */
    public CameraController(Camera camera, Global global) {
        this.camera = camera;
        this.global = global;
    }

    /** Lorsque l'utilisateur appuie sur une touche, on vérifie si c'est une flèche directionnelle, et si c'est le cas, on déplace la caméra dans la direction correspondante
     * et on repaint la display pour que le changement soit visible
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {
            case KeyEvent.VK_UP: camera.move(0,-speed); break;
            case KeyEvent.VK_DOWN: camera.move(0,speed); break;
            case KeyEvent.VK_LEFT: camera.move(-speed, 0); break;
            case KeyEvent.VK_RIGHT: camera.move(speed, 0); break;
        }
        global.repaint();
    }
}

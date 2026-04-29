package src.control;

import src.model.Camera;
import src.view.Global;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/** Controleur pour deplacer la camera avec les fleches du clavier
 * Permet de se deplacer dans le monde en deplaçant la camera
 */
public class CameraController extends KeyAdapter {
    // Le constructeur reçoit la camera pour pouvoir la deplacer, et la display pour pouvoir la repaint apres chaque deplacement
    private Camera camera;
    private Global global;

    // La vitesse de deplacement de la camera, à ajuster selon les besoins
    private final float speed = 0.2f;

    /** Le constructeur reçoit la camera pour pouvoir la deplacer, et la display pour pouvoir la repaint apres chaque deplacement
     * @param camera la caméra à déplacer
     * @param global la display pour repaint apres chaque déplacement
     */
    public CameraController(Camera camera, Global global) {
        this.camera = camera;
        this.global = global;
    }

    /** Lorsque l'utilisateur appuie sur une touche, on vérifie si c'est une fleche directionnelle, et si c'est le cas, on déplace la caméra dans la direction correspondante
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

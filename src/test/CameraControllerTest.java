package src.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.control.CameraController;
import src.model.Camera;
import src.model.World;
import src.view.Global;

import javax.swing.JPanel;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CameraControllerTest {

    private Camera camera;
    private Global global;
    private CameraController controller;

    @BeforeEach
    public void setUp() {
        camera = new Camera(10f, 10f);
        global = new Global(new World(), camera);
        controller = new CameraController(camera, global);
    }

    @Test
    public void testKeyPressedRight() {
        // On simule la création d'un événement clavier (flèche droite)
        KeyEvent rightKey = new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, ' ');

        // On l'envoie au contrôleur
        controller.keyPressed(rightKey);

        // La caméra aurait dû avancer de 0.2f (notre SPEED défini dans le contrôleur)
        assertEquals(10.2f, camera.getX(), 0.001f, "Un appui à droite doit augmenter X de 0.2");
        assertEquals(10f, camera.getY(), 0.001f, "Un appui à droite ne doit pas modifier Y");
    }

    @Test
    public void testKeyPressedUp() {
        // On simule la création d'un événement clavier (Flèche du Haut)
        KeyEvent upKey = new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, ' ');

        controller.keyPressed(upKey);

        // La caméra aurait dû reculer de 0.2f en Y
        assertEquals(10f, camera.getX(), 0.001f, "Un appui en haut ne doit pas modifier X");
        assertEquals(9.8f, camera.getY(), 0.001f, "Un appui en haut doit diminuer Y de 0.2");
    }
}
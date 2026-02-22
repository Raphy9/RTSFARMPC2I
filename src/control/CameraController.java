package src.control;

import src.model.Camera;
import src.view.Global;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CameraController extends KeyAdapter {
    private Camera camera;
    private Global global;

    private final float speed = 0.2f;

    public CameraController(Camera camera, Global global) {
        this.camera = camera;
        this.global = global;
    }

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

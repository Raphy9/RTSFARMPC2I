package src.view;

import src.model.Camera;
import src.model.World;

import javax.swing.*;
import java.awt.*;

/**
 * Classe principale de l'affichage du jeu, elle cree et gere les differents elements de l'affichage
 * Display s'occupe de changer entre les differents modes de vue (global, popup, selection, menu)
 */
public class Display extends JPanel {
    // Ratio multiplicateur pour la taille des objets
    public static final int RATIO_X = 64;
    public static final int RATIO_Y = 64;
    private JFrame frame;
    private World world;
    private Camera camera;

    public Display(JFrame frame) {
        this.frame = frame;
        this.newGame();
        this.frame.setVisible(true);
        this.frame.setPreferredSize(new Dimension(Camera.WIDTH * RATIO_X, Camera.HEIGHT * RATIO_Y));
        Global global = new Global(this.world, this.camera);
        this.frame.add(global);
        this.frame.pack();
    }

    /** Initialise un nouveau monde */
    private void newGame() {
        this.world = new World();
        this.camera = new Camera();
    }

    private void lancePopupNul() {
        frame.add(new Popup());
    }

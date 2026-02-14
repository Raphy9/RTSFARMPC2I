package src.view;

import src.model.World;

import javax.swing.*;

/**
 * Classe principale de l'affichage du jeu, elle cree et gere les differents elements de l'affichage
 * Display s'occupe de changer entre les differents modes de vue (global, popup, selection, menu)
 */
public class Display extends JPanel {
    // Ratio multiplicateur pour la taille des objets
    public static final int RATIO_X = 16;
    public static final int RATIO_Y = 9;
    private JFrame frame;
    private World world;

    public Display(JFrame frame) {
        this.frame = frame;
        this.newGame();
        Global global = new Global(this.world);
        this.frame.add(global);
    }

    /** Initialise un nouveau monde */
    private void newGame() {
        this.world = new World();
    }
}

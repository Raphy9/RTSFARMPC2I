package src.view;

import src.control.GlobalController;
import src.model.Camera;
import src.model.World;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Classe principale de l'affichage du jeu, elle cree et gere les differents elements de l'affichage
 * Display s'occupe de changer entre les differents modes de vue (global, popup, selection, menu)
 */
public class Display {
    // Ratio multiplicateur pour la taille des objets
    public static final int RATIO_X = 64;
    public static final int RATIO_Y = 64;
    private JFrame frame;
    private World world;
    private Camera camera;
    private Global globalView;
    private GlobalController globalController;
    private PopupView popupView;

    public Display(JFrame frame) {
        this.frame = frame;
        this.newGame();
        Dimension gameSize = new Dimension(Camera.WIDTH * RATIO_X, Camera.HEIGHT * RATIO_Y);
        this.frame.setPreferredSize(gameSize);

        // LayeredPane pour pouvoir superposer les popups par dessus la vue globale
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(gameSize);

        // Vue globale
        this.globalView = new Global(this.world, this.camera);
        globalView.setPreferredSize(gameSize);
        globalView.setBounds(0, 0, gameSize.width, gameSize.height);
        this.globalController = new GlobalController(this, globalView);
        layeredPane.add(globalView, JLayeredPane.DEFAULT_LAYER);    // layer du dessous

        // Vue popup
        this.popupView = new PopupView(globalView);
        this.popupView.setBounds(0, 0, gameSize.width, gameSize.height);
        this.popupView.setPreferredSize(gameSize);
        layeredPane.add(popupView, JLayeredPane.PALETTE_LAYER);   // au dessus de la vue globale

        this.frame.setContentPane(layeredPane);
        this.frame.pack();
        this.frame.setVisible(true);
    }

    /**
     * Initialise un nouveau monde
     */
    private void newGame() {
        this.world = new World();
        this.camera = new Camera();
    }

    /** Met la vue en mode popup, en affichant le popup passe en parametre */
    public void switchToPopup(PopupPanel popup) {
        // Desactiver les contoles de la vue globale
        globalView.removeMouseListener(globalController); // ne fait rien si deja enleve
        popupView.showPopup(popup);
    }

    /** Met la vue en mode global, en cachant le popup actif */
    public void switchToGlobal() {
        popupView.hidePopup();
        // Re-activer les controles de la vue globale
        if (! Arrays.asList(globalView.getMouseListeners()).contains(globalController)) {
            globalView.addMouseListener(globalController);
        }
        // TODO changer quand on aura le mode selection
    }

    /** Repaint la fenetre */
    public void repaint() {
        frame.repaint();
        // hmm peut etre changer, a voir si on a besoin de tout repaint tout le temps
    }


}

package src.view;

import src.control.CameraController;
import src.control.GlobalController;
import src.control.SelectionController;
import src.model.Camera;
import src.model.Tile;
import src.model.World;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.function.Predicate;

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
    private CameraController cameraController;
    private PopupView popupView;
    private Selection selectionView;
    private SelectionController selectionController;

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
        // Controlleurs de la vue globale
        this.globalController = new GlobalController(this, globalView, this.world);
        this.cameraController = new CameraController(camera, globalView);
        globalView.addKeyListener(this.cameraController);
        // Pour que la vue globale puisse bien recevoir les inputs
        globalView.setFocusable(true);
        globalView.requestFocusInWindow();

        layeredPane.add(globalView, JLayeredPane.DEFAULT_LAYER);    // commencer avec la vue globale

        // Vue popup
        this.popupView = new PopupView(globalView);
        this.popupView.setBounds(0, 0, gameSize.width, gameSize.height);
        this.popupView.setPreferredSize(gameSize);

        layeredPane.add(popupView, JLayeredPane.MODAL_LAYER);   // au dessus de la vue globale

        // Vue Selection
        this.selectionView = new Selection(this.world, this.camera);
        this.selectionView.setPreferredSize(gameSize);
        this.selectionView.setBounds(0, 0, gameSize.width, gameSize.height);
        // Controleurs de la vue selection
        this.selectionController = new SelectionController(this, world);
        selectionView.addMouseListener(this.selectionController);
        selectionView.addKeyListener(this.cameraController); // pour pouvoir deplacer la camera meme en mode selection
        selectionView.setFocusable(true);
        // Par defaut, la vue selection est invisible, on l'affichera seulement quand on passera en mode selection
        layeredPane.add(selectionView, JLayeredPane.PALETTE_LAYER);   // au dessus de la vue globale, sous les popups
        selectionView.setVisible(false);

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
        // Si on est en mode global, desactiver les contoles de la vue globale
        globalView.removeMouseListener(globalController); // ne fait rien si deja enleve
        globalView.removeKeyListener(cameraController); // ne fait rien si deja enleve
        // Afficher le popup
        popupView.showPopup(popup);
    }

    /** Met la vue en mode global */
    public void switchToGlobal() {
        popupView.hidePopup(); // cacher le popup si on vient d'un popup
        selectionView.setVisible(false);    // si on vient du mode selection, cacher la vue selection
        globalView.setVisible(true);
        // Re-activer les controles de la vue globale si besoin
        if (! Arrays.asList(globalView.getMouseListeners()).contains(globalController)) {
            globalView.addMouseListener(globalController);
        }
        if (! Arrays.asList(globalView.getKeyListeners()).contains(cameraController)) {
            globalView.addKeyListener(cameraController);
        }
        globalView.requestFocusInWindow(); // pour que la vue globale puisse recevoir les inputs apres le changement de vue
    }

    /** Met la vue en mode selection, en affichant la vue selection et en cachant la vue globale
     * @param selectionCriteria le critere de selection pour les cases, par exemple "case avec une plante" ou "case vide,
     * fonction Tile -> boolean qui retourne true si la case est acceptee par la selection, false sinon
     * */
    public void switchToSelection(Predicate<Tile> selectionCriteria, String message) {
        popupView.hidePopup();   // si on vient d'un popup, le cacher
        selectionView.setMessage(message);   // indiquer a l'utilisateur ce qu'il doit selectionner
        selectionController.setSelectionCriteria(selectionCriteria);
        globalView.setVisible(false);
        selectionView.setVisible(true);
        selectionView.requestFocusInWindow(); // pour que la vue selection puisse recevoir les inputs apres le changement de vue
    }

    public Camera getCamera() {
        return camera;
    }

    /** Repaint la fenetre */
    public void repaint() {
        frame.repaint();
        // hmm peut etre changer, a voir si on a besoin de tout repaint tout le temps
    }


}

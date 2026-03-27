package src.view;

import src.control.BuildingManager;
import src.control.CameraController;
import src.control.GlobalController;
import src.control.SelectionController;
import src.control.popups.CloseController;
import src.model.Camera;
import src.model.Tile;
import src.model.World;
import src.model.actions.ActionBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.function.Predicate;

/** Classe principale de la vue, qui gère les différentes vues (globale, popup, selection) et les interactions entre elles.
 * C'est la classe centrale pour le rendu et l'affichage du jeu, elle contient les références vers les autres vues et les contrôleurs.
 * C'est aussi la classe qui gère le changement de vue (par exemple passer de la vue globale à une vue popup ou de selection) et qui permet aux contrôleurs de déclencher des changements de vue.
 * */
public class Display {
    // Ratio multiplicateur pour la taille des objets
    public static final int RATIO_X = 64;
    public static final int RATIO_Y = 64;
    private final JFrame frame;
    private World world;
    private Camera camera;
    private final Global globalView;
    private final GlobalController globalController;
    private final CameraController cameraController;
    private final PopupView popupView;
    private final Selection selectionView;
    private final SelectionController selectionController;
    private JLayeredPane layeredPane; // exposé pour manipulations (moveToFront)
    private JButton btnOpenMenu; // bouton construit devenu champ pour l'accès depuis onClose
    private JPanel controlPanel; // panel conteneur pour le bouton construire (toujours dans layeredPane)

    /** Constructeur de la classe Display, qui initialise les différentes vues et contrôleurs, et configure la fenêtre principale du jeu.
     * @param frame la fenêtre principale du jeu, créée dans la classe Main, pour laquelle on va configurer le contenu et les dimensions
     */
    public Display(JFrame frame) {
        this.frame = frame;
        this.newGame();
        Dimension gameSize = new Dimension(Camera.WIDTH * RATIO_X, Camera.HEIGHT * RATIO_Y);
        this.frame.setPreferredSize(gameSize);

        // LayeredPane pour pouvoir superposer les popups par dessus la vue globale
        this.layeredPane = new JLayeredPane();
        this.layeredPane.setPreferredSize(gameSize);

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
        selectionView.addMouseListener(this.selectionController);   // pour pouvoir selectionner les cases avec la souris
        selectionView.addKeyListener(this.cameraController); // pour pouvoir deplacer la camera meme en mode selection
        selectionView.addKeyListener(new CloseController(this)); // pour pouvoir fermer la vue selection avec Echap
        selectionView.setFocusable(true);
        // Par defaut, la vue selection est invisible, on l'affichera seulement quand on passera en mode selection
        layeredPane.add(selectionView, JLayeredPane.PALETTE_LAYER);   // au dessus de la vue globale, sous les popups
        selectionView.setVisible(false);

        // Contrôleur de placement des bâtiments
        BuildingManager buildingManager = new BuildingManager(world, this);
        globalView.addMouseListener(buildingManager);
        globalView.addMouseMotionListener(buildingManager);

        // Bouton "Construire" (visible par défaut)
        this.btnOpenMenu = ImageButtonFactory.createImageButton(
                "src/assets/UI/build_idle.png",
                "src/assets/UI/build_hover.png",
                "src/assets/UI/build_pressed.png"
        );
        this.btnOpenMenu.setFocusable(false);
        this.btnOpenMenu.setBounds(0, 0, 100, 100);

        // Control panel : conteneur fixe pour le bouton (évite les problèmes de parent/re-add)
        this.controlPanel = new JPanel(null);
        this.controlPanel.setOpaque(false);
        // position will be set after packing / when opening — set initial bounds now
        this.controlPanel.setBounds(gameSize.width - 160, 10, 100, 100);
        this.controlPanel.add(this.btnOpenMenu);

        // Panneau latéral droit : catalogue des bâtiments (plus grand et esthétique)
        BuildingSidePanel sidePanel = new BuildingSidePanel(buildingManager, this, this.world, null);
        // Définir le callback onClose : simplement ré-afficher le bouton et annuler le placement
        sidePanel.setOnClose(() -> {
            System.out.println("[DEBUG] sidePanel.onClose called");
            SwingUtilities.invokeLater(() -> {
                // Reposition controlPanel according to current content width
                int cw = Math.max(100, this.frame.getContentPane().getWidth());
                int ctrlX = Math.max(8, cw - 160);
                this.controlPanel.setBounds(ctrlX, 10, 100, 100);
                // Ensure controlPanel is on top layer and visible
                if (this.controlPanel.getParent() == null) {
                    this.layeredPane.add(this.controlPanel, JLayeredPane.DRAG_LAYER);
                }
                this.layeredPane.setLayer(this.controlPanel, JLayeredPane.DRAG_LAYER);
                this.controlPanel.setVisible(true);
                this.controlPanel.revalidate();
                this.controlPanel.repaint();
                buildingManager.cancelPlacement();
                sidePanel.setVisible(false);
                this.layeredPane.moveToFront(this.controlPanel);
                this.layeredPane.revalidate();
                this.layeredPane.repaint();
                this.controlPanel.requestFocusInWindow();
                globalView.requestFocusInWindow();
                System.out.println("[DEBUG] controlPanel visible=" + this.controlPanel.isVisible());
            });
        });
        int panelWidth = 380;
        // Position initial flush-right (will be adjusted on open using actual pane width)
        sidePanel.setBounds(gameSize.width - panelWidth, 0, panelWidth, gameSize.height);
        sidePanel.setVisible(false); // s'ouvre via le bouton
        layeredPane.add(sidePanel, JLayeredPane.PALETTE_LAYER);

        // Action du bouton "Construire" : afficher le panneau et cacher le bouton
        this.btnOpenMenu.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                // Compute current content width so the panel is flush-right (as before)
                int cw = Math.max(this.frame.getContentPane().getWidth(), gameSize.width);
                int panelX = Math.max(0, cw - panelWidth);
                sidePanel.setBounds(panelX, 0, panelWidth, gameSize.height);
                // Hide control panel (button) while the side panel is open
                this.controlPanel.setVisible(false);
                sidePanel.setVisible(true);
                // Bring panel to front
                this.layeredPane.moveToFront(sidePanel);
                this.layeredPane.revalidate();
                this.layeredPane.repaint();
                globalView.requestFocusInWindow();
            });
        });

        // Add controlPanel (contains the build button) on DRAG_LAYER so it remains above the side panel
        this.layeredPane.add(this.controlPanel, JLayeredPane.DRAG_LAYER);

        // Reposition controlPanel automatically when frame is shown/resized to avoid it being off-screen
        this.frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    int cw = Math.max(100, frame.getContentPane().getWidth());
                    int ctrlX = Math.max(8, cw - 160);
                    controlPanel.setBounds(ctrlX, 10, 100, 100);
                    layeredPane.revalidate();
                    layeredPane.repaint();
                });
            }

            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    int cw = Math.max(100, frame.getContentPane().getWidth());
                    int ctrlX = Math.max(8, cw - 160);
                    controlPanel.setBounds(ctrlX, 10, 100, 100);
                    layeredPane.revalidate();
                    layeredPane.repaint();
                });
            }
        });

        // On remet le LayeredPane comme fond principal
        this.frame.setContentPane(this.layeredPane);
        this.frame.pack();
        this.frame.setVisible(true);

        // Pour que la vue globale puisse bien recevoir les inputs au lancement du jeu
        globalView.requestFocusInWindow();

    }

    /**
     * Accès à la vue globale (Global) pour permettre de déclencher un highlight visuel depuis les contrôleurs.
     */
    public Global getGlobalView() {
        return this.globalView;
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
     * @param message le message a afficher dans la vue selection, par exemple "Selectionner une case plantable"
     * @param builder le builder d'action en cours
     * */
    public void switchToSelection(Predicate<Tile> selectionCriteria, String message, ActionBuilder builder) {
        popupView.hidePopup();   // si on vient d'un popup, le cacher
        selectionView.setMessage(message);   // indiquer a l'utilisateur ce qu'il doit selectionner
        selectionController.setSelectionCriteria(selectionCriteria);
        selectionController.setActionBuilder(builder);
        globalView.setVisible(false);
        selectionView.setVisible(true);
        selectionView.requestFocusInWindow(); // pour que la vue selection puisse recevoir les inputs apres le changement de vue
        // Vider la sélection précédente
        builder.clearTargets();
        // Activer l'écoute du clavier (Entrée)
        selectionView.removeKeyListener(selectionController);
        selectionView.addKeyListener(selectionController);
        selectionView.setVisible(true);
        selectionView.requestFocusInWindow(); // Indispensable pour capter la touche Entrée !
    }

    public Camera getCamera() {
        return camera;
    }

    public Selection getSelectionView() {
        return selectionView;
    }

    /** Repaint la fenetre */
    public void repaint() {
        frame.repaint();
        // hmm peut etre changer, a voir si on a besoin de tout repaint tout le temps
    }

    /** Called by BuildingSidePanel when it is closed (via X or Barn button).
     * Ensures the build control panel is shown and properly positioned. Runs on the EDT.
     */
    public void onBuildingPanelClose() {
        SwingUtilities.invokeLater(() -> {
            int cw = Math.max(100, this.frame.getContentPane().getWidth());
            int ctrlX = Math.max(8, cw - 160);
            this.controlPanel.setBounds(ctrlX, 10, 100, 100);
            if (this.controlPanel.getParent() == null) {
                this.layeredPane.add(this.controlPanel, JLayeredPane.DRAG_LAYER);
            }
            this.layeredPane.setLayer(this.controlPanel, JLayeredPane.DRAG_LAYER);
            this.controlPanel.setVisible(true);
            this.controlPanel.revalidate();
            this.controlPanel.repaint();
            this.layeredPane.moveToFront(this.controlPanel);
            this.frame.requestFocus();
        });
    }

}

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
import java.awt.event.*;
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
    private EdgeScroller edgeScroller; // délègue l'edge-scrolling à une classe dédiée

    /** Constructeur de la classe Display, qui initialise les différentes vues et contrôleurs, et configure la fenêtre principale du jeu.
     * @param frame la fenêtre principale du jeu, créée dans la classe Main, pour laquelle on va configurer le contenu et les dimensions
     */
    public Display(JFrame frame) {
        GameFonts.loadFonts();
        GameFonts.applyGlobalFont(GameFonts.MINECRAFT_FONT.deriveFont(14f));

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

        // ContainerListener pour surveiller l'ajout/suppression de composants sur le layeredPane
        this.layeredPane.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                Component c = e.getChild();
                // ajouter un HierarchyListener pour suivre visibility/showing changes
                c.addHierarchyListener(evt -> {
                    if ((evt.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                        refreshEdgeScrollerState();
                    }
                });
                refreshEdgeScrollerState();
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                refreshEdgeScrollerState();
            }
        });

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
        final BuildingManager buildingManager = new BuildingManager(world, this);
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

        // Control panel : conteneur fixe pour le(s) boutons (évite les problèmes de parent/re-add)
        this.controlPanel = new JPanel(null);
        this.controlPanel.setOpaque(false);
        // Ne pas considérer ce panneau comme une overlay qui désactive le edge-scrolling
        this.controlPanel.putClientProperty("edgeScrollIgnore", Boolean.TRUE);
        // position will be set after packing / when opening — set initial bounds now
        // élargir pour contenir deux boutons côte à côte
        int ctrlW = 200;
        this.controlPanel.setBounds(gameSize.width - ctrlW, 10, ctrlW, 100);
        this.controlPanel.add(this.btnOpenMenu);

        // Bouton Supprimer – état inactif (bulldozer rangé)
        JButton btnDeleteIdle = ImageButtonFactory.createImageButton(
                "src/assets/UI/bulldozer_idle.png",
                "src/assets/UI/bulldozer_idle_hover.png",
                "src/assets/UI/bulldozer_idle_pressed.png"
        );
        btnDeleteIdle.setFocusable(false);
        btnDeleteIdle.setBounds(105, 5, 90, 90);
        btnDeleteIdle.addActionListener(e -> buildingManager.startDeletionMode());
        this.controlPanel.add(btnDeleteIdle);

        // Bouton Supprimer – état actif (bulldozer en action)
        JButton btnDeleteActive = ImageButtonFactory.createImageButton(
                "src/assets/UI/bulldozer_active.png",
                "src/assets/UI/bulldozer_active_hover.png",
                "src/assets/UI/bulldozer_active_pressed.png"
        );
        btnDeleteActive.setFocusable(false);
        btnDeleteActive.setBounds(105, 5, 90, 90);
        btnDeleteActive.setVisible(false); // caché par défaut
        btnDeleteActive.addActionListener(e -> buildingManager.cancelDeletionMode());
        this.controlPanel.add(btnDeleteActive);

        // Basculer entre les deux images selon le mode suppression
        buildingManager.setDeletionModeListener(active -> SwingUtilities.invokeLater(() -> {
            btnDeleteIdle.setVisible(!active);
            btnDeleteActive.setVisible(active);
        }));

        // Panneau latéral droit : catalogue des bâtiments (plus grand et esthétique)
        BuildingSidePanel sidePanel = new BuildingSidePanel(buildingManager, this, this.world, null);
        // Définir le callback onClose : simplement ré-afficher le bouton et annuler le placement
        sidePanel.setOnClose(() -> SwingUtilities.invokeLater(() -> {
            // Repositionner et réafficher le controlPanel
            int cw = Math.max(100, this.frame.getContentPane().getWidth());
            this.controlPanel.setBounds(cw - 200, 10, 200, 100);
            this.layeredPane.setLayer(this.controlPanel, JLayeredPane.DRAG_LAYER);
            this.controlPanel.setVisible(true);
            this.controlPanel.setEnabled(true);
            this.layeredPane.moveToFront(this.controlPanel);
            buildingManager.cancelPlacement();
            sidePanel.setVisible(false);
            this.layeredPane.revalidate();
            this.layeredPane.repaint();
            globalView.requestFocusInWindow();
            overlayClosed();
        }));
        int panelWidth = 380;
        // Position initial flush-right (will be adjusted on open using actual pane width)
        sidePanel.setBounds(gameSize.width - panelWidth, 0, panelWidth, gameSize.height);
        sidePanel.setVisible(false); // s'ouvre via le bouton
        layeredPane.add(sidePanel, JLayeredPane.PALETTE_LAYER);

        // Keep edgeScroller in sync if sidePanel is shown/hidden by other code paths
        sidePanel.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (sidePanel.isShowing()) {
                    // an overlay (side panel) is visible -> notify scroller
                    overlayOpened(panelWidth, sidePanel.getBounds());
                } else {
                    overlayClosed();
                }
            }
        });

        // Action du bouton "Construire" : afficher le panneau et cacher le bouton
        this.btnOpenMenu.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            // Annuler le mode suppression s'il était actif (évite désynchronisation des boutons)
            buildingManager.cancelDeletionMode();
            // Cacher le panel de contrôle pendant que le sidePanel est ouvert
            this.controlPanel.setVisible(false);
            // Positionner le sidePanel flush-right selon la largeur courante
            int cw = Math.max(this.frame.getContentPane().getWidth(), gameSize.width);
            sidePanel.setBounds(cw - panelWidth, 0, panelWidth, gameSize.height);
            sidePanel.setVisible(true);
            this.layeredPane.moveToFront(sidePanel);
            this.layeredPane.revalidate();
            this.layeredPane.repaint();
            // Désactiver le scroll automatique
            overlayOpened(panelWidth, sidePanel.getBounds());
        }));

        // Add controlPanel (contains the build button) on DRAG_LAYER so it remains above the side panel
        this.layeredPane.add(this.controlPanel, JLayeredPane.DRAG_LAYER);

        // Reposition controlPanel automatically when frame is shown/resized to avoid it being off-screen
        this.frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    int cw = Math.max(100, frame.getContentPane().getWidth());
                    int ctrlX = Math.max(8, cw - 200);
                    controlPanel.setBounds(ctrlX, 10, 200, 100);
                    layeredPane.revalidate();
                    layeredPane.repaint();
                });
            }

            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    int cw = Math.max(100, frame.getContentPane().getWidth());
                    int ctrlX = Math.max(8, cw - 200);
                    controlPanel.setBounds(ctrlX, 10, 200, 100);
                    layeredPane.revalidate();
                    layeredPane.repaint();
                });
            }
        });

        // On remet le LayeredPane comme fond principal
        this.frame.setContentPane(this.layeredPane);

        // Nous packons et affichons la fenêtre avant de créer l'EdgeScroller
        this.frame.pack();
        this.frame.setVisible(true);

        // Création de l'edgeScroller APRÈS le pack/visible pour que les dimensions de globalView soient correctes
        // Utiliser une vitesse plus douce par défaut (0.12f) pour éviter un scrolling trop rapide en vue globale
        this.edgeScroller = new EdgeScroller(this.frame, this.layeredPane, this.camera, this.globalView,
                Rendering.FPS, 72, 0.12f);

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
        // Cacher les boutons de contrôle (construire / supprimer)
        controlPanel.setVisible(false);
        // Afficher le popup
        popupView.showPopup(popup);
        overlayOpened(0, null);
    }

    /** Met la vue en mode global */
    public void switchToGlobal() {
        popupView.hidePopup();
        selectionView.setVisible(false);
        globalView.setVisible(true);
        // Réactiver les contrôles de la vue globale si besoin
        if (!Arrays.asList(globalView.getMouseListeners()).contains(globalController)) {
            globalView.addMouseListener(globalController);
        }
        if (!Arrays.asList(globalView.getKeyListeners()).contains(cameraController)) {
            globalView.addKeyListener(cameraController);
        }
        // Restaurer les boutons de contrôle (construire / supprimer)
        controlPanel.setVisible(true);
        controlPanel.setEnabled(true);
        layeredPane.moveToFront(controlPanel);
        globalView.requestFocusInWindow();
        overlayClosed();
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
            int ctrlX = Math.max(8, cw - 200);
            this.controlPanel.setBounds(ctrlX, 10, 200, 100);
            if (this.controlPanel.getParent() == null) {
                this.layeredPane.add(this.controlPanel, JLayeredPane.DRAG_LAYER);
            }
            this.layeredPane.setLayer(this.controlPanel, JLayeredPane.DRAG_LAYER);
            this.controlPanel.setVisible(true);
            this.controlPanel.setEnabled(true);
            this.controlPanel.repaint();

            // On réinitialise le scrolling caméra
            overlayClosed();
            this.globalView.requestFocusInWindow();
        });
    }

    // L'edge-scrolling est exécuté dans un thread dédié (edgeScrollThread). La méthode update() n'est plus nécessaire
    public void update() {
        // Methode laissée pour compatibilité (ancienne Rendering) mais ne fait rien car le thread s'occupe du scrolling.
    }

    /** Met à jour l'état de l'EdgeScroller en fonction des composants visibles sur le layeredPane.
     * Appelée automatiquement lors de l'ajout ou de la suppression de composants, ainsi que lors des changements de visibilité.
     * */
    public void refreshEdgeScrollerState() {
        boolean hasVisibleOverlay = false;
        for (Component comp : layeredPane.getComponents()) {
            if (comp == globalView) continue;
            int layer = layeredPane.getLayer(comp);
            if (layer != JLayeredPane.DEFAULT_LAYER && comp.isVisible()) {
                hasVisibleOverlay = true;
                break;
            }
        }
        try {
            if (this.edgeScroller != null) {
                this.edgeScroller.setEnabled(!hasVisibleOverlay);
            }
        } catch (Exception ex) {}
    }

    /**
     * Notify the edge scroller that an overlay (popup, sidebar, etc.) is open.
     * @param rightSidebarWidth width of the right sidebar (0 if not a sidebar)
     * @param ignoredRegion region to ignore for edge scrolling (null if not applicable)
     */
    public void overlayOpened(int rightSidebarWidth, Rectangle ignoredRegion) {
        if (this.edgeScroller != null) {
            this.edgeScroller.setEnabled(false);
            this.edgeScroller.setRightSidebarWidth(rightSidebarWidth);
            this.edgeScroller.setIgnoredRegion(ignoredRegion);
        }
    }

    /**
     * Notify the edge scroller that overlays are closed and normal scrolling should resume.
     */
    public void overlayClosed() {
        if (this.edgeScroller != null) {
            this.edgeScroller.setIgnoredRegion(null);
            this.edgeScroller.setRightSidebarWidth(0);
            this.edgeScroller.setEnabled(true);
        }
    }
}

package src.view;

import src.control.popups.BuildingManager;
import src.control.CameraController;
import src.control.GlobalController;
import src.control.SelectionController;
import src.control.popups.CloseController;
import src.model.Camera;
import src.model.Gardener;
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
    private JLayeredPane layeredPane;
    private JButton btnOpenMenu;
    private JPanel controlPanel;
    private EdgeScroller edgeScroller;

    /** Constructeur de la classe Display, qui initialise les différentes vues et contrôleurs, et configure la fenêtre principale du jeu.
     * @param frame la fenêtre principale du jeu, créée dans la classe Main, pour laquelle on va configurer le contenu et les dimensions
     */
    public Display(JFrame frame) {
        GameFonts.loadFonts();
        GameFonts.applyGlobalFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(14f) : new Font("Arial", Font.PLAIN, 14));

        this.frame = frame;
        this.newGame();
        Dimension gameSize = new Dimension(Camera.WIDTH * RATIO_X, Camera.HEIGHT * RATIO_Y);
        //this.frame.setPreferredSize(gameSize);

        // LayeredPane pour pouvoir superposer les popups par dessus la vue globale
        this.layeredPane = new JLayeredPane();
        this.layeredPane.setPreferredSize(gameSize);

        // Vue globale
        this.globalView = new Global(this.world, this.camera);
        globalView.setPreferredSize(gameSize);
        globalView.setBounds(0, 0, gameSize.width, gameSize.height);

        this.globalController = new GlobalController(this, globalView, this.world);
        this.cameraController = new CameraController(camera, globalView);
        globalView.addKeyListener(this.cameraController);
        // Pour que la vue globale puisse bien recevoir les inputs
        globalView.setFocusable(true);
        globalView.requestFocusInWindow();

        layeredPane.add(globalView, JLayeredPane.DEFAULT_LAYER);

        // KEYBINDINGS POUR LA HOTBAR (Touches 1 à 4) ---
        InputMap im = globalView.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = globalView.getActionMap();

        for (int i = 1; i <= 4; i++) {
            final int slotIndex = i - 1;
            String keyStr = String.valueOf(i);

            // Touches numériques au-dessus des lettres
            im.put(KeyStroke.getKeyStroke(keyStr), "hotbar_" + i);
            // Pavé numérique (optionnel mais pratique)
            im.put(KeyStroke.getKeyStroke("NUMPAD" + i), "hotbar_" + i);

            am.put("hotbar_" + i, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Ne rien faire si la hotbar est masquée (menu ouvert)
                    if (!globalView.isHotbarVisible()) return;
                    Gardener g = world.getAvailableGardener();
                    if (g == null) return;
                    // Indicateur visuel sur le jardinier 0 (hotbar centrée sur le joueur 0)
                    if (!world.getGardeners().isEmpty()) {
                        world.getGardeners().get(0).setSelectedHotbarIndex(slotIndex);
                    }
                    globalView.repaint();
                    triggerHotbarAction(slotIndex, g);
                }
            });
        }
        // ------------------------------------------------------------

        this.popupView = new PopupView(globalView);
        this.popupView.setBounds(0, 0, gameSize.width, gameSize.height);
        this.popupView.setPreferredSize(gameSize);

        layeredPane.add(popupView, JLayeredPane.MODAL_LAYER);

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

        this.selectionController = new SelectionController(this, world);
        selectionView.addMouseListener(this.selectionController);
        selectionView.addMouseMotionListener(this.selectionController);
        selectionView.addKeyListener(this.cameraController);
        selectionView.addKeyListener(new CloseController(this));
        selectionView.setFocusable(true);
        layeredPane.add(selectionView, JLayeredPane.PALETTE_LAYER);
        selectionView.setVisible(false);

        final BuildingManager buildingManager = new BuildingManager(world, this);
        globalView.addMouseListener(buildingManager);
        globalView.addMouseMotionListener(buildingManager);

        this.btnOpenMenu = ImageButtonFactory.createImageButton(
                "src/assets/UI/build_idle.png",
                "src/assets/UI/build_hover.png",
                "src/assets/UI/build_pressed.png"
        );
        this.btnOpenMenu.setFocusable(false);
        this.btnOpenMenu.setBounds(0, 0, 100, 100);

        this.controlPanel = new JPanel(null);
        this.controlPanel.setOpaque(false);
        this.controlPanel.putClientProperty("edgeScrollIgnore", Boolean.TRUE);
        int ctrlW = 200;
        this.controlPanel.setBounds(gameSize.width - ctrlW, 10, ctrlW, 100);
        this.controlPanel.add(this.btnOpenMenu);

        JButton btnDeleteIdle = ImageButtonFactory.createImageButton(
                "src/assets/UI/bulldozer_idle.png",
                "src/assets/UI/bulldozer_idle_hover.png",
                "src/assets/UI/bulldozer_idle_pressed.png"
        );
        btnDeleteIdle.setFocusable(false);
        btnDeleteIdle.setBounds(105, 5, 90, 90);
        btnDeleteIdle.addActionListener(e -> buildingManager.startDeletionMode());
        this.controlPanel.add(btnDeleteIdle);

        JButton btnDeleteActive = ImageButtonFactory.createImageButton(
                "src/assets/UI/bulldozer_active.png",
                "src/assets/UI/bulldozer_active_hover.png",
                "src/assets/UI/bulldozer_active_pressed.png"
        );
        btnDeleteActive.setFocusable(false);
        btnDeleteActive.setBounds(105, 5, 90, 90);
        btnDeleteActive.setVisible(false);
        btnDeleteActive.addActionListener(e -> buildingManager.cancelDeletionMode());
        this.controlPanel.add(btnDeleteActive);

        buildingManager.setDeletionModeListener(active -> {
            // Synchrone (pas d'invokeLater) : évite que setHotbarVisible(true) arrive après
            // les appels directs à setHotbarVisible(false) lors du passage construction↔destruction
            btnDeleteIdle.setVisible(!active);
            btnDeleteActive.setVisible(active);
            globalView.setHotbarVisible(!active);
        });

        BuildingSidePanel sidePanel = new BuildingSidePanel(buildingManager, this, this.world, null);
        sidePanel.setOnClose(() -> SwingUtilities.invokeLater(() -> {
            buildingManager.cancelPlacement();
        }));
        int panelWidth = 380;
        sidePanel.setBounds(gameSize.width - panelWidth, 0, panelWidth, gameSize.height);
        sidePanel.setVisible(false);
        layeredPane.add(sidePanel, JLayeredPane.PALETTE_LAYER);

        sidePanel.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (sidePanel.isVisible()) {
                    globalView.setHotbarVisible(false);
                    overlayOpened(panelWidth, sidePanel.getBounds());
                } else {
                    globalView.setHotbarVisible(true);
                    overlayClosed();
                }
            }
        });

        this.btnOpenMenu.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            buildingManager.cancelDeletionMode();
            this.controlPanel.setVisible(false);
            int cw = Math.max(this.frame.getContentPane().getWidth(), gameSize.width);
            sidePanel.setBounds(cw - panelWidth, 0, panelWidth, gameSize.height);
            globalView.setHotbarVisible(false);
            sidePanel.setVisible(true);
            this.layeredPane.moveToFront(sidePanel);
            this.layeredPane.revalidate();
            this.layeredPane.repaint();
        }));

        // KEYBINDING ESCAPE : ferme le panel construction ou annule le mode destruction
        // (placé ici car sidePanel et buildingManager sont maintenant en scope)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape_overlay");
        am.put("escape_overlay", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Ignoré si on est en mode sélection (géré par SelectionController/CloseController)
                if (selectionView.isVisible()) return;
                if (sidePanel.isVisible()) {
                    sidePanel.setVisible(false);
                    onBuildingPanelClose();
                    buildingManager.cancelPlacement();
                } else if (buildingManager.isDeletionMode()) {
                    buildingManager.cancelDeletionMode();
                }
            }
        });

        this.layeredPane.add(this.controlPanel, JLayeredPane.DRAG_LAYER);

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

        this.frame.setContentPane(this.layeredPane);
        this.frame.pack();

        this.frame.setLocationRelativeTo(null);
        this.frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        this.frame.setVisible(true);

        this.edgeScroller = new EdgeScroller(this.frame, this.layeredPane, this.camera, this.globalView,
                Rendering.FPS, 72, 0.12f);

        globalView.requestFocusInWindow();

        // Callback level-up : affiche le popup de félicitations
        this.world.setLevelUpCallback(newLevel -> SwingUtilities.invokeLater(() ->
            LevelUpPopup.show(this.globalView, this.world, newLevel)
        ));
    }

    public Global getGlobalView() {
        return this.globalView;
    }

    private void newGame() {
        this.world = new World();
        this.camera = new Camera();
    }

    public void switchToPopup(PopupPanel popup) {
        globalView.removeMouseListener(globalController);
        globalView.removeKeyListener(cameraController);
        controlPanel.setVisible(false);
        popupView.showPopup(popup);
        overlayOpened(0, null);
        globalView.setHotbarVisible(false);
    }

    public void switchToGlobal() {
        popupView.hidePopup();
        selectionView.setVisible(false);
        globalView.setVisible(true);
        // Effacer toutes les surbrillances jaunes résiduelles
        globalView.clearAllHighlights();
        // Désélectionner le slot hotbar
        if (world.getGardeners() != null && !world.getGardeners().isEmpty()) {
            world.getGardeners().get(0).setSelectedHotbarIndex(-1);
        }
        if (!Arrays.asList(globalView.getMouseListeners()).contains(globalController)) {
            globalView.addMouseListener(globalController);
        }
        if (!Arrays.asList(globalView.getKeyListeners()).contains(cameraController)) {
            globalView.addKeyListener(cameraController);
        }
        controlPanel.setVisible(true);
        controlPanel.setEnabled(true);
        layeredPane.moveToFront(controlPanel);
        globalView.requestFocusInWindow();
        globalView.setHotbarVisible(true);
        overlayClosed();
    }

    public void switchToSelection(Predicate<Tile> selectionCriteria, String message, ActionBuilder builder) {
        popupView.hidePopup();
        selectionView.setMessage(message);
        selectionController.setSelectionCriteria(selectionCriteria);
        selectionController.setActionBuilder(builder);
        globalView.setHotbarVisible(false); // bloque les touches hotbar pendant la sélection
        controlPanel.setVisible(false);     // désactive les boutons build/destroy
        globalView.setVisible(false);
        selectionView.setVisible(true);
        selectionView.requestFocusInWindow();
        builder.clearTargets();
        selectionView.removeKeyListener(selectionController);
        selectionView.addKeyListener(selectionController);
        selectionView.setVisible(true);
        selectionView.requestFocusInWindow();
    }

    public Camera getCamera() {
        return camera;
    }

    public Selection getSelectionView() {
        return selectionView;
    }

    public void repaint() {
        frame.repaint();
    }

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
            globalView.setHotbarVisible(true);
            this.globalView.requestFocusInWindow();
        });
    }

    public void update() { }

    /** Déclenche l'action correspondant à un slot de hotbar */
    public void triggerHotbarAction(int slotIndex, Gardener gardener) {
        java.awt.event.ActionEvent fakeEvent = new java.awt.event.ActionEvent(this, java.awt.event.ActionEvent.ACTION_PERFORMED, "");
        if (slotIndex == 0) {
            new src.control.popups.PlowActionSelector(this, world, gardener).actionPerformed(fakeEvent);
        } else if (slotIndex == 1) {
            new src.control.popups.WaterActionSelector(this, world, gardener).actionPerformed(fakeEvent);
        } else if (slotIndex == 2) {
            new src.control.popups.PlantActionSelector(this, world, gardener).actionPerformed(fakeEvent);
        } else if (slotIndex == 3) {
            new src.control.popups.HarvestActionSelector(this, gardener, world).actionPerformed(fakeEvent);
        }
    }

    public void refreshEdgeScrollerState() {
        boolean hasVisibleOverlay = false;
        for (Component comp : layeredPane.getComponents()) {
            // Ignorer la vue principale
            if (comp == globalView) continue;
            // popupView est toujours présent sur MODAL_LAYER : ne compte comme overlay
            // que si un popup est effectivement affiché (au moins un enfant)
            if (comp == popupView) {
                if (popupView.isVisible() && popupView.getComponentCount() > 0) {
                    hasVisibleOverlay = true;
                }
                continue;
            }
            // Ignorer les composants marqués edgeScrollIgnore (ex: controlPanel)
            if (comp instanceof JComponent) {
                Object ignore = ((JComponent) comp).getClientProperty("edgeScrollIgnore");
                if (Boolean.TRUE.equals(ignore)) continue;
            }
            int layer = layeredPane.getLayer(comp);
            if (layer != JLayeredPane.DEFAULT_LAYER && comp.isVisible()) {
                hasVisibleOverlay = true;
                break;
            }
        }
        if (this.edgeScroller != null) {
            this.edgeScroller.setEnabled(!hasVisibleOverlay);
        }
    }

    public void overlayOpened(int rightSidebarWidth, Rectangle ignoredRegion) {
        if (this.edgeScroller != null) {
            this.edgeScroller.setEnabled(false);
            this.edgeScroller.setRightSidebarWidth(rightSidebarWidth);
            this.edgeScroller.setIgnoredRegion(ignoredRegion);
        }
    }

    public void overlayClosed() {
        if (this.edgeScroller != null) {
            this.edgeScroller.setIgnoredRegion(null);
            this.edgeScroller.setRightSidebarWidth(0);
            this.edgeScroller.setEnabled(true);
        }
    }
}
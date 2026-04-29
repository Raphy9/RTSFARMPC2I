package src.view;

import src.control.popups.BuildingManager;
import src.control.popups.QuestMenuController;
import src.control.CameraController;
import src.control.GlobalController;
import src.control.SelectionController;
import src.control.popups.CloseController;
import src.model.*;
import src.model.actions.ActionBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import java.util.function.Predicate;

/** Classe principale de la vue, qui gere les differentes vues (globale, popup, selection) et les interactions entre elles.
 * C'est la classe centrale pour le rendu et l'affichage du jeu, elle contient les references vers les autres vues et les controleurs.
 * C'est aussi la classe qui gere le changement de vue (par exemple passer de la vue globale a une vue popup ou de selection) et qui permet aux controleurs de declencher des changements de vue.
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
    private JButton btnOpenQuestMenu;
    private JPanel controlPanel;
    private EdgeScroller edgeScroller;
    private BuildingManager buildingManager;
    private BuildingSidePanel buildingSidePanel;
    private QuestSidePanel questSidePanel;
    private QuestMenuController questMenuController;
    private final Quests quests;
    private final FloatingTextManager floatingTextManager;
    private String currentSaveName = null;
    private boolean wasQuestPanelOpen = false;
    private Runnable onReturnToMenuCallback;

    private static final int CONTROL_PANEL_WIDTH = 90;
    private static final int CONTROL_PANEL_HEIGHT = 280; // HUD_BUTTON_SIZE * 3 + 10
    private static final int RIGHT_PANEL_WIDTH = 380;
    private static final int HUD_MARGIN = 10;
    private static final int HUD_BUTTON_SIZE = 90;
    private static final int HUD_PANEL_HEIGHT = 100;

    /** Constructeur de la classe Display, qui initialise les différentes vues et controleurs, et configure la fenetre principale du jeu.
     * @param frame la fenetre principale du jeu, créée dans la classe Main, pour laquelle on va configurer le contenu et les dimensions
     */
    public Display(JFrame frame) {
        GameFonts.loadFonts();
        GameFonts.applyGlobalFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(14f) : new Font("Arial", Font.PLAIN, 14));

        this.frame = frame;
        this.newGame();
        this.quests = this.world.getQuests();
        Dimension gameSize = new Dimension(Camera.WIDTH * RATIO_X, Camera.HEIGHT * RATIO_Y);
        this.frame.setPreferredSize(gameSize);
        final int baseGameWidth = gameSize.width;
        final int baseGameHeight = gameSize.height;

        // LayeredPane pour pouvoir superposer les popups par dessus la vue globale
        this.layeredPane = new JLayeredPane();
        this.layeredPane.setPreferredSize(gameSize);

        // Vue globale
        this.globalView = new Global(this.world, this.camera);
        globalView.setPreferredSize(gameSize);
        globalView.setBounds(0, 0, gameSize.width, gameSize.height);

        this.floatingTextManager = new FloatingTextManager();
        this.floatingTextManager.setRepaintCallback(globalView::repaint);
        this.floatingTextManager.start();
        this.globalView.setFloatingTextManager(this.floatingTextManager);

        this.world.getStats().setMoneyGainCallback(amount -> showMoneyText(amount, 150, 28));
        this.world.getStats().setExpGainCallback(amount -> showExpText(amount, 150, 56));

        this.globalController = new GlobalController(this, globalView, this.world);
        this.cameraController = new CameraController(camera, globalView);
        globalView.addKeyListener(this.cameraController);
        // Pour que la vue globale puisse bien recevoir les inputs
        globalView.setFocusable(true);
        globalView.requestFocusInWindow();

        layeredPane.add(globalView, JLayeredPane.DEFAULT_LAYER);

        // KEYBINDINGS POUR LA HOTBAR (Touches 1 a 4) ---
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

                    // Ne rien faire si le slot est inactif
                    if (!Tutorial.isHotbarSlotActive(slotIndex)) return;

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

        this.buildingManager = new BuildingManager(world, this);
        final BuildingManager buildingManager = this.buildingManager;
        globalView.addMouseListener(buildingManager);
        globalView.addMouseMotionListener(buildingManager);

        this.buildingSidePanel = new BuildingSidePanel(buildingManager, this, this.world, null);
        this.buildingSidePanel.setOnClose(() -> SwingUtilities.invokeLater(() -> buildingManager.cancelPlacement()));
        this.buildingSidePanel.setBounds(gameSize.width - RIGHT_PANEL_WIDTH, 0, RIGHT_PANEL_WIDTH, gameSize.height);
        this.buildingSidePanel.setVisible(false);
        layeredPane.add(this.buildingSidePanel, JLayeredPane.PALETTE_LAYER);
        buildingManager.setOnPlacementComplete(this.buildingSidePanel::refresh);

        this.questSidePanel = new QuestSidePanel(this.quests);
        this.questSidePanel.putClientProperty("edgeScrollIgnore", Boolean.TRUE);
        Rectangle initialQuestBounds = computeQuestOverlayBounds(gameSize.width, gameSize.height);
        this.questSidePanel.setBounds(initialQuestBounds);
        this.questSidePanel.setVisible(false);
        layeredPane.add(this.questSidePanel, JLayeredPane.PALETTE_LAYER);


        this.btnOpenMenu = ImageButtonFactory.createImageButton(
                "src/assets/UI/build_idle.png",
                "src/assets/UI/build_hover.png",
                "src/assets/UI/build_pressed.png"
        );
        this.btnOpenMenu.setFocusable(false);
        this.btnOpenMenu.setBounds(0, 0, HUD_BUTTON_SIZE, HUD_BUTTON_SIZE);

        File questButtonImage = new File("src/assets/UI/quetes.png");
        if (questButtonImage.exists()) {
            this.btnOpenQuestMenu = ImageButtonFactory.createImageButton(
                    "src/assets/UI/quetes.png",
                    "src/assets/UI/quetes.png",
                    "src/assets/UI/quetes.png"
            );
            this.btnOpenQuestMenu.setFocusable(false);
            this.btnOpenQuestMenu.setBounds(0, 0, HUD_BUTTON_SIZE, HUD_BUTTON_SIZE);
            this.btnOpenQuestMenu.setOpaque(false);
            this.btnOpenQuestMenu.setContentAreaFilled(false);
            this.btnOpenQuestMenu.setBorderPainted(false);
        } else {
            // Fallback visuel en attendant l'image quetes.png.
            this.btnOpenQuestMenu = new JButton("Quetes");
            this.btnOpenQuestMenu.setFocusable(false);
            this.btnOpenQuestMenu.setFont(GameFonts.MINECRAFT_FONT != null
                    ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 14f)
                    : new Font("Arial", Font.BOLD, 14));
            this.btnOpenQuestMenu.setBackground(new Color(235, 185, 120));
            this.btnOpenQuestMenu.setForeground(new Color(75, 35, 10));
            this.btnOpenQuestMenu.setBorder(BorderFactory.createLineBorder(new Color(110, 45, 15), 2));
            this.btnOpenQuestMenu.setBounds(0, 0, HUD_BUTTON_SIZE, HUD_BUTTON_SIZE);
            this.btnOpenQuestMenu.setOpaque(true);
        }

        this.controlPanel = new JPanel(null);
        this.controlPanel.setOpaque(false);
        this.controlPanel.putClientProperty("edgeScrollIgnore", Boolean.TRUE);
        this.controlPanel.setBounds(gameSize.width - CONTROL_PANEL_WIDTH - HUD_MARGIN, gameSize.height / 2 - CONTROL_PANEL_HEIGHT / 2, CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT);
        this.controlPanel.add(this.btnOpenMenu);

        this.questMenuController = new QuestMenuController(this);
        this.questMenuController.bind();

        JButton btnDeleteIdle = ImageButtonFactory.createImageButton(
                "src/assets/UI/bulldozer_idle.png",
                "src/assets/UI/bulldozer_idle_hover.png",
                "src/assets/UI/bulldozer_idle_pressed.png"
        );
        btnDeleteIdle.setFocusable(false);
        btnDeleteIdle.setBounds(0, HUD_BUTTON_SIZE + 5, HUD_BUTTON_SIZE, HUD_BUTTON_SIZE);
        btnDeleteIdle.addActionListener(e -> {
            buildingManager.setOnPlacementComplete(this.buildingSidePanel::refresh);
            buildingManager.startDeletionMode();
        });
        this.controlPanel.add(btnDeleteIdle);

        JButton btnDeleteActive = ImageButtonFactory.createImageButton(
                "src/assets/UI/bulldozer_active.png",
                "src/assets/UI/bulldozer_active_hover.png",
                "src/assets/UI/bulldozer_active_pressed.png"
        );
        btnDeleteActive.setFocusable(false);
        btnDeleteActive.setBounds(0, HUD_BUTTON_SIZE + 5, HUD_BUTTON_SIZE, HUD_BUTTON_SIZE);
        btnDeleteActive.setVisible(false);
        btnDeleteActive.addActionListener(e -> buildingManager.cancelDeletionMode());
        this.controlPanel.add(btnDeleteActive);

        this.btnOpenQuestMenu.setBounds(0, (HUD_BUTTON_SIZE + 5) * 2, HUD_BUTTON_SIZE, HUD_BUTTON_SIZE);
        this.controlPanel.add(this.btnOpenQuestMenu);

        buildingManager.setDeletionModeListener(active -> {
            // Synchrone (pas d'invokeLater) : évite que setHotbarVisible(true) arrive apres
            // les appels directs a setHotbarVisible(false) lors du passage construction↔destruction
            if (active) {
                rememberQuestPanelStateBeforeTransientAction();
                setQuestPanelVisible(false);
            } else {
                restoreQuestPanelIfNeeded();
            }
            btnDeleteIdle.setVisible(!active);
            btnDeleteActive.setVisible(active);
            globalView.setHotbarVisible(!active);
        });



        this.buildingSidePanel.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.buildingSidePanel.isVisible()) {
                    rememberQuestPanelStateBeforeTransientAction();
                    setQuestPanelVisible(false);
                    globalView.setHotbarVisible(false);
                    overlayOpened(RIGHT_PANEL_WIDTH);
                } else {
                    globalView.setHotbarVisible(true);
                    overlayClosed();
                }
            }
        });

        this.questSidePanel.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.questSidePanel.isVisible()) {
                    this.buildingSidePanel.setVisible(false);
                }
            }
        });

        this.btnOpenMenu.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            buildingManager.cancelDeletionMode();
            rememberQuestPanelStateBeforeTransientAction();
            setQuestPanelVisible(false);
            this.controlPanel.setVisible(false);
            int cw = Math.max(this.frame.getContentPane().getWidth(), baseGameWidth);
            this.buildingSidePanel.setBounds(cw - RIGHT_PANEL_WIDTH, 0, RIGHT_PANEL_WIDTH, baseGameHeight);
            globalView.setHotbarVisible(false);
            this.buildingSidePanel.refresh();
            this.buildingSidePanel.setVisible(true);
            this.layeredPane.moveToFront(this.buildingSidePanel);
            this.layeredPane.revalidate();
            this.layeredPane.repaint();
        }));

        // KEYBINDING ESCAPE : ferme le panel construction ou annule le mode destruction
        // (placé ici car sidePanel et buildingManager sont maintenant en scope)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape_overlay");
        am.put("escape_overlay", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectionView.isVisible()) return;

                // NOUVEAU : Si un popup est DÉJA ouvert (inventaire, grange, pause...), on le ferme
                if (popupView.isVisible() && popupView.getComponentCount() > 0) {
                    switchToGlobal();
                    return;
                }

                if (buildingSidePanel.isVisible()) {
                    buildingSidePanel.setVisible(false);
                    onBuildingPanelClose();
                    buildingManager.cancelPlacement();
                } else if (buildingManager.isDeletionMode()) {
                    buildingManager.cancelDeletionMode();
                } else if (questSidePanel.isVisible()) {
                    questSidePanel.setVisible(false);
                    onQuestPanelClose();
                } else if (!selectionView.hasFocus()) {
                    // NOUVEAU : Si rien n'est ouvert, Echap ouvre le menu Pause !
                    switchToPopup(new PauseMenuPopup(Display.this));
                }
            }
        });

        this.layeredPane.add(this.controlPanel, JLayeredPane.DRAG_LAYER);

        this.frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    int cw = Math.max(100, frame.getContentPane().getWidth());
                    int ch = Math.max(100, frame.getContentPane().getHeight());
                    int ctrlX = Math.max(8, cw - CONTROL_PANEL_WIDTH - HUD_MARGIN);
                    int ctrlY = Math.max(8, ch / 2 - CONTROL_PANEL_HEIGHT / 2);
                    if (controlPanel != null) {
                        controlPanel.setBounds(ctrlX, ctrlY, CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT);
                    }
                    if (buildingSidePanel != null && buildingSidePanel.isVisible()) {
                        buildingSidePanel.setBounds(cw - RIGHT_PANEL_WIDTH, 0, RIGHT_PANEL_WIDTH, frame.getContentPane().getHeight());
                    }
                    if (questSidePanel != null && questSidePanel.isVisible()) {
                        questSidePanel.setBounds(computeQuestOverlayBounds(cw, ch));
                    }
                    layeredPane.revalidate();
                    layeredPane.repaint();
                });
            }

            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    int cw = Math.max(100, frame.getContentPane().getWidth());
                    int ch = Math.max(100, frame.getContentPane().getHeight());
                    int ctrlX = Math.max(8, cw - CONTROL_PANEL_WIDTH - HUD_MARGIN);
                    int ctrlY = Math.max(8, ch / 2 - CONTROL_PANEL_HEIGHT / 2);
                    if (controlPanel != null) {
                        controlPanel.setBounds(ctrlX, ctrlY, CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT);
                    }
                    if (buildingSidePanel != null && buildingSidePanel.isVisible()) {
                        buildingSidePanel.setBounds(cw - RIGHT_PANEL_WIDTH, 0, RIGHT_PANEL_WIDTH, frame.getContentPane().getHeight());
                    }
                    if (questSidePanel != null && questSidePanel.isVisible()) {
                        questSidePanel.setBounds(computeQuestOverlayBounds(cw, ch));
                    }
                    layeredPane.revalidate();
                    layeredPane.repaint();
                });
            }
        });

        this.frame.setContentPane(this.layeredPane);
        this.frame.pack();
        enforceExactContentSize(gameSize);
        this.frame.setLocationRelativeTo(null);

        this.frame.setVisible(true);

        // Ajuste une fois apres affichage pour éviter tout dépassement visuel initial des boutons.
        SwingUtilities.invokeLater(this::layoutHudButtons);

        this.edgeScroller = new EdgeScroller(this.frame, this.layeredPane, this.camera, this.globalView,
                Rendering.FPS, 72, 0.12f);

        globalView.requestFocusInWindow();

        // Callback level-up : affiche le popup de félicitations
        this.world.setLevelUpCallback(newLevel -> SwingUtilities.invokeLater(() ->
            {
                SoundManager.playSound(SoundManager.LEVEL_UP);
                LevelUpPopup.show(this.globalView, this.world, newLevel);
            }
        ));
    }

    public Global getGlobalView() {
        return this.globalView;
    }

    public FloatingTextManager getFloatingTextManager() {
        return this.floatingTextManager;
    }

    public void showFloatingText(String text, int screenX, int screenY, Color color) {
        this.floatingTextManager.addText(text, screenX, screenY, color);
    }

    public void showMoneyText(int amount, int screenX, int screenY) {
        this.floatingTextManager.addMoney(amount, screenX, screenY);
    }

    public void showExpText(int amount, int screenX, int screenY) {
        this.floatingTextManager.addExp(amount, screenX, screenY);
    }

    public void showQuestRewardText(int money, int exp) {
        if (money > 0) {
            showMoneyText(money, 150, 28);
        }
        if (exp > 0) {
            showExpText(exp, 150, 56);
        }
    }

    public Point worldToScreen(int worldX, int worldY) {
        int fstTileX = (int) camera.getX();
        int fstTileY = (int) camera.getY();
        int pixelDiffX = (int) ((camera.getX() - fstTileX) * RATIO_X);
        int pixelDiffY = (int) ((camera.getY() - fstTileY) * RATIO_Y);

        int relX = worldX - fstTileX;
        int relY = worldY - fstTileY;
        int screenX = (relX * RATIO_X) - pixelDiffX;
        int screenY = (relY * RATIO_Y) - pixelDiffY;
        return new Point(screenX, screenY);
    }

    public void showFloatingTextWorld(String text, int worldX, int worldY, Color color) {
        Point p = worldToScreen(worldX, worldY);
        int x = p.x + (RATIO_X / 2);
        int y = p.y + (RATIO_Y / 3);
        showFloatingText(text, x, y, color);
    }

    public void showMoneyTextWorld(int amount, int worldX, int worldY) {
        Point p = worldToScreen(worldX, worldY);
        int x = p.x + (RATIO_X / 2);
        int y = p.y + (RATIO_Y / 3);
        showMoneyText(amount, x, y);
    }

    public void showExpTextWorld(int amount, int worldX, int worldY) {
        Point p = worldToScreen(worldX, worldY);
        int x = p.x + (RATIO_X / 2);
        int y = p.y + (RATIO_Y / 3);
        showExpText(amount, x, y);
    }

    public World getWorld() {
        return this.world;
    }

    public Quests getQuests() {
        return this.quests;
    }

    public void setQuestChangeCallback(Runnable callback) {
        this.world.setQuestChangeCallback(callback);
    }

    public void setCurrentSaveName(String saveName) {
        this.currentSaveName = saveName;
    }

    public String getCurrentSaveName() {
        return this.currentSaveName;
    }

    public JButton getQuestMenuButton() {
        return this.btnOpenQuestMenu;
    }

    public QuestSidePanel getQuestSidePanel() {
        return this.questSidePanel;
    }

    public BuildingSidePanel getBuildingSidePanel() {
        return this.buildingSidePanel;
    }

    public void refreshQuestPanel() {
        if (this.questMenuController != null) {
            this.questMenuController.refreshFromModel();
        }
    }

    public void showQuestPanel() {
        SwingUtilities.invokeLater(() -> {
            buildingManager.cancelDeletionMode();
            if (this.buildingSidePanel != null) {
                this.buildingSidePanel.setVisible(false);
            }
            int cw = Math.max(100, this.frame.getContentPane().getWidth());
            int ch = Math.max(100, this.frame.getContentPane().getHeight());
            this.questSidePanel.setBounds(computeQuestOverlayBounds(cw, ch));
            setQuestPanelVisible(true);
            this.layeredPane.moveToFront(this.questSidePanel);
            this.layeredPane.revalidate();
            this.layeredPane.repaint();
        });
    }

    public void hideQuestPanel() {
        SwingUtilities.invokeLater(() -> {
            setQuestPanelVisible(false);
            this.layeredPane.revalidate();
            this.layeredPane.repaint();
        });
    }

    public void saveGame() {
        if (this.currentSaveName != null) {
            SaveManager.saveGame(this.currentSaveName, this.world);
        }
    }

    private void newGame() {
        this.world = new World();
        this.camera = new Camera();
    }

    public void switchToPopup(PopupPanel popup) {
        globalView.removeMouseListener(globalController);
        globalView.removeKeyListener(cameraController);
        if (buildingSidePanel != null) buildingSidePanel.setVisible(false);
        rememberQuestPanelStateBeforeTransientAction();
        setQuestPanelVisible(false);
        controlPanel.setVisible(false);
        popupView.showPopup(popup);
        overlayOpened(0);
        globalView.setHotbarVisible(false);
    }

    public void switchToGlobal() {
        popupView.hidePopup();
        selectionView.setVisible(false);
        globalView.setVisible(true);
        if (buildingSidePanel != null) buildingSidePanel.setVisible(false);
        restoreQuestPanelIfNeeded();
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



    // Ajoute ce getter s'il n'existe pas
    public BuildingManager getBuildingManager() {
        return this.buildingManager;
    }

    public void switchToSelection(Predicate<Tile> selectionCriteria, String message, ActionBuilder builder) {

        popupView.hidePopup();
        selectionView.setMessage(message);selectionController.setSelectionCriteria(selectionCriteria);
        selectionController.setActionBuilder(builder);
        globalView.setHotbarVisible(false); // bloque les touches hotbar pendant la sélection
        rememberQuestPanelStateBeforeTransientAction();
        setQuestPanelVisible(false);
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
            int ch = Math.max(100, this.frame.getContentPane().getHeight());
            int ctrlX = Math.max(8, cw - CONTROL_PANEL_WIDTH - HUD_MARGIN);
            int ctrlY = Math.max(8, ch / 2 - CONTROL_PANEL_HEIGHT / 2);
            this.controlPanel.setBounds(ctrlX, ctrlY, CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT);
            if (this.controlPanel.getParent() == null) {
                this.layeredPane.add(this.controlPanel, JLayeredPane.DRAG_LAYER);
            }
            this.layeredPane.setLayer(this.controlPanel, JLayeredPane.DRAG_LAYER);
            this.controlPanel.setVisible(true);
            this.controlPanel.setEnabled(true);
            this.controlPanel.repaint();
            restoreQuestPanelIfNeeded();
            globalView.setHotbarVisible(true);
            this.globalView.requestFocusInWindow();
        });
    }

    public void onQuestPanelClose() {
        SwingUtilities.invokeLater(() -> {
            int cw = Math.max(100, this.frame.getContentPane().getWidth());
            int ch = Math.max(100, this.frame.getContentPane().getHeight());
            int ctrlX = Math.max(8, cw - CONTROL_PANEL_WIDTH - HUD_MARGIN);
            int ctrlY = Math.max(8, ch / 2 - CONTROL_PANEL_HEIGHT / 2);
            this.controlPanel.setBounds(ctrlX, ctrlY, CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT);
            if (this.controlPanel.getParent() == null) {
                this.layeredPane.add(this.controlPanel, JLayeredPane.DRAG_LAYER);
            }
            this.layeredPane.setLayer(this.controlPanel, JLayeredPane.DRAG_LAYER);
            this.controlPanel.setVisible(true);
            this.controlPanel.setEnabled(true);
            this.controlPanel.repaint();
            setQuestPanelVisible(false);
        });
    }

    /**
     * Le panneau quetes est un overlay d'information: largeur max 1/5 et hauteur max 2/3 de l'écran.
     */
    private Rectangle computeQuestOverlayBounds(int contentWidth, int contentHeight) {
        int width = Math.max(1, contentWidth / 5);
        int height = Math.max(1, (contentHeight * 2) / 3);

        // Le panneau quetes doit s'ouvrir a gauche.
        int y = Math.max(HUD_MARGIN, (contentHeight - height) / 2);
        return new Rectangle(HUD_MARGIN, y, width, height);
    }

    /** Place les boutons HUD en bas de l'écran sans qu'ils débordent. */
    private void layoutHudButtons() {
        int cw = Math.max(100, frame.getContentPane().getWidth());
        int ch = Math.max(100, frame.getContentPane().getHeight());
        int ctrlX = Math.max(HUD_MARGIN, cw - CONTROL_PANEL_WIDTH - HUD_MARGIN);
        int ctrlY = Math.max(8, ch / 2 - CONTROL_PANEL_HEIGHT / 2);

        if (controlPanel != null) {
            controlPanel.setBounds(ctrlX, ctrlY, CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT);
        }
    }

    public void update() { }

    /** Déclenche l'action correspondant a un slot de hotbar */
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

            // On met a jour les zones ignorées (boutons HUD + marges)
            java.util.List<Rectangle> ignoredRegions = new java.util.ArrayList<>();
            if (this.controlPanel != null && this.controlPanel.isVisible()) {
                Rectangle bounds = this.controlPanel.getBounds();
                bounds.grow(20, 20); // Marge pour ne pas scroller si on approche
                ignoredRegions.add(bounds);
            }
            this.edgeScroller.setIgnoredRegions(ignoredRegions);
        }
    }

    private void setQuestPanelVisible(boolean visible) {
        if (this.questSidePanel != null) {
            this.questSidePanel.setVisible(visible);
        }
        if (this.btnOpenQuestMenu != null) {
            this.btnOpenQuestMenu.setVisible(!visible);
        }
    }

    private void rememberQuestPanelStateBeforeTransientAction() {
        if (this.questSidePanel != null && this.questSidePanel.isVisible()) {
            this.wasQuestPanelOpen = true;
        }
    }

    private void restoreQuestPanelIfNeeded() {
        if (!this.wasQuestPanelOpen) {
            return;
        }
        if (this.buildingManager != null && this.buildingManager.isDeletionMode()) {
            return;
        }
        if (this.buildingSidePanel != null && this.buildingSidePanel.isVisible()) {
            return;
        }
        int cw = Math.max(100, this.frame.getContentPane().getWidth());
        int ch = Math.max(100, this.frame.getContentPane().getHeight());
        this.questSidePanel.setBounds(computeQuestOverlayBounds(cw, ch));
        setQuestPanelVisible(true);
        this.layeredPane.moveToFront(this.questSidePanel);
        this.layeredPane.revalidate();
        this.layeredPane.repaint();
        this.wasQuestPanelOpen = false;
    }

    public void overlayOpened(int rightSidebarWidth) {
        if (this.edgeScroller != null) {
            this.edgeScroller.setEnabled(false);
            this.edgeScroller.setRightSidebarWidth(rightSidebarWidth);
        }
    }

    public void overlayClosed() {
        if (this.edgeScroller != null) {
            this.edgeScroller.setRightSidebarWidth(0);
            this.edgeScroller.setEnabled(true);
        }
    }

    /**
     * Garantit une zone de contenu exacte (hors bordures systeme) pour éviter
     * les écarts de quelques pixels selon l'état précédent de la JFrame.
     */
    private void enforceExactContentSize(Dimension contentSize) {
        Insets insets = this.frame.getInsets();
        int outerW = contentSize.width + insets.left + insets.right;
        int outerH = contentSize.height + insets.top + insets.bottom;
        this.frame.setSize(outerW, outerH);
    }

    public void setReturnToMenuCallback(Runnable callback) {
        this.onReturnToMenuCallback = callback;
    }

    public void returnToMainMenu() {
        // 1. On sauvegarde l'état actuel
        saveGame();
        // 2. On arrete tous les threads des entités pour ne pas faire planter le jeu en arriere-plan
        world.stopWorld();
        // 3. On arrete le thread de la caméra
        if (this.edgeScroller != null) {
            this.edgeScroller.stop();
        }
        // 4. On prévient la classe Main de re-afficher le HomeScreenPanel
        if (onReturnToMenuCallback != null) {
            onReturnToMenuCallback.run();
        }
    }
}


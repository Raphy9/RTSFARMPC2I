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
    // --- CONSTANTES DE RENDU ---
    // Ratio multiplicateur pour transformer les coordonnées du modèle (tuiles) en pixels
    public static final int RATIO_X = 64;
    public static final int RATIO_Y = 64;

    // --- COMPOSANTS SWING ---
    private final JFrame frame;                   // Fenêtre principale du système
    private JLayeredPane layeredPane;             // Panneau permettant de superposer des composants (Z-order)
    private JPanel controlPanel;                  // HUD contenant les boutons d'action (Build/Quest)
    private JButton btnOpenMenu;                  // Bouton pour le shop de construction
    private JButton btnOpenQuestMenu;             // Bouton pour le journal de quêtes

    // --- MODÈLE ET CAMÉRA ---
    private World world;                          // Référence au modèle logique du monde
    private Camera camera;                        // Référence à la caméra gérant la vue

    // --- SOUS-VUES (Layers) ---
    private final Global globalView;              // Vue principale du terrain
    private final PopupView popupView;            // Couche dédiée aux fenêtres surgissantes
    private final Selection selectionView;        // Couche pour les modes de sélection (ciblage)
    private BuildingSidePanel buildingSidePanel;  // Panneau latéral de construction (Shop)
    private QuestSidePanel questSidePanel;        // Panneau latéral affichant les quêtes actives

    // --- CONTRÔLEURS ---
    private final GlobalController globalController;
    private final CameraController cameraController;
    private final SelectionController selectionController;
    private BuildingManager buildingManager;      // Gère le placement des bâtiments
    private QuestMenuController questMenuController;
    private EdgeScroller edgeScroller;            // Gère le défilement automatique aux bords de l'écran

    // --- GESTIONNAIRES ANNEXES ---
    private final Quests quests;                  // Référence aux quêtes
    private final FloatingTextManager floatingTextManager; // Gère les textes flottants (+XP, +PO)
    private String currentSaveName = null;        // Nom de la sauvegarde actuelle
    private boolean wasQuestPanelOpen = false;    // Mémorise l'état du panneau quêtes lors des transitions
    private Runnable onReturnToMenuCallback;      // Action lors du retour au menu principal

    // --- CONSTANTES DE MISE EN PAGE HUD ---
    private static final int CONTROL_PANEL_WIDTH = 90;
    private static final int CONTROL_PANEL_HEIGHT = 280;
    private static final int RIGHT_PANEL_WIDTH = 380;
    private static final int HUD_MARGIN = 10;
    private static final int HUD_BUTTON_SIZE = 90;
    private static final int HUD_PANEL_HEIGHT = 100;

    /** Constructeur de la classe Display, qui initialise les différentes vues et controleurs, et configure la fenetre principale du jeu.
     * @param frame la fenetre principale du jeu, créée dans la classe Main, pour laquelle on va configurer le contenu et les dimensions
     */
    public Display(JFrame frame) {
        // Initialisation des polices de caractères
        GameFonts.loadFonts();
        GameFonts.applyGlobalFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(14f) : new Font("Arial", Font.PLAIN, 14));

        this.frame = frame;
        this.newGame(); // Initialisation du modèle World et de la Caméra
        this.quests = this.world.getQuests();

        // Configuration de la taille de la fenêtre basée sur la grille du monde
        Dimension gameSize = new Dimension(Camera.WIDTH * RATIO_X, Camera.HEIGHT * RATIO_Y);
        this.frame.setPreferredSize(gameSize);
        final int baseGameWidth = gameSize.width;
        final int baseGameHeight = gameSize.height;

        // Configuration du LayeredPane (Z-Order : Jeu < Popups < Menus)
        this.layeredPane = new JLayeredPane();
        this.layeredPane.setPreferredSize(gameSize);

        // --- INITIALISATION VUE GLOBALE ---
        this.globalView = new Global(this.world, this.camera);
        globalView.setPreferredSize(gameSize);
        globalView.setBounds(0, 0, gameSize.width, gameSize.height);

        // Configuration des textes flottants (+XP, +PO)
        this.floatingTextManager = new FloatingTextManager();
        this.floatingTextManager.setRepaintCallback(globalView::repaint);
        this.floatingTextManager.start();
        this.globalView.setFloatingTextManager(this.floatingTextManager);

        // Liaison des statistiques vers l'UI (quand l'XP ou l'argent change)
        this.world.getStats().setMoneyGainCallback(amount -> showMoneyText(amount, 150, 28));
        this.world.getStats().setExpGainCallback(amount -> showExpText(amount, 150, 56));

        // Initialisation des contrôleurs principaux
        this.globalController = new GlobalController(this, globalView, this.world);
        this.cameraController = new CameraController(camera, globalView);
        globalView.addKeyListener(this.cameraController);
        globalView.setFocusable(true);
        globalView.requestFocusInWindow();

        layeredPane.add(globalView, JLayeredPane.DEFAULT_LAYER);

        // --- GESTION DE LA HOTBAR (Touches 1 à 4) ---
        InputMap im = globalView.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = globalView.getActionMap();

        for (int i = 1; i <= 4; i++) {
            final int slotIndex = i - 1;
            String keyStr = String.valueOf(i);

            // Mapping des touches numériques standard et pavé numérique
            im.put(KeyStroke.getKeyStroke(keyStr), "hotbar_" + i);
            im.put(KeyStroke.getKeyStroke("NUMPAD" + i), "hotbar_" + i);

            am.put("hotbar_" + i, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!globalView.isHotbarVisible()) return;
                    if (!Tutorial.isHotbarSlotActive(slotIndex)) return;

                    Gardener g = world.getAvailableGardener();
                    if (g == null) return;

                    // Mise à jour visuelle du slot sélectionné sur le premier jardinier
                    if (!world.getGardeners().isEmpty()) {
                        world.getGardeners().get(0).setSelectedHotbarIndex(slotIndex);
                    }
                    globalView.repaint();
                    triggerHotbarAction(slotIndex, g); // Déclenche l'action (Labourer/Arroser...)
                }
            });
        }

        // --- INITIALISATION VUE POPUP ---
        this.popupView = new PopupView(globalView);
        this.popupView.setBounds(0, 0, gameSize.width, gameSize.height);
        this.popupView.setPreferredSize(gameSize);
        layeredPane.add(popupView, JLayeredPane.MODAL_LAYER);

        // Listener pour rafraîchir l'EdgeScroller quand des popups s'affichent
        this.layeredPane.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                Component c = e.getChild();
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

        // --- INITIALISATION VUE SÉLECTION ---
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

        // --- INITIALISATION CONSTRUCTION ET SHOP ---
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

        // --- INITIALISATION SYSTÈME DE QUÊTES ---
        this.questSidePanel = new QuestSidePanel(this.quests);
        this.questSidePanel.putClientProperty("edgeScrollIgnore", Boolean.TRUE);
        Rectangle initialQuestBounds = computeQuestOverlayBounds(gameSize.width, gameSize.height);
        this.questSidePanel.setBounds(initialQuestBounds);
        this.questSidePanel.setVisible(false);
        layeredPane.add(this.questSidePanel, JLayeredPane.PALETTE_LAYER);

        // --- INITIALISATION DU HUD (ControlPanel) ---
        this.btnOpenMenu = ImageButtonFactory.createImageButton(
                "src/assets/UI/build_idle.png",
                "src/assets/UI/build_hover.png",
                "src/assets/UI/build_pressed.png"
        );
        this.btnOpenMenu.setFocusable(false);
        this.btnOpenMenu.setBounds(0, 0, HUD_BUTTON_SIZE, HUD_BUTTON_SIZE);

        // Chargement du bouton de quêtes (avec fallback si image manquante)
        File questButtonImage = new File("src/assets/UI/quetes.png");
        if (questButtonImage.exists()) {
            this.btnOpenQuestMenu = ImageButtonFactory.createImageButton(
                    "src/assets/UI/quetes.png", "src/assets/UI/quetes.png", "src/assets/UI/quetes.png"
            );
            this.btnOpenQuestMenu.setFocusable(false);
            this.btnOpenQuestMenu.setBounds(0, 0, HUD_BUTTON_SIZE, HUD_BUTTON_SIZE);
            this.btnOpenQuestMenu.setOpaque(false);
            this.btnOpenQuestMenu.setContentAreaFilled(false);
            this.btnOpenQuestMenu.setBorderPainted(false);
        } else {
            this.btnOpenQuestMenu = new JButton("Quetes");
            this.btnOpenQuestMenu.setFocusable(false);
            this.btnOpenQuestMenu.setBackground(new Color(235, 185, 120));
            this.btnOpenQuestMenu.setBounds(0, 0, HUD_BUTTON_SIZE, HUD_BUTTON_SIZE);
        }

        // Création du conteneur des boutons HUD
        this.controlPanel = new JPanel(null);
        this.controlPanel.setOpaque(false);
        this.controlPanel.putClientProperty("edgeScrollIgnore", Boolean.TRUE);
        this.controlPanel.setBounds(gameSize.width - CONTROL_PANEL_WIDTH - HUD_MARGIN, gameSize.height / 2 - CONTROL_PANEL_HEIGHT / 2, CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT);
        this.controlPanel.add(this.btnOpenMenu);

        this.questMenuController = new QuestMenuController(this);
        this.questMenuController.bind();

        // Bouton Destruction (Bulldozer)
        JButton btnDeleteIdle = ImageButtonFactory.createImageButton(
                "src/assets/UI/bulldozer_idle.png", "src/assets/UI/bulldozer_idle_hover.png", "src/assets/UI/bulldozer_idle_pressed.png"
        );
        btnDeleteIdle.setFocusable(false);
        btnDeleteIdle.setBounds(0, HUD_BUTTON_SIZE + 5, HUD_BUTTON_SIZE, HUD_BUTTON_SIZE);
        btnDeleteIdle.addActionListener(e -> {
            buildingManager.setOnPlacementComplete(this.buildingSidePanel::refresh);
            buildingManager.startDeletionMode();
        });
        this.controlPanel.add(btnDeleteIdle);

        // Bouton Destruction quand actif
        JButton btnDeleteActive = ImageButtonFactory.createImageButton(
                "src/assets/UI/bulldozer_active.png", "src/assets/UI/bulldozer_active_hover.png", "src/assets/UI/bulldozer_active_pressed.png"
        );
        btnDeleteActive.setFocusable(false);
        btnDeleteActive.setBounds(0, HUD_BUTTON_SIZE + 5, HUD_BUTTON_SIZE, HUD_BUTTON_SIZE);
        btnDeleteActive.setVisible(false);
        btnDeleteActive.addActionListener(e -> buildingManager.cancelDeletionMode());
        this.controlPanel.add(btnDeleteActive);

        this.btnOpenQuestMenu.setBounds(0, (HUD_BUTTON_SIZE + 5) * 2, HUD_BUTTON_SIZE, HUD_BUTTON_SIZE);
        this.controlPanel.add(this.btnOpenQuestMenu);

        // Listener pour synchroniser l'UI quand on passe en mode suppression
        buildingManager.setDeletionModeListener(active -> {
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

        // Gestion de l'affichage du panel de construction
        this.buildingSidePanel.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.buildingSidePanel.isVisible()) {
                    rememberQuestPanelStateBeforeTransientAction();
                    setQuestPanelVisible(false);
                    globalView.setHotbarVisible(false);
                    overlayOpened(RIGHT_PANEL_WIDTH); // Bloque le scroll caméra sur la droite
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

        // GESTION DE LA TOUCHE ECHAP (Menu Pause et fermeture de panels)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape_overlay");
        am.put("escape_overlay", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectionView.isVisible()) return;

                // Priorité 1 : Fermer les popups (Grange, Inventaire...)
                if (popupView.isVisible() && popupView.getComponentCount() > 0) {
                    switchToGlobal();
                    return;
                }

                // Priorité 2 : Fermer le shop ou annuler le placement
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
                    // Priorité 3 : Menu Pause si rien d'autre n'est ouvert
                    switchToPopup(new PauseMenuPopup(Display.this));
                }
            }
        });

        this.layeredPane.add(this.controlPanel, JLayeredPane.DRAG_LAYER);

        // Listener pour gérer le redimensionnement de la fenêtre (Responsive HUD)
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
        });

        this.frame.setContentPane(this.layeredPane);
        this.frame.pack();
        enforceExactContentSize(gameSize);
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);

        SwingUtilities.invokeLater(this::layoutHudButtons);

        // Initialisation de l'EdgeScroller (Caméra qui bouge quand souris au bord)
        this.edgeScroller = new EdgeScroller(this.frame, this.layeredPane, this.camera, this.globalView, Rendering.FPS, 72, 0.12f);

        globalView.requestFocusInWindow();

        // Notification visuelle lors du passage de niveau
        this.world.setLevelUpCallback(newLevel -> SwingUtilities.invokeLater(() ->
                {
                    SoundManager.playSound(SoundManager.LEVEL_UP);
                    LevelUpPopup.show(this.globalView, this.world, newLevel);
                }
        ));
    }

    // --- MÉTHODES D'AFFICHAGE DE TEXTES FLOTTANTS (Feedback) ---

    public Global getGlobalView() { return this.globalView; }

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
        if (money > 0) showMoneyText(money, 150, 28);
        if (exp > 0) showExpText(exp, 150, 56);
    }

    /** Convertit les coordonnées logiques du monde en pixels écran selon la caméra */
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

    // --- WRAPPERS POUR TEXTES FLOTTANTS DANS LE MONDE ---
    public void showFloatingTextWorld(String text, int worldX, int worldY, Color color) {
        Point p = worldToScreen(worldX, worldY);
        showFloatingText(text, p.x + (RATIO_X / 2), p.y + (RATIO_Y / 3), color);
    }

    public void showMoneyTextWorld(int amount, int worldX, int worldY) {
        Point p = worldToScreen(worldX, worldY);
        showMoneyText(amount, p.x + (RATIO_X / 2), p.y + (RATIO_Y / 3));
    }

    public void showExpTextWorld(int amount, int worldX, int worldY) {
        Point p = worldToScreen(worldX, worldY);
        showExpText(amount, p.x + (RATIO_X / 2), p.y + (RATIO_Y / 3));
    }

    // --- ACCESSEURS MODÈLE ---
    public World getWorld() { return this.world; }
    public Quests getQuests() { return this.quests; }
    public void setQuestChangeCallback(Runnable callback) { this.world.setQuestChangeCallback(callback); }
    public void setCurrentSaveName(String saveName) { this.currentSaveName = saveName; }
    public String getCurrentSaveName() { return this.currentSaveName; }

    // --- GESTION DES PANNEAUX (Quêtes et Construction) ---
    public JButton getQuestMenuButton() { return this.btnOpenQuestMenu; }
    public QuestSidePanel getQuestSidePanel() { return this.questSidePanel; }
    public BuildingSidePanel getBuildingSidePanel() { return this.buildingSidePanel; }

    public void refreshQuestPanel() {
        if (this.questMenuController != null) this.questMenuController.refreshFromModel();
    }

    /** Affiche le journal de quêtes et cache le shop si nécessaire */
    public void showQuestPanel() {
        SwingUtilities.invokeLater(() -> {
            buildingManager.cancelDeletionMode();
            if (this.buildingSidePanel != null) this.buildingSidePanel.setVisible(false);
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
        if (this.currentSaveName != null) SaveManager.saveGame(this.currentSaveName, this.world);
    }

    private void newGame() {
        this.world = new World();
        this.camera = new Camera();
    }

    // --- TRANSITIONS DE VUE ---

    /** Désactive le jeu pour afficher une fenêtre popup (Inventaire, Pause...) */
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

    /** Revient au jeu normal, restaure les listeners et le HUD */
    public void switchToGlobal() {
        popupView.hidePopup();
        selectionView.setVisible(false);
        globalView.setVisible(true);
        if (buildingSidePanel != null) buildingSidePanel.setVisible(false);
        restoreQuestPanelIfNeeded();
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

    public BuildingManager getBuildingManager() { return this.buildingManager; }

    /** Active le mode "sélection de cible" (ex: cliquer sur une tuile pour agir) */
    public void switchToSelection(Predicate<Tile> selectionCriteria, String message, ActionBuilder builder) {
        popupView.hidePopup();
        selectionView.setMessage(message);
        selectionController.setSelectionCriteria(selectionCriteria);
        selectionController.setActionBuilder(builder);
        globalView.setHotbarVisible(false);
        rememberQuestPanelStateBeforeTransientAction();
        setQuestPanelVisible(false);
        controlPanel.setVisible(false);
        globalView.setVisible(false);
        selectionView.setVisible(true);
        selectionView.requestFocusInWindow();
        builder.clearTargets();
        selectionView.removeKeyListener(selectionController);
        selectionView.addKeyListener(selectionController);
    }

    public Camera getCamera() { return camera; }
    public Selection getSelectionView() { return selectionView; }
    public void repaint() { frame.repaint(); }

    // --- GESTION HUD ET RÉ-AFFICHAGE ---

    public void onBuildingPanelClose() {
        SwingUtilities.invokeLater(() -> {
            restoreHUDLayout();
            restoreQuestPanelIfNeeded();
            globalView.setHotbarVisible(true);
            this.globalView.requestFocusInWindow();
        });
    }

    public void onQuestPanelClose() {
        SwingUtilities.invokeLater(() -> {
            restoreHUDLayout();
            setQuestPanelVisible(false);
        });
    }

    /** Repositionne le controlPanel au bon endroit après fermeture d'un panel */
    private void restoreHUDLayout() {
        int cw = Math.max(100, this.frame.getContentPane().getWidth());
        int ch = Math.max(100, this.frame.getContentPane().getHeight());
        int ctrlX = Math.max(8, cw - CONTROL_PANEL_WIDTH - HUD_MARGIN);
        int ctrlY = Math.max(8, ch / 2 - CONTROL_PANEL_HEIGHT / 2);
        this.controlPanel.setBounds(ctrlX, ctrlY, CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT);
        if (this.controlPanel.getParent() == null) this.layeredPane.add(this.controlPanel, JLayeredPane.DRAG_LAYER);
        this.layeredPane.setLayer(this.controlPanel, JLayeredPane.DRAG_LAYER);
        this.controlPanel.setVisible(true);
        this.controlPanel.setEnabled(true);
        this.controlPanel.repaint();
    }

    private Rectangle computeQuestOverlayBounds(int contentWidth, int contentHeight) {
        int width = Math.max(1, contentWidth / 5);
        int height = Math.max(1, (contentHeight * 2) / 3);
        int y = Math.max(HUD_MARGIN, (contentHeight - height) / 2);
        return new Rectangle(HUD_MARGIN, y, width, height);
    }

    private void layoutHudButtons() {
        int cw = Math.max(100, frame.getContentPane().getWidth());
        int ch = Math.max(100, frame.getContentPane().getHeight());
        int ctrlX = Math.max(HUD_MARGIN, cw - CONTROL_PANEL_WIDTH - HUD_MARGIN);
        int ctrlY = Math.max(8, ch / 2 - CONTROL_PANEL_HEIGHT / 2);
        if (controlPanel != null) controlPanel.setBounds(ctrlX, ctrlY, CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT);
    }

    /** Déclenche l'action correspondant à un slot de hotbar (1-4) */
    public void triggerHotbarAction(int slotIndex, Gardener gardener) {
        java.awt.event.ActionEvent fakeEvent = new java.awt.event.ActionEvent(this, java.awt.event.ActionEvent.ACTION_PERFORMED, "");
        if (slotIndex == 0) new src.control.popups.PlowActionSelector(this, world, gardener).actionPerformed(fakeEvent);
        else if (slotIndex == 1) new src.control.popups.WaterActionSelector(this, world, gardener).actionPerformed(fakeEvent);
        else if (slotIndex == 2) new src.control.popups.PlantActionSelector(this, world, gardener).actionPerformed(fakeEvent);
        else if (slotIndex == 3) new src.control.popups.HarvestActionSelector(this, gardener, world).actionPerformed(fakeEvent);
    }

    /** Analyse les couches pour savoir si l'EdgeScroller (scroll caméra) doit être actif ou non */
    public void refreshEdgeScrollerState() {
        boolean hasVisibleOverlay = false;
        for (Component comp : layeredPane.getComponents()) {
            if (comp == globalView) continue;
            if (comp == popupView) {
                if (popupView.isVisible() && popupView.getComponentCount() > 0) hasVisibleOverlay = true;
                continue;
            }
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
            java.util.List<Rectangle> ignoredRegions = new java.util.ArrayList<>();
            if (this.controlPanel != null && this.controlPanel.isVisible()) {
                Rectangle bounds = this.controlPanel.getBounds();
                bounds.grow(20, 20);
                ignoredRegions.add(bounds);
            }
            this.edgeScroller.setIgnoredRegions(ignoredRegions);
        }
    }

    // --- HELPERS ÉTAT HUD ---

    private void setQuestPanelVisible(boolean visible) {
        if (this.questSidePanel != null) this.questSidePanel.setVisible(visible);
        if (this.btnOpenQuestMenu != null) this.btnOpenQuestMenu.setVisible(!visible);
    }

    private void rememberQuestPanelStateBeforeTransientAction() {
        if (this.questSidePanel != null && this.questSidePanel.isVisible()) this.wasQuestPanelOpen = true;
    }

    private void restoreQuestPanelIfNeeded() {
        if (!this.wasQuestPanelOpen) return;
        if (this.buildingManager != null && this.buildingManager.isDeletionMode()) return;
        if (this.buildingSidePanel != null && this.buildingSidePanel.isVisible()) return;
        showQuestPanel();
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

    private void enforceExactContentSize(Dimension contentSize) {
        Insets insets = this.frame.getInsets();
        this.frame.setSize(contentSize.width + insets.left + insets.right, contentSize.height + insets.top + insets.bottom);
    }

    public void setReturnToMenuCallback(Runnable callback) { this.onReturnToMenuCallback = callback; }

    /** Arrête le jeu proprement et retourne au menu d'accueil */
    public void returnToMainMenu() {
        saveGame();
        world.stopWorld(); // Arrête les threads de l'IA (Poules, Corbeaux)
        if (this.edgeScroller != null) this.edgeScroller.stop();
        if (onReturnToMenuCallback != null) onReturnToMenuCallback.run();
    }
}
package src.view;

import src.model.*;
import src.model.buildings.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.*;
import java.util.Set;

/**
 * Vue principale du jeu (le terrain).
 * Gère le rendu des tuiles, des bâtiments, des entités et de l'interface (hotbar, stats).
 */
public class Global extends JPanel {
    private final World world;
    private final Camera camera;

    // --- Chargeurs de Sprites ---
    private final SpriteSheetLoader gardenerLoader;
    private final long startTime; // Temps de référence pour synchroniser les animations
    private ChickenSpriteSheetLoader chickenLoader;
    private CrowSpriteSheetLoader crowLoader;

    // --- Assets Graphiques UI ---
    private Image progressBarEmpty;
    private ImageIcon slowCoinGif;
    private Image houeImg;
    private Image arrosoirImg;
    private Image planterImg;
    private Image recolterImg;

    // --- États d'affichage ---
    private int hoveredX = -1; // Coordonnée X de la souris sur la grille
    private int hoveredY = -1; // Coordonnée Y de la souris sur la grille
    private boolean hotbarVisible = true;

    // Gère la prévisualisation (fantôme) lors de la pose d'un bâtiment
    private src.control.popups.BuildingManager ghostManager;

    // Système de surbrillance pour les actions en cours des jardiniers
    private final Map<Point, Integer> highlights = new HashMap<>();
    // Système de surbrillance bleue pour la sélection de zone par le joueur
    private Set<Point> selectedTilesBlueHighlight = new HashSet<Point>();
    private FloatingTextManager floatingTextManager;

    /** Définit les cases à surligner en bleu (sélection manuelle) */
    public void setSelectedTilesBlueHighlight(Set<Point> selectedTiles) {
        this.selectedTilesBlueHighlight = selectedTiles;
        this.repaint();
    }

    /** Injecte le gestionnaire de textes flottants (+XP, +PO) */
    public void setFloatingTextManager(FloatingTextManager floatingTextManager) {
        this.floatingTextManager = floatingTextManager;
    }

    public Global(World world, Camera camera) {
        super();
        this.world = world;
        this.camera = camera;
        this.setOpaque(true);

        this.startTime = System.currentTimeMillis();

        // Initialisation des outils de chargement d'images
        this.gardenerLoader = new SpriteSheetLoader("src/assets/Tiny Wonder Farm Free/characters/main character/walk and idle.png");
        this.chickenLoader = new ChickenSpriteSheetLoader();
        this.progressBarEmpty = new ImageIcon("src/assets/progress_bar_ui.png").getImage();
        this.slowCoinGif = new ImageIcon("src/assets/money.gif");
        this.crowLoader = new CrowSpriteSheetLoader();

        // Chargement des icônes de la Hotbar
        this.houeImg = new ImageIcon("src/assets/UI/houe.png").getImage();
        this.arrosoirImg = new ImageIcon("src/assets/UI/arrosoir.png").getImage();
        this.planterImg = new ImageIcon("src/assets/UI/seeds.png").getImage();
        this.recolterImg = new ImageIcon("src/assets/UI/growingplant.png").getImage();
    }

    /** Ajoute une surbrillance (jaune) sur une case. Utilise un compteur pour gérer les accès concurrents. */
    public void setHighlight(int wx, int wy) {
        synchronized (highlights) {
            Point p = new Point(wx, wy);
            int count = highlights.getOrDefault(p, 0);
            highlights.put(p, count + 1);
        }
        repaint();
    }

    /** Retire une surbrillance (jaune). Supprime l'entrée si le compteur tombe à zéro. */
    public void clearHighlight(int wx, int wy) {
        synchronized (highlights) {
            Point p = new Point(wx, wy);
            Integer count = highlights.get(p);
            if (count == null) return;
            if (count <= 1) highlights.remove(p);
            else highlights.put(p, count - 1);
        }
        repaint();
    }

    public void setGhostBuilding(src.control.popups.BuildingManager manager) {
        this.ghostManager = manager;
    }

    public void setHotbarVisible(boolean visible) {
        this.hotbarVisible = visible;
        repaint();
    }

    public boolean isHotbarVisible() {
        return hotbarVisible;
    }

    public boolean isHighlighted(int wx, int wy) {
        synchronized (highlights) {
            return highlights.containsKey(new Point(wx, wy));
        }
    }

    public void clearAllHighlights() {
        synchronized (highlights) {
            highlights.clear();
        }
        repaint();
    }

    /** Met à jour la case survolée par la souris pour l'affichage des jauges */
    public void setHoveredTile(int x, int y) {
        if (this.hoveredX != x || this.hoveredY != y) {
            this.hoveredX = x;
            this.hoveredY = y;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // --- CALCUL DU SCROLLING ---
        // On récupère la position de la caméra et le décalage fin en pixels pour un rendu fluide
        int fstTileX = (int) camera.getX();
        int fstTileY = (int) camera.getY();
        int pixelDiffX = (int) ((camera.getX() - fstTileX) * Display.RATIO_X);
        int pixelDiffY = (int) ((camera.getY() - fstTileY) * Display.RATIO_Y);

        // --- RENDU DU TERRAIN ET DES PLANTES ---
        for (int x = 0; x <= Camera.WIDTH; x++) {
            for (int y = 0; y <= Camera.HEIGHT; y++) {
                int worldX = fstTileX + x;
                int worldY = fstTileY + y;
                if (worldX >= 0 && worldX < World.WIDTH && worldY >= 0 && worldY < World.HEIGHT) {
                    Tile tile = world.getTile(worldX, worldY);
                    int paintX = (x * Display.RATIO_X) - pixelDiffX;
                    int paintY = (y * Display.RATIO_Y) - pixelDiffY;

                    // Dessin de la tuile (Herbe, Terre labourée, etc.)
                    g.drawImage(tile.getSprite().getImage(), paintX, paintY, Display.RATIO_X, Display.RATIO_Y, this);

                    // Dessin de la plante si la tuile en contient une
                    if (tile instanceof PlantTile) {
                        Plant plant = ((PlantTile) tile).getPlant();
                        if (plant != null) {
                            g.drawImage(plant.getSprite().getImage(), paintX, paintY, Display.RATIO_X, Display.RATIO_Y, this);
                        }
                    }
                }
            }
        }

        // --- RENDU DU FANTÔME DE CONSTRUCTION (GHOST) ---
        if (ghostManager != null && ghostManager.getGhostBuilding() != null) {
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setStroke(new BasicStroke(3));

            Building b = ghostManager.getGhostBuilding();
            int gx = ghostManager.getGhostX();
            int gy = ghostManager.getGhostY();
            boolean isValid = ghostManager.canPlace(gx, gy, b);

            Color fillColor = isValid ? new Color(0, 200, 255, 100) : new Color(255, 0, 0, 100);
            Color borderColor = isValid ? new Color(0, 150, 255) : new Color(255, 0, 0);

            // Dessin de l'empreinte au sol (rectangles bleus ou rouges)
            for (int dx = 0; dx < b.getWidth(); dx++) {
                for (int dy = 0; dy < b.getHeight(); dy++) {
                    int relX = (gx + dx) - fstTileX;
                    int relY = (gy + dy) - fstTileY;
                    if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                        int px = (relX * Display.RATIO_X) - pixelDiffX;
                        int py = (relY * Display.RATIO_Y) - pixelDiffY;
                        g3.setColor(fillColor);
                        g3.fillRect(px, py, Display.RATIO_X, Display.RATIO_Y);
                        g3.setColor(borderColor);
                        g3.drawRect(px + 1, py + 1, Display.RATIO_X - 3, Display.RATIO_Y - 3);
                    }
                }
            }

            // Dessin du sprite du bâtiment avec 50% d'opacité
            g3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            if (isBuildingVisibleInCamera(gx, gy, b.getWidth(), b.getHeight(), fstTileX, fstTileY)) {
                int relX = gx - fstTileX;
                int relY = gy - fstTileY;
                int px = (relX * Display.RATIO_X) - pixelDiffX;
                int py = (relY * Display.RATIO_Y) - pixelDiffY;
                Image ghostImg = b.getSprite(world, gx, gy).getImage();
                g3.drawImage(ghostImg, px, py, Display.RATIO_X * b.getWidth(), Display.RATIO_Y * b.getHeight(), null);
            }
            g3.dispose();
        }

        // --- RENDU DES BÂTIMENTS DÉJÀ CONSTRUITS ---
        if (world.getBuildings() != null) {
            for (src.model.buildings.Building b : world.getBuildings()) {
                if (isBuildingVisibleInCamera(b.getX(), b.getY(), b.getWidth(), b.getHeight(), fstTileX, fstTileY)) {
                    int relX = b.getX() - fstTileX;
                    int relY = b.getY() - fstTileY;
                    int px = (relX * Display.RATIO_X) - pixelDiffX;
                    int py = (relY * Display.RATIO_Y) - pixelDiffY;

                    Image spriteImg = b.getSprite(world, b.getX(), b.getY()).getImage();

                    // Logique spécifique pour les Barrières et Portes (connexions visuelles)
                    if (b instanceof GateFace) {
                        // Rendu complexe des poteaux et battants de porte
                        if (((Gate) b).hasFenceRight(world, b.getX(), b.getY())) {
                            g.drawImage(((Gate) b).getFaceSprite().getImage(), 8+px + Display.RATIO_X , py, (Display.RATIO_X * b.getWidth()/3), Display.RATIO_Y * b.getHeight(), null);
                        }
                        if (((Gate) b).hasFenceLeft(world, b.getX(), b.getY())) {
                            g.drawImage(((Gate) b).getFaceSprite().getImage(), 6 + px - Display.RATIO_X / 2, py, (Display.RATIO_X * b.getWidth())/3, Display.RATIO_Y * b.getHeight(), null);
                        }
                        g.drawImage(((Gate)b).getGateLeftSprite().getImage(),  px +5, py, (int) ((Display.RATIO_X * b.getWidth())/1.5), Display.RATIO_Y * b.getHeight(), null);
                        g.drawImage(((Gate)b).getGateRightSprite().getImage(), px + Display.RATIO_X/2-10, py, (int) (Display.RATIO_X * b.getWidth()/1.5), Display.RATIO_Y * b.getHeight(), null);
                        g.drawImage(spriteImg, px-Display.RATIO_Y/2, py, Display.RATIO_X * b.getWidth(), Display.RATIO_Y * b.getHeight(), null);
                        g.drawImage(spriteImg, px+Display.RATIO_Y/2, py, Display.RATIO_X * b.getWidth(), Display.RATIO_Y * b.getHeight(), null);
                    } else if (b instanceof GateSide) {
                        // Rendu des portes de côté
                        if (((Fence)b).hasFenceAbove(world, b.getX(), b.getY())) {
                            g.drawImage(((Fence) b).getSideSprite().getImage(), 2 + px, py - Display.RATIO_Y / 2, Display.RATIO_X * b.getWidth(), (Display.RATIO_Y * b.getHeight()) /3, null);
                        }
                        g.drawImage(((Fence)b).getSideSprite().getImage(), px +3, py+Display.RATIO_Y/2, (Display.RATIO_X * b.getWidth()), Display.RATIO_Y * b.getHeight()/3, null);
                        g.drawImage(spriteImg, px, py-Display.RATIO_Y/2, Display.RATIO_X * b.getWidth(), Display.RATIO_Y * b.getHeight(), null);
                        g.drawImage(((Fence) b).getSideSprite().getImage(),  px+3 , py, ((Display.RATIO_X * b.getWidth())), Display.RATIO_Y * b.getHeight()/3, null);
                        g.drawImage(spriteImg, px, py+Display.RATIO_Y/2, Display.RATIO_X * b.getWidth(), Display.RATIO_Y * b.getHeight(), null);
                        if (((Fence) b).hasFenceBelow(world, b.getX(), b.getY())) {
                            g.drawImage(((Fence) b).getSideSprite().getImage(), 2 + px, py + Display.RATIO_Y, Display.RATIO_X * b.getWidth(), (Display.RATIO_Y * b.getHeight())/3, null);
                        }
                    } else {
                        // Rendu standard pour les clôtures et autres bâtiments
                        if (b instanceof Fence && ((Fence) b).hasFenceRight(world, b.getX(), b.getY())) {
                            g.drawImage(((Fence) b).getFaceSprite().getImage(), 6 + px + Display.RATIO_X / 2, py, (Display.RATIO_X * b.getWidth()) - 10, Display.RATIO_Y * b.getHeight(), null);
                        }
                        g.drawImage(spriteImg, px, py, Display.RATIO_X * b.getWidth(), Display.RATIO_Y * b.getHeight(), null);
                        if (b instanceof Fence && ((Fence) b).hasFenceBelow(world, b.getX(), b.getY())) {
                            g.drawImage(((Fence) b).getSideSprite().getImage(), 2 + px, py + Display.RATIO_Y / 2, Display.RATIO_X * b.getWidth(), (Display.RATIO_Y * b.getHeight()) - 16, null);
                        }
                    }
                }
            }
        }

        // --- RENDU DU MODE SUPPRESSION (Bulldozer) ---
        if (ghostManager != null && ghostManager.isDeletionMode()) {
            Graphics2D g4 = (Graphics2D) g.create();
            g4.setStroke(new BasicStroke(3));

            // Surligne en rouge les bâtiments sélectionnés pour suppression
            for (src.model.buildings.Building selected : ghostManager.getPendingDeletionBuildings()) {
                for (int dx = 0; dx < selected.getWidth(); dx++) {
                    for (int dy = 0; dy < selected.getHeight(); dy++) {
                        int relX = (selected.getX() + dx) - fstTileX;
                        int relY = (selected.getY() + dy) - fstTileY;
                        if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                            int px = (relX * Display.RATIO_X) - pixelDiffX;
                            int py = (relY * Display.RATIO_Y) - pixelDiffY;
                            g4.setColor(new Color(255, 0, 0, 110));
                            g4.fillRect(px, py, Display.RATIO_X, Display.RATIO_Y);
                            g4.setColor(new Color(210, 20, 20));
                            g4.drawRect(px + 1, py + 1, Display.RATIO_X - 3, Display.RATIO_Y - 3);
                        }
                    }
                }
            }

            // Surligne les tuiles de plantation sélectionnées pour suppression
            for (Point selectedTile : ghostManager.getPendingDeletionPlantTiles()) {
                int relX = selectedTile.x - fstTileX;
                int relY = selectedTile.y - fstTileY;
                if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                    int px = (relX * Display.RATIO_X) - pixelDiffX;
                    int py = (relY * Display.RATIO_Y) - pixelDiffY;
                    g4.setColor(new Color(255, 0, 0, 110));
                    g4.fillRect(px, py, Display.RATIO_X, Display.RATIO_Y);
                    g4.setColor(new Color(210, 20, 20));
                    g4.drawRect(px + 1, py + 1, Display.RATIO_X - 3, Display.RATIO_Y - 3);
                }
            }

            // Surligne en rouge clair l'élément actuellement survolé par la souris
            int gx = ghostManager.getGhostX();
            int gy = ghostManager.getGhostY();
            src.model.buildings.Building hovered = world.getBuildingAt(gx, gy);
            if (hovered != null && !ghostManager.getPendingDeletionBuildings().contains(hovered)) {
                for (int dx = 0; dx < hovered.getWidth(); dx++) {
                    for (int dy = 0; dy < hovered.getHeight(); dy++) {
                        int relX = (hovered.getX() + dx) - fstTileX;
                        int relY = (hovered.getY() + dy) - fstTileY;
                        if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                            int px = (relX * Display.RATIO_X) - pixelDiffX;
                            int py = (relY * Display.RATIO_Y) - pixelDiffY;
                            g4.setColor(new Color(255, 120, 120, 90));
                            g4.fillRect(px, py, Display.RATIO_X, Display.RATIO_Y);
                            g4.setColor(new Color(255, 80, 80));
                            g4.drawRect(px + 1, py + 1, Display.RATIO_X - 3, Display.RATIO_Y - 3);
                        }
                    }
                }
            }
            g4.dispose();
        }

        // --- RENDU DES HIGHLIGHTS (SÉLECTION JOUEUR ET CIBLES IA) ---
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setStroke(new BasicStroke(3));

        // 1. Surbrillance BLEUE (Mode sélection de cases)
        if (selectedTilesBlueHighlight != null && !selectedTilesBlueHighlight.isEmpty()) {
            for (Point p : selectedTilesBlueHighlight) {
                int relX = p.x - fstTileX;
                int relY = p.y - fstTileY;
                if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                    int hx = (relX * Display.RATIO_X) - pixelDiffX;
                    int hy = (relY * Display.RATIO_Y) - pixelDiffY;
                    g2.setColor(new Color(60, 140, 255, 110));
                    g2.fillRect(hx, hy, Display.RATIO_X, Display.RATIO_Y);
                    g2.setColor(new Color(20, 90, 220));
                    g2.drawRect(hx + 1, hy + 1, Display.RATIO_X - 3, Display.RATIO_Y - 3);
                }
            }
        }

        // 2. Surbrillance JAUNE (Cases ciblées par les actions des jardiniers)
        Set<Point> highlightSnapshot;
        synchronized (highlights) {
            highlightSnapshot = new HashSet<>(highlights.keySet());
        }
        if (!highlightSnapshot.isEmpty()) {
            for (Point p : highlightSnapshot) {
                int relX = p.x - fstTileX;
                int relY = p.y - fstTileY;
                if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                    int hx = (relX * Display.RATIO_X) - pixelDiffX;
                    int hy = (relY * Display.RATIO_Y) - pixelDiffY;
                    g2.setColor(new Color(255, 255, 0, 80));
                    g2.fillRect(hx, hy, Display.RATIO_X, Display.RATIO_Y);
                    g2.setColor(new Color(255, 200, 0));
                    g2.drawRect(hx + 1, hy + 1, Display.RATIO_X - 3, Display.RATIO_Y - 3);
                }
            }
        }
        g2.dispose();

        // --- RENDU DES ENTITÉS ---
        drawEntities(g, fstTileX, fstTileY, pixelDiffX, pixelDiffY);

        // --- RENDU DES JAUGES AU SURVOL (CROISSANCE + EAU) ---
        if (hoveredX >= 0 && hoveredY >= 0 && hoveredX < World.WIDTH && hoveredY < World.HEIGHT) {
            Tile hoveredTile = world.getTile(hoveredX, hoveredY);
            if (hoveredTile instanceof PlantTile) {
                Plant plant = ((PlantTile) hoveredTile).getPlant();
                // N'affiche les barres que si la plante est vivante et non récoltable
                if (plant != null && plant.getState() != PlantState.MORT && plant.getState() != PlantState.EATEN && !plant.isHarvestable()) {
                    int relX = hoveredX - fstTileX;
                    int relY = hoveredY - fstTileY;
                    if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                        int drawX = (relX * Display.RATIO_X) - pixelDiffX;
                        int drawY = (relY * Display.RATIO_Y) - pixelDiffY;

                        int barW = 48;
                        int barH = 12;
                        int barX = drawX + (Display.RATIO_X / 2) - (barW / 2);
                        int innerW = barW - 4;
                        int innerH = barH - 4;

                        // Jauge de croissance (Verte)
                        int growthBarY = drawY - 15;
                        if (progressBarEmpty != null) g.drawImage(progressBarEmpty, barX, growthBarY, barW, barH, this);
                        else { g.setColor(Color.BLACK); g.drawRect(barX, growthBarY, barW, barH); }

                        float growthProgress = plant.getGrowthPercentage();
                        int fillGrowthW = (int) (innerW * growthProgress);
                        if (fillGrowthW > 0) {
                            g.setColor(new Color(50, 205, 50));
                            g.fillRect(barX + 2, growthBarY + 2, fillGrowthW, innerH);
                            g.setColor(new Color(144, 238, 144, 180)); // Reflet
                            g.fillRect(barX + 2, growthBarY + 2, fillGrowthW, innerH / 2);
                        }

                        // Jauge d'eau (Bleue)
                        int waterBarY = growthBarY + barH + 3;
                        if (progressBarEmpty != null) g.drawImage(progressBarEmpty, barX, waterBarY, barW, barH, this);
                        else { g.setColor(Color.BLACK); g.drawRect(barX, waterBarY, barW, barH); }

                        float waterProgress = plant.getWaterLevel() / Plant.MAX_WATER_LEVEL;
                        int fillWaterW = (int) (innerW * Math.min(1.0f, Math.max(0.0f, waterProgress)));
                        if (fillWaterW > 0) {
                            g.setColor(new Color(30, 144, 255));
                            g.fillRect(barX + 2, waterBarY + 2, fillWaterW, innerH);
                            g.setColor(new Color(135, 206, 250, 180)); // Reflet
                            g.fillRect(barX + 2, waterBarY + 2, fillWaterW, innerH / 2);
                        }
                    }
                }
            }
        }

        // Stats (XP/Or) en haut de l'écran
        drawTopStatsRow(g);

        // Panneau d'information en bas si le bulldozer est actif
        if (ghostManager != null && ghostManager.isDeletionMode()) {
            drawDeletionModePanel(g);
        }

        // Barre d'outils (Hotbar)
        drawHotbar(g);

        // Dessin des textes flottants (gains d'or, etc.)
        if (floatingTextManager != null) {
            Graphics2D g3 = (Graphics2D) g.create();
            floatingTextManager.draw(g3);
            g3.dispose();
        }
    }

    /** Affiche un bandeau d'aide lors du mode suppression */
    private void drawDeletionModePanel(Graphics g) {
        Graphics2D gPanel = (Graphics2D) g.create();
        int panelHeight = 64;
        int panelWidth = Math.min(Math.min(getWidth() - 40, 760), getWidth() - 40);
        int hotbarOffset = (hotbarVisible ? 80 : 0);
        int px = (getWidth() - panelWidth) / 2;
        int py = getHeight() - panelHeight - 20 - hotbarOffset;

        gPanel.setColor(new Color(0, 0, 0, 180));
        gPanel.fillRoundRect(px, py, panelWidth, panelHeight, 12, 12);
        gPanel.setColor(new Color(200, 200, 200, 100));
        gPanel.setStroke(new BasicStroke(2));
        gPanel.drawRoundRect(px, py, panelWidth, panelHeight, 12, 12);

        String message = "Mode suppression - clic gauche: supprimer, clic droit: annuler";
        Font font = (GameFonts.MINECRAFT_FONT != null) ? GameFonts.MINECRAFT_FONT.deriveFont(Font.PLAIN, 24f) : new Font("Arial", Font.BOLD, 16);
        gPanel.setFont(font);
        FontMetrics fm = gPanel.getFontMetrics(font);
        int textX = px + (panelWidth - fm.stringWidth(message)) / 2;
        int textY = py + (panelHeight + fm.getAscent() - fm.getDescent()) / 2;

        gPanel.setColor(new Color(255, 255, 255, 220));
        gPanel.drawString(message, textX, textY);
        gPanel.dispose();
    }

    /** Dessine la Hotbar (Barre d'outils) centrée en bas */
    private void drawHotbar(Graphics g) {
        if (!hotbarVisible) return;

        Graphics2D g2 = (Graphics2D) g.create();
        int nbSlots = 4;
        int slotSize = 52;
        int spacing = 8;
        int totalWidth = (slotSize * nbSlots) + (spacing * (nbSlots - 1));
        int startX = (getWidth() - totalWidth) / 2;
        int startY = getHeight() - slotSize - 50;

        Gardener player = (world.getGardeners() != null && !world.getGardeners().isEmpty()) ? world.getGardeners().get(0) : null;
        int selectedIndex = (player != null) ? player.getSelectedHotbarIndex() : -1;

        Color slotBg = new Color(235, 185, 120, 230);
        Color darkBorder = new Color(110, 45, 15);

        for (int i = 0; i < nbSlots; i++) {
            int x = startX + i * (slotSize + spacing);
            boolean slotActive = Tutorial.isHotbarSlotActive(i);

            // Fond
            g2.setColor(slotActive ? slotBg : new Color(120, 120, 120, 200));
            g2.fillRect(x, startY, slotSize, slotSize);

            // Icônes
            Composite prevComp = g2.getComposite();
            if (!slotActive) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));

            Image icon = null;
            if (i == 0) icon = houeImg;
            else if (i == 1) icon = arrosoirImg;
            else if (i == 2) icon = planterImg;
            else if (i == 3) icon = recolterImg;

            if (icon != null) g2.drawImage(icon, x + 8, startY + 8, slotSize - 16, slotSize - 16, null);
            g2.setComposite(prevComp);

            // Bordure (Blanche si sélectionnée, Marron sinon)
            if (selectedIndex >= 0 && i == selectedIndex) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(5));
            } else {
                g2.setColor(slotActive ? darkBorder : new Color(90, 90, 90));
                g2.setStroke(new BasicStroke(3));
            }
            g2.drawRect(x, startY, slotSize, slotSize);

            // Numéro de raccourci (1, 2, 3, 4)
            g2.setColor(slotActive ? new Color(255, 255, 255, 180) : new Color(200, 200, 200, 160));
            g2.setFont((GameFonts.MINECRAFT_FONT != null) ? GameFonts.MINECRAFT_FONT.deriveFont(12f) : new Font("Arial", Font.BOLD, 10));
            g2.drawString(String.valueOf(i + 1), x + 4, startY + 14);
        }
        g2.dispose();
    }

    /** Affiche les boîtes d'XP et d'argent en haut à gauche */
    private void drawTopStatsRow(Graphics g) {
        src.model.Stats stats = world.getStats();
        int money = world.getBarn().getMoney();
        String expText = "Niv." + stats.getLevel() + " " + stats.getExp() + "/" + stats.getExpForNextLevel() + " XP";
        String moneyText = "" + money;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
        FontMetrics metrics = g2.getFontMetrics();

        int iconSize = 25, paddingX = 10, paddingY = 6, x = 12, y = 12;
        int expBoxWidth = metrics.stringWidth(expText) + paddingX * 2;
        int moneyBoxWidth = iconSize + 8 + metrics.stringWidth(moneyText) + paddingX * 2;
        int height = Math.max(iconSize, metrics.getHeight()) + paddingY * 2;

        // Rendu XP
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(x, y, expBoxWidth, height, 12, 12);
        g2.setColor(new Color(255, 225, 120));
        g2.drawRoundRect(x, y, expBoxWidth, height, 12, 12);
        g2.setColor(Color.WHITE);
        g2.drawString(expText, x + paddingX, y + paddingY + metrics.getAscent());

        // Rendu Argent
        int mX = x + expBoxWidth + 14;
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(mX, y, moneyBoxWidth, height, 12, 12);
        g2.setColor(new Color(255, 225, 120));
        g2.drawRoundRect(mX, y, moneyBoxWidth, height, 12, 12);
        if (slowCoinGif != null) g2.drawImage(slowCoinGif.getImage(), mX + paddingX, y + paddingY, iconSize, iconSize, this);
        g2.setColor(Color.WHITE);
        g2.drawString(moneyText, mX + paddingX + iconSize + 8, y + paddingY + metrics.getAscent());
        g2.dispose();
    }

    /** Vérifie si un bâtiment intersecte le rectangle de vue de la caméra */
    private boolean isBuildingVisibleInCamera(int x, int y, int width, int height, int fstTileX, int fstTileY) {
        return (x + width - 1) >= fstTileX && x <= (fstTileX + Camera.WIDTH)
                && (y + height - 1) >= fstTileY && y <= (fstTileY + Camera.HEIGHT);
    }

    /** Dessine les jardiniers, poules et corbeaux animés */
    private void drawEntities(Graphics g, int fstTileX, int fstTileY, int pixelDiffX, int pixelDiffY) {
        long elapsedTime = System.currentTimeMillis() - startTime;

        // 1. Jardiniers
        for (Gardener gardener : world.getGardeners()) {
            if (gardener.getX() >= fstTileX && gardener.getX() <= fstTileX + Camera.WIDTH &&
                    gardener.getY() >= fstTileY && gardener.getY() <= fstTileY + Camera.HEIGHT) {

                int frameIdx = (int) (elapsedTime / 150) % gardenerLoader.getNbFrames();
                BufferedImage sprite = (gardener.getCurrentState() == Gardener.State.MOVING) ?
                        gardenerLoader.getWalkFrame(gardener.getFacingDirection(), frameIdx) :
                        gardenerLoader.getIdleFrame(gardener.getFacingDirection(), frameIdx);

                int drawX = ((gardener.getX() - fstTileX) * Display.RATIO_X) - pixelDiffX;
                int drawY = ((gardener.getY() - fstTileY) * Display.RATIO_Y) - pixelDiffY;

                // Effet miroir si le jardinier regarde vers la DROITE
                if (gardener.getFacingDirection() == Entity.RIGHT) {
                    g.drawImage(sprite, drawX + Display.RATIO_X, drawY, drawX, drawY + Display.RATIO_Y, 0, 0, 24, 24, null);
                } else {
                    g.drawImage(sprite, drawX, drawY, drawX + Display.RATIO_X, drawY + Display.RATIO_Y, 0, 0, 24, 24, null);
                }
            }
        }

        // 2. Poules
        java.util.List<Chicken> enemies = world.getEnemies();
        if (enemies != null) {
            int frame = (int) (elapsedTime / 150) % 4;
            for (Chicken c : enemies) {
                if (c.getX() >= fstTileX && c.getX() <= fstTileX + Camera.WIDTH && c.getY() >= fstTileY && c.getY() <= fstTileY + Camera.HEIGHT) {
                    BufferedImage s = chickenLoader.getFrame(c.getCurrentStateActionIndex(), c.getFacingDirection(), frame);
                    int dx = ((c.getX() - fstTileX) * Display.RATIO_X) - pixelDiffX;
                    int dy = ((c.getY() - fstTileY) * Display.RATIO_Y) - pixelDiffY;
                    if (s != null) g.drawImage(s, dx, dy, Display.RATIO_X, Display.RATIO_Y, null);
                }
            }
        }

        // 3. Corbeaux
        java.util.List<src.model.Crow> corbeaux = world.getCrows();
        if (corbeaux != null) {
            int frame = (int) (elapsedTime / 150) % 4;
            for (src.model.Crow cr : corbeaux) {
                if (cr.getX() >= fstTileX && cr.getX() <= fstTileX + Camera.WIDTH && cr.getY() >= fstTileY && cr.getY() <= fstTileY + Camera.HEIGHT) {
                    BufferedImage s = crowLoader.getFrame(cr.getCurrentStateActionIndex(), cr.getFacingDirection(), frame);
                    int dx = ((cr.getX() - fstTileX) * Display.RATIO_X) - pixelDiffX;
                    int dy = ((cr.getY() - fstTileY) * Display.RATIO_Y) - pixelDiffY;
                    if (s != null) g.drawImage(s, dx, dy, Display.RATIO_X, Display.RATIO_Y, null);
                }
            }
        }
    }
}
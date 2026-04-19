package src.view;

import src.model.*;
import src.model.buildings.Building;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.*;
import java.util.Set;

public class Global extends JPanel {
    private final World world;
    private final Camera camera;

    private final SpriteSheetLoader gardenerLoader;
    private final long startTime;
    private ChickenSpriteSheetLoader chickenLoader;

    private Image progressBarEmpty;
    private ImageIcon slowCoinGif;

    // --- Images des outils et actions ---
    private Image houeImg;
    private Image arrosoirImg;
    private Image planterImg;
    private Image recolterImg;

    private int hoveredX = -1;
    private int hoveredY = -1;

    private boolean hotbarVisible = true;

    private src.control.popups.BuildingManager ghostManager;

    private final Set<Point> highlights = new HashSet<>();
    private Set<Point> selectedTilesBlueHighlight = new HashSet<Point>();

    public void setSelectedTilesBlueHighlight(Set<Point> selectedTiles) {
        this.selectedTilesBlueHighlight = selectedTiles;
        this.repaint();
    }

    public Global(World world, Camera camera) {
        super();
        this.world = world;
        this.camera = camera;
        this.setOpaque(true);

        this.startTime = System.currentTimeMillis();
        this.gardenerLoader = new SpriteSheetLoader("src/assets/Tiny Wonder Farm Free/characters/main character/walk and idle.png");
        this.chickenLoader = new ChickenSpriteSheetLoader();
        this.progressBarEmpty = new ImageIcon("src/assets/progress_bar_ui.png").getImage();
        this.slowCoinGif = new ImageIcon("src/assets/money.gif");

        // --- Chargement des outils et actions ---
        this.houeImg = new ImageIcon("src/assets/UI/houe.png").getImage();
        this.arrosoirImg = new ImageIcon("src/assets/UI/arrosoir.png").getImage();
        this.planterImg = new ImageIcon("src/assets/UI/seeds.png").getImage();
        this.recolterImg = new ImageIcon("src/assets/UI/growingplant.png").getImage();
    }

    public void setHighlight(int wx, int wy) {
        highlights.add(new Point(wx, wy));
        repaint();
    }

    public void clearHighlight(int wx, int wy) {
        highlights.remove(new Point(wx, wy));
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
        return highlights.contains(new Point(wx, wy));
    }

    public void clearAllHighlights() {
        highlights.clear();
        repaint();
    }

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

        int fstTileX = (int) camera.getX();
        int fstTileY = (int) camera.getY();
        int pixelDiffX = (int) ((camera.getX() - fstTileX) * Display.RATIO_X);
        int pixelDiffY = (int) ((camera.getY() - fstTileY) * Display.RATIO_Y);

        for (int x = 0; x <= Camera.WIDTH; x++) {
            for (int y = 0; y <= Camera.HEIGHT; y++) {
                int worldX = fstTileX + x;
                int worldY = fstTileY + y;
                if (worldX >= 0 && worldX < World.WIDTH && worldY >= 0 && worldY < World.HEIGHT) {
                    Tile tile = world.getTile(worldX, worldY);
                    int paintX = (x * Display.RATIO_X) - pixelDiffX;
                    int paintY = (y * Display.RATIO_Y) - pixelDiffY;
                    g.drawImage(tile.getSprite().getImage(), paintX, paintY, Display.RATIO_X, Display.RATIO_Y, this);

                    if (tile instanceof PlantTile) {
                        PlantTile casePlantable = (PlantTile) tile;
                        Plant plant = casePlantable.getPlant();
                        if (plant != null) {
                            g.drawImage(plant.getSprite().getImage(), paintX, paintY, Display.RATIO_X, Display.RATIO_Y, this);
                        }
                    }
                }
            }
        }

        if (ghostManager != null && ghostManager.getGhostBuilding() != null) {
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setStroke(new BasicStroke(3));

            Building b = ghostManager.getGhostBuilding();
            int gx = ghostManager.getGhostX();
            int gy = ghostManager.getGhostY();
            boolean isValid = ghostManager.canPlace(gx, gy, b);

            Color fillColor;
            Color borderColor;
            if (isValid) {
                fillColor = new Color(0, 200, 255, 100);
                borderColor = new Color(0, 150, 255);
            } else {
                fillColor = new Color(255, 0, 0, 100);
                borderColor = new Color(255, 0, 0);
            }

            // 1. Dessiner la surbrillance sur chaque case de l'empreinte
            for (int dx = 0; dx < b.getWidth(); dx++) {
                for (int dy = 0; dy < b.getHeight(); dy++) {
                    int relX = (gx + dx) - fstTileX;
                    int relY = (gy + dy) - fstTileY;

                    if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                        int px = (relX * Display.RATIO_X) - pixelDiffX;
                        int py = (relY * Display.RATIO_Y) - pixelDiffY;

                        // Remplissage semi-transparent
                        g3.setColor(fillColor);
                        g3.fillRect(px, py, Display.RATIO_X, Display.RATIO_Y);

                        // Bordure opaque (avec +1 et -3 pour ne pas déborder, comme tes highlights)
                        g3.setColor(borderColor);
                        g3.drawRect(px + 1, py + 1, Display.RATIO_X - 3, Display.RATIO_Y - 3);
                    }
                }
            }

            // 2. Appliquer la transparence (50%) *uniquement* pour le sprite du bâtiment
            g3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

            // 3. Dessiner l'image du bâtiment au-dessus de la zone surlignée
            int relX = gx - fstTileX;
            int relY = gy - fstTileY;
            if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                int px = (relX * Display.RATIO_X) - pixelDiffX;
                int py = (relY * Display.RATIO_Y) - pixelDiffY;
                g3.drawImage(b.getSprite().getImage(), px, py, Display.RATIO_X * b.getWidth(), Display.RATIO_Y * b.getHeight(), null);
            }
            g3.dispose();
        }

        // === DELETION HIGHLIGHT (si on est en mode suppression) ===
        // On réutilise ghostManager pour obtenir les coordonnées du curseur
        try {
            if (ghostManager != null && ghostManager.isDeletionMode()) {
                int gx = ghostManager.getGhostX();
                int gy = ghostManager.getGhostY();
                src.model.buildings.Building b = world.getBuildingAt(gx, gy);
                if (b != null) {
                    Graphics2D g4 = (Graphics2D) g.create();
                    g4.setStroke(new BasicStroke(3));
                    Color fill = new Color(255, 0, 0, 100);
                    Color border = new Color(200, 0, 0);
                    for (int dx = 0; dx < b.getWidth(); dx++) {
                        for (int dy = 0; dy < b.getHeight(); dy++) {
                            int relX = (b.getX() + dx) - fstTileX;
                            int relY = (b.getY() + dy) - fstTileY;
                            if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                                int px = (relX * Display.RATIO_X) - pixelDiffX;
                                int py = (relY * Display.RATIO_Y) - pixelDiffY;
                                g4.setColor(fill);
                                g4.fillRect(px, py, Display.RATIO_X, Display.RATIO_Y);
                                g4.setColor(border);
                                g4.drawRect(px + 1, py + 1, Display.RATIO_X - 3, Display.RATIO_Y - 3);
                            }
                        }
                    }
                    g4.dispose();
                }
            }
        } catch (Throwable t) {}

        // === DESSIN DES BÂTIMENTS DÉJÀ CONSTRUITS ===
        if (world.getBuildings() != null) {
            for (src.model.buildings.Building b : world.getBuildings()) {

                int relX = b.getX() - fstTileX;
                int relY = b.getY() - fstTileY;

                // Si le bâtiment est dans le champ de la caméra
                if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                    int px = (relX * Display.RATIO_X) - pixelDiffX;
                    int py = (relY * Display.RATIO_Y) - pixelDiffY;
                    g.drawImage(b.getSprite().getImage(), px, py, Display.RATIO_X * b.getWidth(), Display.RATIO_Y * b.getHeight(), null);
                }
            }
        }

        // Highlight : on utilise un Graphics2D pour pouvoir dessiner des rectangles avec une bordure plus épaisse, et des couleurs semi-transparentes pour le remplissage
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setStroke(new BasicStroke(3));

        // Dessiner d'abord le surlignement BLEU (Sélection en cours de l'utilisateur)
        if (selectedTilesBlueHighlight != null && !selectedTilesBlueHighlight.isEmpty()) {
            for (Point p : selectedTilesBlueHighlight) {
                int relX = p.x - fstTileX;
                int relY = p.y - fstTileY;

                // Vérifier si la case est visible à l'écran
                if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                    int hx = (relX * Display.RATIO_X) - pixelDiffX;
                    int hy = (relY * Display.RATIO_Y) - pixelDiffY;

                    // Remplissage marron semi-transparent
                    g2.setColor(new Color(110, 45, 15, 100));
                    g2.fillRect(hx, hy, Display.RATIO_X, Display.RATIO_Y);

                    // Bordure marron foncé opaque
                    g2.setColor(new Color(110, 45, 15));
                    g2.drawRect(hx + 1, hy + 1, Display.RATIO_X - 3, Display.RATIO_Y - 3);
                }
            }
        }

        // Dessiner ENSUITE le surlignement JAUNE classique (Cible des actions du jardinier)
        if (highlights != null && !highlights.isEmpty()) {
            for (Point p : highlights) {
                int relX = p.x - fstTileX;
                int relY = p.y - fstTileY;

                // Vérifier si la case est visible à l'écran
                if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                    int hx = (relX * Display.RATIO_X) - pixelDiffX;
                    int hy = (relY * Display.RATIO_Y) - pixelDiffY;

                    // Remplissage jaune semi-transparent
                    g2.setColor(new Color(255, 255, 0, 80));
                    g2.fillRect(hx, hy, Display.RATIO_X, Display.RATIO_Y);

                    // Bordure jaune opaque
                    g2.setColor(new Color(255, 200, 0));
                    g2.drawRect(hx + 1, hy + 1, Display.RATIO_X - 3, Display.RATIO_Y - 3);
                }
            }
        }
        g2.dispose();

        // dessiner les entités (jardiniers) - Une seule fois !
        drawEntities(g, fstTileX, fstTileY, pixelDiffX, pixelDiffY);

        // Dessin des jauges (Croissance + Eau) sur la case survolée
        if (hoveredX >= 0 && hoveredY >= 0 && hoveredX < World.WIDTH && hoveredY < World.HEIGHT) {
            Tile hoveredTile = world.getTile(hoveredX, hoveredY);

            // Vérifier si c'est une case plantable
            if (hoveredTile instanceof PlantTile) {
                PlantTile plantTile = (PlantTile) hoveredTile;
                Plant plant = plantTile.getPlant();
                if (plant != null && plant.getState() != PlantState.MORT && plant.getState() != PlantState.EATEN && !plant.isHarvestable()) {
                    int relX = hoveredX - fstTileX;
                    int relY = hoveredY - fstTileY;

                    // On vérifie que la case survolée est bien visible à l'écran
                    if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                        int drawX = (relX * Display.RATIO_X) - pixelDiffX;
                        int drawY = (relY * Display.RATIO_Y) - pixelDiffY;

                        int barW = 48;
                        int barH = 12;
                        int barX = drawX + (Display.RATIO_X / 2) - (barW / 2);
                        int innerW = barW - 4;
                        int innerH = barH - 4;

                        int growthBarY = drawY - 15;
                        if (progressBarEmpty != null) {
                            g.drawImage(progressBarEmpty, barX, growthBarY, barW, barH, this);
                        } else {
                            g.setColor(Color.BLACK); g.drawRect(barX, growthBarY, barW, barH);
                        }

                        float growthProgress = plant.getGrowthPercentage();
                        int fillGrowthW = (int) (innerW * growthProgress);

                        if (fillGrowthW > 0) {
                            g.setColor(new Color(50, 205, 50));
                            g.fillRect(barX + 2, growthBarY + 2, fillGrowthW, innerH);
                            g.setColor(new Color(144, 238, 144, 180));
                            g.fillRect(barX + 2, growthBarY + 2, fillGrowthW, innerH / 2);
                        }
                        // 2ème JAUGE : L'EAU (BLEUE)
                        int waterBarY = growthBarY + barH + 3; // Placée juste en dessous de la barre verte (+2px d'écart)

                        if (progressBarEmpty != null) {
                            g.drawImage(progressBarEmpty, barX, waterBarY, barW, barH, this);
                        } else {
                            g.setColor(Color.BLACK); g.drawRect(barX, waterBarY, barW, barH);
                        }

                        // Le niveau d'eau maximum dans ta classe Plant est de 100.0f
                        float waterProgress = plant.getWaterLevel() / 100.0f;
                        if (waterProgress > 1.0f) waterProgress = 1.0f;
                        if (waterProgress < 0.0f) waterProgress = 0.0f;

                        int fillWaterW = (int) (innerW * waterProgress);

                        if (fillWaterW > 0) {
                            g.setColor(new Color(30, 144, 255));
                            g.fillRect(barX + 2, waterBarY + 2, fillWaterW, innerH);
                            g.setColor(new Color(135, 206, 250, 180));
                            g.fillRect(barX + 2, waterBarY + 2, fillWaterW, innerH / 2);
                        }
                    }
                }
            }
        }

        // argent visible en permanence sur l'affichage principal.
        drawMoney(g);
        drawLevel(g);

        // --- Dessin de la barre d'action par dessus tout ! ---
        drawHotbar(g);
    }

    // --- Méthode pour dessiner la Hotbar ---
    private void drawHotbar(Graphics g) {
        if (!hotbarVisible) return;

        Graphics2D g2 = (Graphics2D) g.create();

        int nbSlots = 4;
        int slotSize = 52; // Taille d'une case
        int spacing = 8;   // Espacement entre les cases

        int totalWidth = (slotSize * nbSlots) + (spacing * (nbSlots - 1));
        int startX = (getWidth() - totalWidth) / 2; // Centré horizontalement
        int startY = getHeight() - slotSize - 50;   // En bas de l'écran

        // Récupérer le premier jardinier (le joueur)
        Gardener player = null;
        if (world.getGardeners() != null && !world.getGardeners().isEmpty()) {
            player = world.getGardeners().get(0);
        }
        int selectedIndex = (player != null) ? player.getSelectedHotbarIndex() : -1;

        // Couleurs de style Stardew Valley
        Color slotBg = new Color(235, 185, 120, 230); // Sable semi-transparent
        Color darkBorder = new Color(110, 45, 15);    // Marron foncé

        for (int i = 0; i < nbSlots; i++) {
            int x = startX + i * (slotSize + spacing);

            // 1. Fond de la case
            g2.setColor(slotBg);
            g2.fillRect(x, startY, slotSize, slotSize);

            // 2. Contenu de la case (Outils)
            if (i == 0) {
                // Emplacement 1 : La Houe
                if (houeImg != null) g2.drawImage(houeImg, x + 8, startY + 8, slotSize - 16, slotSize - 16, null);
            } else if (i == 1) {
                // Emplacement 2 : L'Arrosoir
                if (arrosoirImg != null) g2.drawImage(arrosoirImg, x + 8, startY + 8, slotSize - 16, slotSize - 16, null);
            } else if (i == 2) {
                // Emplacement 3 : Planter
                if (planterImg != null) g2.drawImage(planterImg, x + 8, startY + 8, slotSize - 16, slotSize - 16, null);
            } else if (i == 3) {
                // Emplacement 4 : Récolter
                if (recolterImg != null) g2.drawImage(recolterImg, x + 8, startY + 8, slotSize - 16, slotSize - 16, null);
            }

            // 3. Dessiner la bordure
            if (selectedIndex >= 0 && i == selectedIndex) {
                // Case sélectionnée : Gros contour Blanc (Style Minecraft)
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(5));
            } else {
                // Case normale : Petit contour Marron (Style Stardew)
                g2.setColor(darkBorder);
                g2.setStroke(new BasicStroke(3));
            }
            g2.drawRect(x, startY, slotSize, slotSize);

            // 4. Dessiner le chiffre de raccourci (1 à 4) en haut à gauche
            g2.setColor(new Color(255, 255, 255, 180));
            if (GameFonts.MINECRAFT_FONT != null) {
                g2.setFont(GameFonts.MINECRAFT_FONT.deriveFont(12f));
            } else {
                g2.setFont(new Font("Arial", Font.BOLD, 10));
            }
            g2.drawString(String.valueOf(i + 1), x + 4, startY + 14);
        }
        g2.dispose();
    }

    private void drawMoney(Graphics g) {
        Barn barn = world.getBarn();
        int money = barn.getMoney();
        String text = "" + money;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
        FontMetrics metrics = g2.getFontMetrics();

        int iconSize = 25;
        int paddingX = 10;
        int paddingY = 6;
        int x = 12;
        int y = 12;
        int textWidth = metrics.stringWidth(text);
        int width = iconSize + 8 + textWidth + paddingX * 2;
        int height = Math.max(iconSize, metrics.getHeight()) + paddingY * 2;

        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(x, y, width, height, 12, 12);
        g2.setColor(new Color(255, 225, 120));
        g2.drawRoundRect(x, y, width, height, 12, 12);

        int contentY = y + paddingY;
        if (slowCoinGif != null && slowCoinGif.getImage() != null) {
            g2.drawImage(slowCoinGif.getImage(), x + paddingX, contentY, iconSize, iconSize, this);
        }

        g2.setColor(Color.WHITE);
        int textX = x + paddingX + iconSize + 8;
        int textY = y + paddingY + metrics.getAscent() + (iconSize - metrics.getHeight()) / 2;
        g2.drawString(text, textX, textY);
        g2.dispose();
    }

    private void drawLevel(Graphics g) {
        src.model.Stats stats = world.getStats();
        int level = stats.getLevel();
        int exp   = stats.getExp();
        int expMax = stats.getExpForNextLevel();
        String text = "Niv." + level + "  " + exp + "/" + expMax + " XP";

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
        FontMetrics metrics = g2.getFontMetrics();

        int paddingX = 10;
        int paddingY = 6;
        int x = 12;
        // Positionné juste sous le badge argent (y=12, hauteur ≈ max(25,h)+12 ≈ 37 → on part de 55)
        int y = 55;
        int textWidth = metrics.stringWidth(text);
        int width  = textWidth + paddingX * 2;
        int height = metrics.getHeight() + paddingY * 2;

        // Fond semi-transparent
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(x, y, width, height, 12, 12);
        // Bordure violette (couleur XP)
        g2.setColor(new Color(200, 150, 255));
        g2.drawRoundRect(x, y, width, height, 12, 12);

        // Étoile + texte
        g2.setColor(Color.WHITE);
        int textY = y + paddingY + metrics.getAscent();
        g2.drawString(text, x + paddingX, textY);
        g2.dispose();
    }

    /** Méthode pour dessiner les entités (jardiniers et ennemis) à l'écran, en fonction de leur position dans le monde et de la caméra.
     * @param g le contexte graphique pour dessiner
     * @param fstTileX la coordonnée x de la première tuile visible à l'écran (en monde)
     * @param fstTileY la coordonnée y de la première tuile visible à l'écran (en monde)
     * @param pixelDiffX le décalage en pixels entre la caméra et la première tuile visible (pour un scrolling fluide)
     * @param pixelDiffY le décalage en pixels entre la caméra et la première tuile visible (pour un scrolling fluide)
     */
    private void drawEntities(Graphics g, int fstTileX, int fstTileY, int pixelDiffX, int pixelDiffY) {
        long elapsedTime = System.currentTimeMillis() - startTime;

        //  Dessin du jardinier
        for (Gardener gardener : world.getGardeners()) {
            // Savoir s'il est à l'écran
            if (gardener.getX() >= fstTileX && gardener.getX() <= fstTileX + Camera.WIDTH &&
                    gardener.getY() >= fstTileY && gardener.getY() <= fstTileY + Camera.HEIGHT) {

                // Calcul de la frame actuelle (change toutes les 150ms)
                int currentFrameIndex = (int) (elapsedTime / 150) % gardenerLoader.getNbFrames();

                BufferedImage spriteToDraw;
                int direction = gardener.getFacingDirection();

                // Sélection du sprite (marche ou attente)
                if (gardener.getCurrentState() == Gardener.State.MOVING) {
                    spriteToDraw = gardenerLoader.getWalkFrame(direction, currentFrameIndex);
                } else {
                    spriteToDraw = gardenerLoader.getIdleFrame(direction, currentFrameIndex);
                }

                // Calcul de la position à l'écran (identique aux tuiles)
                int drawX = ((gardener.getX() - fstTileX) * Display.RATIO_X) - pixelDiffX;
                int drawY = ((gardener.getY() - fstTileY) * Display.RATIO_Y) - pixelDiffY;

                // Dessiner avec effet miroir si droite
                if (direction == Entity.RIGHT) {
                    g.drawImage(spriteToDraw,
                            drawX + Display.RATIO_X, drawY,
                            drawX, drawY + Display.RATIO_Y,
                            0, 0, 24, 24, null);
                } else {
                    // Affichage normal
                    g.drawImage(spriteToDraw,
                            drawX, drawY,
                            drawX + Display.RATIO_X, drawY + Display.RATIO_Y,
                            0, 0, 24, 24, null);
                }
            }
        }

        // dessin des ennemis (poules)
        java.util.List<Chicken> enemies = world.getEnemies();

        if (enemies != null && !enemies.isEmpty()) {
            // La poule a 4 frames par animation, on change toutes les 150ms
            int currentChickenFrame = (int) (elapsedTime / 150) % 4;

            for (Chicken chicken : enemies) {
                // Vérifier si la poule est visible à l'écran
                if (chicken.getX() >= fstTileX && chicken.getX() <= fstTileX + Camera.WIDTH &&
                        chicken.getY() >= fstTileY && chicken.getY() <= fstTileY + Camera.HEIGHT) {

                    // Récupérer la bonne frame via le loader
                    BufferedImage spriteToDraw = chickenLoader.getFrame(
                            chicken.getCurrentStateActionIndex(),
                            chicken.getFacingDirection(),
                            currentChickenFrame
                    );

                    // Calcul de la position à l'écran
                    int drawX = ((chicken.getX() - fstTileX) * Display.RATIO_X) - pixelDiffX;
                    int drawY = ((chicken.getY() - fstTileY) * Display.RATIO_Y) - pixelDiffY;

                    // Les sprites de poule ont déjà un fichier "left" et un fichier "right"
                    // On les affiche donc normalement, sans avoir besoin d'inverser l'image !
                    if (spriteToDraw != null) {
                        g.drawImage(spriteToDraw,
                                drawX, drawY,
                                Display.RATIO_X, Display.RATIO_Y, null);
                    }
                }
            }
        }
    }
}


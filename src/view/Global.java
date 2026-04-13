package src.view;

import src.model.*;
import src.model.buildings.Building;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.*;
import java.util.Set;

/** Vue globale du monde : terrain + entités. C'est la classe centrale de l'affichage, elle connaît le monde et la caméra.
 * C'est elle qui dessine tout, y compris les entités (jardiniers). Elle gère aussi le highlight des cases ciblées par les actions. */
public class Global extends JPanel {
    // Références au monde et à la caméra pour savoir quoi dessiner et où
    private final World world;
    private final Camera camera;

    //variables d'animation
    private final SpriteSheetLoader gardenerLoader;
    private final long startTime;

    //Ajout l'attribut du loader
    private ChickenSpriteSheetLoader chickenLoader;

    //images pour la jauge de croissance des plantes
    private Image progressBarEmpty;
    private ImageIcon slowCoinGif;
    private int hoveredX = -1; // -1 veut dire qu'aucune case n'est survolée
    private int hoveredY = -1;

    private src.control.BuildingManager ghostManager;

    // Highlight: maintenant plusieurs tuiles peuvent être surlignées
    private final Set<Point> highlights = new HashSet<>();

    private Set<Point> selectedTilesBlueHighlight = new HashSet<Point>();

    public void setSelectedTilesBlueHighlight(Set<Point> selectedTiles) {
        this.selectedTilesBlueHighlight = selectedTiles;
        this.repaint(); // Force le redessin pour voir les changements
    }

    public Global(World world, Camera camera) {
        super();
        this.world = world;
        this.camera = camera;
        this.setOpaque(true);

        this.startTime = System.currentTimeMillis();
        // Charge la sprite sheet du personnage principal pour l'animation
        this.gardenerLoader = new SpriteSheetLoader("src/assets/Tiny Wonder Farm Free/characters/main character/walk and idle.png");
        this.chickenLoader = new ChickenSpriteSheetLoader();
        this.progressBarEmpty = new ImageIcon("src/assets/progress_bar_ui.png").getImage();
        this.slowCoinGif = new ImageIcon("src/assets/money.gif");
    }

    /** Affiche un highlight sur la case ciblée par une action (ex: la case où on veut planter). Les coordonnées sont en "monde" (pas en pixels).
     * @param wx coordonnée x de la tuile à surligner
     * @param wy coordonnée y de la tuile à surligner
     */
    public void setHighlight(int wx, int wy) {
        // Ajoute le point à l'ensemble des surlignages
        highlights.add(new Point(wx, wy));
        repaint();
    }

    /** Enlève le highlight de la case ciblée. */
    public void clearHighlight(int wx, int wy) {
        // Supprime le point de l'ensemble des surlignages
        highlights.remove(new Point(wx, wy));
        repaint();
    }

    public void setGhostBuilding(src.control.BuildingManager manager) {
        this.ghostManager = manager;
    }

    /** Retourne true si la case est surlignée */
    public boolean isHighlighted(int wx, int wy) {
        return highlights.contains(new Point(wx, wy));
    }

    /** Enlève tous les highlights */
    public void clearAllHighlights() {
        highlights.clear();
        repaint();
    }

    /** Met à jour la case actuellement survolée par la souris */
    public void setHoveredTile(int x, int y) {
        // On ne redessine que si la case a changé (pour optimiser les performances)
        if (this.hoveredX != x || this.hoveredY != y) {
            this.hoveredX = x;
            this.hoveredY = y;
            repaint();
        }
    }

    /** Méthode centrale de dessin : elle dessine le terrain en fonction de la position de la caméra, puis les entités (jardiniers) par-dessus.
     * Le dessin du terrain est optimisé pour ne dessiner que les tuiles visibles à l'écran, en calculant la première tuile à dessiner et les offsets de pixels.
     * Ensuite, elle dessine les entités (jardiniers) en fonction de leur position dans le monde et de la caméra, avec une animation basée sur le temps écoulé.
     * Le highlight est dessiné par-dessus le terrain mais en dessous des entités, pour que les jardiniers soient toujours visibles même sur une case surlignée.
     * Note : cette méthode est appelée automatiquement par Swing lorsque le panneau doit être redessiné (ex: après un repaint()), et elle doit appeler super.paintComponent(g) pour assurer un bon comportement de dessin.
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // dessiner le terrain
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
                    int paintY =
                            (y * Display.RATIO_Y) - pixelDiffY;
                    g.drawImage(tile.getSprite().getImage(), paintX, paintY, Display.RATIO_X, Display.RATIO_Y, this);
                    //On vérifie si c'est une case plantable et on dessine la plante
                    if (tile instanceof PlantTile) {
                        PlantTile casePlantable = (PlantTile) tile;
                        Plant plant = casePlantable.getPlant();

                        if (plant != null) {
                            // On suppose que la classe Plant a une méthode getSprite()
                            g.drawImage(plant.getSprite().getImage(), paintX, paintY, Display.RATIO_X, Display.RATIO_Y, this);
                        }
                    }

                }
            }
        }

        // === RENDU DU GHOST BUILDING ===
        if (ghostManager != null && ghostManager.getGhostBuilding() != null) {
            Graphics2D g3 = (Graphics2D) g.create();
            // On utilise la même épaisseur de trait que pour les highlights
            g3.setStroke(new BasicStroke(3));

            Building b = ghostManager.getGhostBuilding();
            int gx = ghostManager.getGhostX();
            int gy = ghostManager.getGhostY();

            // Calculer si on peut poser ou non
            boolean isValid = ghostManager.canPlace(gx, gy, b);

            // Définition des couleurs façon "Highlight"
            Color fillColor;
            Color borderColor;

            if (isValid) {
                // Bleu Cyan clair transparent pour le remplissage, Cyan opaque pour la bordure
                fillColor = new Color(0, 200, 255, 100);
                borderColor = new Color(0, 150, 255);
            } else {
                // Rouge transparent pour le remplissage, Rouge opaque pour la bordure
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

                // UTILISATION DE getSprite(world, x, y) POUR LE GHOST !
                ImageIcon ghostSprite = b.getSprite(world, gx, gy);
                g3.drawImage(ghostSprite.getImage(), px, py,
                        Display.RATIO_X * b.getWidth(),
                        Display.RATIO_Y * b.getHeight(), null);
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
                            int relX = (gx + dx) - fstTileX;
                            int relY = (gy + dy) - fstTileY;
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
        } catch (Throwable t) {
            // ne pas échouer le rendu si quelque chose se passe mal
        }

        // === DESSIN DES BÂTIMENTS DÉJÀ CONSTRUITS ===
        if (world.getBuildings() != null) {
            for (src.model.buildings.Building b : world.getBuildings()) {

                int relX = b.getX() - fstTileX;
                int relY = b.getY() - fstTileY;

                // Si le bâtiment est dans le champ de la caméra
                if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                    int px = (relX * Display.RATIO_X) - pixelDiffX;
                    int py = (relY * Display.RATIO_Y) - pixelDiffY;

                    ImageIcon actualSprite = b.getSprite(world, b.getX(), b.getY());
                    if (b.isGate()) {
                        Graphics2D g3 = (Graphics2D) g.create();
                        g3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // 50% transparent
                        g3.drawImage(actualSprite.getImage(), px, py,
                                Display.RATIO_X * b.getWidth(),
                                Display.RATIO_Y * b.getHeight(), null);
                        g3.dispose();
                    } else {
                        // Dessin normal opaque
                        g.drawImage(actualSprite.getImage(), px, py,
                                Display.RATIO_X * b.getWidth(),
                                Display.RATIO_Y * b.getHeight(), null);
                    }
                }
            }
        }

        // Highlight : on utilise un Graphics2D pour pouvoir dessiner des rectangles avec une bordure plus épaisse, et des couleurs semi-transparentes pour le remplissage
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
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

                    // Remplissage bleu semi-transparent
                    g2.setColor(new Color(0, 150, 255, 100));
                    g2.fillRect(hx, hy, Display.RATIO_X, Display.RATIO_Y);

                    // Bordure bleue opaque
                    g2.setColor(new Color(0, 100, 255));
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

                // On affiche les jauges si la plante existe, qu'elle n'est pas morte et pas encore mûre
                if (plant != null && plant.getState() != PlantState.MORT && plant.getState() != PlantState.EATEN && !plant.isHarvestable()) {                    // 1. Calcul des coordonnées d'affichage
                    int relX = hoveredX - fstTileX;
                    int relY = hoveredY - fstTileY;

                    // On vérifie que la case survolée est bien visible à l'écran
                    if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                        int drawX = (relX * Display.RATIO_X) - pixelDiffX;
                        int drawY = (relY * Display.RATIO_Y) - pixelDiffY;

                        //  PARAMÈTRES COMMUNS DES JAUGES
                        int barW = 48; // Largeur de la barre
                        int barH = 12; // Hauteur de la barre
                        int barX = drawX + (Display.RATIO_X / 2) - (barW / 2); // Centré horizontalement
                        int innerW = barW - 4; // Espace de remplissage (sans les bords)
                        int innerH = barH - 4;

                        // 1ère JAUGE : LA CROISSANCE (VERTE)
                        int growthBarY = drawY - 15; // Placée bien au-dessus

                        if (progressBarEmpty != null) {
                            g.drawImage(progressBarEmpty, barX, growthBarY, barW, barH, this);
                        } else {
                            g.setColor(Color.BLACK); g.drawRect(barX, growthBarY, barW, barH);
                        }

                        float growthProgress = plant.getGrowthPercentage();
                        int fillGrowthW = (int) (innerW * growthProgress);

                        if (fillGrowthW > 0) {
                            g.setColor(new Color(50, 205, 50)); // Vert
                            g.fillRect(barX + 2, growthBarY + 2, fillGrowthW, innerH);
                            g.setColor(new Color(144, 238, 144, 180)); // Reflet
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
                        if (waterProgress > 1.0f) waterProgress = 1.0f; // Sécurité si on dépasse 100%
                        if (waterProgress < 0.0f) waterProgress = 0.0f;

                        int fillWaterW = (int) (innerW * waterProgress);

                        if (fillWaterW > 0) {
                            g.setColor(new Color(30, 144, 255)); // Bleu (Dodger Blue)
                            g.fillRect(barX + 2, waterBarY + 2, fillWaterW, innerH);
                            g.setColor(new Color(135, 206, 250, 180)); // Reflet bleu clair
                            g.fillRect(barX + 2, waterBarY + 2, fillWaterW, innerH / 2);
                        }
                    }
                }
            }
        }

        // argent visible en permanence sur l'affichage principal.
        drawMoney(g);


    }

    private void drawMoney(Graphics g) {
        Barn barn = world.getBarn();
        int money = barn.getMoney();
        String text = "" + money;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setFont(GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 18f));        FontMetrics metrics = g2.getFontMetrics();

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
                            drawX + Display.RATIO_X, drawY, // Point haut-gauche destination (inversé)
                            drawX, drawY + Display.RATIO_Y, // Point bas-droite destination (inversé)
                            0, 0, 24, 24, null); // L'image source fait 24x24
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
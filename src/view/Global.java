package src.view;

import src.model.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/** Vue globale du monde : terrain + entités. C'est la classe centrale de l'affichage, elle connaît le monde et la caméra.
 * C'est elle qui dessine tout, y compris les entités (jardiniers). Elle gère aussi le highlight des cases ciblées par les actions. */
public class Global extends JPanel {
    // Références au monde et à la caméra pour savoir quoi dessiner et où
    private final World world;
    private final Camera camera;

    //variables d'animation
    private final SpriteSheetLoader gardenerLoader;
    private final long startTime;

    // Highlight: coordonnées monde de la tuile surbrillée (-1 = none)
    private int highlightX = -1;
    private int highlightY = -1;

    public Global(World world, Camera camera) {
        super();
        this.world = world;
        this.camera = camera;
        this.setOpaque(true);

        this.startTime = System.currentTimeMillis();
        // Charge la sprite sheet du personnage principal pour l'animation
        this.gardenerLoader = new SpriteSheetLoader("src/assets/Tiny Wonder Farm Free/characters/main character/walk and idle.png");
    }

    /** Affiche un highlight sur la case ciblée par une action (ex: la case où on veut planter). Les coordonnées sont en "monde" (pas en pixels).
     * @param wx coordonnée x de la tuile à surligner
     * @param wy coordonnée y de la tuile à surligner
     */
    public void setHighlight(int wx, int wy) {
        this.highlightX = wx;
        this.highlightY = wy;
        repaint();
    }

    /** Enlève le highlight de la case ciblée. */
    public void clearHighlight() {
        this.highlightX = -1;
        this.highlightY = -1;
        repaint();
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
                    int paintY = (y * Display.RATIO_Y) - pixelDiffY;
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

        // Dessiner le highlight si demandé
        if (highlightX >= 0 && highlightY >= 0) {
            int relX = highlightX - fstTileX;
            int relY = highlightY - fstTileY;
            if (relX >= 0 && relX <= Camera.WIDTH && relY >= 0 && relY <= Camera.HEIGHT) {
                int hx = (relX * Display.RATIO_X) - pixelDiffX;
                int hy = (relY * Display.RATIO_Y) - pixelDiffY;
                Graphics2D g2 = (Graphics2D) g.create();
                // overlay semi-transparent
                g2.setColor(new Color(255, 255, 0, 80));
                g2.fillRect(hx, hy, Display.RATIO_X, Display.RATIO_Y);
                // contour
                g2.setColor(new Color(255, 200, 0));
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(hx + 1, hy + 1, Display.RATIO_X - 3, Display.RATIO_Y - 3);
                g2.dispose();
            }
        }

        // dessiner les entités (jardiniers)
        drawEntities(g, fstTileX, fstTileY, pixelDiffX, pixelDiffY);
    }

    /** Méthode pour dessiner les entités (jardiniers) à l'écran, en fonction de leur position dans le monde et de la caméra.
     * Elle utilise une animation basée sur le temps écoulé pour alterner entre les frames de marche et d'attente du jardinier, en fonction de son état actuel.
     * Le dessin prend aussi en compte la direction du jardinier pour afficher le sprite dans le bon sens (mirroring pour la gauche).
     * Note : cette méthode est appelée depuis paintComponent() après avoir dessiné le terrain, pour que les jardiniers soient dessinés par-dessus le terrain.
     * @param g le contexte graphique pour dessiner
     * @param fstTileX la coordonnée x de la première tuile visible à l'écran (en monde)
     * @param fstTileY la coordonnée y de la première tuile visible à l'écran (en monde)
     * @param pixelDiffX le décalage en pixels entre la caméra et la première tuile visible (pour un scrolling fluide)
     * @param pixelDiffY le décalage en pixels entre la caméra et la première tuile visible (pour un scrolling fluide)
     */
    private void drawEntities(Graphics g, int fstTileX, int fstTileY, int pixelDiffX, int pixelDiffY) {
        // Remplacer par la vraie méthode pour obtenir vos jardiniers
        Gardener gardener = world.getGardenerTest();

        if (gardener != null) {
            // Savoir s'il est à l'écran
            if (gardener.getX() >= fstTileX && gardener.getX() <= fstTileX + Camera.WIDTH &&
                    gardener.getY() >= fstTileY && gardener.getY() <= fstTileY + Camera.HEIGHT) {

                // Calcul de la frame actuelle (change toutes les 150ms)
                long elapsedTime = System.currentTimeMillis() - startTime;
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

                // Dessiner avec effet miroir si gauche
                if (direction == Entity.RIGHT) {
                    // g.drawImage(img, destX1, destY1, destX2, destY2, srcX1, srcY1, srcX2, srcY2, observer)
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
    }
}
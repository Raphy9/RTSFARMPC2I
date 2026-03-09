package src.view;

import src.model.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class Global extends JPanel {

    private World world;
    private Camera camera;

    //variables d'animation
    private SpriteSheetLoader gardenerLoader;
    private long startTime;

    public Global(World world, Camera camera) {
        super();
        this.world = world;
        this.camera = camera;
        this.setOpaque(true);

        this.startTime = System.currentTimeMillis();
        this.gardenerLoader = new SpriteSheetLoader("src/assets/Tiny Wonder Farm Free/characters/main character/walk and idle.png");
    }
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
                    if (tile instanceof CasePlantable) {
                        CasePlantable casePlantable = (CasePlantable) tile;
                        Plant plant = casePlantable.getPlant();

                        if (plant != null) {
                            // On suppose que la classe Plant a une méthode getSprite()
                            g.drawImage(plant.getSprite().getImage(), paintX, paintY, Display.RATIO_X, Display.RATIO_Y, this);
                        }
                    }

                }
            }
        }

        // -NOUVEAU : DESSINER LES ENTITÉS
        drawEntities(g, fstTileX, fstTileY, pixelDiffX, pixelDiffY);
    }

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
                if (direction == Entity.LEFT) {
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
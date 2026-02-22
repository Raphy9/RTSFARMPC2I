package src.view;

import src.model.Camera;
import src.model.Tile;
import src.model.World;

import java.awt.*;
import javax.swing.*;


/**
 * Classe qui represente la vue de base du jeu : le terrain, les entites, etc
 * Utilise World et Camera pour permettre le scrolling
 * Permet de selectionner les cases et les entites pour afficher des popups d'information ou des menus d'action
 * Permet l'activation / desactivation du Panel de controle
 */
public class Global extends JPanel {

    private World world;
    private Camera camera;

    public Global(World world, Camera camera) {
        super();
        this.world = world;
        this.camera = camera;
        this.setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // On récupère la première case où la caméra vise
        int fstTileX = (int) camera.getX();
        int fstTileY = (int) camera.getY();

        // Calcul du décalage en pixels (exemple si x = 5.2, le décalage est de 0.2 * 64 = 12 pixels)
        int pixelDiffX = (int) ((camera.getX() - fstTileX) * Display.RATIO_X);
        int pixelDiffY = (int) ((camera.getY() - fstTileY) * Display.RATIO_Y);

        // On fait bien un décalage pour prendre les derniers tiles coupés à droite de l'écran
        for (int x = 0; x <= Camera.WIDTH; x++) {
            for (int y = 0; y <= Camera.HEIGHT; y++) {
                // Coordonnées de là où l'on va dessiner sur le panel
                int worldX = fstTileX + x;
                int worldY = fstTileY + y;

                // On s'assure ne pas dessiner des cases non existantes (en dehors de la matrice de tuiles)
                if (worldX >= 0 && worldX < World.WIDTH && worldY >= 0 && worldY < World.HEIGHT) {
                    Tile tile = world.getTile(worldX, worldY);

                    // On dessine la tuile avec ses coordonnées moins le décalage en pixels
                    int paintX = (x * Display.RATIO_X) - pixelDiffX;
                    int paintY = (y * Display.RATIO_Y) - pixelDiffY;

                    g.drawImage(tile.getSprite().getImage(), paintX, paintY, Display.RATIO_X, Display.RATIO_Y, this);
                }
            }
        }
    }
}
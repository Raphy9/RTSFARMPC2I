package src.view;

import src.control.GlobalController;
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
        // Dessiner les tuiles visibles en fonction de la position de la caméra
        // On dessine les tuiles dans la caméra, même les portions de tuiles qui sont partiellement visibles
        for (int x = 0; x < Camera.WIDTH; x++) {
            for (int y = 0; y < Camera.HEIGHT; y++) {
                float paintX = camera.getX() + x;
                float paintY = camera.getY() + y;
                Tile tile = world.getTile((int) paintX, (int) paintY);
                g.drawImage(tile.getSprite().getImage(), x * Display.RATIO_X, y * Display.RATIO_Y, Display.RATIO_X, Display.RATIO_Y, this);
            }
        }
    }
}
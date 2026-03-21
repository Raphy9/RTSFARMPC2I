package src.control;

import src.model.*;
import src.view.*;

import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

/** Classe qui gère les interactions de l'utilisateur avec la vue globale,
 * comme les clics de souris pour sélectionner des cases ou des entités */
public class GlobalController implements MouseListener{

    private Display display;
    private World world;

    public GlobalController(Display display, Global globalView, World world) {
        globalView.addMouseListener(this);
        this.display = display;
        this.world = world;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Selectionner la case
        Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());
        Tile tile = world.getTile(coords.x, coords.y);
//        System.out.println("Clic sur la case: " + tile.getX() + ", " + tile.getY());
        // Si la case continent un jardinier, le selectionner
        for (Entity entity : tile.getEntities()) {
            if (entity instanceof Gardener) {
                System.out.println("Clic sur un jardinier! " + entity);
                // Enchaine avec le popup d'actions pour le jardinier selectionne
                display.switchToPopup(new ActionsPopup(display, world, (Gardener) entity));
                return;
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
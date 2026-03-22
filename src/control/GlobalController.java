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
        Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());

        if (coords.x < 0 || coords.x >= World.WIDTH || coords.y < 0 || coords.y >= World.HEIGHT) {
            return;
        }

        Tile tile = world.getTile(coords.x, coords.y);

        // 1. Si on clique sur le jardinier : Ouvre le Menu
        for (src.model.Entity entity : tile.getEntities()) {
            if (entity instanceof src.model.Gardener) {
                System.out.println("Clic sur le jardinier -> Ouverture du menu");
                display.switchToPopup(new src.view.ActionsPopup(display, world, (src.model.Gardener) entity));
                return;
            }
        }

        // 2. Si on clique ailleurs : Déplacement (TEMPORAIRE)
        src.model.Gardener gardener = world.getGardenerTest();
        if (gardener != null && tile.isWalkable()) {
            System.out.println("Clic sur case vide -> Envoi de l'ordre MoveAction vers (" + coords.x + ", " + coords.y + ")");

            int dx = coords.x - gardener.getX();
            int dy = coords.y - gardener.getY();
            if (Math.abs(dx) > Math.abs(dy)) {
                gardener.setFacingDirection(dx > 0 ? src.model.Entity.RIGHT : src.model.Entity.LEFT);
            } else if (dy != 0) {
                gardener.setFacingDirection(dy > 0 ? src.model.Entity.DOWN : src.model.Entity.UP);
            }

            gardener.interruptGardener();
            gardener.addAction(new src.model.actions.MoveAction(coords.x, coords.y));
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
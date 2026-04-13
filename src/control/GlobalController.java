package src.control;

import src.model.*;
import src.view.*;

import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import java.awt.event.MouseMotionListener;

/** Classe qui gère les interactions de l'utilisateur avec la vue globale,
 * comme les clics de souris pour sélectionner des cases ou des entités */
public class GlobalController implements MouseListener, MouseMotionListener{

    private final Display display;
    private final World world;

    public GlobalController(Display display, Global globalView, World world) {
        globalView.addMouseListener(this);
        globalView.addMouseMotionListener(this);
        this.display = display;
        this.world = world;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());

        // Vérifier que les coordonnées sont dans les limites du monde
        if (coords.x < 0 || coords.x >= World.WIDTH || coords.y < 0 || coords.y >= World.HEIGHT) {
            return;
        }

        Tile tile = world.getTile(coords.x, coords.y);

        // 0. Si on clique sur la grange -> Ouvrir PopupBarn
        if (world.isBarnAt(coords.x, coords.y)) {
            System.out.println("Clic sur la grange -> Ouverture PopupBarn");
            display.switchToPopup(new src.view.PopupBarn(display, world));
            return;
        }

        // 1. Si on clique sur une entité (Poule ou Jardinier)
        for (src.model.Entity entity : tile.getEntities()) {
            if (entity instanceof src.model.Chicken) {
                src.model.Chicken chicken = (src.model.Chicken) entity;
                chicken.flee();
                return;
            }
            if (entity instanceof src.model.Gardener) {
                System.out.println("Clic sur le jardinier -> Ouverture du menu");
                display.switchToPopup(new src.view.ActionsPopup(display, world, (src.model.Gardener) entity));
                return;
            }
        }

        if (world.getGardeners() != null && !world.getGardeners().isEmpty()) {
            Gardener gardener = world.getGardeners().get(0);
            int hotbarIndex = gardener.getSelectedHotbarIndex();
            java.awt.event.ActionEvent fakeEvent = new java.awt.event.ActionEvent(this, java.awt.event.ActionEvent.ACTION_PERFORMED, "");

            if (hotbarIndex == 0) { // Case 1 : LABOURER
                new src.control.popups.PlowActionSelector(display, world, gardener).actionPerformed(fakeEvent);
            }
            else if (hotbarIndex == 1) { // Case 2 : ARROSER
                new src.control.popups.WaterActionSelector(display, world, gardener).actionPerformed(fakeEvent);
            }
            else if (hotbarIndex == 2) { // Case 3 : PLANTER
                System.out.println("Mode Plantation activé via Hotbar");
                new src.control.popups.PlantActionSelector(display, world, gardener).actionPerformed(fakeEvent);
            }
            else if (hotbarIndex == 3) { // Case 4 : RÉCOLTER
                System.out.println("Mode Récolte activé via Hotbar");
                new src.control.popups.HarvestActionSelector(display, gardener, world).actionPerformed(fakeEvent);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());
        if (coords.x >= 0 && coords.x < World.WIDTH && coords.y >= 0 && coords.y < World.HEIGHT) {
            display.getGlobalView().setHoveredTile(coords.x, coords.y);
        } else {
            display.getGlobalView().setHoveredTile(-1, -1);
        }
    }
}
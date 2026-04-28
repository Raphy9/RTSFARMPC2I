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
    private boolean ignoreNextClick = false;

    public GlobalController(Display display, Global globalView, World world) {
        globalView.addMouseListener(this);
        globalView.addMouseMotionListener(this);
        this.display = display;
        this.world = world;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (display.getBuildingManager() != null) {
            // Si on a le bâtiment au bout de la souris, on ignore le clic
            if (display.getBuildingManager().isPlacing() || display.getBuildingManager().isDeletionMode()) {
                return;
            }
            // Si le manager vient TOUT JUSTE de poser/supprimer un bâtiment (il y a moins de 200ms)
            if (display.getBuildingManager().hasJustActed()) {
                return;
            }
        }

        if (!display.getGlobalView().isHotbarVisible()) {
            return;
        }

        // --- Détection du clic sur la hotbar ---
        if (display.getGlobalView().isHotbarVisible() &&
                world.getGardeners() != null && !world.getGardeners().isEmpty()) {
            int nbSlots = 4;
            int slotSize = 52;
            int spacing = 8;
            int totalWidth = (slotSize * nbSlots) + (spacing * (nbSlots - 1));
            int panelWidth = display.getGlobalView().getWidth();
            int panelHeight = display.getGlobalView().getHeight();
            int startX = (panelWidth - totalWidth) / 2;
            int startY = panelHeight - slotSize - 50;

            if (e.getY() >= startY && e.getY() <= startY + slotSize) {
                for (int i = 0; i < nbSlots; i++) {
                    int slotX = startX + i * (slotSize + spacing);
                    if (e.getX() >= slotX && e.getX() <= slotX + slotSize) {
                        Gardener gardener = world.getAvailableGardener();
                        if (gardener == null) return;
                        // Indicateur visuel sur le jardinier 0
                        world.getGardeners().get(0).setSelectedHotbarIndex(i);
                        display.getGlobalView().repaint();
                        display.triggerHotbarAction(i, gardener);
                        return;
                    }
                }
            }
        }

        Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());

        // Vérifier que les coordonnées sont dans les limites du monde
        if (coords.x < 0 || coords.x >= World.WIDTH || coords.y < 0 || coords.y >= World.HEIGHT) {
            return;
        }

        Tile tile = world.getTile(coords.x, coords.y);

        // 0. Si on clique sur la grange ou une case voisine -> Ouvrir PopupBarn
        if (world.isBarnInside(coords.x, coords.y)) {
            System.out.println("Clic proche de la grange -> Ouverture PopupBarn");
            display.switchToPopup(new src.view.PopupBarn(display, world));
            return;
        }

        // 1. Si on clique sur une entité (Poule, Corbeau ou Jardinier)
        src.model.Chicken chickenToClick = null;
        src.model.Crow crowToClick = null; // --- NOUVEAU : Corbeau ---
        src.model.Gardener gardenerToClick = null;

        for (src.model.Entity entity : tile.getEntities()) {
            if (chickenToClick == null && entity instanceof src.model.Chicken) {
                chickenToClick = (src.model.Chicken) entity;
            } else if (crowToClick == null && entity instanceof src.model.Crow) {
                crowToClick = (src.model.Crow) entity;
            } else if (gardenerToClick == null && entity instanceof src.model.Gardener) {
                gardenerToClick = (src.model.Gardener) entity;
            }
        }

        if (chickenToClick != null) {
            chickenToClick.flee();
            world.registerQuestAction(Quests.ACTION_CHASE_CHICKEN);
            return;
        }

        // --- NOUVEAU : Fait fuir le corbeau s'il est cliqué ---
        if (crowToClick != null) {
            crowToClick.flee();
            world.registerQuestAction(Quests.ACTION_CHASE_CHICKEN); // On compte ça comme "chasser une nuisance" pour les quêtes
            return;
        }

        if (gardenerToClick != null) {
            System.out.println("Clic sur le jardinier -> Ouverture du menu");
            display.switchToPopup(new src.view.ActionsPopup(display, world, gardenerToClick));
            return;
        }

        // Le clic sur le monde sans entité ne déclenche plus d'action automatiquement.
        // Les actions sont déclenchées par clic direct sur la hotbar ou via les touches 1-4.
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
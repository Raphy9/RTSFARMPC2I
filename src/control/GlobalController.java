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

    // Références à la display et au monde pour pouvoir interagir avec eux
    private final Display display;
    private final World world;

    /** Le constructeur reçoit la display pour pouvoir ajouter le MouseListener et interagir avec la vue globale,
     * et le monde pour pouvoir interagir avec les entités et les tuiles du monde en fonction des clics de l'utilisateur
     * @param display la display pour ajouter le MouseListener et interagir avec la vue globale
     * @param globalView la vue globale pour ajouter le MouseListener
     * @param world le monde pour interagir avec les entités et les tuiles du monde en fonction des clics de l'utilisateur
     */
    public GlobalController(Display display, Global globalView, World world) {
        globalView.addMouseListener(this);
        globalView.addMouseMotionListener(this);
        this.display = display;
        this.world = world;
    }

    /** Lorsque l'utilisateur clique sur la vue globale, cette méthode est appelée.
     * Elle doit déterminer ce qui a été cliqué (une tuile vide, un jardinier, la grange, etc.) et réagir en conséquence :
     * - Si on clique sur un jardinier : Ouvre le menu d'actions pour ce jardinier
     * - Si on clique sur la grange : Ouvre le menu de la grange si le jardinier est à côté, sinon déplace le jardinier vers la grange
     * - Si on clique sur une tuile vide : Envoie une MoveAction au jardinier pour se déplacer vers cette tuile
     * Note : les déplacements vers la grange doivent utiliser la méthode utilitaire du world pour trouver la meilleure tuile adjacente marchable à la grange
     * et planifier le déplacement du jardinier vers cette tuile avant d'ouvrir le menu de la grange une fois arrivé
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());

        // Vérifier que les coordonnées sont dans les limites du monde
        if (coords.x < 0 || coords.x >= World.WIDTH || coords.y < 0 || coords.y >= World.HEIGHT) {
            return;
        }

        Tile tile = world.getTile(coords.x, coords.y);

        // 0. Si on clique sur la tuile de la grange -> Ouvrir PopupBarn seulement si jardinier proche (distance Manhattan <= 1, diagonales exclues)
        if (world.isBarnAt(coords.x, coords.y)) {
            Gardener g = world.getGardenerTest();
            if (g != null) { // vérifier la distance entre le jardinier et la grange
                int dx = Math.abs(g.getX() - coords.x);
                int dy = Math.abs(g.getY() - coords.y);
                int manh = dx + dy;
                if (manh <= 1) { // le jardinier est déjà à côté de la grange, on peut ouvrir directement le menu de la grange
                    System.out.println("Clic sur la grange -> Ouverture PopupBarn");
                    display.switchToPopup(new src.view.PopupBarn(display, world, world.getGardenerTest()));
                } else { // le jardinier n'est pas à côté de la grange, il faut le déplacer vers la grange avant d'ouvrir le menu
                    // Utiliser la méthode utilitaire du world pour trouver la meilleure tuile adjacente
                    Point barnAdj = world.findClosestWalkableAdjacent(world.getBarnX(), world.getBarnY(), g);
                    // Interrompre les actions en cours du jardinier avant de planifier le déplacement
                    g.interruptGardener();
                    if (barnAdj != null) { // trouvé une tuile adjacente marchable -> planifier le déplacement vers cette tuile
                        // demander highlight mais SUR LA CASE SELECTIONNEE (la grange) et planifier le déplacement
                        display.getGlobalView().setHighlight(coords.x, coords.y);
                        Runnable onArrival = () -> SwingUtilities.invokeLater(() -> {
                            display.getGlobalView().clearHighlight(coords.x, coords.y);
                            display.switchToPopup(new src.view.PopupBarn(display, world, g));
                        });
                        g.addAction(new src.model.actions.MoveAction(barnAdj.x, barnAdj.y, onArrival)); // planifier le déplacement vers la tuile adjacente de la grange, avec un callback pour ouvrir le menu une fois arrivé
                        System.out.println("Jardinier déplacé vers la tuile proche de la grange: (" + barnAdj.x + ", " + barnAdj.y + ")"); // log pour vérifier que le déplacement est planifié vers la bonne tuile
                    } else { // Pas trouvé de tuile adjacente marchable -> tenter d'aller aux coordonnées de la grange (même si c'est pas censé marcher, au moins ça montre l'intention de déplacement vers la grange)
                        // Pas trouvé de tuile adjacente marchable -> tenter d'aller aux coordonnées de la grange
                        display.getGlobalView().setHighlight(coords.x, coords.y);
                        Runnable onArrival = () -> SwingUtilities.invokeLater(() -> {
                            display.getGlobalView().clearHighlight(coords.x, coords.y);
                            display.switchToPopup(new src.view.PopupBarn(display, world, g));
                        });
                        g.addAction(new src.model.actions.MoveAction(world.getBarnX(), world.getBarnY(), onArrival));
                        System.out.println("Jardinier déplacé vers la grange aux coordonnées: (" + world.getBarnX() + ", " + world.getBarnY() + ")");
                    }
                }
            }
            return;
        }

        // 1. Si on clique sur le jardinier : Ouvre le Menu
        for (src.model.Entity entity : tile.getEntities()) {
            // Si on clique sur une poule, on la chasse (sans ouvrir de menu)
            if (entity instanceof src.model.Chicken) {
                src.model.Chicken chicken = (src.model.Chicken) entity;
                chicken.flee(); // Arrête le thread de la poule
                return; // On arrête l'interaction ici
            }
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
            // Afficher un highlight sur la case cible pendant le déplacement et l'effacer à l'arrivée
            display.getGlobalView().setHighlight(coords.x, coords.y);
            Runnable clearHighlight = () -> SwingUtilities.invokeLater(() -> display.getGlobalView().clearHighlight(coords.x, coords.y));
            gardener.addAction(new src.model.actions.MoveAction(coords.x, coords.y, clearHighlight));
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

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // On utilise votre méthode existante pour trouver la case survolée
        Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());

        //  On vérifie qu'on ne sort pas de la carte
        if (coords.x >= 0 && coords.x < World.WIDTH && coords.y >= 0 && coords.y < World.HEIGHT) {
            // On envoie la coordonnée à la vue
            display.getGlobalView().setHoveredTile(coords.x, coords.y);
        } else {
            // Si on sort de la carte, on efface la jauge (-1, -1)
            display.getGlobalView().setHoveredTile(-1, -1);
        }
    }
}
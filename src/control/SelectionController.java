package src.control;

import src.model.Tile;
import src.model.World;
import src.view.Display;
import src.view.Global;
import src.view.Selection;
import src.view.TextPopup;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Predicate;

/** Classe qui gère les interactions de l'utilisateur avec la vue selection,
 * principalement la selection de l'objet demande */
public class SelectionController implements MouseListener {

    private Display display;
    private World world;
    // Critere de selection pour les cases, par exemple "case avec une plante" ou "case vide
    private Predicate<Tile> selectionCriteria = tile -> true;    // par defaut accepte toutes les cases

    /** Constructeur du controleur de selection
     * @param display la classe d'affichage
     */
    public SelectionController(Display display, World world) {
        this.display = display;
        this.world = world;
    }

    /** Change le critere de selection pour les cases. A utiliser avant de lancer la vue selection pour definir le type de case a selectionner
     * @param selectionCriteria le nouveau critere de selection pour les cases, par exemple "case avec une plante" ou "case vide,
     * fonction Tile -> boolean qui retourne true si la case est acceptee par la selection, false sinon
     */
    public void setSelectionCriteria(Predicate<Tile> selectionCriteria) {
        this.selectionCriteria = selectionCriteria;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Selectionner la case
        Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());
        Tile tile = world.getTile(coords.x, coords.y);
        // Determiner si la case selectionnee correspond au critere de selection
        if (selectionCriteria.test(tile)) {
            System.out.println("Case selectionnee! : " + tile.getX() + ", " + tile.getY());
            // TODO : enchaine avec l'action associee a la case selectionnee, par exemple planter une graine si on est en train de selectionner une case pour planter
            display.switchToGlobal();
        } else {
            // pour le moment
            System.out.println("Case non valide pour la selection! : " + tile.getX() + ", " + tile.getY());
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
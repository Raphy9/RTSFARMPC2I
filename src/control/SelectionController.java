package src.control;

import src.model.PlantTile;
import src.model.Tile;
import src.model.World;
import src.model.actions.ActionBuilder;
import src.view.Display;
import src.view.Global;
import src.view.Selection;
import src.view.TextPopup;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyListener;
import java.util.function.Predicate;
import java.util.Set;
import java.util.HashSet;

/** Classe qui gère les interactions de l'utilisateur avec la vue selection,
 * principalement la selection de l'objet demande */
public class SelectionController implements MouseListener, KeyListener {

    private Display display;
    private World world;
    private ActionBuilder builder;    // pour stocker l'action en construction et pouvoir lui passer les infos de la case selectionnee
    // Critere de selection pour les cases, par exemple "case avec une plante" ou "case vide
    private Predicate<Tile> selectionCriteria = tile -> true;    // par defaut accepte toutes les cases
    private Set<Point> currentSelectionForHighlight = new HashSet<Point>();

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

    /** Change le builder d'action en construction, pour pouvoir lui passer les infos de la case selectionnee
     * A utiliser avant de lancer la vue selection pour definir l'action en construction a laquelle la selection doit fournir des infos
     * @param builder le builder d'action en construction, pour pouvoir lui passer les infos de la case selectionnee
     */
    public void setActionBuilder(ActionBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());
        Tile tile = world.getTile(coords.x, coords.y);
        Point targetPoint = new Point(coords.x, coords.y);

        if (selectionCriteria.test(tile)) {
            // SHIFT + Clic : Sélectionne toute la parcelle
            if (e.isShiftDown() && tile instanceof PlantTile) {
                for (PlantTile pt : ((PlantTile) tile).getParcel().getTiles()) {
                    if (selectionCriteria.test(pt)) {
                        Point p = new Point(pt.getX(), pt.getY());
                        builder.addTarget(p);
                        display.getGlobalView().setHighlight(p.x, p.y);
                    }
                }
            }
            // CTRL + Clic : Ajoute ou retire une case spécifique
            else if (e.isControlDown()) {
                if (builder.getSelectedPoints().contains(targetPoint)) {
                    builder.removeTarget(targetPoint);
                    display.getGlobalView().clearHighlight(targetPoint.x, targetPoint.y);
                } else {
                    builder.addTarget(targetPoint);
                    display.getGlobalView().setHighlight(targetPoint.x, targetPoint.y);
                }
            }
            // Clic normal : Sélection unique
            else {
                for (Point p : builder.getSelectedPoints()) {
                    display.getGlobalView().clearHighlight(p.x, p.y);
                }
                builder.clearTargets();
                builder.addTarget(targetPoint);
                display.getGlobalView().setHighlight(targetPoint.x, targetPoint.y);
            }
        }
        display.getSelectionView().setSelectedTilesBlueHighlight(
                new java.util.HashSet<>(builder.getSelectedPoints())
        );
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
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Validation avec la touche ENTRÉE
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!builder.getSelectedPoints().isEmpty()) {

                // On nettoie le visuel bleu
                display.getSelectionView().setSelectedTilesBlueHighlight(new java.util.HashSet<>());

                // On nettoie le visuel jaune
                display.switchToGlobal();

                // On construit l'action avec les cases sélectionnées
                builder.buildAction();

                // On vide pour la prochaine fois
                builder.clearTargets();
            }
        }

        // Annulation avec la touche ÉCHAP
        else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {

            // Nettoyage du jaune de la vue globale
            display.getGlobalView().clearAllHighlights();

            // Nettoyage de l'écran BLEU de la vue de sélection
            display.getSelectionView().setSelectedTilesBlueHighlight(new java.util.HashSet<>());

            // On vide la mémoire du builder
            builder.clearTargets();

            // On ferme le menu
            display.switchToGlobal();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

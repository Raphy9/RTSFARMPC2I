package src.control;

import src.model.PlantTile;
import src.model.Tile;
import src.model.World;
import src.model.actions.ActionBuilder;
import src.view.Display;

import java.awt.*;
import java.awt.event.*;
import java.util.function.Predicate;
import java.util.HashSet;
import javax.swing.SwingUtilities;

/** Classe qui gère les interactions de l'utilisateur avec la vue selection,
 * principalement la selection de l'objet demande */
public class SelectionController implements MouseListener, MouseMotionListener, KeyListener {

    private Display display;
    private World world;
    private ActionBuilder builder;
    private Predicate<Tile> selectionCriteria = tile -> true;

    /** true quand le bouton gauche est maintenu enfoncé */
    private boolean dragging = false;
    /** true si la souris a bougé pendant le drag (= drag réel, pas un simple clic) */
    private boolean movedDuringDrag = false;
    /** Dernière case traitée pendant un drag, pour éviter de retraiter la même case */
    private Point lastDragPoint = null;

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

    // -------------------------------------------------------
    // MouseListener
    // -------------------------------------------------------

    @Override
    public void mouseClicked(MouseEvent e) {
        // Si on vient d'un drag, ne pas retraiter ici
        if (movedDuringDrag) {
            movedDuringDrag = false;
            return;
        }
        if (!SwingUtilities.isLeftMouseButton(e)) return;

        Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());
        Tile tile = world.getTile(coords.x, coords.y);
        Point targetPoint = new Point(coords.x, coords.y);

        if (selectionCriteria.test(tile)) {
            // Shift + Clic : sélectionne toute la parcelle d'un coup
            if (e.isShiftDown() && tile instanceof PlantTile) {
                for (PlantTile pt : ((PlantTile) tile).getParcel().getTiles()) {
                    if (selectionCriteria.test(pt)) {
                        Point p = new Point(pt.getX(), pt.getY());
                        builder.addTarget(p);
                        display.getGlobalView().setHighlight(p.x, p.y);
                    }
                }
            }
            // Clic simple : toggle (ajoute ou retire)
            else if (builder.getSelectedPoints().contains(targetPoint)) {
                builder.removeTarget(targetPoint);
                display.getGlobalView().clearHighlight(targetPoint.x, targetPoint.y);
            } else {
                builder.addTarget(targetPoint);
                display.getGlobalView().setHighlight(targetPoint.x, targetPoint.y);
            }
        }
        display.getSelectionView().setSelectedTilesBlueHighlight(
                new HashSet<>(builder.getSelectedPoints())
        );
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            dragging = true;
            movedDuringDrag = false;
            lastDragPoint = null;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragging = false;
        lastDragPoint = null;
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // -------------------------------------------------------
    // MouseMotionListener : sélection par glissement (drag simple)
    // -------------------------------------------------------

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!dragging) return;

        Point coords;
        try {
            coords = display.getCamera().screenToWorld(e.getX(), e.getY());
        } catch (Exception ex) { return; }

        Point targetPoint = new Point(coords.x, coords.y);
        if (targetPoint.equals(lastDragPoint)) return; // même case, on ignore
        lastDragPoint = targetPoint;
        movedDuringDrag = true;

        try {
            Tile tile = world.getTile(coords.x, coords.y);
            if (selectionCriteria.test(tile) && !builder.getSelectedPoints().contains(targetPoint)) {
                builder.addTarget(targetPoint);
                display.getGlobalView().setHighlight(targetPoint.x, targetPoint.y);
                display.getSelectionView().setSelectedTilesBlueHighlight(
                        new HashSet<>(builder.getSelectedPoints())
                );
            }
        } catch (IndexOutOfBoundsException ex) { /* hors limites, on ignore */ }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    // -------------------------------------------------------
    // KeyListener
    // -------------------------------------------------------

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        // Validation avec la touche ENTRÉE
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!builder.getSelectedPoints().isEmpty()) {

                // On nettoie le visuel bleu
                display.getSelectionView().setSelectedTilesBlueHighlight(new HashSet<>());

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
            display.getSelectionView().setSelectedTilesBlueHighlight(new HashSet<>());

            // On vide la mémoire du builder
            builder.clearTargets();

            // On ferme le menu
            display.switchToGlobal();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}

package src.control.popups;

import src.model.*;
import src.model.actions.HarvestActionBuilder;
import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Predicate;

/** Controleur du bouton recolter dans le menu de choix d'action
 * Lance le mode de selection d'une plante à recolter
 */
public class HarvestActionSelector implements ActionListener {
    // Le constructeur reçoit la display pour pouvoir lancer le mode de selection et
    private Display display;
    private HarvestActionBuilder builder;

    /** Le constructeur reçoit la display pour pouvoir lancer le mode de selection et
     * builder pour construire l'action une fois la case selectionnee
     * @param display la display pour lancer le mode de selection
     * @param gardener le jardinier pour construire le HarvestActionBuilder
     */
    public HarvestActionSelector(Display display, Gardener gardener, World world) {
        this.display = display;
        this.builder = new HarvestActionBuilder(gardener, world);
        this.builder.setDisplay(display);
    }

    /** Lorsque le bouton est clique, on lance le mode de selection de la display avec un critere de recolte.
     * La display doit alors permettre à l'utilisateur de cliquer sur une plante valide pour récolter, et une fois la plante sélectionnée,
     * elle doit appeler builder.buildAction() pour construire et ajouter l'action au jardinier.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Le critere : La case doit avoir une plante mure (MATURE), morte (MORT), ou mangée (EATEN)
        Predicate<Tile> criteria = tile -> {
            if (tile instanceof PlantTile) {
                Plant p = ((PlantTile) tile).getPlant();
                return p != null && (p.isHarvestable() || p.getState() == src.model.PlantState.MORT || p.getState() == src.model.PlantState.EATEN);
            }
            return false;
        };

        // On lance directement la sélection sur le terrain !
        display.switchToSelection(criteria, "Sélectionnez une plante à récolter/nettoyer", builder);
    }

}
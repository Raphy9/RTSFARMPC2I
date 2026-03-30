package src.control.popups;

import src.model.*;
import src.model.actions.HarvestActionBuilder;
import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Predicate;

/** Controleur du bouton récolter dans le menu de choix d'action
 * Lance le mode de sélection d'une plante à récolter
 */
public class HarvestActionSelector implements ActionListener {
    // Le constructeur reçoit la display pour pouvoir lancerle mode de sélection et
    private Display display;
    private HarvestActionBuilder builder;

    /** Le constructeur reçoit la display pour pouvoir lancer le mode de sélection et
     * builder pour construire l'action une fois la case sélectionnée
     * @param display la display pour lancer le mode de sélection
     * @param gardener le jardinier pour construire le HarvestActionBuilder
     */
    public HarvestActionSelector(Display display, Gardener gardener, World world) {
        this.display = display;
        this.builder = new HarvestActionBuilder(gardener, world);
        this.builder.setDisplay(display);
    }

    /** Lorsque le bouton est cliqué, on lance le mode de sélection de la display avec un critère de récolte.
     * La display doit alors permettre à l'utilisateur de cliquer sur une plante valide pour récolter, et une fois la plante sélectionnée,
     * elle doit appeler builder.buildAction() pour construire et ajouter l'action au jardinier.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Le critère : La case doit avoir une plante mûre (MATURE), morte (MORT), ou mangée (EATEN)
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
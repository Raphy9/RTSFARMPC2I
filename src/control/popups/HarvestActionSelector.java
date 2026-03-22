package src.control.popups;

import src.model.PlantTile;
import src.model.Gardener;
import src.model.Plant;
import src.model.Tile;
import src.model.actions.HarvestActionBuilder;
import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Predicate;

public class HarvestActionSelector implements ActionListener {
    private Display display;
    private HarvestActionBuilder builder;

    public HarvestActionSelector(Display display, Gardener gardener) {
        this.display = display;
        this.builder = new HarvestActionBuilder(gardener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Le critère : La case doit être plantable, avoir une plante, et être mûre (MATURE)
        Predicate<Tile> criteria = tile -> {
             if (tile instanceof PlantTile) {
                Plant p = ((PlantTile) tile).getPlant();
                return p != null && (p.isHarvestable() || p.getState() == src.model.PlantState.MORT);            }
            return false;
        };

        // On lance directement la sélection sur le terrain !
        display.switchToSelection(criteria, "Sélectionnez une plante à récolter", builder);
    }
}
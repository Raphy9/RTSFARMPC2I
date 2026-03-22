package src.control.popups;

import src.model.Gardener;
import src.model.Plant;
import src.model.PlantTile;
import src.model.actions.WaterActionBuilder;
import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WaterActionSelector implements ActionListener {
        private Display display;
        private WaterActionBuilder builder;

        public WaterActionSelector(Display display, Gardener gardener) {
            this.display = display;
            this.builder = new WaterActionBuilder(gardener);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Le critère : La case doit être type PlantTile et avoir une plante
            display.switchToSelection(tile -> {
                if (tile instanceof PlantTile) {
                    Plant p = ((PlantTile) tile).getPlant();
                    return p != null; // On peut arroser une plante même si elle n'est pas encore mûre
                }
                return false;
            }, "Sélectionnez une plante à arroser", builder);
        }
}

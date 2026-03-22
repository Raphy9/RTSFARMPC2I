package src.control.popups;

import src.model.Gardener;
import src.model.Plant;
import src.model.PlantTile;
import src.model.World;
import src.model.actions.WaterActionBuilder;
import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Permet de sélectionner une plante à arroser, puis de construire l'action d'arrosage correspondante.
 */
public class WaterActionSelector implements ActionListener {
        private Display display;
        private WaterActionBuilder builder;

        /** Le constructeur reçoit la display pour pouvoir lancer le mode de sélection et
         * builder pour construire l'action une fois la case sélectionnée
         * @param display la display pour lancer le mode de sélection
         * @param world le monde pour construire le WaterActionBuilder
         * @param gardener le jardinier pour construire le WaterActionBuilder
         */
        public WaterActionSelector(Display display, World world, Gardener gardener) {
            this.display = display;
            this.builder = new WaterActionBuilder(gardener, world);
            this.builder.setDisplay(display);
        }

        /** Lorsque le bouton est cliqué, on lance le mode de sélection de la display pour sélectionner une plante à arroser, avec un critère d'arrosage (case doit être type PlantTile et avoir une plante)
         * et une fois la case sélectionnée, il doit construire une WaterAction avec la plante sélectionnée
         * et l'ajouter au jardinier.
         * Note : le mode de sélection de la display doit être lancé à partir du builder pour pouvoir construire l'action une fois la case sélectionnée
         */
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

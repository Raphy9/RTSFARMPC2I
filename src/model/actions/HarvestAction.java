package src.model.actions;

import src.model.PlantTile;
import src.model.Gardener;
import src.model.Item;
import src.model.ItemPlant;
import src.model.Plant;
import src.model.PlantType;
import src.model.Tile;
import src.model.World;

public class HarvestAction extends Action {

    public HarvestAction(int targetX, int targetY) {
        super(targetX, targetY);
    }

    @Override
    public void perform(Gardener gardener, World world) {
        Tile tile = world.getTile(targetX, targetY);

        if (tile instanceof PlantTile) {
            PlantTile parcel = (PlantTile) tile;
            Plant plant = parcel.getPlant();

            if (plant != null && plant.isHarvestable()) {
                PlantType type = plant.getType();

                // 1. Vider la case (votre méthode recolter() met this.plant à null)
                // Au prochain tick graphique (30 FPS), la plante disparaîtra de l'écran !
                parcel.recolter();

                // 2. Ajouter la plante récoltée à l'inventaire de la grange
                boolean found = false;
                for (Object obj : world.getBarn().getItems()) {
                    Item item = (Item) obj;
                    // Si on a déjà ce type de plante, on augmente juste la quantité
                    if (item instanceof ItemPlant && item.getPlantType() == type) {
                        item.addQuantity(1);
                        found = true;
                        break;
                    }
                }

                // Si on n'en avait pas encore, on crée un nouveau "tas" de 1
                if (!found) {
                    world.getBarn().addItem(new ItemPlant(type, 1));
                }

                System.out.println("Succès : Le jardinier a récolté " + type.getName() + " !");
            } else {
                System.out.println("Échec : La plante n'est pas prête ou a disparu.");
            }
        }
    }
}
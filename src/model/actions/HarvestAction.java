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

            if (plant != null) {

                //  La plante est mûre (Récolte normale)
                if (plant.isHarvestable()) {
                    PlantType type = plant.getType();

                    parcel.harvest(); // Vide la case

                    // Ajout à l'inventaire
                    boolean found = false;
                    for (Object obj : world.getBarn().getItems()) {
                        Item item = (Item) obj;
                        if (item instanceof ItemPlant && item.getPlantType() == type) {
                            item.addQuantity(1);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        world.getBarn().addItem(new ItemPlant(type, 1));
                    }
                    System.out.println("Succès : Le jardinier a récolté " + type.getName() + " !");
                }

                //  La plante est morte (Nettoyage)
                else if (plant.getState() == src.model.PlantState.MORT) {
                    parcel.clean(); // Vide la case (votre méthode dans PlantTile)
                    System.out.println("Le jardinier a arraché une plante morte. Rien n'a été ajouté à l'inventaire.");
                }

            } else {
                System.out.println("Échec : Il n'y a rien sur cette case.");
            }
        }
    }
}
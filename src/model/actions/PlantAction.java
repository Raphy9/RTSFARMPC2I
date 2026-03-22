package src.model.actions;

import src.model.PlantTile;
import src.model.Gardener;
import src.model.ItemSeed;
import src.model.Tile;
import src.model.World;

/**
 * Action concrète exécutée par le jardinier pour planter une graine sur le terrain.
 */
public class PlantAction extends Action {

    private ItemSeed seed;

    public PlantAction(int targetX, int targetY, ItemSeed seed) {
        super(targetX, targetY);
        this.seed = seed;
    }

    @Override
    public void perform(Gardener gardener, World world) {
        Tile tile = world.getTile(targetX, targetY);

        //  On vérifie qu'on est bien arrivé sur une case plantable
        if (tile instanceof PlantTile) {
            PlantTile parcel = (PlantTile) tile;

            // On vérifie que la case est toujours libre (farmable) au moment où le jardinier arrive
            if (parcel.isFarmable()) {

                // On plante la graine ! (Cela va créer l'objet Plant dans la case)
                boolean success = parcel.planter(seed.getPlantType());

                if (success) {
                    System.out.println("Succès : Le jardinier a planté " + seed.getPlantType().getName() + " !");

                    //  On déduit la graine utilisée
                    seed.removeQuantity(1);
                    if (seed.getQuantity() <= 0) {
                        world.getBarn().removeItem(seed);
                    }

                }
            } else {
                System.out.println("Trop tard ! La case est déjà occupée.");
            }
        } else {
            System.out.println("Erreur : Ce n'est pas une case plantable.");
        }
    }
}
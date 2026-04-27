package src.model.actions;

import src.model.*;

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

                //  La plante est mûre (Récolte normale : Donne le légume)
                if (plant.isHarvestable()) {
                    PlantType type = plant.getType();

                    parcel.harvest(); // Vide la case

                    // Ajout à l'inventaire du jardinier
                    gardener.getInventory().addItem(new ItemPlant(type, 1));
                    System.out.println("Succès : Le jardinier a récolté " + type.getName() + " !");

                    SoundManager.playSound(SoundManager.HARVEST);
                }

                //  La plante est morte ou mangée (Nettoyage : Ne donne rien)
                else if (plant.getState() == src.model.PlantState.MORT || plant.getState() == src.model.PlantState.EATEN) {
                    parcel.clean(); // Vide la case
                    System.out.println("Le jardinier a nettoyé les restes de la plante. La case est prête pour une nouvelle graine !");
                }

            } else {
                System.out.println("Échec : Il n'y a rien sur cette case.");
            }
        }
    }
}
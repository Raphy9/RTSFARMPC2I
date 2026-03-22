package src.model.actions;

import src.model.Gardener;
import src.model.PlantTile;
import src.model.Tile;
import src.model.World;

public class WaterAction extends Action {

    public WaterAction(int targetX, int targetY) {
        super(targetX, targetY);
    }

    public void perform(Gardener gardener, World world) {
        Tile tile = world.getTile(targetX, targetY);

        if (tile instanceof PlantTile) {
            PlantTile plantTile = (PlantTile) tile;
            plantTile.water();
        } else {
            System.out.println("Erreur : Ce n'est pas une case avec une plante à arroser.");
        }
    }
}

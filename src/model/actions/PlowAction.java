package src.model.actions;

import src.model.Gardener;
import src.model.PlantTile;
import src.model.Tile;
import src.model.World;

public class PlowAction extends Action {

    public PlowAction(int targetX, int targetY) {
        super(targetX, targetY);
    }

    /**
     * Le jardinier arrive sur la case ciblée. S'il s'agit d'une case non labourée,
     * il la laboure (la transforme en PlantTile).
     * @param gardener
     * @param world
     */
    @Override
    public void perform(Gardener gardener, World world) {
        Tile tile = world.getTile(targetX, targetY);

        if (tile.isPlowable()) {
            // On remplace la tile actuelle par une nouvelle PlantTile
            world.toPlantTile(targetX, targetY);
            System.out.println("Succès : Le jardinier a labouré la case (" + targetX + ", " + targetY + ") !");
        } else {
            System.out.println("Échec : La case (" + targetX + ", " + targetY + ") est déjà labourée ou n'existe pas.");
        }
    }
}

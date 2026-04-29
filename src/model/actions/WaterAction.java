package src.model.actions;

import src.model.*;

/**
 * Action d'arrosage : le jardinier se déplace sur la tuile d'exécution (execX,execY) et arrose la case (waterX,waterY).
 */
public class WaterAction extends Action {

    // Les coordonnées de la case a arroser
    private int waterX, waterY;

    /**
     * Constructeur de WaterAction.
     * @param execX Les coordonnées x de la tuile d'exécution (où le jardinier doit se déplacer pour effectuer l'action).
     * @param execY Les coordonnées y de la tuile d'exécution.
     * @param waterX Les coordonnées x de la case a arroser.
     * @param waterY Les coordonnées y de la case a arroser.
     */
    public WaterAction(int execX, int execY, int waterX, int waterY) {
        super(execX, execY);
        this.waterX = waterX;
        this.waterY = waterY;
    }

    /**
     * Le jardinier arrive sur la tuile d'exécution (execX,execY). Cette action arrose la case (waterX,waterY).
     */
    public void perform(Gardener gardener, World world) {
        Tile tile = world.getTile(waterX, waterY);

        // Vérifier que la tile est une PlantTile et qu'elle contient une plante a arroser
        if (tile instanceof PlantTile) {
            PlantTile plantTile = (PlantTile) tile;
            if (plantTile.getPlant() != null) {
                plantTile.water();
                SoundManager.playSound(SoundManager.WATER);
                world.registerQuestAction(Quests.ACTION_WATER_TILE);
            }
            else {
                System.out.println("Erreur : Il n'y a pas de plante a arroser sur cette case.");
            }
        } else {
            System.out.println("Erreur : Ce n'est pas une case avec une plante a arroser.");
        }
    }
}

package src.model.actions;

import src.model.Gardener;
import src.model.SoundManager;
import src.model.Tile;
import src.model.World;

/**
 * Action de labourage : le jardinier se déplace sur la tuile d'exécution (execX,execY) et laboure la case (plowX,plowY).
 */
public class PlowAction extends Action {

    // Les coordonnées de la case à labourer
    private int plowX, plowY;

    /** Constructeur de PlowAction.
     * @param execX Les coordonnées x de la tuile d'exécution (où le jardinier doit se déplacer pour effectuer l'action).
     * @param execY Les coordonnées y de la tuile d'exécution.
     * @param plowX Les coordonnées x de la case à labourer.
     * @param plowY Les coordonnées y de la case à labourer.
     */
    public PlowAction(int execX, int execY, int plowX, int plowY) {
        super(execX, execY);
        this.plowX = plowX;
        this.plowY = plowY;
    }

    /** Le jardinier arrive sur la tuile d'exécution (execX,execY). Cette action laboure la case (plowX,plowY).
     * Si la case est déjà labourée ou n'existe pas, un message d'erreur est affiché dans la console.
     */
    @Override
    public void perform(Gardener gardener, World world) {
        Tile tile = world.getTile(plowX, plowY);

        if (tile.isPlowable()) {
            // On remplace la tile actuelle par une nouvelle PlantTile
            world.toPlantTile(plowX, plowY);
            System.out.println("Succès : Le jardinier a labouré la case (" + plowX + ", " + plowY + ") !");
            SoundManager.playSound(SoundManager.PLOW);
            world.computeParcels();
        } else {
            System.out.println("Échec : La case (" + plowX + ", " + plowY + ") est déjà labourée ou n'existe pas.");
        }
    }
}

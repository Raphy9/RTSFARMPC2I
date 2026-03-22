package src.model.actions;

import src.model.Gardener;
import src.model.World;
import java.awt.Point;

/** ActionBuilder pour l'action d'arroser
 * Trouve la case adjacente la plus proche pour exécuter l'action, et construit une séquence d'actions pour y aller et arroser
 */
public class WaterActionBuilder extends ActionBuilder {

    // Le monde pour trouver la case adjacente la plus proche
    private World world;

    /** Constructeur de WaterActionBuilder
     * @param gardener le jardinier qui va effectuer l'action
     * @param world le monde pour trouver la case adjacente la plus proche
     */
    public WaterActionBuilder(Gardener gardener, World world) {
        super(gardener);
        this.world = world;
    }

    /** Construit une séquence d'actions pour que le jardinier aille sur la case adjacente la plus proche de la case cible (getX(), getY()) et arrose cette case.
     * Si aucune case adjacente n'est trouvée, le jardinier essaie d'arroser depuis sa position actuelle, mais un message d'erreur sera affiché dans la console.
     * Si une display est disponible, la case cible (getX(), getY()) est surlignée pendant la construction de l'action, et le surlignage est retiré une fois l'action construite.
     */
    @Override
    public void buildAction() {
        // Trouver la case adjacente la plus proche pour exécuter l'action
        Point adjacent = world.findClosestWalkableAdjacent(getX(), getY(), getGardener());

        // Si une case adjacente est trouvée, le jardinier se déplacera dessus pour arroser la case cible. Sinon, il essaiera d'arroser depuis sa position actuelle (mais cela échouera probablement).
        int execX = (adjacent != null) ? adjacent.x : getX();
        int execY = (adjacent != null) ? adjacent.y : getY();

        // highlight via Display si disponible: maintenant on surligne la CASE CIBLE (getX(), getY())
        Runnable clearHighlight = null;
        if (getDisplay() != null) {
            getDisplay().getGlobalView().setHighlight(getX(), getY());
            clearHighlight = () -> getDisplay().getGlobalView().clearHighlight();
        }

        // Construire l'action d'arrosage
        WaterAction action = new WaterAction(execX, execY, getX(), getY());

        // Interrompre les actions en cours du jardinier et ajouter la séquence d'actions pour se déplacer (si nécessaire) et arroser
        getGardener().interruptGardener();
        if (clearHighlight != null) {
            getGardener().addAction(new src.model.actions.MoveAction(execX, execY, clearHighlight));
        } else {
            getGardener().addAction(new src.model.actions.MoveAction(execX, execY));
        }
        getGardener().addAction(action);

        System.out.println("Ordre d'arroser envoyé en (" + getX() + ", " + getY() + ") !");
    }
}

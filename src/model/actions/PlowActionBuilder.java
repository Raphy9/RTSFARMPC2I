package src.model.actions;

import src.model.Gardener;
import src.model.World;
import java.awt.Point;

/** Permet de construire une action de labourage (PlowAction) à partir d'une case cible sélectionnée par l'utilisateur, en vérifiant que la case est bien
 *  labourable et en gérant les déplacements nécessaires du jardinier pour atteindre la case cible si besoin. */
public class PlowActionBuilder extends ActionBuilder {

    // Le monde pour trouver la case adjacente la plus proche
    private World world;

    /** Constructeur de PlowActionBuilder
     * @param gardener le jardinier qui va effectuer l'action
     * @param world le monde pour trouver la case adjacente la plus proche
     */
    public PlowActionBuilder(Gardener gardener, World world) {
        super(gardener);
        this.world = world;
    }

    /** Construit une séquence d'actions pour que le jardinier aille sur la case adjacente la plus proche de la case cible (getX(), getY()) et laboure cette case.
     * Si aucune case adjacente n'est trouvée, le jardinier essaie de labourer depuis sa position actuelle, mais un message d'erreur sera affiché dans la console.
     * Si une display est disponible, la case cible (getX(), getY()) est surlignée pendant la construction de l'action, et le surlignage est retiré une fois l'action construite.
     */
    @Override
    public void buildAction() {
        // Trouver la case adjacente la plus proche pour exécuter l'action
        Point adjacent = world.findClosestWalkableAdjacent(getX(), getY(), getGardener());

        // Si une case adjacente est trouvée, le jardinier se déplacera dessus pour labourer la case cible. Sinon, il essaiera de labourer depuis sa position actuelle (mais cela échouera probablement).
        int execX = (adjacent != null) ? adjacent.x : getX();
        int execY = (adjacent != null) ? adjacent.y : getY();

        // highlight via Display si disponible: sur la CASE CIBLE (getX(), getY())
        Runnable clearHighlight = null;
        if (getDisplay() != null) {
            getDisplay().getGlobalView().setHighlight(getX(), getY());
            clearHighlight = () -> getDisplay().getGlobalView().clearHighlight();
        }

        PlowAction action = new PlowAction(execX, execY, getX(), getY());

        getGardener().interruptGardener();
        if (clearHighlight != null) {
            getGardener().addAction(new src.model.actions.MoveAction(execX, execY, clearHighlight));
        } else {
            getGardener().addAction(new src.model.actions.MoveAction(execX, execY));
        }
        getGardener().addAction(action);

        System.out.println("Ordre de labourer envoyé en (" + getX() + ", " + getY() + ") !");
    }
}

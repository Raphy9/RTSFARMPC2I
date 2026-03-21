package src.model.actions;

import src.model.Gardener;
import src.model.World;

/**
 * Action simple ordonnant au jardinier de se déplacer vers une case.
 */
public class MoveAction extends Action {

    public MoveAction(int targetX, int targetY) {
        super(targetX, targetY);
    }

    @Override
    public void perform(Gardener gardener, World world) {
        // Le déplacement en lui-même (pathfinding + mouvement) est déjà
        // géré par la boucle principale de la classe Gardener.
        // Quand cette méthode est appelée, c'est que le jardinier est arrivé.
        System.out.println("Jardinier arrivé à destination : (" + targetX + ", " + targetY + ")");
    }
}
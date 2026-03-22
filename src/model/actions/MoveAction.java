package src.model.actions;

import src.model.Gardener;
import src.model.World;

/**
 * Action simple ordonnant au jardinier de se déplacer vers une case.
 * Ce MoveAction est conçu pour être exécuté par le thread du jardinier :
 * - le pathfinding et le déplacement sont gérés par le Gardener.
 * - lorsque le jardinier arrive sur la case cible, la méthode perform() est appelée.
 * On peut fournir un callback (Runnable) qui sera exécuté une fois arrivé (par ex. pour ouvrir un popup).
 */
public class MoveAction extends Action {

    private final Runnable onArrival;

    /** Constructeur de MoveAction.
     * @param targetX Les coordonnées x de la case cible.
     * @param targetY Les coordonnées y de la case cible.
     */
    public MoveAction(int targetX, int targetY) {
        super(targetX, targetY);
        this.onArrival = null;
    }

    /** Constructeur de MoveAction avec callback.
     * @param targetX Les coordonnées x de la case cible.
     * @param targetY Les coordonnées y de la case cible.
     * @param onArrival Un Runnable qui sera exécuté une fois que le jardinier arrive sur la case cible. Peut être null si aucun callback n'est nécessaire.
     */
    public MoveAction(int targetX, int targetY, Runnable onArrival) {
        super(targetX, targetY);
        this.onArrival = onArrival;
    }

    /**
     * Cette méthode est appelée lorsque le jardinier arrive sur la case cible (targetX, targetY).
     * Le déplacement en lui-même est géré par le Gardener, donc ici on se concentre sur ce qui doit se passer à l'arrivée.
     * Par défaut, on affiche un message dans la console, et si un callback est fourni, on l'exécute.
     */
    @Override
    public void perform(Gardener gardener, World world) {
        // Le déplacement en lui-même (pathfinding + mouvement) est déjà
        // géré par la boucle principale de la classe Gardener.
        // Quand cette méthode est appelée, c'est que le jardinier est arrivé.
        System.out.println("Jardinier arrivé à destination : (" + targetX + ", " + targetY + ")");

        // Exécuter le callback après l'arrivée si fourni
        if (onArrival != null) {
            try {
                onArrival.run();
            } catch (Exception ex) {
                // Log simple, éviter printStackTrace dans la version finale
                System.err.println("Erreur dans onArrival callback: " + ex.getMessage());
            }
        }
    }
}
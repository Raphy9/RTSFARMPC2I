package src.model.actions;

import src.model.Gardener;
import src.model.World;
import java.awt.Point;

/**
 * Monteur (Builder) dédié à l'action d'arroser (Water).
 * Cette classe orchestre la séquence de tâches permettant au jardinier de se déplacer
 * vers les parcelles sélectionnées pour leur apporter de l'eau.
 */
public class WaterActionBuilder extends ActionBuilder {
    // Référence au monde pour effectuer des calculs de voisinage et de trajectoire
    private World world;

    /**
     * Constructeur initialisant le constructeur d'action.
     * @param gardener Le jardinier qui recevra la liste des tâches à accomplir.
     * @param world Le contexte du monde pour la recherche de cases adjacentes.
     */
    public WaterActionBuilder(Gardener gardener, World world) {
        super(gardener);
        this.world = world;
    }

    /**
     * Méthode principale qui assemble la file d'attente d'actions.
     * Elle parcourt tous les points sélectionnés par l'utilisateur et génère
     * les couples d'actions (Déplacement + Arrosage) correspondants.
     */
    @Override
    public void buildAction() {

        // Itération sur chaque point (case) que l'utilisateur a choisi d'arroser
        for (Point p : getSelectedPoints()) {

            // Logique de positionnement : on cherche la case marchable la plus proche
            // située à côté de la cible pour que le jardinier ne marche pas sur la plante.
            Point adjacent = world.findClosestWalkableAdjacent(p.x, p.y, getGardener());

            // Coordonnées où le jardinier doit se placer pour exécuter l'action
            int execX = (adjacent != null) ? adjacent.x : p.x;
            int execY = (adjacent != null) ? adjacent.y : p.y;

            // Feedback visuel : on illumine la case dans la vue globale (souvent en jaune)
            // pour indiquer qu'une action est programmée sur cette tuile.
            if (getDisplay() != null) {
                getDisplay().getGlobalView().setHighlight(p.x, p.y);
            }

            // Définition d'un callback (Runnable) qui sera déclenché uniquement
            // lorsque le jardinier atteindra sa destination, afin de nettoyer le surlignage.
            Runnable clearHighlight = () -> {
                if (getDisplay() != null) {
                    getDisplay().getGlobalView().clearHighlight(p.x, p.y);
                }
            };

            // Ajout de la séquence dans la pile d'actions du jardinier :
            // 1. Se déplacer vers la case adjacente (et nettoyer le highlight à l'arrivée)
            getGardener().addAction(new MoveAction(execX, execY, clearHighlight));

            // 2. Exécuter l'arrosage effectif sur la case cible p.x, p.y
            getGardener().addAction(new WaterAction(execX, execY, p.x, p.y));
        }
    }
}
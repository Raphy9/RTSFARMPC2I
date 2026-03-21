package src.model.actions;

import src.model.Gardener;
import src.model.World;

/**
 * Classe abstraite représentant une tâche pour un jardinier.
 */
public abstract class Action {
    protected int targetX;
    protected int targetY;

    public Action(int targetX, int targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }

    /**
     * Méthode appelée par le jardinier une fois arrivé sur la case cible.
     * Sera redéfinie par les actions spécifiques (PlanterAction, RecolterAction...).
     */
    public abstract void perform(Gardener gardener, World world);
}
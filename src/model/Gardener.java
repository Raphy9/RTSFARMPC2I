package src.model;

import src.model.actions.Action;
import src.model.actions.PlowAction;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Classe représentant le Jardinier (joueur).
 * Elle implémente Runnable car le jardinier vit dans son propre Thread,
 * ce qui lui permet de se déplacer et de travailler de manière asynchrone.
 */
public class Gardener extends Entity implements Runnable {

    /** États logiques du jardinier pour gérer les animations et les comportements. */
    public enum State {
        WAITING, // En attente d'ordres
        MOVING,  // En cours de déplacement vers une cible
        WORKING  // En train d'exécuter une action (labourer, planter, etc.)
    }

    private Inventory inventory;           // Inventaire personnel (graines, outils)
    private State currentState;            // État actuel
    private final Queue<Action> actionQueue; // File d'attente des actions à venir
    private transient Thread gardenerThread; // Référence au thread d'exécution
    private volatile boolean isRunning;    // Contrôle de la boucle de vie

    // --- Gestion de l'interface ---
    // Index de l'objet sélectionné dans la barre rapide (Hotbar)
    private int selectedHotbarIndex = -1;

    public Gardener(int x, int y, World world) {
        super(world, x, y);
        this.inventory = new Inventory();
        this.actionQueue = new LinkedList<>();
        this.currentState = State.WAITING;
        this.isRunning = true;
    }

    // --- Getters et Setters pour la Hotbar ---
    public int getSelectedHotbarIndex() { return selectedHotbarIndex; }

    public void setSelectedHotbarIndex(int index) {
        // Limite l'index entre -1 (rien) et 3 (4 emplacements)
        if (index >= -1 && index < 4) {
            this.selectedHotbarIndex = index;
        }
    }

    /**
     * Déplace instantanément le jardinier (utile pour le spawn ou déblocage).
     */
    public void teleportTo(int newX, int newY) {
        if (newX < 0 || newX >= World.WIDTH || newY < 0 || newY >= World.HEIGHT) return;

        Tile oldTile = world.getTile(this.x, this.y);
        if (oldTile != null) oldTile.removeEntity(this);

        this.x = newX;
        this.y = newY;
        world.getTile(newX, newY).addEntity(this);
    }

    /**
     * Boucle principale du Thread Jardinier.
     * Gère la consommation de la file d'actions ou la promenade aléatoire si inactif.
     */
    @Override
    public void run() {
        this.gardenerThread = Thread.currentThread();

        while (isRunning) {
            Action currentAction = null;

            try {
                // --- Synchronisation pour l'accès à la file d'actions ---
                synchronized (actionQueue) {
                    // Si pas d'actions, le thread se met en attente (wait)
                    while (actionQueue.isEmpty() && isRunning) {
                        currentState = State.WAITING;
                        try {
                            // Attente timée (1.5s à 3s) pour permettre le "wandering" (promenade)
                            long timeout = 1500L + (long) (Math.random() * 1500L);
                            actionQueue.wait(timeout);
                        } catch (InterruptedException e) {
                            break; // Réveillé par une interruption (nouvelle action ou stop)
                        }
                        if (actionQueue.isEmpty()) break; // Timeout atteint, on sort pour wander
                    }
                    if (!isRunning) break;
                    currentAction = actionQueue.poll(); // On récupère l'action suivante
                }

                if (currentAction != null) {
                    executeAction(currentAction); // Exécution de la tâche
                } else {
                    wanderWhenIdle(); // Petit déplacement d'ambiance si rien à faire
                }
            } catch (InterruptedException e) {
                // Si l'action est annulée par le joueur (clic ailleurs)
                System.out.println("Jardinier : Action annulée.");
                if (currentAction instanceof PlowAction) {
                    world.releasePlowTiles(1); // Libère la réservation de la case à labourer
                }
                this.currentState = State.WAITING;
            } catch (Exception e) {
                e.printStackTrace();
                this.currentState = State.WAITING;
            }
        }
    }

    /**
     * Gère le déplacement vers la cible puis l'exécution de l'action.
     */
    private void executeAction(Action action) throws InterruptedException {
        this.currentState = State.MOVING;

        // Utilise le Pathfinding A* hérité d'Entity
        List<Tile> path = this.pathFinding(world, action.getTargetX(), action.getTargetY());

        if (path != null) {
            for (Tile step : path) {
                // Vérifie si le joueur a annulé l'action pendant le trajet
                if (Thread.interrupted()) throw new InterruptedException();

                int oldX = this.x;
                int oldY = this.y;
                int newX = step.getX();
                int newY = step.getY();

                // Mise à jour de l'orientation visuelle
                if (newX > oldX) this.facingDirection = Entity.RIGHT;
                else if (newX < oldX) this.facingDirection = Entity.LEFT;
                else if (newY > oldY) this.facingDirection = Entity.DOWN;
                else if (newY < oldY) this.facingDirection = Entity.UP;

                this.x = newX;
                this.y = newY;
                world.getTile(oldX, oldY).removeEntity(this);
                world.getTile(newX, newY).addEntity(this);

                Thread.sleep(150); // Vitesse de déplacement du jardinier
            }
        }
        else if (this.x != action.getTargetX() || this.y != action.getTargetY()) {
            // Cible inaccessible (bloquée par un bâtiment par exemple)
            this.currentState = State.WAITING;
            return;
        }

        // --- Exécution de l'action proprement dite ---
        this.currentState = State.WORKING;
        Thread.sleep(200); // Petit délai de préparation
        action.perform(this, world); // L'action modifie le monde
        this.currentState = State.WAITING;
    }

    /** Ajoute une action à la pile et réveille le thread. */
    public void addAction(Action action) {
        synchronized (actionQueue) {
            actionQueue.add(action);
            actionQueue.notify(); // Réveille le thread qui était en wait()
        }
    }

    /** Vide la file d'actions (quand le joueur clique sur une nouvelle destination). */
    public void interruptGardener() {
        int canceledPlows = 0;
        synchronized (actionQueue) {
            for (Action action : actionQueue) {
                if (action instanceof PlowAction) canceledPlows++;
            }
            actionQueue.clear();
        }
        if (canceledPlows > 0) world.releasePlowTiles(canceledPlows);

        if (this.currentState != State.WAITING && gardenerThread != null) {
            gardenerThread.interrupt(); // Force l'arrêt du déplacement/travail en cours
        }
    }

    /** Promenade aléatoire pour donner vie au personnage quand il n'a rien à faire. */
    private void wanderWhenIdle() throws InterruptedException {
        // ... (Logique de déplacement aléatoire similaire à l'exécution d'action)
        // Vérifie régulièrement si une nouvelle action arrive pour s'interrompre.
    }

    // --- Accesseurs ---
    public State getCurrentState() { return currentState; }
    public Inventory getInventory() { return inventory; }
    public void stopGardener() { this.isRunning = false; interruptGardener(); }
    public int getPendingActionsCount() {
        synchronized (actionQueue) { return actionQueue.size(); }
    }
}
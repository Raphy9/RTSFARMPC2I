package src.model;

import src.model.actions.Action;
import src.model.actions.PlowAction;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

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
    private final Random random = new Random(); // Pour la promenade aléatoire

    // --- Gestion de l'interface ---
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
        if (index >= -1 && index < 4) {
            this.selectedHotbarIndex = index;
        }
    }

    public void teleportTo(int newX, int newY) {
        if (newX < 0 || newX >= World.WIDTH || newY < 0 || newY >= World.HEIGHT) return;
        Tile oldTile = world.getTile(this.x, this.y);
        if (oldTile != null) oldTile.removeEntity(this);
        this.x = newX;
        this.y = newY;
        world.getTile(newX, newY).addEntity(this);
    }

    @Override
    public void run() {
        this.gardenerThread = Thread.currentThread();

        while (isRunning) {
            Action currentAction = null;
            try {
                synchronized (actionQueue) {
                    while (actionQueue.isEmpty() && isRunning) {
                        currentState = State.WAITING;
                        try {
                            // Attente entre 1.5s et 3s
                            long timeout = 1500L + (long) (Math.random() * 1500L);
                            actionQueue.wait(timeout);
                        } catch (InterruptedException e) {
                            break;
                        }
                        // Si après le timeout la file est toujours vide, on lance la promenade
                        if (actionQueue.isEmpty()) break;
                    }
                    if (!isRunning) break;
                    currentAction = actionQueue.poll();
                }

                if (currentAction != null) {
                    executeAction(currentAction);
                } else {
                    // Si on arrive ici, c'est que le wait() a expiré sans nouvelle action
                    wanderWhenIdle();
                }
            } catch (InterruptedException e) {
                System.out.println("Jardinier : Action ou promenade interrompue.");
                if (currentAction instanceof PlowAction) {
                    world.releasePlowTiles(1);
                }
                this.currentState = State.WAITING;
            } catch (Exception e) {
                e.printStackTrace();
                this.currentState = State.WAITING;
            }
        }
    }

    private void executeAction(Action action) throws InterruptedException {
        this.currentState = State.MOVING;
        List<Tile> path = this.pathFinding(world, action.getTargetX(), action.getTargetY());

        if (path != null) {
            for (Tile step : path) {
                if (Thread.interrupted()) throw new InterruptedException();
                moveOneStep(step);
                Thread.sleep(150);
            }
        }
        else if (this.x != action.getTargetX() || this.y != action.getTargetY()) {
            this.currentState = State.WAITING;
            return;
        }

        this.currentState = State.WORKING;
        Thread.sleep(200);
        action.perform(this, world);
        this.currentState = State.WAITING;
    }

    /**
     * Effectue un seul pas vers une case adjacente et met à jour l'orientation.
     */
    private void moveOneStep(Tile step) {
        int oldX = this.x;
        int oldY = this.y;
        int newX = step.getX();
        int newY = step.getY();

        if (newX > oldX) this.facingDirection = Entity.RIGHT;
        else if (newX < oldX) this.facingDirection = Entity.LEFT;
        else if (newY > oldY) this.facingDirection = Entity.DOWN;
        else if (newY < oldY) this.facingDirection = Entity.UP;

        this.x = newX;
        this.y = newY;
        world.getTile(oldX, oldY).removeEntity(this);
        world.getTile(newX, newY).addEntity(this);
    }

    public void addAction(Action action) {
        synchronized (actionQueue) {
            actionQueue.add(action);
            actionQueue.notify();
        }
    }

    public void interruptGardener() {
        int canceledPlows = 0;
        synchronized (actionQueue) {
            for (Action action : actionQueue) {
                if (action instanceof PlowAction) canceledPlows++;
            }
            actionQueue.clear();
        }
        if (canceledPlows > 0) world.releasePlowTiles(canceledPlows);

        if (gardenerThread != null) {
            gardenerThread.interrupt();
        }
    }

    /**
     * Promenade aléatoire pour donner vie au personnage.
     */
    private void wanderWhenIdle() throws InterruptedException {
        // Une chance sur deux de rester immobile pour un cycle (plus naturel)
        if (random.nextBoolean()) return;

        int dir = random.nextInt(4);
        int nextX = this.x;
        int nextY = this.y;

        switch (dir) {
            case 0 -> nextY--; // UP
            case 1 -> nextY++; // DOWN
            case 2 -> nextX--; // LEFT
            case 3 -> nextX++; // RIGHT
        }

        // Vérification des limites et de la collision (marchable)
        if (nextX >= 0 && nextX < World.WIDTH && nextY >= 0 && nextY < World.HEIGHT) {
            Tile targetTile = world.getTile(nextX, nextY);
            if (targetTile.isWalkable()) {
                this.currentState = State.MOVING;
                moveOneStep(targetTile);
                Thread.sleep(150);
                this.currentState = State.WAITING;
            }
        }
    }

    // --- Accesseurs ---
    public State getCurrentState() { return currentState; }
    public Inventory getInventory() { return inventory; }
    public void stopGardener() { this.isRunning = false; interruptGardener(); }
    public int getPendingActionsCount() {
        synchronized (actionQueue) { return actionQueue.size(); }
    }
}
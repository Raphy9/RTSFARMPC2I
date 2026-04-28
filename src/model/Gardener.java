package src.model;

import src.model.actions.Action;
import src.model.actions.PlowAction;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Gardener extends Entity implements Runnable {

    public enum State {
        WAITING,
        MOVING,
        WORKING
    }

    private Inventory inventory;
    private State currentState;
    private final Queue<Action> actionQueue;
    private transient Thread gardenerThread;
    private volatile boolean isRunning;

    // --- NOUVEAU : Index de la barre d'action (-1 = rien sélectionné) ---
    private int selectedHotbarIndex = -1;

    public Gardener(int x, int y, World world) {
        super(world, x, y);
        this.inventory = new Inventory();
        this.actionQueue = new LinkedList<>();
        this.currentState = State.WAITING;
        this.isRunning = true;
    }

    // NOUVEAU : Getters et Setters pour la Hotbar
    public int getSelectedHotbarIndex() {
        return selectedHotbarIndex;
    }

    public void setSelectedHotbarIndex(int index) {
        if (index >= -1 && index < 4) {
            this.selectedHotbarIndex = index;
        }
    }

    public void teleportTo(int newX, int newY) {
        if (newX < 0 || newX >= World.WIDTH || newY < 0 || newY >= World.HEIGHT) {
            return;
        }
        Tile oldTile = world.getTile(this.x, this.y);
        if (oldTile != null) {
            oldTile.removeEntity(this);
        }
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
                // Use a timed wait so the gardener can wander when idle
                synchronized (actionQueue) {
                    while (actionQueue.isEmpty() && isRunning) {
                        currentState = State.WAITING;
                        try {
                            // wait between 1.5s and 3s
                            long timeout = 1500L + (long) (Math.random() * 1500L);
                            actionQueue.wait(timeout);
                        } catch (InterruptedException e) {
                            break;
                        }
                        // if we wake up due to timeout and still no actions, break to do wandering
                        if (actionQueue.isEmpty()) break;
                    }
                    if (!isRunning) break;
                    // If there's an action available, take it
                    currentAction = actionQueue.poll();
                }

                if (currentAction != null) {
                    Thread.interrupted();
                    executeAction(currentAction);
                } else {
                    // No action after timed wait -> do a short random wander
                    wanderWhenIdle();
                }
            } catch (InterruptedException e) {
                System.out.println("Jardinier : Action annulée en cours de route.");
                if (currentAction instanceof PlowAction) {
                    world.releasePlowTiles(1);
                }
                this.currentState = State.WAITING;
            } catch (Exception e) {
                System.err.println("ERREUR FATALE DANS LE THREAD DU JARDINIER !");
                e.printStackTrace();
                this.currentState = State.WAITING;
            }
        }
    }

    private void executeAction(Action action) throws InterruptedException {
        this.currentState = State.MOVING;
        System.out.println("Jardinier : Calcul du chemin vers (" + action.getTargetX() + ", " + action.getTargetY() + ")");

        List<Tile> path = this.pathFinding(world, action.getTargetX(), action.getTargetY());

        if (path != null) {
            System.out.println("Jardinier : Chemin trouvé ! (" + path.size() + " cases)");
            for (Tile step : path) {
                if (Thread.interrupted()) {
                    throw new InterruptedException("Déplacement annulé.");
                }

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

                Thread.sleep(150);
            }
        }
        else if (this.x != action.getTargetX() || this.y != action.getTargetY()) {
            System.out.println("Jardinier : Impossible de trouver un chemin !");
            this.currentState = State.WAITING;
            return;
        }

        this.currentState = State.WORKING;
        Thread.sleep(200);
        action.perform(this, world);
        this.currentState = State.WAITING;
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
                if (action instanceof PlowAction) {
                    canceledPlows++;
                }
            }
            actionQueue.clear();
        }
        if (canceledPlows > 0) {
            world.releasePlowTiles(canceledPlows);
        }
        if (this.currentState != State.WAITING && gardenerThread != null && gardenerThread.isAlive()) {
            gardenerThread.interrupt();
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void stopGardener() {
        this.isRunning = false;
        interruptGardener();
    }

    public int getPendingActionsCount() {
        synchronized (actionQueue) {
            return actionQueue.size();
        }
    }

    /**
     * Promenade aléatoire courte lorsque le jardinier est en attente (inspirée de Chicken.wanderRandomly).
     * Vérifie la file d'actions avant et pendant la promenade pour pouvoir s'interrompre proprement.
     */
    private void wanderWhenIdle() throws InterruptedException {
        // If actions appeared meanwhile, abort wandering
        synchronized (actionQueue) {
            if (!actionQueue.isEmpty() || !isRunning) return;
        }

        int radius = 2; // jardinier se promène moins loin que la poule
        int randomX = this.x + (int) (Math.random() * (radius * 2 + 1)) - radius;
        int randomY = this.y + (int) (Math.random() * (radius * 2 + 1)) - radius;

        if (randomX >= 0 && randomX < World.WIDTH && randomY >= 0 && randomY < World.HEIGHT) {
            Tile destTile = world.getTile(randomX, randomY);
            if (destTile.isWalkable()) {
                List<Tile> path = pathFinding(world, randomX, randomY);
                if (path != null && !path.isEmpty()) {
                    this.currentState = State.MOVING;

                    for (Tile step : path) {
                        // If new actions queued, abort wandering
                        synchronized (actionQueue) {
                            if (!actionQueue.isEmpty() || !isRunning) {
                                this.currentState = State.WAITING;
                                return;
                            }
                        }

                        if (Thread.interrupted()) throw new InterruptedException();

                        int newX = step.getX();
                        int newY = step.getY();

                        // anticollision: avoid stepping onto a tile occupied by other entities (gardener may share?)
                        Tile nextTile = world.getTile(newX, newY);
                        if (!nextTile.isWalkable()) break;

                        if (newX > this.x) this.facingDirection = Entity.RIGHT;
                        else if (newX < this.x) this.facingDirection = Entity.LEFT;

                        int oldX = this.x;
                        int oldY = this.y;
                        this.x = newX;
                        this.y = newY;

                        world.getTile(oldX, oldY).removeEntity(this);
                        world.getTile(newX, newY).addEntity(this);

                        Thread.sleep(350 + (int) (Math.random() * 250)); // promenade lente
                    }
                }
            }
        }

        if (this.currentState != State.WORKING && isRunning) {
            this.currentState = State.WAITING;
            // pause aléatoire avant prochaine promenade
            Thread.sleep(500 + (int) (Math.random() * 1000));
        }
    }
}
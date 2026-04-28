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
                synchronized (actionQueue) {
                    while (actionQueue.isEmpty() && isRunning) {
                        currentState = State.WAITING;
                        try {
                            actionQueue.wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (!isRunning) break;
                    currentAction = actionQueue.poll();
                }

                if (currentAction != null) {
                    Thread.interrupted();
                    executeAction(currentAction);
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
}
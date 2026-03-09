package src.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * La classe Gardener représente un jardinier.
 * Il hérite de Entity (pour la position et le pathfinding) et implémente Runnable
 * pour fonctionner sur son propre Thread et gérer son cycle de vie de manière autonome.
 */
public class Gardener extends Entity implements Runnable {

    // états du jardinier
    public enum State {
        WAITING,    // En attente
        MOVING,     // En déplacement
        WORKING     // Au travail
    }

    private World world;
    private Inventory inventory;
    private State currentState;

    // File d'actions ordonnée chronologiquement
    private final Queue<Action> actionQueue;

    // Référence au thread de ce jardinier (utile pour l'interruption)
    private transient Thread gardenerThread;
    private volatile boolean isRunning;

    public Gardener(int x, int y, World world) {
        super(x, y); // Appelle le constructeur de Entity
        this.world = world;
        this.inventory = new Inventory();
        this.actionQueue = new LinkedList<>();
        this.currentState = State.WAITING;
        this.isRunning = true;
    }

    /**
     * Boucle principale du Thread du jardinier.
     * Vérifie sa liste de tâches et agit en conséquence.
     */
    @Override
    public void run() {
        this.gardenerThread = Thread.currentThread();

        while (isRunning) {
            Action currentAction = null;

            //  Récupération d'une action de manière synchronisée pour éviter les problèmes de concurrence avec le joueur qui ajoute des actions
            synchronized (actionQueue) {
                while (actionQueue.isEmpty() && isRunning) {
                    currentState = State.WAITING;
                    try {
                        // Le jardinier s'endort s'il n'a rien à faire (wait())
                        actionQueue.wait();
                    } catch (InterruptedException e) {
                        // Réveil forcé
                        break;
                    }
                }
                if (!isRunning) break;
                currentAction = actionQueue.poll(); // Récupère et retire la prochaine action
            }

            // Exécution de l'action s'il y en a une
            if (currentAction != null) {
                try {
                    executeAction(currentAction);
                } catch (InterruptedException e) {
                    // L'action a été interrompue en cours de route (ex: pendant un déplacement)
                    System.out.println("Jardinier interrompu dans son action !");
                    this.currentState = State.WAITING;
                }
            }
        }
    }


    /**
     * Ajoute une action à la file et réveille le jardinier s'il dormait.
     */
    public void addAction(Action action) {
        synchronized (actionQueue) {
            actionQueue.add(action);
            actionQueue.notify(); // Réveille le Thread bloqué dans le wait()
        }
    }

    /**
     * Contrainte forte de la F2 : Gestion de l'annulation.
     * Interrompt l'action courante, vide la file et retourne en état d'attente.
     */
    public void interruptGardener() {
        synchronized (actionQueue) {
            actionQueue.clear(); // Vide les prochaines actions
        }
        if (gardenerThread != null && gardenerThread.isAlive()) {
            gardenerThread.interrupt(); // Coupe le Thread.sleep() ou wait() en cours
        }
    }

    // Getters
    public State getCurrentState() { return currentState; }
    public Inventory getInventory() { return inventory; }

    // Pour arrêter définitivement le thread à la fermeture du jeu
    public void stopGardener() {
        this.isRunning = false;
        interruptGardener();
    }

    // Dans src.model.Gardener

    private void executeAction(Action action) throws InterruptedException {
        this.currentState = State.MOVING;

        List<Tile> path = this.pathFinding(world, action.getTargetX(), action.getTargetY());

        if (path != null) {
            for (Tile step : path) {
                // --- NOUVEAU : Calculer la direction du prochain pas ---
                int oldX = this.x;
                int oldY = this.y;
                int newX = step.getX();
                int newY = step.getY();

                if (newX > oldX) this.facingDirection = Entity.RIGHT;
                else if (newX < oldX) this.facingDirection = Entity.LEFT;
                else if (newY > oldY) this.facingDirection = Entity.DOWN;
                else if (newY < oldY) this.facingDirection = Entity.UP;
                // -------------------------------------------------------

                this.x = newX;
                this.y = newY;

                Thread.sleep(150); // Temps entre chaque case
            }
        }

        this.currentState = State.WORKING;
        Thread.sleep(1000);
        action.perform(this, world);
        this.currentState = State.WAITING;
    }
}
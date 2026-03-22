package src.model;

import src.model.actions.Action;

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

    private Inventory inventory;
    private State currentState;

    // File d'actions ordonnée chronologiquement
    private final Queue<Action> actionQueue;

    // Référence au thread de ce jardinier (utile pour l'interruption)
    private transient Thread gardenerThread;
    private volatile boolean isRunning;

    public Gardener(int x, int y, World world) {
        super(world, x, y); // Appelle le constructeur de Entity
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

            try {
                synchronized (actionQueue) {
                    while (actionQueue.isEmpty() && isRunning) {
                        currentState = State.WAITING;
                        try {
                            actionQueue.wait();
                        } catch (InterruptedException e) {
                            break; // Le joueur a cliqué, on sort du sommeil !
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
                this.currentState = State.WAITING;
            } catch (Exception e) {
                // SÉCURITÉ ANTI-CRASH : Empêche le thread de mourir en silence !
                System.err.println("ERREUR FATALE DANS LE THREAD DU JARDINIER !");
                e.printStackTrace();
                this.currentState = State.WAITING;
            }
        }
    }

    /**
     * Exécute une action : gère le déplacement vers la cible, puis l'exécution de l'action.
     * Cette méthode est appelée depuis la boucle principale du Thread du jardinier.
     */
    private void executeAction(Action action) throws InterruptedException {
        this.currentState = State.MOVING; // Le jardinier se prépare à bouger
        System.out.println("Jardinier : Calcul du chemin vers (" + action.getTargetX() + ", " + action.getTargetY() + ")");

        // Contrainte forte de la F2 : Gestion de l'annulation pendant le déplacement
        List<Tile> path = this.pathFinding(world, action.getTargetX(), action.getTargetY());

        // Si le pathfinding retourne null, cela signifie que la cible est inaccessible (ex: bloquée par des obstacles).
        if (path != null) {
            System.out.println("Jardinier : Chemin trouvé ! (" + path.size() + " cases)");
            for (Tile step : path) { // Parcours du chemin case par case
                if (Thread.interrupted()) { // Si une interruption est détectée, on arrête immédiatement le déplacement
                    throw new InterruptedException("Déplacement annulé.");
                }

                // Mise à jour de la position du jardinier et de sa direction
                int oldX = this.x;
                int oldY = this.y;
                int newX = step.getX();
                int newY = step.getY();

                // Mise à jour de la direction du jardinier en fonction du mouvement
                if (newX > oldX) this.facingDirection = Entity.RIGHT;
                else if (newX < oldX) this.facingDirection = Entity.LEFT;
                else if (newY > oldY) this.facingDirection = Entity.DOWN;
                else if (newY < oldY) this.facingDirection = Entity.UP;

                // Mise à jour de la position du jardinier dans le monde
                this.x = newX;
                this.y = newY;
                world.getTile(oldX, oldY).removeEntity(this);
                world.getTile(newX, newY).addEntity(this);

                Thread.sleep(150); // Pause pour l'animation
            }
        } // Si le pathfinding retourne null, cela signifie que la cible est inaccessible (ex: bloquée par des obstacles).
        else if (this.x != action.getTargetX() || this.y != action.getTargetY()) {
            System.out.println("Jardinier : Impossible de trouver un chemin !");
            this.currentState = State.WAITING;
            return;
        }

        // Arrivé à destination, on exécute l'action
        this.currentState = State.WORKING;
        Thread.sleep(200);
        action.perform(this, world);
        this.currentState = State.WAITING;
    }

    /**
     * Ajoute une action à la file d'attente du jardinier.
     * Cette méthode est appelée par le Thread principal du jeu lorsque le joueur planifie une action.
     * Si le jardinier est en train de dormir (wait), il sera réveillé pour traiter cette nouvelle action.
     */
    public void addAction(Action action) {
        synchronized (actionQueue) {
            actionQueue.add(action);
            actionQueue.notify(); // Réveille le Thread bloqué dans le wait()
        }
    }

    /** Méthode pour interrompre le jardinier : elle vide les actions en attente et interrompt le thread s'il est en train de se déplacer ou de travailler.
     * Cette méthode est appelée lorsque le joueur clique sur une nouvelle action alors que le jardinier est déjà occupé.
     */
    public void interruptGardener() {
        synchronized (actionQueue) {
            actionQueue.clear(); // Vide les prochaines actions
        }
        if (this.currentState != State.WAITING && gardenerThread != null && gardenerThread.isAlive()) {
            gardenerThread.interrupt();
        }
    }

    // Getters
    public State getCurrentState() {
        return currentState;
    }

    public Inventory getInventory() {
        return inventory;
    }

    // Pour arrêter définitivement le thread à la fermeture du jeu
    public void stopGardener() {
        this.isRunning = false;
        interruptGardener();
    }

    // Méthode de test : renvoie le nombre d'actions en attente (pour les tests unitaires)
    public int getPendingActionsCount() {
        synchronized (actionQueue) {
            return actionQueue.size();
        }
    }

}
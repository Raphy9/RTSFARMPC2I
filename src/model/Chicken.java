package src.model;

import java.awt.*;
import java.util.List;

/**
 * Ennemi autonome (Poule) qui cherche la plante la plus proche pour la détruire.
 * S'exécute dans son propre Thread pour ne pas bloquer l'interface graphique.
 */
public class Chicken extends Entity implements Runnable {

    /** États possibles de la poule influençant ses animations et son comportement */
    public enum State { IDLE, RUNNING, EATING, FLEEING }

    private State currentState;
    private boolean isRunning;
    private Thread chickenThread;
    private boolean hasClucked = false; // Pour éviter de jouer le son en boucle

    /** Compteur de plantes mangées : après 3, la poule s'en va d'elle-même */
    private int plantsEaten = 0;

    public Chicken(int x, int y, World world) {
        super(world, x, y);
        this.currentState = State.IDLE;
        this.isRunning = true;
        this.setFacingDirection(Entity.LEFT);
    }

    /** Démarre le thread de vie de la poule */
    public void start() {
        this.chickenThread = new Thread(this, "ChickenThread");
        this.chickenThread.start();
    }

    /** Arrête proprement le thread */
    public void stop() {
        this.isRunning = false;
        if (chickenThread != null) chickenThread.interrupt();
    }

    /** Boucle de vie principale de l'IA */
    @Override
    public void run() {
        System.out.println("La poule apparaît !");

        while (isRunning) {
            try {
                // 1. Priorité absolue : La fuite
                if (this.currentState == State.FLEEING) {
                    handleFleeing();
                    continue;
                }

                // 2. Recherche d'une cible
                PlantTile targetPlant = findNearestPlant();

                // 3. Si aucune plante à manger, la poule se balade au hasard
                if (targetPlant == null) {
                    wanderRandomly();
                    continue;
                }

                // 4. Calcul d'itinéraire vers la plante
                int targetX = targetPlant.getX();
                int targetY = targetPlant.getY();
                List<Tile> path = pathFinding(world, targetX, targetY);

                if (path == null) {
                    this.currentState = State.IDLE;
                    Thread.sleep(1000); // Bloquée ? On attend un peu avant de retenter
                    continue;
                }

                // 5. Déplacement étape par étape
                if (!path.isEmpty()) {
                    this.currentState = State.RUNNING;
                    for (Tile step : path) {
                        if (this.currentState == State.FLEEING) break;

                        int newX = step.getX();
                        int newY = step.getY();

                        // Anticollision : On vérifie si une autre poule est déjà sur la case
                        // ou si le joueur a posé une barrière entre-temps.
                        Tile nextTile = world.getTile(newX, newY);
                        if (nextTile.hasChicken() || !nextTile.isWalkable()) {
                            break; // On s'arrête pour recalculer un chemin au prochain cycle
                        }

                        // Mise à jour de la direction visuelle
                        if (newX > this.x) setFacingDirection(Entity.RIGHT);
                        else if (newX < this.x) setFacingDirection(Entity.LEFT);

                        // Déplacement physique sur la grille
                        int oldX = this.x;
                        int oldY = this.y;
                        this.x = newX;
                        this.y = newY;

                        world.getTile(oldX, oldY).removeEntity(this);
                        world.getTile(newX, newY).addEntity(this);

                        // Ambiance sonore : cri d'approche
                        if (!hasClucked && Math.abs(newX - targetX) + Math.abs(newY - targetY) <= 10) {
                            SoundManager.playSound(SoundManager.CHICKEN_AMBIENT);
                            hasClucked = true;
                        }

                        Thread.sleep(280); // Vitesse de marche
                    }
                }

                // 6. Arrivée sur la plante : Action de manger
                if (this.currentState != State.FLEEING) {
                    Plant plant = targetPlant.getPlant();
                    if (this.x == targetX && this.y == targetY && plant != null
                            && plant.getState() != PlantState.MORT && plant.getState() != PlantState.EATEN) {

                        this.currentState = State.EATING;
                        Thread.sleep(3000); // Temps nécessaire pour manger
                        plant.destroyByEnemy();
                        hasClucked = false;

                        plantsEaten++;

                        // Gestion de la satiété
                        if (plantsEaten >= 3) {
                            System.out.println("Ventre plein, la poule quitte la ferme !");
                            this.currentState = State.FLEEING;
                            continue;
                        } else {
                            System.out.println("Digestion en cours...");
                            this.currentState = State.IDLE;
                            Thread.sleep(1000);
                            wanderRandomly(); // Évite de manger tout un champ en une ligne droite
                            continue;
                        }
                    }
                    this.currentState = State.IDLE;
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                if (this.currentState != State.FLEEING) isRunning = false;
            }
        }
    }

    /** Logique de fuite : la poule cherche le bord de map le plus proche pour disparaître */
    private void handleFleeing() throws InterruptedException {
        int distLeft = this.x;
        int distRight = World.WIDTH - 1 - this.x;
        int distTop = this.y;
        int distBottom = World.HEIGHT - 1 - this.y;

        // Choix du bord le plus proche
        int minDist = Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));
        int targetX = this.x;
        int targetY = this.y;

        if (minDist == distLeft) targetX = 0;
        else if (minDist == distRight) targetX = World.WIDTH - 1;
        else if (minDist == distTop) targetY = 0;
        else targetY = World.HEIGHT - 1;

        List<Tile> path = pathFinding(world, targetX, targetY);

        if (path != null && !path.isEmpty()) {
            for (Tile step : path) {
                // ... (Logique de déplacement rapide vers la sortie)
                Thread.sleep(100); // Fuite beaucoup plus rapide que la marche
            }
        }

        // Suppression de la poule du jeu
        world.getTile(this.x, this.y).removeEntity(this);
        world.removeEnemy(this);
        this.isRunning = false;
    }

    /**
     * Algorithme de détection : cherche la plante la plus proche non encore
     * occupée par une consœur.
     */
    private PlantTile findNearestPlant() {
        PlantTile nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                Tile tile = world.getTile(x, y);
                if (tile instanceof PlantTile) {
                    PlantTile pt = (PlantTile) tile;
                    Plant p = pt.getPlant();

                    if (p != null && p.getState() != PlantState.MORT && !p.isHarvestable()) {
                        // Empêche deux poules de viser la même plante
                        if (!pt.hasChicken() || (pt.getX() == this.x && pt.getY() == this.y)) {
                            int dist = Math.abs(x - getX()) + Math.abs(y - getY());
                            if (dist < minDistance) {
                                minDistance = dist;
                                nearest = pt;
                            }
                        }
                    }
                }
            }
        }
        return nearest;
    }

    /** Balade aléatoire dans un petit rayon pour donner une impression de vie */
    private void wanderRandomly() throws InterruptedException {
        // ... (Logique de choix de destination aléatoire et marche lente)
    }

    /** Force la poule à passer en état de fuite (appelé par le joueur) */
    public void flee() {
        if (this.currentState == State.FLEEING) return;
        SoundManager.playSound(SoundManager.CHICKEN_RUN);
        this.currentState = State.FLEEING;
        if (chickenThread != null) chickenThread.interrupt(); // Réveille le thread s'il dormait
    }

    public State getCurrentState() { return currentState; }

    /** Retourne l'index pour l'animation (ex: 0=repos, 1=marche, 2=mange) */
    public int getCurrentStateActionIndex() {
        switch (currentState) {
            case IDLE: return 0;
            case RUNNING:
            case FLEEING : return 1;
            case EATING: return 2;
            default: return 0;
        }
    }
}
package src.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ennemi volant (Corbeau).
 * Vole par-dessus les obstacles pour manger les cultures.
 */
public class Crow extends Entity implements Runnable {

    public enum State { IDLE, FLYING, EATING, FLEEING }

    private State currentState;
    private boolean isRunning;
    private Thread crowThread;
    private boolean hasCawed = false;

    // Compteur de satiété et d'ennui
    private int plantsEaten = 0;
    private int boredom = 0;

    public Crow(int x, int y, World world) {
        super(world, x, y);
        this.currentState = State.FLYING; // Il arrive en volant
        this.isRunning = true;
        this.setFacingDirection(Entity.LEFT);
    }

    public void start() {
        this.crowThread = new Thread(this, "CrowThread");
        this.crowThread.start();
    }

    public void stop() {
        this.isRunning = false;
        if (crowThread != null) crowThread.interrupt();
    }

    @Override
    public void run() {
        System.out.println("Un corbeau approche !");

        while (isRunning) {
            try {
                if (this.currentState == State.FLEEING) {
                    handleFleeing();
                    continue;
                }

                PlantTile targetPlant = findNearestPlant();

                // S'il n'y a pas de plantes, il s'ennuie et finit par fuir
                if (targetPlant == null) {
                    boredom++;
                    if (boredom >= 3) {
                        System.out.println("Le corbeau ne trouve rien à manger et s'envole !");
                        this.currentState = State.FLEEING;
                    } else {
                        wanderRandomly();
                    }
                    continue;
                } else {
                    boredom = 0;
                }

                int targetX = targetPlant.getX();
                int targetY = targetPlant.getY();

                // Le corbeau vole en ligne droite, sans se soucier des obstacles !
                List<Tile> path = getFlightPath(targetX, targetY);

                if (path == null || path.isEmpty()) {
                    this.currentState = State.IDLE;
                    Thread.sleep(1000);
                    continue;
                }

                // Phase de VOL
                this.currentState = State.FLYING;
                for (Tile step : path) {
                    if (this.currentState == State.FLEEING) break;

                    int newX = step.getX();
                    int newY = step.getY();

                    // On empêche simplement que deux corbeaux se posent sur la même case
                    if (hasCrow(world.getTile(newX, newY))) {
                        break;
                    }

                    if (newX > this.x) setFacingDirection(Entity.RIGHT);
                    else if (newX < this.x) setFacingDirection(Entity.LEFT);

                    int oldX = this.x;
                    int oldY = this.y;
                    this.x = newX;
                    this.y = newY;

                    world.getTile(oldX, oldY).removeEntity(this);
                    world.getTile(newX, newY).addEntity(this);

                    if (!hasCawed && Math.abs(newX - targetX) + Math.abs(newY - targetY) <= 10) {
                        // Tu pourras ajouter un SoundManager.CROW_CRAW plus tard !
                        hasCawed = true;
                    }

                    Thread.sleep(200); // Le corbeau vole assez vite
                }

                if (this.currentState != State.FLEEING) {
                    Plant plant = targetPlant.getPlant();
                    if (this.x == targetX && this.y == targetY && plant != null && plant.getState() != PlantState.MORT && plant.getState() != PlantState.EATEN) {

                        // 1. IL SE POSE (IDLE)
                        this.currentState = State.IDLE;
                        System.out.println("Le corbeau se pose sur une plante...");
                        Thread.sleep(1000); // Reste posé 1 seconde

                        // 2. IL MANGE (EATING)
                        this.currentState = State.EATING;
                        System.out.println("Le corbeau mange !");
                        Thread.sleep(3000);
                        plant.destroyByEnemy();
                        hasCawed = false;

                        plantsEaten++;

                        if (plantsEaten >= 3) {
                            System.out.println("Le corbeau a le ventre plein (3 plantes) et repart !");
                            // IL REPART (IDLE puis FLEEING)
                            this.currentState = State.IDLE;
                            Thread.sleep(800);
                            this.currentState = State.FLEEING;
                            continue;
                        } else {
                            System.out.println("Le corbeau digère et se prépare à voler vers une autre...");
                            // IL REPART (IDLE puis se balade)
                            this.currentState = State.IDLE;
                            Thread.sleep(1000);
                            wanderRandomly();
                            continue;
                        }
                    }

                    this.currentState = State.IDLE;
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                if (this.currentState != State.FLEEING) {
                    isRunning = false;
                }
            }
        }
    }

    /**
     * Génère un chemin en ligne droite vers la cible, en ignorant tous les obstacles (parce qu'il vole).
     */
    private List<Tile> getFlightPath(int tx, int ty) {
        List<Tile> path = new ArrayList<>();
        int currX = this.x;
        int currY = this.y;

        // Tant qu'on n'est pas arrivé, on avance d'une case (horizontalement ou verticalement)
        while (currX != tx || currY != ty) {
            if (currX != tx) {
                currX += Integer.compare(tx, currX);
            } else {
                currY += Integer.compare(ty, currY);
            }

            // On vérifie qu'on ne sort pas de la carte
            if (currX >= 0 && currX < World.WIDTH && currY >= 0 && currY < World.HEIGHT) {
                path.add(world.getTile(currX, currY));
            } else {
                break;
            }
        }
        return path;
    }

    private void handleFleeing() throws InterruptedException {
        int distLeft = this.x;
        int distRight = World.WIDTH - 1 - this.x;
        int distTop = this.y;
        int distBottom = World.HEIGHT - 1 - this.y;

        int minDist = Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));

        int targetX = this.x;
        int targetY = this.y;

        if (minDist == distLeft) targetX = 0;
        else if (minDist == distRight) targetX = World.WIDTH - 1;
        else if (minDist == distTop) targetY = 0;
        else targetY = World.HEIGHT - 1;

        List<Tile> path = getFlightPath(targetX, targetY);

        if (path != null && !path.isEmpty()) {
            this.currentState = State.FLYING; // Il s'enfuit en volant
            for (Tile step : path) {
                int newX = step.getX();
                int newY = step.getY();

                if (newX > this.x) setFacingDirection(Entity.RIGHT);
                else if (newX < this.x) setFacingDirection(Entity.LEFT);

                int oldX = this.x;
                int oldY = this.y;
                this.x = newX;
                this.y = newY;

                world.getTile(oldX, oldY).removeEntity(this);
                world.getTile(newX, newY).addEntity(this);

                Thread.sleep(120); // Vol rapide pour la fuite
            }
        }

        world.getTile(this.x, this.y).removeEntity(this);
        // /!\ NB: Il faudra peut-être adapter ta méthode removeEnemy(Chicken) dans World.java
        // pour qu'elle accepte des (Entity) ou qu'elle ait une méthode removeCrow(Crow).
        this.isRunning = false;
    }

    private PlantTile findNearestPlant() {
        PlantTile nearest = null;
        int minDistance = Integer.MAX_VALUE;

        if (world == null) return null;

        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                Tile tile = world.getTile(x, y);

                if (tile instanceof PlantTile) {
                    PlantTile pt = (PlantTile) tile;
                    Plant p = pt.getPlant();

                    if (p != null && p.getState() != PlantState.MORT && p.getState() != PlantState.EATEN && !p.isHarvestable()) {
                        if (!hasCrow(pt) || (pt.getX() == this.x && pt.getY() == this.y)) {
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

    private void wanderRandomly() throws InterruptedException {
        int radius = 8; // Le corbeau vole plus loin qu'une poule
        int randomX = this.x + (int)(Math.random() * (radius * 2 + 1)) - radius;
        int randomY = this.y + (int)(Math.random() * (radius * 2 + 1)) - radius;

        if (randomX >= 0 && randomX < World.WIDTH && randomY >= 0 && randomY < World.HEIGHT) {
            List<Tile> path = getFlightPath(randomX, randomY);

            if (path != null && !path.isEmpty()) {
                this.currentState = State.FLYING;

                for (Tile step : path) {
                    if (!isRunning || this.currentState == State.FLEEING) break;

                    if (hasCrow(world.getTile(step.getX(), step.getY()))) break;

                    if (step.getX() > this.x) setFacingDirection(Entity.RIGHT);
                    else if (step.getX() < this.x) setFacingDirection(Entity.LEFT);

                    int oldX = this.x;
                    int oldY = this.y;
                    this.x = step.getX();
                    this.y = step.getY();

                    world.getTile(oldX, oldY).removeEntity(this);
                    world.getTile(this.x, this.y).addEntity(this);

                    Thread.sleep(250);
                }
            }
        }

        if (this.currentState != State.FLEEING) {
            this.currentState = State.IDLE;
            Thread.sleep(1500 + (int)(Math.random() * 1500));
        }
    }

    /** Vérifie s'il y a déjà un corbeau sur cette case pour éviter la superposition */
    private boolean hasCrow(Tile t) {
        for (Entity e : t.getEntities()) {
            if (e instanceof Crow) return true;
        }
        return false;
    }

    public void flee() {
        if (this.currentState == State.FLEEING) return;
        System.out.println("Croa ! Le corbeau s'envole !");
        this.currentState = State.FLEEING;
        if (crowThread != null) {
            crowThread.interrupt();
        }
    }

    public State getCurrentState() { return currentState; }

    /** Renvoie l'index correspondant au CrowSpriteSheetLoader */
    public int getCurrentStateActionIndex() {
        switch (currentState) {
            case IDLE: return 0;
            case FLYING:
            case FLEEING : return 1;
            case EATING: return 2;
            default: return 0;
        }
    }
}
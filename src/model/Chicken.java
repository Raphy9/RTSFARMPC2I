package src.model;

import java.awt.*;
import java.util.List;

/**
 * Ennemi autonome (Poule) qui cherche la plante la plus proche pour la détruire.
 * S'exécute dans son propre Thread.
 */
public class Chicken extends Entity implements Runnable {

    public enum State { IDLE, RUNNING, EATING, FLEEING }

    private State currentState;
    private boolean isRunning;
    private Thread chickenThread;
    private boolean hasClucked = false;

    public Chicken(int x, int y, World world) {
        super(world, x, y);
        this.currentState = State.IDLE;
        this.isRunning = true;
        this.setFacingDirection(Entity.LEFT);
    }

    public void start() {
        this.chickenThread = new Thread(this, "ChickenThread");
        this.chickenThread.start();
    }

    public void stop() {
        this.isRunning = false;
        if (chickenThread != null) chickenThread.interrupt();
    }

    @Override
    public void run() {
        System.out.println("La poule apparaît !");

        while (isRunning) {
            try {
                if (this.currentState == State.FLEEING) {
                    handleFleeing();
                    continue;
                }

                PlantTile targetPlant = findNearestPlant();

                if (targetPlant == null) {
                    wanderRandomly();
                    continue;
                }

                int targetX = targetPlant.getX();
                int targetY = targetPlant.getY();
                List<Tile> path = pathFinding(world, targetX, targetY);

                if (path == null) {
                    this.currentState = State.IDLE;
                    Thread.sleep(1000);
                    continue;
                }

                if (!path.isEmpty()) {
                    this.currentState = State.RUNNING;
                    for (Tile step : path) {
                        if (this.currentState == State.FLEEING) break;

                        int newX = step.getX();
                        int newY = step.getY();

                        // anticollision de la poule avec les obstacles
                        Tile nextTile = world.getTile(newX, newY);
                        // Si la case est occupée par une poule OU si un obstacle vient d'être posé
                        if (nextTile.hasChicken() || !nextTile.isWalkable()) {
                            break; // Coupe le déplacement, l'IA recalculera au prochain cycle
                        }

                        if (newX > this.x) setFacingDirection(Entity.RIGHT);
                        else if (newX < this.x) setFacingDirection(Entity.LEFT);

                        int oldX = this.x;
                        int oldY = this.y;
                        this.x = newX;
                        this.y = newY;

                        world.getTile(oldX, oldY).removeEntity(this);
                        world.getTile(newX, newY).addEntity(this);

                        if (!hasClucked) {
                            if (Math.abs(newX - targetX) + Math.abs(newY - targetY) <= 10) {
                                SoundManager.playSound(SoundManager.CHICKEN_AMBIENT);
                                hasClucked = true;
                            }
                        }

                        Thread.sleep(280);
                    }
                }

                if (this.currentState != State.FLEEING) {
                    Plant plant = targetPlant.getPlant();
                    // On vérifie qu'elle est bien arrivée sur la plante (et pas bloquée par une autre poule)
                    if (this.x == targetX && this.y == targetY && plant != null && plant.getState() != PlantState.MORT && plant.getState() != PlantState.EATEN) {                        System.out.println("La poule mange une plante en (" + targetX + "," + targetY + ")");
                        this.currentState = State.EATING;
                        Thread.sleep(3000);
                        plant.destroyByEnemy();
                        hasClucked = false;     // jsp, enlever peut etre
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

        List<Tile> path = pathFinding(world, targetX, targetY);

        if (path != null && !path.isEmpty()) {
            for (Tile step : path) {
                int newX = step.getX();
                int newY = step.getY();

                // ANTICOLLISION PENDANT LA FUITE
                if (world.getTile(newX, newY).hasChicken()) {
                    break; // On esquive en recalculant un chemin
                }

                if (newX > this.x) setFacingDirection(Entity.RIGHT);
                else if (newX < this.x) setFacingDirection(Entity.LEFT);

                int oldX = this.x;
                int oldY = this.y;
                this.x = newX;
                this.y = newY;

                world.getTile(oldX, oldY).removeEntity(this);
                world.getTile(newX, newY).addEntity(this);

                Thread.sleep(100); // Fuite très rapide
            }
        }

        // Si elle est coincée ou arrivée, elle disparait
        world.getTile(this.x, this.y).removeEntity(this);
        world.removeEnemy(this);
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
                        //  On ne cible pas une plante si une AUTRE poule est déjà dessus !
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

    private void wanderRandomly() throws InterruptedException {
        int radius = 4;
        int randomX = this.x + (int)(Math.random() * (radius * 2 + 1)) - radius;
        int randomY = this.y + (int)(Math.random() * (radius * 2 + 1)) - radius;

        if (randomX >= 0 && randomX < World.WIDTH && randomY >= 0 && randomY < World.HEIGHT) {
            if (world.getTile(randomX, randomY).isWalkable()) {

                List<Tile> path = pathFinding(world, randomX, randomY);

                if (path != null && !path.isEmpty()) {
                    this.currentState = State.RUNNING;

                    for (Tile step : path) {
                        if (!isRunning || this.currentState == State.FLEEING) break;

                        int newX = step.getX();
                        int newY = step.getY();

                        // ANTICOLLISION PENDANT LA PROMENADE
                        if (world.getTile(newX, newY).hasChicken()) {
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

                        Thread.sleep(400);
                    }
                }
            }
        }

        if (this.currentState != State.FLEEING) {
            this.currentState = State.IDLE;
            Thread.sleep(1500 + (int)(Math.random() * 1500));
        }
    }

    public void flee() {
        if (this.currentState == State.FLEEING) return;

        System.out.println("Cot cot ! La poule a été chassée !");
        SoundManager.playSound(SoundManager.CHICKEN_RUN);

        this.currentState = State.FLEEING;

        if (chickenThread != null) {
            chickenThread.interrupt();
        }
    }

    public State getCurrentState() { return currentState; }

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
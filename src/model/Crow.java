package src.model;

import src.view.Display;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ennemi volant (Corbeau).
 * Gère ses propres déplacements via un Thread dédié.
 */
public class Crow extends Entity implements Runnable {

    public enum State { IDLE, FLYING, EATING, FLEEING }

    private State currentState;
    private boolean isRunning;
    private Thread crowThread;
    private boolean hasCawed = false;

    private int plantsEaten = 0;
    private int boredom = 0;

    public Crow(int x, int y, World world) {
        super(world, x, y);
        this.currentState = State.FLYING;
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
        while (isRunning) {
            try {
                if (this.currentState == State.FLEEING) {
                    handleFleeing();
                    continue;
                }

                PlantTile targetPlant = findNearestPlant();

                if (targetPlant == null) {
                    boredom++;
                    if (boredom >= 3) {
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

                List<Tile> path = getFlightPath(targetX, targetY);

                if (path == null || path.isEmpty()) {
                    this.currentState = State.IDLE;
                    Thread.sleep(1000);
                    continue;
                }

                this.currentState = State.FLYING;
                for (Tile step : path) {
                    if (this.currentState == State.FLEEING) break;

                    if (isNearScarecrow()) {
                        setFleeing(true, false);
                        break;
                    }

                    // --- MISE À JOUR ATOMIQUE DE LA POSITION ---
                    updatePosition(step.getX(), step.getY());

                    if (!hasCawed && Math.abs(this.x - targetX) + Math.abs(this.y - targetY) <= 10) {
                        SoundManager.playSound(SoundManager.CROW_AMBIENT);
                        hasCawed = true;
                    }

                    Thread.sleep(200);
                }

                if (this.currentState != State.FLEEING) {
                    Plant plant = targetPlant.getPlant();
                    if (this.x == targetX && this.y == targetY && plant != null
                            && plant.getState() != PlantState.MORT && plant.getState() != PlantState.EATEN) {

                        this.currentState = State.IDLE;
                        Thread.sleep(1000);

                        this.currentState = State.EATING;
                        Thread.sleep(3000);
                        plant.destroyByEnemy();

                        plantsEaten++;

                        if (plantsEaten >= 3) {
                            this.currentState = State.FLEEING;
                        } else {
                            this.currentState = State.IDLE;
                            Thread.sleep(1000);
                            wanderRandomly();
                        }
                    }
                }

            } catch (InterruptedException e) {
                if (this.currentState != State.FLEEING) isRunning = false;
            }
        }
    }

    /**
     * Méthode utilitaire pour déplacer l'entité proprement entre deux tuiles.
     * Évite les erreurs de synchronisation avec le rendu.
     */
    private void updatePosition(int newX, int newY) {
        if (newX > this.x) setFacingDirection(Entity.RIGHT);
        else if (newX < this.x) setFacingDirection(Entity.LEFT);

        // On retire de l'ancienne tuile AVANT de changer les coordonnées internes
        world.getTile(this.x, this.y).removeEntity(this);

        this.x = newX;
        this.y = newY;

        // On ajoute à la nouvelle tuile
        world.getTile(this.x, this.y).addEntity(this);
    }

    private boolean isNearScarecrow() {
        if (world.getBuildings() != null) {
            for (src.model.buildings.Building b : world.getBuildings()) {
                if (b instanceof src.model.buildings.Scarecrow) {
                    int radius = src.model.buildings.Scarecrow.RADIUS;
                    if (Math.abs(b.getX() - this.x) <= radius && Math.abs(b.getY() - this.y) <= radius) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<Tile> getFlightPath(int tx, int ty) {
        List<Tile> path = new ArrayList<>();
        int currX = this.x;
        int currY = this.y;
        while (currX != tx || currY != ty) {
            if (currX != tx) currX += Integer.compare(tx, currX);
            else currY += Integer.compare(ty, currY);
            if (currX >= 0 && currX < World.WIDTH && currY >= 0 && currY < World.HEIGHT) {
                path.add(world.getTile(currX, currY));
            } else break;
        }
        return path;
    }

    private void handleFleeing() throws InterruptedException {
        int targetX = (this.x < World.WIDTH / 2) ? 0 : World.WIDTH - 1;
        int targetY = (this.y < World.HEIGHT / 2) ? 0 : World.HEIGHT - 1;

        List<Tile> path = getFlightPath(targetX, targetY);
        if (path != null) {
            for (Tile step : path) {
                updatePosition(step.getX(), step.getY());
                Thread.sleep(120);
            }
        }
        world.getTile(this.x, this.y).removeEntity(this);
        world.removeEnemy(this);
        this.isRunning = false;
    }

    private PlantTile findNearestPlant() {
        PlantTile nearest = null;
        int minDistance = Integer.MAX_VALUE;
        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                Tile tile = world.getTile(x, y);
                if (tile instanceof PlantTile pt) {
                    Plant p = pt.getPlant();
                    if (p != null && p.getState() != PlantState.MORT && !p.isHarvestable()) {
                        int dist = Math.abs(x - this.x) + Math.abs(y - this.y);
                        if (dist < minDistance) {
                            minDistance = dist;
                            nearest = pt;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private void wanderRandomly() throws InterruptedException {
        int rx = (int)(Math.random() * World.WIDTH);
        int ry = (int)(Math.random() * World.HEIGHT);
        List<Tile> path = getFlightPath(rx, ry);
        if (path != null) {
            for (int i = 0; i < Math.min(path.size(), 5); i++) {
                if (this.currentState == State.FLEEING) break;
                updatePosition(path.get(i).getX(), path.get(i).getY());
                Thread.sleep(250);
            }
        }
        this.currentState = State.IDLE;
        Thread.sleep(1000);
    }

    private boolean hasCrow(Tile t) {
        return t.getEntities().stream().anyMatch(e -> e instanceof Crow);
    }

    public void flee(boolean countForQuest) {
        setFleeing(countForQuest, true);
    }

    private void setFleeing(boolean countForQuest, boolean interruptThread) {
        if (this.currentState == State.FLEEING) return;
        if (countForQuest && world != null) world.registerQuestAction(Quests.ACTION_CLICK_CROW);
        this.currentState = State.FLEEING;
        SoundManager.playSound(SoundManager.CROW_RUN);
        if (interruptThread && crowThread != null && Thread.currentThread() != crowThread) {
            crowThread.interrupt();
        }
    }

    public State getCurrentState() { return currentState; }

    public int getCurrentStateActionIndex() {
        return switch (currentState) {
            case IDLE -> 0;
            case FLYING, FLEEING -> 1;
            case EATING -> 2;
            default -> 0;
        };
    }
}
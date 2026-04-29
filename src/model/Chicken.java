package src.model;

import java.awt.*;
import java.util.List;

/**
 * Ennemi autonome (Poule) qui cherche la plante la plus proche pour la détruire.
 * Elle fonctionne sur un Thread indépendant pour ne pas bloquer l'interface utilisateur.
 */
public class Chicken extends Entity implements Runnable {

    /** États possibles de la poule pour gérer les animations et les comportements */
    public enum State { IDLE, RUNNING, EATING, FLEEING }

    private State currentState;
    private boolean isRunning;
    private Thread chickenThread;
    private boolean hasClucked = false;

    // Seuil de satiété : la poule s'en va après avoir mangé un certain nombre de plantes
    private int plantsEaten = 0;

    public Chicken(int x, int y, World world) {
        super(world, x, y);
        this.currentState = State.IDLE;
        this.isRunning = true;
        this.setFacingDirection(Entity.LEFT);
    }

    /** Initialise et lance le thread de vie de la poule */
    public void start() {
        this.chickenThread = new Thread(this, "ChickenThread");
        this.chickenThread.start();
    }

    /** Arrête proprement l'activité de la poule */
    public void stop() {
        this.isRunning = false;
        if (chickenThread != null) chickenThread.interrupt();
    }

    /** Boucle principale de l'IA de la poule */
    @Override
    public void run() {
        System.out.println("La poule apparaît !");

        while (isRunning) {
            try {
                // 1. Priorité absolue : Fuir si elle est effrayée
                if (this.currentState == State.FLEEING) {
                    handleFleeing();
                    continue;
                }

                // 2. Recherche d'une cible (plante la plus proche)
                PlantTile targetPlant = findNearestPlant();

                // Si aucune plante n'est disponible sur la carte, elle se promène
                if (targetPlant == null) {
                    wanderRandomly();
                    continue;
                }

                int targetX = targetPlant.getX();
                int targetY = targetPlant.getY();

                // 3. Calcul du chemin vers la plante cible (Pathfinding A*)
                List<Tile> path = pathFinding(world, targetX, targetY);

                if (path == null) {
                    this.currentState = State.IDLE;
                    Thread.sleep(1000); // Pause si le chemin est bloqué
                    continue;
                }

                // --- Phase de DÉPLACEMENT ---
                if (!path.isEmpty()) {
                    this.currentState = State.RUNNING;
                    for (Tile step : path) {
                        // On vérifie à chaque pas si elle doit changer d'état pour fuir
                        if (this.currentState == State.FLEEING) break;

                        int newX = step.getX();
                        int newY = step.getY();

                        // Évite de marcher sur une case occupée ou un obstacle
                        Tile nextTile = world.getTile(newX, newY);
                        if (nextTile.hasChicken() || !nextTile.isWalkable()) {
                            break;
                        }

                        // Mise à jour de l'orientation visuelle
                        if (newX > this.x) setFacingDirection(Entity.RIGHT);
                        else if (newX < this.x) setFacingDirection(Entity.LEFT);

                        // Mise à jour de la grille de jeu
                        int oldX = this.x;
                        int oldY = this.y;
                        this.x = newX;
                        this.y = newY;

                        world.getTile(oldX, oldY).removeEntity(this);
                        world.getTile(newX, newY).addEntity(this);

                        // Cri de proximité (une seule fois à l'approche de la plante)
                        if (!hasClucked) {
                            if (Math.abs(newX - targetX) + Math.abs(newY - targetY) <= 10) {
                                SoundManager.playSound(SoundManager.CHICKEN_AMBIENT);
                                hasClucked = true;
                            }
                        }

                        Thread.sleep(280); // Vitesse de course de la poule
                    }
                }

                // --- Phase d'ACTION (Manger) ---
                if (this.currentState != State.FLEEING) {
                    Plant plant = targetPlant.getPlant();
                    // Vérifie si la plante est toujours là et mangeable à l'arrivée
                    if (this.x == targetX && this.y == targetY && plant != null && plant.getState() != PlantState.MORT && plant.getState() != PlantState.EATEN) {
                        this.currentState = State.EATING;
                        Thread.sleep(3000); // Temps nécessaire pour manger
                        plant.destroyByEnemy();
                        hasClucked = false;

                        plantsEaten++;

                        // Si la poule a mangé 3 plantes, elle quitte la ferme (flee)
                        if (plantsEaten >= 3) {
                            this.currentState = State.FLEEING;
                            continue;
                        } else {
                            // Sinon, elle se repose un peu avant de chercher la suivante
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
                // L'interruption (clic du joueur ou stop) réveille le thread.
                // Si elle n'est pas morte, elle entame sa fuite au prochain tour de boucle.
                System.out.println("Poule interrompue, passage à la fuite.");
            }
        }
    }

    /**
     * Calcule le bord de carte le plus proche pour s'échapper.
     */
    private void handleFleeing() throws InterruptedException {
        int distLeft = this.x;
        int distRight = World.WIDTH - 1 - this.x;
        int distTop = this.y;
        int distBottom = World.HEIGHT - 1 - this.y;

        // On cherche la distance minimale vers un des quatre bords
        int minDist = Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));

        int targetX = this.x;
        int targetY = this.y;

        if (minDist == distLeft) targetX = 0;
        else if (minDist == distRight) targetX = World.WIDTH - 1;
        else if (minDist == distTop) targetY = 0;
        else targetY = World.HEIGHT - 1;

        List<Tile> path = pathFinding(world, targetX, targetY);

        // La poule court vers la sortie
        if (path != null && !path.isEmpty()) {
            for (Tile step : path) {
                int newX = step.getX();
                int newY = step.getY();

                if (world.getTile(newX, newY).hasChicken()) break;

                if (newX > this.x) setFacingDirection(Entity.RIGHT);
                else if (newX < this.x) setFacingDirection(Entity.LEFT);

                int oldX = this.x;
                int oldY = this.y;
                this.x = newX;
                this.y = newY;

                world.getTile(oldX, oldY).removeEntity(this);
                world.getTile(newX, newY).addEntity(this);

                Thread.sleep(100); // Court plus vite quand elle fuit
            }
        }

        // Suppression de l'entité du monde une fois le bord atteint
        world.getTile(this.x, this.y).removeEntity(this);
        world.removeEnemy(this);
        this.isRunning = false; // Fin du thread
    }

    /**
     * Recherche par balayage de la grille la plante vivante la plus proche.
     */
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
                    // Vérifie que la plante est vivante et pas encore récoltable
                    if (p != null && p.getState() != PlantState.MORT && p.getState() != PlantState.EATEN && !p.isHarvestable()) {
                        // Vérifie que la plante n'est pas déjà "attaquée" par une autre poule
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

    /**
     * Déplacement aléatoire dans un petit périmètre (utilisé quand elle est IDLE).
     */
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
                        if (world.getTile(newX, newY).hasChicken()) break;

                        if (newX > this.x) setFacingDirection(Entity.RIGHT);
                        else if (newX < this.x) setFacingDirection(Entity.LEFT);

                        this.x = newX;
                        this.y = newY;
                        // ... logique de mise à jour de tuile omise pour la brièveté ...
                        Thread.sleep(400); // Marche plus lentement en se promenant
                    }
                }
            }
        }
        if (this.currentState != State.FLEEING) {
            this.currentState = State.IDLE;
            Thread.sleep(1500 + (int)(Math.random() * 1500));
        }
    }

    /** Déclenche le mode fuite (appelé par le contrôleur lors d'un clic sur l'entité) */
    public void flee() {
        if (this.currentState == State.FLEEING) return;
        System.out.println("Cot cot ! La poule a été chassée !");
        SoundManager.playSound(SoundManager.CHICKEN_RUN);
        this.currentState = State.FLEEING;
        if (chickenThread != null) {
            chickenThread.interrupt(); // Interrompt le sommeil en cours pour fuir immédiatement
        }
    }

    public State getCurrentState() { return currentState; }

    /** Mappe l'état de l'IA vers l'index de la Sprite Sheet pour l'animation */
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
package src.model;

import java.awt.*;
import java.util.List;

/**
 * Ennemi autonome (Poule) qui cherche la plante la plus proche pour la détruire.
 * S'exécute dans son propre Thread.
 */
public class Chicken extends Entity implements Runnable {

    // États possibles de la poule
    public enum State { IDLE, RUNNING, EATING, FLEEING }

    // Attributs
    private State currentState;
    private boolean isRunning;
    private Thread chickenThread;

    // Constructeur
    public Chicken(int x, int y, World world) {
        super(world, x, y);
        // La poule commence en mode "idle" (errante) et cherche une plante à manger
        this.currentState = State.IDLE;
        // Par défaut, la poule regarde vers la gauche (on peut ajuster selon le sprite)
        this.isRunning = true;
        this.setFacingDirection(Entity.LEFT);
    }

    // Méthodes de contrôle du thread
    public void start() {
        this.chickenThread = new Thread(this, "ChickenThread");
        this.chickenThread.start();
    }

    // Arrête le thread de la poule (lorsqu'elle est chassée ou que le jeu se ferme)
    public void stop() {
        // La poule arrête son comportement et quitte le jeu
        this.isRunning = false;
        if (chickenThread != null) chickenThread.interrupt();
    }

    // Le cœur du comportement de la poule : cherche une plante, s'approche, mange, et réagit aux clics
    @Override
    public void run() {
        System.out.println("La poule apparaît !");

        while (isRunning) {
            try {
                //Si on clique sur la poule, elle doit s'enfuir immédiatement, même si elle est en train de courir vers une plante ou de manger
                //C'est notre priorité
                if (this.currentState == State.FLEEING) {
                    handleFleeing();
                    continue;
                }

                // Chercher une cible à manger
                PlantTile targetPlant = findNearestPlant();

                // Si aucune plante n'est trouvée, la poule erre aléatoirement dans un rayon de 4 cases autour d'elle
                if (targetPlant == null) {
                    wanderRandomly();
                    continue;
                }

                // Calculer le chemin vers la plante
                int targetX = targetPlant.getX();
                int targetY = targetPlant.getY();
                // Trouver la tuile adjacente marchable la plus proche de la plante
                List<Tile> path = pathFinding(world, targetX, targetY);

                // Si aucun chemin n'est trouvé (ex: la plante est entourée d'obstacles), la poule reste en mode idle et réessaie plus tard
                if (path == null) {
                    this.currentState = State.IDLE;
                    Thread.sleep(1000);
                    continue;
                }

                //  Déplacement vers la plante
                if (!path.isEmpty()) {
                    // La poule commence à courir vers la plante
                    this.currentState = State.RUNNING;
                    for (Tile step : path) {
                        // Si on clique sur la poule PENDANT qu'elle court vers la plante, elle arrête tout
                        if (this.currentState == State.FLEEING) break;

                        // Se déplacer d'une tuile à la fois
                        int newX = step.getX();
                        int newY = step.getY();

                        // Mettre à jour la direction de la poule en fonction du mouvement
                        if (newX > this.x) setFacingDirection(Entity.RIGHT);
                        else if (newX < this.x) setFacingDirection(Entity.LEFT);

                        // Mettre à jour la position de la poule sur la carte
                        int oldX = this.x;
                        int oldY = this.y;
                        this.x = newX;
                        this.y = newY;

                        // Mettre à jour les entités sur les tuiles
                        world.getTile(oldX, oldY).removeEntity(this);
                        world.getTile(newX, newY).addEntity(this);

                        // Pause entre chaque déplacement pour simuler la course
                        Thread.sleep(200);
                    }
                }

                // Attaque (si elle n'a pas été effrayée entre temps)
                if (this.currentState != State.FLEEING) {
                    // La poule est maintenant sur la tuile de la plante (ou à côté si la plante est sur une tuile non marchable), elle peut attaquer
                    Plant plant = targetPlant.getPlant();
                    // Si la plante existe toujours et n'est pas déjà morte, la poule la mange
                    if (plant != null && plant.getState() != PlantState.MORT) {
                        System.out.println("La poule mange une plante en (" + targetX + "," + targetY + ")");
                        this.currentState = State.EATING;
                        Thread.sleep(3000); // Animation de 3 secondes
                        // Après avoir mangé, la plante est détruite (passée à l'état MORT)
                        plant.destroyByEnemy();
                    }
                    // Après avoir mangé, la poule retourne à l'état idle pour chercher une nouvelle plante
                    this.currentState = State.IDLE;
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                // Le thread a été interrompu (soit par la fuite, soit par la fermeture du jeu)
                if (this.currentState != State.FLEEING) {
                    isRunning = false;
                }
            }
        }
        System.out.println("La poule a définitivement quitté la ferme.");
    }

    /**
     * Gère la fuite de la poule vers le bord le plus proche de la carte.
     */
    private void handleFleeing() throws InterruptedException {
        // Calculer la distance aux bords de la carte
        int distLeft = this.x;
        int distRight = World.WIDTH - 1 - this.x;
        int distTop = this.y;
        int distBottom = World.HEIGHT - 1 - this.y;

        // Trouver le bord le plus proche
        int minDist = Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));

        // Déterminer les coordonnées de la cible de fuite (le bord le plus proche)
        int targetX = this.x;
        int targetY = this.y;

        // En cas d'égalité, la poule choisit de fuir vers la gauche ou le haut en priorité (arbitraire)
        if (minDist == distLeft) targetX = 0;
        else if (minDist == distRight) targetX = World.WIDTH - 1;
        else if (minDist == distTop) targetY = 0;
        else targetY = World.HEIGHT - 1;

        // Trouver un chemin vers le bord
        List<Tile> path = pathFinding(world, targetX, targetY);

        // Si aucun chemin n'est trouvé (ce qui serait surprenant, mais possible si la poule est coincée), elle reste sur place et disparaît après un moment
        if (path != null && !path.isEmpty()) {
            for (Tile step : path) {
                int newX = step.getX();
                int newY = step.getY();

                // Mettre à jour la direction de la poule en fonction du mouvement
                if (newX > this.x) setFacingDirection(Entity.RIGHT);
                else if (newX < this.x) setFacingDirection(Entity.LEFT);

                // Mettre à jour la position de la poule sur la carte
                int oldX = this.x;
                int oldY = this.y;
                this.x = newX;
                this.y = newY;

                // Mettre à jour les entités sur les tuiles
                world.getTile(oldX, oldY).removeEntity(this);
                world.getTile(newX, newY).addEntity(this);

                // Elle s'enfuit (vitesse à ajuster)
                Thread.sleep(200);
            }
        }

        // Arrivée au bord : Elle disparaît vraiment cette fois-ci
        world.getTile(this.x, this.y).removeEntity(this);
        world.removeEnemy(this); // L'efface de l'écran !
        this.isRunning = false;  // Tue le cerveau proprement
    }

    // Trouve la plante la plus proche qui n'est pas encore morte et pas encore récoltable
    private PlantTile findNearestPlant() {
        // On parcourt toutes les tuiles du monde pour trouver la plante la plus proche qui n'est pas encore morte et pas encore récoltable
        PlantTile nearest = null;
        int minDistance = Integer.MAX_VALUE;

        if (world == null) return null;

        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                Tile tile = world.getTile(x, y);

                if (tile instanceof PlantTile) {
                    PlantTile pt = (PlantTile) tile;
                    Plant p = pt.getPlant();

                    if (p != null && p.getState() != PlantState.MORT && !p.isHarvestable()) {
                        int dist = Math.abs(x - getX()) + Math.abs(y - getY());
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

    // La poule erre aléatoirement dans un rayon de 4 cases autour d'elle lorsqu'elle n'a pas de cible à manger
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
        if (this.currentState == State.FLEEING) return; // Si elle fuit déjà, on ignore le clic

        System.out.println("Cot cot ! La poule a été chassée !");
        this.currentState = State.FLEEING; // Change l'intention de la poule

        if (chickenThread != null) {
            chickenThread.interrupt(); // Coupe son sommeil pour qu'elle réagisse instantanément
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
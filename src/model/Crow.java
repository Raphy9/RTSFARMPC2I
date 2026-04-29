package src.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ennemi volant (Corbeau).
 * Cette entité a la particularité de pouvoir ignorer les obstacles physiques
 * (murs, bâtiments) grâce à son mode de déplacement aérien.
 */
public class Crow extends Entity implements Runnable {

    /** États possibles de l'IA pour l'animation et la logique de comportement */
    public enum State { IDLE, FLYING, EATING, FLEEING }

    private State currentState;
    private boolean isRunning;
    private Thread crowThread;
    private boolean hasCawed = false;

    // Paramètres de besoins : satiété (nombre de plantes mangées) et lassitude (si rien à manger)
    private int plantsEaten = 0;
    private int boredom = 0;

    public Crow(int x, int y, World world) {
        super(world, x, y);
        this.currentState = State.FLYING; // Le corbeau apparaît toujours en plein vol
        this.isRunning = true;
        this.setFacingDirection(Entity.LEFT);
    }

    /** Lance le thread dédié à l'IA du corbeau */
    public void start() {
        this.crowThread = new Thread(this, "CrowThread");
        this.crowThread.start();
    }

    /** Arrête le thread proprement lors de la fermeture ou suppression */
    public void stop() {
        this.isRunning = false;
        if (crowThread != null) crowThread.interrupt();
    }

    /** Boucle de vie principale du corbeau */
    @Override
    public void run() {
        System.out.println("Un corbeau approche !");

        while (isRunning) {
            try {
                // Gestion prioritaire de la fuite
                if (this.currentState == State.FLEEING) {
                    handleFleeing();
                    continue;
                }

                // Recherche de la plante la plus proche
                PlantTile targetPlant = findNearestPlant();

                // Si aucune plante n'est disponible sur la carte
                if (targetPlant == null) {
                    boredom++;
                    if (boredom >= 3) {
                        System.out.println("Le corbeau ne trouve rien a manger et s'envole !");
                        this.currentState = State.FLEEING;
                    } else {
                        wanderRandomly(); // Le corbeau tourne en attendant que quelque chose pousse
                    }
                    continue;
                } else {
                    boredom = 0; // Réinitialise l'ennui si une cible est trouvée
                }

                int targetX = targetPlant.getX();
                int targetY = targetPlant.getY();

                // Calcul du trajet en ligne droite (vol aérien)
                List<Tile> path = getFlightPath(targetX, targetY);

                if (path == null || path.isEmpty()) {
                    this.currentState = State.IDLE;
                    Thread.sleep(1000);
                    continue;
                }

                // --- Phase de VOL vers la plante ---
                this.currentState = State.FLYING;
                for (Tile step : path) {
                    if (this.currentState == State.FLEEING) break;

                    // Détection dynamique des épouvantails pendant le trajet
                    if (isNearScarecrow()) {
                        System.out.println("Croa ! Le corbeau a vu un épouvantail et panique !");
                        flee();
                        break;
                    }

                    int newX = step.getX();
                    int newY = step.getY();

                    // Évite la superposition parfaite de deux corbeaux sur la même case
                    if (hasCrow(world.getTile(newX, newY))) {
                        break;
                    }

                    // Mise à jour visuelle de la direction
                    if (newX > this.x) setFacingDirection(Entity.RIGHT);
                    else if (newX < this.x) setFacingDirection(Entity.LEFT);

                    // Mise à jour de la position sur la grille du monde
                    int oldX = this.x;
                    int oldY = this.y;
                    this.x = newX;
                    this.y = newY;

                    world.getTile(oldX, oldY).removeEntity(this);
                    world.getTile(newX, newY).addEntity(this);

                    // Déclenchement du cri d'ambiance à l'approche du but
                    if (!hasCawed && Math.abs(newX - targetX) + Math.abs(newY - targetY) <= 10) {
                        SoundManager.playSound(SoundManager.CROW_AMBIENT);
                        hasCawed = true;
                    }

                    Thread.sleep(200); // Vitesse de croisière du vol
                }

                // --- Phase d'ACTION (manger la plante) ---
                if (this.currentState != State.FLEEING) {
                    Plant plant = targetPlant.getPlant();
                    if (this.x == targetX && this.y == targetY && plant != null && plant.getState() != PlantState.MORT && plant.getState() != PlantState.EATEN) {

                        // Atterrissage
                        this.currentState = State.IDLE;
                        System.out.println("Le corbeau se pose sur une plante...");
                        Thread.sleep(1000);

                        // Consommation de la plante
                        this.currentState = State.EATING;
                        System.out.println("Le corbeau mange !");
                        Thread.sleep(3000);
                        plant.destroyByEnemy();

                        plantsEaten++;

                        // Vérification de la satiété : repart après 3 plantes mangées
                        if (plantsEaten >= 3) {
                            System.out.println("Le corbeau a le ventre plein (3 plantes) et repart !");
                            this.currentState = State.IDLE;
                            Thread.sleep(800);
                            this.currentState = State.FLEEING;
                            continue;
                        } else {
                            System.out.println("Le corbeau digere et se prépare a voler vers une autre...");
                            this.currentState = State.IDLE;
                            Thread.sleep(1000);
                            wanderRandomly(); // Petite pause avant la prochaine plante
                            continue;
                        }
                    }

                    this.currentState = State.IDLE;
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                // Si interrompu pendant un sommeil, on vérifie si c'est pour s'arrêter ou pour fuir
                if (this.currentState != State.FLEEING) {
                    isRunning = false;
                }
            }
        }
    }

    /**
     * Scanne les bâtiments du monde pour trouver un épouvantail.
     * Utilise le RADIUS statique de la classe Scarecrow pour définir la zone de peur.
     */
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

    /**
     * Algorithme de trajectoire simplifiée.
     * Ignore les obstacles (isWalkable) car le corbeau survole le terrain.
     */
    private List<Tile> getFlightPath(int tx, int ty) {
        List<Tile> path = new ArrayList<>();
        int currX = this.x;
        int currY = this.y;

        while (currX != tx || currY != ty) {
            if (currX != tx) {
                currX += Integer.compare(tx, currX);
            } else {
                currY += Integer.compare(ty, currY);
            }

            if (currX >= 0 && currX < World.WIDTH && currY >= 0 && currY < World.HEIGHT) {
                path.add(world.getTile(currX, currY));
            } else {
                break;
            }
        }
        return path;
    }

    /** Calcule le bord de carte le plus proche pour faire sortir le corbeau du jeu */
    private void handleFleeing() throws InterruptedException {
        int distLeft = this.x;
        int distRight = World.WIDTH - 1 - this.x;
        int distTop = this.y;
        int distBottom = World.HEIGHT - 1 - this.y;

        int minDist = Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));

        int targetX = this.x;
        int targetY = this.y;

        // Choix du bord de sortie
        if (minDist == distLeft) targetX = 0;
        else if (minDist == distRight) targetX = World.WIDTH - 1;
        else if (minDist == distTop) targetY = 0;
        else targetY = World.HEIGHT - 1;

        List<Tile> path = getFlightPath(targetX, targetY);

        if (path != null && !path.isEmpty()) {
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

                Thread.sleep(120); // Vitesse de fuite accélérée
            }
        }

        // Suppression de l'entité du monde à la fin du trajet
        world.getTile(this.x, this.y).removeEntity(this);
        world.removeEnemy(this);
        this.isRunning = false;
    }

    /** Scanne la carte pour trouver une plante vivante non récoltable */
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

                    // Cible uniquement les plantes vivantes qui ne sont pas encore prêtes à être récoltées
                    if (p != null && p.getState() != PlantState.MORT && p.getState() != PlantState.EATEN && !p.isHarvestable()) {
                        // Ne cible pas une plante déjà occupée par un autre corbeau
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

    /** Déplacement aléatoire pour simuler un comportement naturel quand aucune cible n'est présente */
    private void wanderRandomly() throws InterruptedException {
        int radius = 8;
        int randomX = this.x + (int)(Math.random() * (radius * 2 + 1)) - radius;
        int randomY = this.y + (int)(Math.random() * (radius * 2 + 1)) - radius;

        if (randomX >= 0 && randomX < World.WIDTH && randomY >= 0 && randomY < World.HEIGHT) {
            List<Tile> path = getFlightPath(randomX, randomY);

            if (path != null && !path.isEmpty()) {
                this.currentState = State.FLYING;

                for (Tile step : path) {
                    if (!isRunning || this.currentState == State.FLEEING) break;

                    if (isNearScarecrow()) {
                        flee();
                        break;
                    }

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

    /** Vérifie si un corbeau est déjà présent sur une tuile donnée */
    private boolean hasCrow(Tile t) {
        for (Entity e : t.getEntities()) {
            if (e instanceof Crow) return true;
        }
        return false;
    }

    /** Déclenche la fuite de l'entité (utilisé par le clic joueur ou l'épouvantail) */
    public void flee() {
        if (this.currentState == State.FLEEING) return;

        System.out.println("Croa ! Le corbeau s'envole !");
        SoundManager.playSound(SoundManager.CROW_RUN);

        // Enregistre l'action pour la progression des quêtes
        if (world != null) {
            world.registerQuestAction(Quests.ACTION_CLICK_CROW);
        }

        this.currentState = State.FLEEING;

        // Interrompt le thread pour forcer la réaction immédiate (sort du sleep actuel)
        if (crowThread != null) {
            crowThread.interrupt();
        }
    }

    public State getCurrentState() { return currentState; }

    /** Retourne l'index pour l'animation visuelle en fonction de l'état actuel */
    public int getCurrentStateActionIndex() {
        switch (currentState) {
            case IDLE: return 0;
            case FLYING:
            case FLEEING : return 1; // Utilise la même animation de vol pour la fuite
            case EATING: return 2;
            default: return 0;
        }
    }
}
package src.model;

import java.awt.*;
import java.util.List;

/**
 * Ennemi autonome (Poule) qui cherche la plante la plus proche pour la détruire.
 * S'exécute dans son propre Thread.
 * La poule suit l'algorithme A*, comme toutes les entités du jeu
 */
public class Chicken extends Entity implements Runnable {

    // États spécifiques de la poule
    public enum State { IDLE, RUNNING, EATING }

    // La poule n'a pas de direction "Haut/Bas" spécifique, elle regarde soit à gauche soit à droite
    private State currentState;
    // Flag pour contrôler l'exécution du thread
    private boolean isRunning;
    // Thread dédié pour la poule
    private Thread chickenThread;

    // Constructeur : reçoit la position de départ et une référence au monde pour pouvoir interagir avec lui
    public Chicken(int x, int y, World world) {
        super(world, x, y);
        this.currentState = State.IDLE;
        this.isRunning = true;

        // La poule regarde vers la gauche par défaut
        this.setFacingDirection(Entity.LEFT);
    }

    // Méthodes de contrôle du thread
    public void start() {
        this.chickenThread = new Thread(this, "ChickenThread");
        this.chickenThread.start();
    }

    // Arrêt propre du thread de la poule
    public void stop() {
        this.isRunning = false;
        if (chickenThread != null) chickenThread.interrupt();
    }


    // Le cœur du comportement de la poule : elle cherche, court et mange en boucle tant qu'elle est vivante
    @Override
    public void run() {
        System.out.println("La poule sauvage apparaît !");

        while (isRunning) {
            try {
                // Trouver la plante la plus proche
                PlantTile targetPlant = findNearestPlant();

                if (targetPlant == null) {
                    // Pas de culture, On attend 2 secondes avant de rescanner
                    this.currentState = State.IDLE;
                    Thread.sleep(2000);
                    continue; // Recommence la boucle
                }

                //  Calculer le chemin vers la plante
                int targetX = targetPlant.getX();
                int targetY = targetPlant.getY();

                // Calcul A* vers la case exacte de la plante, comme pour toutes les entités
                List<Tile> path = pathFinding(world, targetX, targetY);

                // Si le chemin est null, la plante est bloquée par des murs
                if (path == null) {
                    this.currentState = State.IDLE;
                    Thread.sleep(1000);
                    continue;
                }

                //Déplacement
                if (!path.isEmpty()) {
                    // La poule passe en mode course
                    this.currentState = State.RUNNING;

                    // Parcours du chemin étape par étape
                    for (Tile step : path) {
                        int newX = step.getX();
                        int newY = step.getY();

                        // Mise à jour de la direction visuelle
                        if (newX > this.x) setFacingDirection(Entity.RIGHT);
                        else if (newX < this.x) setFacingDirection(Entity.LEFT);

                        // Déplacement physique (on l'enlève de l'ancienne case et on l'ajoute à la nouvelle)
                        int oldX = this.x;
                        int oldY = this.y;
                        this.x = newX;
                        this.y = newY;
                        world.getTile(oldX, oldY).removeEntity(this);
                        world.getTile(newX, newY).addEntity(this);

                        // Vitesse de course (plus c'est bas, plus la poule est rapide)
                        Thread.sleep(200);
                    }
                }

                // Attaque
                // Vérifier si la plante est toujours là
                Plant plant = targetPlant.getPlant();
                if (plant != null && plant.getState() != PlantState.MORT) {

                    System.out.println("La poule mange une plante en (" + targetX + "," + targetY + ")");
                    this.currentState = State.EATING;

                    // Temps d'animation de "manger" (3 secondes)
                    Thread.sleep(3000);

                    // Destruction de la plante (elle devient pourrie, peut-être changer plus tard?)
                    plant.destroyByEnemy();
                }

                // Petite pause avant de chercher la prochaine cible
                this.currentState = State.IDLE;
                Thread.sleep(500);
                // Si la poule est interrompue pendant son sommeil, on sort proprement de la boucle
            } catch (InterruptedException e) {
                // Arrêt propre du thread
                isRunning = false;
            }
        }
        System.out.println("La poule a quitté la ferme.");
    }

    /** Scanne le monde pour trouver la PlantTile contenant une plante vivante la plus proche (distance Manhattan) */
    private PlantTile findNearestPlant() {
        PlantTile nearest = null;
        int minDistance = Integer.MAX_VALUE;

        // Optimisation : On ne scanne que si le monde existe
        if (world == null) return null;

        // Parcours de toutes les cases du monde pour trouver la plante vivante la plus proche
        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                // Vérifier si la tuile est une PlantTile
                Tile tile = world.getTile(x, y);

                // Si c'est une PlantTile, vérifier si elle contient une plante vivante (pas morte, pas mûre)
                if (tile instanceof PlantTile) {
                    // On récupère la plante de la tuile pour vérifier son état
                    PlantTile pt = (PlantTile) tile;
                    // On ne cible que les plantes vivantes (pas mûres, pas mortes)
                    Plant p = pt.getPlant();

                    // On ne cible que les plantes vivantes (pas mûres, pas mortes)
                    if (p != null && p.getState() != PlantState.MORT && !p.isHarvestable()) {
                        // Calcul de la distance Manhattan entre la poule et la plante
                        int dist = Math.abs(x - getX()) + Math.abs(y - getY());
                        // Si cette plante est plus proche que la précédente, on la garde comme cible
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

    // Getters pour la vue
    public State getCurrentState() { return currentState; }

    /** Convertit l'état Enum en Index (0,1,2) pour le ChickenSpriteSheetLoader */
    public int getCurrentStateActionIndex() {
        switch (currentState) {
            case IDLE: return 0;
            case RUNNING: return 1;
            case EATING: return 2;
            default: return 0;
        }
    }
}
package src.model;

import java.util.Random;

/**
 * Générateur d'ennemis (Poules).
 * Tourne dans son propre Thread et fait apparaître des poules sur les bords de la carte à intervalles réguliers.
 */
public class ChickenSpawner implements Runnable {

    private final World world;
    private boolean isRunning;
    private Thread spawnerThread;
    private final Random random;

    //  Paramètres d'apparition
    // Temps entre chaque apparition (en millisecondes)
    private static final int SPAWN_INTERVAL = 20000;
    // Nombre maximum de poules en même temps sur la carte (pour éviter de surcharger le jeu)
    private static final int MAX_CHICKENS = 3;

    public ChickenSpawner(World world) {
        this.world = world;
        this.isRunning = true;
        this.random = new Random();
    }

    public void start() {
        this.spawnerThread = new Thread(this, "ChickenSpawnerThread");
        this.spawnerThread.start();
    }

    public void stop() {
        this.isRunning = false;
        if (this.spawnerThread != null) {
            this.spawnerThread.interrupt();
        }
    }

    @Override
    public void run() {
        System.out.println("Générateur de poules activé !");

        while (isRunning) {
            try {
                // On attend X secondes avant de faire apparaître la prochaine poule
                Thread.sleep(SPAWN_INTERVAL);

                // On ne fait spawn une poule que si on n'a pas atteint la limite
                if (world.getEnemies().size() < MAX_CHICKENS) {
                    spawnChickenAtEdge();
                }

            } catch (InterruptedException e) {
                // Si le jeu se ferme, on arrête le thread
                isRunning = false;
            }
        }
    }

    /**
     * Calcule des coordonnées aléatoires sur l'un des 4 bords de la carte
     * et y crée une nouvelle poule.
     */
    private void spawnChickenAtEdge() {
        int x = 0;
        int y = 0;
        boolean validSpot = false;
        int attempts = 0;

        // On cherche une case marchable sur un bord (max 10 tentatives pour éviter une boucle infinie si le bord est un gros mur)
        while (!validSpot && attempts < 10) {
            int edge = random.nextInt(4); // 0 = Haut, 1 = Bas, 2 = Gauche, 3 = Droite

            switch (edge) {
                case 0: // Bord Haut
                    x = random.nextInt(World.WIDTH);
                    y = 0;
                    break;
                case 1: // Bord Bas
                    x = random.nextInt(World.WIDTH);
                    y = World.HEIGHT - 1;
                    break;
                case 2: // Bord Gauche
                    x = 0;
                    y = random.nextInt(World.HEIGHT);
                    break;
                case 3: // Bord Droite
                    x = World.WIDTH - 1;
                    y = random.nextInt(World.HEIGHT);
                    break;
            }

            // On vérifie que la case choisie n'est pas un obstacle bloquant
            if (world.getTile(x, y).isWalkable()) {
                validSpot = true;
            }
            attempts++;
        }

        // Si on a trouvé une bonne case, on fait apparaître la poule !
        if (validSpot) {
            Chicken newChicken = new Chicken(x, y, world);
            world.getEnemies().add(newChicken); // CopyOnWriteArrayList protège contre les crashs !
            newChicken.start(); // Lance le script de la poule dans son propre thread
        }
    }
}
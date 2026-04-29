package src.model;

import java.util.Random;

/**
 * Générateur d'ennemis (Poules).
 * Tourne dans son propre Thread et fait apparaître des poules sur les bords
 * de la carte à intervalles réguliers.
 */
public class ChickenSpawner implements Runnable {

    private final World world;
    private boolean isRunning;
    private Thread spawnerThread;
    private final Random random;

    /**
     * Permet de mettre le spawn en pause (ex: pendant un dialogue ou le tuto)
     * sans détruire le thread.
     */
    private static boolean isActive = true;

    // --- Paramètres d'apparition ---

    /** Temps d'attente entre deux tentatives d'apparition (20 secondes). */
    private static final int SPAWN_INTERVAL = 20000;

    /** Limite de population pour ne pas transformer le jeu en simulateur de basse-cour. */
    private static final int MAX_CHICKENS = 3;

    public ChickenSpawner(World world) {
        this.world = world;
        this.isRunning = true;
        this.random = new Random();
    }

    /** Initialise et lance le thread dédié au spawn. */
    public void start() {
        this.spawnerThread = new Thread(this, "ChickenSpawnerThread");
        this.spawnerThread.start();
    }

    /** Arrête proprement la boucle de génération. */
    public void stop() {
        this.isRunning = false;
        if (this.spawnerThread != null) {
            this.spawnerThread.interrupt();
        }
    }

    public static void setActive(boolean active) {
        isActive = active;
    }

    /** Boucle de contrôle du générateur. */
    @Override
    public void run() {
        System.out.println("Générateur de poules activé !");

        while (isRunning) {
            try {
                // Rythme de croisière du générateur
                Thread.sleep(SPAWN_INTERVAL);

                // Conditions de spawn : activité activée ET limite de population non atteinte
                if (isActive && world.getEnemies().size() < MAX_CHICKENS) {
                    spawnChickenAtEdge();
                }

            } catch (InterruptedException e) {
                // Interruption propre lors de la fermeture du jeu
                isRunning = false;
            }
        }
    }

    /**
     * Sélectionne aléatoirement un point d'entrée sur les bordures du monde.
     * S'assure que la poule n'apparaît pas coincée dans un mur ou un objet.
     */
    private void spawnChickenAtEdge() {
        int x = 0;
        int y = 0;
        boolean validSpot = false;
        int attempts = 0;

        // Tentative de trouver une case "marchable" sur un bord (max 10 essais)
        while (!validSpot && attempts < 10) {
            int edge = random.nextInt(4); // 0 = Haut, 1 = Bas, 2 = Gauche, 3 = Droite

            switch (edge) {
                case 0: // Bordure supérieure
                    x = random.nextInt(World.WIDTH);
                    y = 0;
                    break;
                case 1: // Bordure inférieure
                    x = random.nextInt(World.WIDTH);
                    y = World.HEIGHT - 1;
                    break;
                case 2: // Bordure gauche
                    x = 0;
                    y = random.nextInt(World.HEIGHT);
                    break;
                case 3: // Bordure droite
                    x = World.WIDTH - 1;
                    y = random.nextInt(World.HEIGHT);
                    break;
            }

            // Vérification de la collision : la case doit être libre (isWalkable)
            if (world.getTile(x, y).isWalkable()) {
                validSpot = true;
            }
            attempts++;
        }

        // Si un emplacement valide est trouvé, on instancie et lance l'entité
        if (validSpot) {
            Chicken newChicken = new Chicken(x, y, world);
            // Ajout sécurisé à la liste globale des ennemis
            world.getEnemies().add(newChicken);
            // Chaque poule commence sa propre routine d'IA (Thread)
            newChicken.start();
        }
    }
}
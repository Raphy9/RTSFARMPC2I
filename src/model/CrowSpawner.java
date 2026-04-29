package src.model;

import java.util.Random;

/**
 * Générateur d'ennemis volants (Corbeaux).
 * Cette classe implémente Runnable pour fonctionner de manière asynchrone.
 * Elle gère le cycle de vie de l'apparition des corbeaux sans bloquer la boucle principale.
 */
public class CrowSpawner implements Runnable {

    private final World world;
    private boolean isRunning;
    private Thread spawnerThread;
    private final Random random;

    /** Intervalle entre deux tentatives d'apparition (20 secondes). */
    private static final int SPAWN_INTERVAL = 20000;
    /** Limite de population simultanée pour les corbeaux. */
    private static final int MAX_CROWS = 3;

    public CrowSpawner(World world) {
        this.world = world;
        this.isRunning = true;
        this.random = new Random();
    }

    /** Initialise et démarre le thread dédié au spawner. */
    public void start() {
        this.spawnerThread = new Thread(this, "CrowSpawnerThread");
        this.spawnerThread.start();
    }

    /**
     * Arrête la boucle de génération et interrompt le thread
     * pour une fermeture propre du programme.
     */
    public void stop() {
        this.isRunning = false;
        if (this.spawnerThread != null) {
            this.spawnerThread.interrupt();
        }
    }

    /**
     * Boucle principale du thread.
     * Gère l'attente et vérifie les conditions de jeu avant de générer un ennemi.
     */
    @Override
    public void run() {
        System.out.println("Générateur de corbeaux activé ! (Apparition a partir du niveau 4)");

        while (isRunning) {
            try {
                // Rythme de spawn
                Thread.sleep(SPAWN_INTERVAL);

                /*
                 * Condition de progression :
                 * Le corbeau est un ennemi "avancé". On vérifie que le joueur a atteint
                 * le niveau 4 (via la classe Stats) avant d'autoriser l'apparition.
                 */
                if (world.getStats() != null && world.getStats().getLevel() >= 4) {

                    // Vérifie que la limite de 3 corbeaux n'est pas déjà atteinte
                    if (world.getCrows().size() < MAX_CROWS) {
                        spawnCrowAtEdge();
                    }
                }

            } catch (InterruptedException e) {
                // Sortie de boucle si le thread est interrompu (stop)
                isRunning = false;
            }
        }
    }

    /**
     * Calcule une position aléatoire sur les contours du monde.
     * Instancie un nouveau Corbeau et lance son propre Thread d'IA.
     */
    private void spawnCrowAtEdge() {
        int edge = random.nextInt(4); // Sélectionne un des 4 bords (0:Haut, 1:Bas, 2:Gauche, 3:Droite)
        int x = 0;
        int y = 0;

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

        /*
         * Note importante : Contrairement à la poule, le corbeau "volant"
         * s'affranchit des collisions de départ. On n'a donc pas besoin
         * de vérifier 'isWalkable()' sur la tuile de spawn.
         */
        Crow newCrow = new Crow(x, y, world);

        // Ajout à la liste spécifique des corbeaux dans le modèle
        world.getCrows().add(newCrow);

        // Démarre l'IA individuelle du corbeau (Thread Crow)
        newCrow.start();
    }
}
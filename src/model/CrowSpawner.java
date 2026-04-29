package src.model;

import java.util.Random;

/**
 * Générateur d'ennemis volants (Corbeaux).
 * Tourne dans son propre Thread et fait apparaître des corbeaux sur les bords de la carte.
 */
public class CrowSpawner implements Runnable {

    private final World world;
    private boolean isRunning;
    private Thread spawnerThread;
    private final Random random;

    // Temps entre chaque apparition
    private static final int SPAWN_INTERVAL = 20000;
    // Nombre maximum de corbeaux en meme temps
    private static final int MAX_CROWS = 3;

    public CrowSpawner(World world) {
        this.world = world;
        this.isRunning = true;
        this.random = new Random();
    }

    public void start() {
        this.spawnerThread = new Thread(this, "CrowSpawnerThread");
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
        System.out.println("Générateur de corbeaux activé ! (Apparition a partir du niveau 4)");

        while (isRunning) {
            try {
                // On attend X secondes avant la prochaine tentative d'apparition
                Thread.sleep(SPAWN_INTERVAL);

                // On s'assure que les stats existent et que le niveau est au moins 4
                if (world.getStats() != null && world.getStats().getLevel() >= 4) {

                    // On ne fait apparaître un corbeau que si on n'a pas atteint la limite
                    if (world.getCrows().size() < MAX_CROWS) {
                        spawnCrowAtEdge();
                    }
                }

            } catch (InterruptedException e) {
                isRunning = false;
            }
        }
    }
    /**
     * Fait apparaître un corbeau aléatoirement sur un des 4 bords de l'écran.
     */
    private void spawnCrowAtEdge() {
        int edge = random.nextInt(4); // 0 = Haut, 1 = Bas, 2 = Gauche, 3 = Droite
        int x = 0;
        int y = 0;

        switch (edge) {
            case 0:
                x = random.nextInt(World.WIDTH);
                y = 0;
                break;
            case 1:
                x = random.nextInt(World.WIDTH);
                y = World.HEIGHT - 1;
                break;
            case 2:
                x = 0;
                y = random.nextInt(World.HEIGHT);
                break;
            case 3:
                x = World.WIDTH - 1;
                y = random.nextInt(World.HEIGHT);
                break;
        }

        // Le corbeau vole, donc on n'a pas besoin de vérifier si la case de départ est "Walkable" !
        Crow newCrow = new Crow(x, y, world);
        world.getCrows().add(newCrow);
        newCrow.start();
    }
}
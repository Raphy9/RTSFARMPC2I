package src.model;

import javax.swing.*;
import java.util.ArrayList;
import javax.swing.Timer;
import java.awt.Point;
import java.util.List;

/** * La classe World représente le monde du jeu, contenant les cases (tiles), le jardinier, la grange, etc.
 * Elle gère l'initialisation du monde, le chargement des sprites, et la logique de mise à jour (tick).
 */
public class World {
    // Dimensions du monde (en nombre de cases)
    public static final int WIDTH = 100;
    public static final int HEIGHT = 100;

    private Tile[][] tiles;

    // Image principale d'herbe
    private ImageIcon grassSprite;
    private Gardener testGardener;
    private Barn barn;

    // Coordonnées de la grange (Option A : position fixe au démarrage)
    private int barnX = 55;
    private int barnY = 55;

    private Stats stats;

    // Liste des ennemis (poules) présents dans le monde
    private List<Chicken> enemies;

    /** Constructeur du monde : charge les sprites, initialise les cases, crée le jardinier et la grange, et lance le thread du jardinier et l'horloge de tick.
     */
    public World() {
        loadTerrainSprites();
        initializeTiles();
        this.testGardener = new Gardener(WIDTH/2, HEIGHT/2, this);

        // Création et lancement d'une poule pour tester les ennemis
        this.enemies = new ArrayList<>();
        Chicken chicky = new Chicken(5, 10, this);
        this.enemies.add(chicky);
        chicky.start(); //Lance le thread de la poule

        // Initialisation de la grange et remplissage de départ pour les tests
        barn = new Barn();
        fstSetBarn();

        stats = new Stats(100);

        Thread t = new Thread(this.testGardener);
        t.start();

        // Création d'une horloge qui appelle la méthode tick() toutes les secondes (1000 ms)
        Timer gameTimer = new Timer(1000, e -> this.tick());
        gameTimer.start();
    }

    /**
     * Charge et découpe la Sprite Sheet du terrain.
     */
    private void loadTerrainSprites() {
        try {
            // Charger  une image pour l'herbe
            grassSprite = new ImageIcon("src/assets/grass2.jpg");

        } catch (Exception e) {
            System.err.println("Erreur : Impossible de charger les sprites ! " + e.getMessage());
        }
    }

    /**
     * Initialise les cases du monde avec l'herbe choisie.
     */
    private void initializeTiles() {
        this.tiles = new Tile[HEIGHT][WIDTH];

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {

                this.tiles[y][x] = new Tile(x, y, grassSprite);

            }
        }

        // Marquer la tuile de la grange avec un sprite chest et la rendre non franchissable
        try {
            ImageIcon chest = new ImageIcon("src/assets/chest.png");
            this.tiles[barnY][barnX].setSprite(chest);
            this.tiles[barnY][barnX].setWalkable(false);
        } catch (Exception e) {
            System.err.println("Warning: impossible de charger src/assets/chest.png: " + e.getMessage());
        }

        // TEMPORAIRE : mettre une parcelle de debut
        ArrayList<PlantTile> parcelTiles = new ArrayList<>();
        for (int x = 45; x < 47; x++) {
            for (int y = 45; y < 50; y++) {
                PlantTile plantTile = new PlantTile(x, y);
                this.tiles[y][x] = plantTile;
                parcelTiles.add(plantTile);
            }
        }
        Parcel startingParcel = new Parcel(parcelTiles);
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            throw new IndexOutOfBoundsException("Position hors du monde : (" + x + ", " + y + ")");
        }
        return this.tiles[y][x];
    }

    public Tile[][] getTiles() {
        return this.tiles;
    }

    public Gardener getGardenerTest() {
        return this.testGardener;
    }

    public Barn getBarn() {
        return this.barn;
    }

    /** Retourne la coordonnée X de la grange */
    public int getBarnX() { return barnX; }
    /** Retourne la coordonnée Y de la grange */
    public int getBarnY() { return barnY; }

    /** Retourne la liste des ennemis (poules) présents dans le monde */
    public List<Chicken> getEnemies() { return enemies; }

    /** Indique si les coordonnées données correspondent à la grange */
    public boolean isBarnAt(int x, int y) { return x == barnX && y == barnY; }

    public void toPlantTile(int x, int y) {
        ArrayList<Entity> entities = this.tiles[y][x].getEntities();
        PlantTile plantTile = new PlantTile(x, y);

        for (Entity entity : entities) {
            plantTile.addEntity(entity);
        }

        this.tiles[y][x] = plantTile;
    }

    /*
     Méthode pour remplir la grange de départ avec quelques items, à appeler dans le constructeur du monde.
     On peut la modifier pour tester différents items dans la grange.
     */
    private void fstSetBarn() {
        barn.addItem(new ItemPlant(PlantType.CAROTTE, 5));
        barn.addItem(new ItemPlant(PlantType.CHOUX, 5));
        barn.addItem(new ItemPlant(PlantType.FRAISE, 5));
        barn.addItem(new ItemSeed(PlantType.CAROTTE, 5));
        barn.addItem(new ItemSeed(PlantType.CHOUX, 5));
        barn.addItem(new ItemSeed(PlantType.FRAISE, 5));
    }

    /**
     * Fait avancer le temps d'un cycle dans tout le jeu.
     */
    public void tick() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (this.tiles[y][x] != null) {
                    this.tiles[y][x].tick(); // Transmet le tick à la case (et donc à la plante)
                }
            }
        }
    }

    /**
     * Trouve la meilleure tuile adjacente orthogonale (haut, bas, gauche, droite) à la case cible (tx,ty)
     * qui est marchable (isWalkable). Ne considère pas la case cible elle-même.
     * Retourne la tuile adjacente la plus proche de l'entité (distance Manhattan minimale) ou null.
     */
    public Point findClosestWalkableAdjacent(int tx, int ty, Entity entity) {
        int bestX = -1, bestY = -1;
        int bestDist = Integer.MAX_VALUE;

        if (entity == null) return null; // Modifié ici

        // Directions orthogonales uniquement
        int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        for (int[] d : dirs) {
            int nx = tx + d[0];
            int ny = ty + d[1];
            if (nx < 0 || nx >= WIDTH || ny < 0 || ny >= HEIGHT) continue;
            Tile t = getTile(nx, ny);
            if (t.isWalkable()) {
                // Modifié ici : on utilise entity.getX() et entity.getY()
                int dist = Math.abs(nx - entity.getX()) + Math.abs(ny - entity.getY());
                if (dist < bestDist) {
                    bestDist = dist;
                    bestX = nx;
                    bestY = ny;
                }
            }
        }
        if (bestX == -1) return null;
        return new Point(bestX, bestY);
    }

    /**
     * Arrête tous les threads actifs du monde.
     * À appeler lors de la fermeture du jeu ou du retour au menu principal.
     */
    public void stopWorld() {
        System.out.println("Arrêt du monde : fermeture des Threads...");

        // Arrêter le jardinier
        if (this.testGardener != null) {
            this.testGardener.stopGardener();
        }

        // Arrêter tous les ennemis (poules)
        if (this.enemies != null) {
            for (Chicken enemy : enemies) {
                enemy.stop();
            }
        }
    }
}
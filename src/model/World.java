package src.model;

import src.model.buildings.Building;

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
    private Gardener testGardener;  // seulement pour tester
    private ArrayList<Gardener> gardeners = new ArrayList<>();
    private Barn barn;

    // Coordonnées de la grange (Option A : position fixe au démarrage)
    private int barnX = 55;
    private int barnY = 55;

    private src.control.BuildingManager ghostManager;

    private Stats stats;

    // Liste des ennemis (poules) présents dans le monde
    private List<Chicken> enemies;

    // Liste des sprites d'obstacles (ex: cailloux, arbres) à ajouter plus tard pour diversifier le terrain
    private List<ImageIcon> obstacleSprites = new ArrayList<>();

    // Liste des bâtiments (grange, futur silo, etc.) présents dans le monde
    private List<Building> buildings = new ArrayList<>();

    /** Constructeur du monde : charge les sprites, initialise les cases, crée le jardinier et la grange, et lance le thread du jardinier et l'horloge de tick.
     */
    public World() {
        loadTerrainSprites();
        initializeTiles();
        initalizeStats();
        computeParcels();

        // Jardiniers
        this.testGardener = new Gardener(WIDTH/2, HEIGHT/2, this);
        this.gardeners.add(testGardener);
        this.gardeners.add(new Gardener(WIDTH/2+1, HEIGHT/2, this));
        for (Gardener gardener : gardeners) {
            Thread t = new Thread(gardener);
            t.start();; // Lance le thread du jardinier
        }

        // Création et lancement d'une poule pour tester les ennemis
        this.enemies = new java.util.concurrent.CopyOnWriteArrayList<>();
        this.chickenSpawner = new ChickenSpawner(this);
        this.chickenSpawner.start();

        // Initialisation de la grange et remplissage de départ pour les tests
        barn = new Barn(stats);
        fstSetBarn();

        stats = new Stats(100);

        // Création d'une horloge qui appelle la méthode tick() toutes les secondes (1000 ms)
        Timer gameTimer = new Timer(1000, e -> this.tick());
        gameTimer.start();
    }


    public void setGhostBuilding(src.control.BuildingManager manager) {
        this.ghostManager = manager;
    }

    /**
     * Charge et découpe la Sprite Sheet du terrain.
     */
    private void loadTerrainSprites() {
        try {
            // Charger  une image pour l'herbe
            grassSprite = new ImageIcon("src/assets/grass.jpg");

        } catch (Exception e) {
            System.err.println("Erreur : Impossible de charger les sprites ! " + e.getMessage());
        }
    }

    /** Charge les sprites d'obstacles depuis le dossier src/assets/Obstacles et les stocke dans la liste obstacleSprites.
     * Ces sprites pourront être utilisés pour ajouter de la variété au terrain avec des obstacles décoratifs (non franchissables).
     */
    private void loadObstacleSprites() {
        String[] names = {
                "buisson1", "buisson2",
                "champi1", "champi2", "champi3",
                "buche", "rondin", "rocher1", "rocher2"
        };
        for (String name : names) {
            // Assure-toi que le chemin correspond à ton dossier assets
            obstacleSprites.add(new ImageIcon("src/assets/Obstacles/" + name + ".png"));
        }
    }

    /**
     * Initialise les statistiques du monde
     */
    private void initalizeStats() {
        stats = new Stats(100); // Commence avec 100 pièces d'argent
    }

    /**
     * Initialise les cases du monde avec l'herbe choisie.
     */
    private void initializeTiles() {
        loadObstacleSprites();
        this.tiles = new Tile[HEIGHT][WIDTH];

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {

                // --- ZONE DE SÉCURITÉ ---
                // On ne met pas d'obstacle sur la grange ou le jardinier au départ
                if ((x >= barnX - 2 && x <= barnX + 2 && y >= barnY - 2 && y <= barnY + 2) ||
                        (x == WIDTH/2 && y == HEIGHT/2)) {
                    tiles[y][x] = new Tile(x, y, grassSprite);
                    continue;
                }

                // --- GÉNÉRATION ALÉATOIRE (5% de chance) ---
                if (Math.random() < 0.05) {
                    int randomIndex = (int)(Math.random() * obstacleSprites.size());
                    ImageIcon obsSprite = obstacleSprites.get(randomIndex);

                    Tile obsTile = new Tile(x, y, obsSprite);
                    obsTile.setWalkable(false); // Le jardinier ne peut pas marcher dessus
                    obsTile.setPlowable(false); // On ne peut pas labourer un rocher !
                    tiles[y][x] = obsTile;
                }
                else {
                    // Case d'herbe normale
                    tiles[y][x] = new Tile(x, y, grassSprite);
                }
            }
        }


        // Marquer la tuile de la grange avec un sprite chest et la rendre non franchissable
        try {
            ImageIcon chest = new ImageIcon("src/assets/chest.png");
            this.tiles[barnY][barnX].setSprite(chest);
            this.tiles[barnY][barnX].setWalkable(false);
            this.tiles[barnY][barnX].setPlowable(false);
        } catch (Exception e) {
            System.err.println("Warning: impossible de charger src/assets/chest.png: " + e.getMessage());
        }

        computeParcels();
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

    /** Pour tester seulement */
    public Gardener getGardenerTest() {
        return this.testGardener;
    }

    public ArrayList<Gardener> getGardeners() {
        return this.gardeners;
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
    private ChickenSpawner chickenSpawner;

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
        for (PlantType plantType : PlantType.values()) {
            barn.addItem(new ItemPlant(plantType, 0));
        }
        for (PlantType plantType : PlantType.values()) {
            barn.addItem(new ItemSeed(plantType, 5));
        }

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

    /** Retire un ennemi du monde (quand il est chassé) */
    public void removeEnemy(Chicken chicken) {
        this.enemies.remove(chicken);
    }

    /**
     * Arrête tous les threads actifs du monde.
     * À appeler lors de la fermeture du jeu ou du retour au menu principal.
     */
    public void stopWorld() {
        System.out.println("Arrêt du monde : fermeture des Threads...");

        // Arrêter le spawner de poules
        if (this.chickenSpawner != null) {
            this.chickenSpawner.stop();
        }

        // Arrêter le jardinier
        for (Gardener gardener : gardeners) {
             gardener.stopGardener();
        }

        // Arrêter tous les ennemis (poules)
        if (this.enemies != null) {
            for (Chicken enemy : enemies) {
                enemy.stop();
            }
        }
    }
    /** Méthode pour recalculer automatiquement les parcelles du monde en utilisant un algorithme de Flood Fill.
     * Parcourt toutes les cases du monde, et chaque fois qu'il trouve une PlantTile non visitée, il lance un
     * Flood Fill pour trouver toutes les PlantTile connectées orthogonalement
     */
    public void computeParcels() {
        boolean[][] visited = new boolean[WIDTH][HEIGHT];

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Tile t = getTile(x, y);
                // Si on trouve une PlantTile non visitée, c'est le début d'une nouvelle parcelle
                if (t instanceof PlantTile && !visited[x][y]) {
                    ArrayList<PlantTile> parcelTiles = new ArrayList<>();
                    floodFillParcel(x, y, visited, parcelTiles);

                    // On crée la parcelle (le constructeur de Parcel lie automatiquement les cases à lui-même)
                    new Parcel(parcelTiles);
                }
            }
        }
        System.out.println("Parcelles recalculées automatiquement !");
    }

    /** Algorithme de Flood Fill pour trouver toutes les PlantTile connectées orthogonalement à partir d'une tuile de départ (startX, startY).
     * Marque les cases visitées dans le tableau visited pour éviter les cycles, et ajoute les PlantTile trouvées à la liste parcelTiles.
     */
    private void floodFillParcel(int startX, int startY, boolean[][] visited, ArrayList<PlantTile> parcelTiles) {
        java.util.Queue<Point> queue = new java.util.LinkedList<>();
        queue.add(new Point(startX, startY));
        visited[startX][startY] = true;

        int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}}; // Droite, Gauche, Bas, Haut

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            Tile t = getTile(p.x, p.y);
            parcelTiles.add((PlantTile) t);

            // Vérifier les 4 voisins
            for (int[] d : dirs) {
                int nx = p.x + d[0];
                int ny = p.y + d[1];

                // Vérifier les limites de la carte
                if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT) {
                    if (!visited[nx][ny] && getTile(nx, ny) instanceof PlantTile) {
                        visited[nx][ny] = true;
                        queue.add(new Point(nx, ny));
                    }
                }
            }
        }
    }

    // Ajoute un bâtiment au monde
    public void addBuilding(Building b) {
        buildings.add(b);
    }

    // Récupère la liste des bâtiments (utile pour l'affichage)
    public List<Building> getBuildings() {
        return buildings;
    }

    /**
     * Vérifie si un bâtiment recouvre la case (x, y)
     */
    public boolean hasBuildingAt(int x, int y) {
        for (src.model.buildings.Building b : buildings) {
            // Si la case testée est à l'intérieur de l'empreinte du bâtiment
            if (x >= b.getX() && x < b.getX() + b.getWidth() &&
                    y >= b.getY() && y < b.getY() + b.getHeight()) {
                return true;
            }
        }
        return false;
    }

    public Building getBuildingAt(int x, int y) {
        for (Building b : buildings) {
            if (x >= b.getX() && x < b.getX() + b.getWidth() &&
                    y >= b.getY() && y < b.getY() + b.getHeight()) {
                return b;
            }
        }
        return null; // Rien sur cette case
    }

    public Stats getStats() {
        return stats;
    }

}
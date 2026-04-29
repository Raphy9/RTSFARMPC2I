package src.model;

import src.model.buildings.*;

import javax.swing.*;
import java.util.ArrayList;
import javax.swing.Timer;
import java.awt.Point;
import java.util.List;

/** * La classe World represente le monde du jeu, contenant les cases (tiles), le jardinier, la grange, etc.
 * Elle gere l'initialisation du monde, le chargement des sprites, et la logique de mise a jour (tick).
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


    private Stats stats;
    // Gestionnaire global des quetes du joueur (chapitres + progression).
    private src.model.Quests quests;
    private java.util.function.IntConsumer levelUpCallback;

    // Reservations de labour en attente (actions planifiees mais pas encore executees).
    private int reservedPlowTiles = 0;

    // --- ENNEMIS ---
    private List<Chicken> enemies; // Poules
    private List<Crow> crows;      // Corbeaux (NOUVEAU)
    private ChickenSpawner chickenSpawner;
    private CrowSpawner crowSpawner;   // (NOUVEAU)

    // Liste des sprites d'obstacles (ex: cailloux, arbres) à ajouter plus tard pour diversifier le terrain
    private List<ImageIcon> obstacleSprites = new ArrayList<>();

    // Liste des batiments (grange, futur silo, etc.) presents dans le monde
    private List<Building> buildings = new ArrayList<>();

    /** Constructeur du monde : charge les sprites, initialise les cases, cree le jardinier et la grange, et lance le thread du jardinier et l'horloge de tick.
     */
    public World() {
        loadTerrainSprites();
        initializeTiles();
        initalizeStats();
        computeParcels();

        // Jardiniers - Commencer avec un seul jardinier
        this.testGardener = new Gardener(WIDTH/2, HEIGHT/2, this);
        this.gardeners.add(testGardener);
        // Les autres jardiniers seront debloques aux niveaux 3 et 7

        for (Gardener gardener : gardeners) {
            Thread t = new Thread(gardener);
            t.start(); // Lance le thread du jardinier
        }

        // --- Initialisation des Poules ---
        this.enemies = new java.util.concurrent.CopyOnWriteArrayList<>();
        this.chickenSpawner = new ChickenSpawner(this);
        this.chickenSpawner.start();

        // --- Initialisation des Corbeaux (NOUVEAU) ---
        this.crows = new java.util.concurrent.CopyOnWriteArrayList<>();
        this.crowSpawner = new CrowSpawner(this);
        this.crowSpawner.start();

        // Initialisation de la grange et remplissage de depart pour les tests
        barn = new Barn(stats);
        fstSetBarn();


        // Creation d'une horloge qui appelle la méthode tick() toutes les secondes (1000 ms)
        Timer gameTimer = new Timer(1000, e -> this.tick());
        gameTimer.start();
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
     * Ces sprites pourront etre utilisés pour ajouter de la variété au terrain avec des obstacles décoratifs (non franchissables).
     */
    private void loadObstacleSprites() {
        String[] names = {
                "buisson1", "buisson2",
                "champi1", "champi2", "champi3",
                "buche", "rondin", "rocher1", "rocher2"
        };
        for (String name : names) {
            String path = "src/assets/Obstacles/" + name + ".png";
            // On garde le path dans la description pour la sérialisation des obstacles.
            obstacleSprites.add(new ImageIcon(path, path));
        }
    }

    /**
     * Initialise les statistiques du monde
     */
    private void initalizeStats() {
        stats = new Stats(1000); // Commence avec 0 pieces d'argent
        quests = new src.model.Quests();
        stats.setLevelUpCallback(this::onLevelUp);
    }

    private void onLevelUp(int newLevel) {
        syncLevelMilestones();
        syncGardenersForLevel(newLevel);

        if (levelUpCallback != null) {
            levelUpCallback.accept(newLevel);
        }
    }

    private void syncLevelMilestones() {
        if (quests != null) {
            int level = stats != null ? stats.getLevel() : 1;
            if (level >= 3) {
                quests.onAction(src.model.Quests.ACTION_REACH_LEVEL_3, stats);
            }
            if (level >= 4) {
                quests.onAction(src.model.Quests.ACTION_REACH_LEVEL_4, stats);
            }
            if (level >= 5) {
                quests.onAction(src.model.Quests.ACTION_REACH_LEVEL_5, stats);
            }
        }
    }

    public int getUnlockedGardenersCountForLevel(int level) {
        if (level >= 7) return 3;
        if (level >= 3) return 2;
        return 1;
    }

    /**
     * Garantit le nombre de jardiniers débloqués selon le niveau.
     * N'en enlève pas: le niveau ne baisse pas dans le gameplay normal.
     */
    public synchronized void syncGardenersForLevel(int level) {
        int target = getUnlockedGardenersCountForLevel(level);
        while (gardeners.size() < target) {
            int idx = gardeners.size();
            int spawnX = WIDTH / 2;
            int spawnY = HEIGHT / 2;
            if (idx == 1) {
                spawnX = WIDTH / 2 + 1;
            } else if (idx == 2) {
                spawnX = WIDTH / 2 - 1;
            }

            Gardener gardener = new Gardener(spawnX, spawnY, this);
            gardeners.add(gardener);
            Thread t = new Thread(gardener, "GardenerThread-" + (idx + 1));
            t.start();
        }
    }

    /** Initialise les cases du monde, en créant une zone de sécurité autour du point (55, 55) et en générant aléatoirement des obstacles sur le reste du terrain. */
    private void initializeTiles() {
        loadObstacleSprites();
        this.tiles = new Tile[HEIGHT][WIDTH];

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {

                // --- ZONE DE SÉCURITÉ ---
                // On garde une zone de sécurité fixe (autour de 55, 55) pour le démarrage
                if ((x >= 55 - 2 && x <= 55 + 2 && y >= 55 - 2 && y <= 55 + 2) ||
                        (x == WIDTH/2 && y == HEIGHT/2)) {
                    tiles[y][x] = new Tile(x, y, grassSprite);
                    continue;
                }

                // --- GÉNÉRATION ALÉATOIRE (5% de chance) ---
                if (Math.random() < 0.05) {
                    int randomIndex = (int)(Math.random() * obstacleSprites.size());
                    ImageIcon obsSprite = obstacleSprites.get(randomIndex);

                    // On laisse la tuile comme une Tile d'herbe mais on marque qu'on ne peut pas y marcher ni labourer
                    Tile base = new Tile(x, y, grassSprite);
                    base.setWalkable(false);
                    base.setPlowable(false);
                    tiles[y][x] = base;

                    // Créer un Obstacle (Building) et l'ajouter à la liste des batiments
                    Obstacle obs = new Obstacle(obsSprite);
                    obs.setPosition(x, y);
                    buildings.add(obs);
                }
                else {
                    // Case d'herbe normale
                    tiles[y][x] = new Tile(x, y, grassSprite);
                }
            }
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

    /** Index du dernier jardinier assigné, pour le round-robin. */
    private int lastAssignedIndex = -1;

    /**
     * Retourne le prochain jardinier disponible en round-robin :
     * - d'abord un jardinier completement libre (WAITING + 0 actions en attente),
     * en commençant APRES le dernier assigné pour répartir équitablement la charge,
     * - sinon celui qui a le moins d'actions en attente (toujours en round-robin si égalité).
     * Fonctionne pour n jardiniers.
     */
    public Gardener getAvailableGardener() {
        if (gardeners == null || gardeners.isEmpty()) return null;
        int n = gardeners.size();

        // 1. Chercher un jardinier completement libre en round-robin
        for (int i = 1; i <= n; i++) {
            int idx = (lastAssignedIndex + i) % n;
            Gardener g = gardeners.get(idx);
            if (g.getCurrentState() == Gardener.State.WAITING && g.getPendingActionsCount() == 0) {
                lastAssignedIndex = idx;
                return g;
            }
        }

        // 2. Aucun completement libre : prendre le moins chargé (round-robin si égalité)
        Gardener best = null;
        int bestPending = Integer.MAX_VALUE;
        int bestIdx = -1;
        for (int i = 1; i <= n; i++) {
            int idx = (lastAssignedIndex + i) % n;
            int pending = gardeners.get(idx).getPendingActionsCount();
            if (pending < bestPending) {
                bestPending = pending;
                best = gardeners.get(idx);
                bestIdx = idx;
            }
        }
        if (bestIdx >= 0) lastAssignedIndex = bestIdx;
        return best;
    }

    public Barn getBarn() {
        return this.barn;
    }

    /** Vérifie si une grange est déjà posée sur la carte */
    public boolean hasBarn() {
        return buildings.stream().anyMatch(b -> b instanceof BarnBuilding);
    }

    public int getBarnX() {
        for (Building b : buildings) {
            if (b instanceof BarnBuilding) {
                // Point d'acces: colonne centrale de la grange
                return b.getX() + (b.getWidth() / 2);
            }
        }
        return -1;
    }

    public int getBarnY() {
        for (Building b : buildings) {
            if (b instanceof BarnBuilding) {
                // Point d'acces: ligne basse de la grange
                return b.getY() + (b.getHeight() - 1);
            }
        }
        return -1;
    }

    public boolean isBarnAt(int x, int y) {
        Building b = getBuildingAt(x, y);
        return b instanceof BarnBuilding;
    }

    /**
     * Retourne true si la case (x,y) est sur la grange ou adjacente (8 directions)
     * à l'empreinte de la grange.
     */
    public boolean isBarnAdjacentOrInside(int x, int y) {
        for (Building b : buildings) {
            if (!(b instanceof BarnBuilding)) continue;
            int minX = b.getX() - 1;
            int maxX = b.getX() + b.getWidth();
            int minY = b.getY() - 1;
            int maxY = b.getY() + b.getHeight();
            if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retourne true si la case (x,y) est sur la grange.
     */
    public boolean isBarnInside(int x, int y) {
        for (Building b : buildings) {
            if (!(b instanceof BarnBuilding)) continue;
            int minX = b.getX();
            int maxX = b.getX() + b.getWidth() - 1;
            int minY = b.getY();
            int maxY = b.getY() + b.getHeight() - 1;
            if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                return true;
            }
        }
        return false;
    }

    /** Retourne la liste des ennemis (poules) présents dans le monde */
    public List<Chicken> getEnemies() { return enemies; }

    /** Retourne la liste des corbeaux présents dans le monde (NOUVEAU) */
    public List<Crow> getCrows() { return crows; }

    public void toPlantTile(int x, int y) {
        ArrayList<Entity> entities = this.tiles[y][x].getEntities();
        PlantTile plantTile = new PlantTile(x, y);

        for (Entity entity : entities) {
            plantTile.addEntity(entity);
        }

        this.tiles[y][x] = plantTile;
    }

    /** Retransforme une PlantTile vide en case d'herbe normale. */
    public void toNormalTile(int x, int y) {
        Tile current = this.tiles[y][x];
        if (!(current instanceof PlantTile)) return;
        if (((PlantTile) current).getPlant() != null) return;

        Tile normal = new Tile(x, y, grassSprite);
        for (Entity entity : current.getEntities()) {
            normal.addEntity(entity);
        }
        this.tiles[y][x] = normal;
        computeParcels();
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
            barn.addItem(new ItemSeed(plantType, 0));
        }
        // Initial seeds
        barn.addItem(new ItemSeed(PlantType.CAROTTE, 10));

    }

    /**
     * Garantit que la grange contient au minimum le catalogue de base
     * (une entrée graines + plante pour chaque type), sans réinitialiser les quantités existantes.
     */
    public void ensureBarnCatalog() {
        for (PlantType plantType : PlantType.values()) {
            if (barn.findSameItem(new ItemPlant(plantType, 0)) == null) {
                barn.addItem(new ItemPlant(plantType, 0));
            }
            if (barn.findSameItem(new ItemSeed(plantType, 0)) == null) {
                barn.addItem(new ItemSeed(plantType, 0));
            }
        }
    }

    /**
     * Fait avancer le temps d'un cycle dans tout le jeu.
     */
    public void tick() {
        for (Building building : buildings) {
            building.applyEffect(this);
        }

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
     * qui est marchable (isWalkable). Ne considere pas la case cible elle-meme.
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

    /** * Retire un ennemi du monde.
     * Cette méthode accepte maintenant n'importe quelle Entity pour gérer poules et corbeaux.
     */
    public void removeEnemy(Entity enemy) {
        if (enemy instanceof Chicken) {
            this.enemies.remove(enemy);
        } else if (enemy instanceof Crow) {
            this.crows.remove(enemy);
        }
    }

    /**
     * Arrete tous les threads actifs du monde.
     * À appeler lors de la fermeture du jeu ou du retour au menu principal.
     */
    public void stopWorld() {
        System.out.println("Arret du monde : fermeture des Threads...");

        // Arreter les spawners
        if (this.chickenSpawner != null) this.chickenSpawner.stop();
        if (this.crowSpawner != null) this.crowSpawner.stop();

        // Arreter le jardinier
        for (Gardener gardener : gardeners) {
            gardener.stopGardener();
        }

        // Arreter tous les ennemis (poules)
        if (this.enemies != null) {
            for (Chicken enemy : enemies) {
                enemy.stop();
            }
        }

        // Arreter tous les corbeaux (NOUVEAU)
        if (this.crows != null) {
            for (Crow crow : crows) {
                crow.stop();
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

                    // On crée la parcelle (le constructeur de Parcel lie automatiquement les cases à lui-meme)
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

    // Ajoute un batiment au monde
    public void addBuilding(Building b) {
        buildings.add(b);
    }

    /**
     * Nettoie la carte avant restauration d'une sauvegarde pour éviter que
     * les obstacles générés aléatoirement au démarrage restent présents.
     */
    public void prepareForLoad() {
        buildings.clear();

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Tile oldTile = tiles[y][x];
                Tile clean = new Tile(x, y, grassSprite);
                if (oldTile != null) {
                    for (Entity entity : oldTile.getEntities()) {
                        clean.addEntity(entity);
                    }
                }
                tiles[y][x] = clean;
            }
        }
    }

    // Récupere la liste des batiments (utile pour l'affichage)
    public List<Building> getBuildings() {
        return buildings;
    }

    public void organizeFences() {
        ArrayList<Building> fences = new ArrayList<>();
        for (Building b : buildings) {
            if (b instanceof Fence) {
                fences.add(b);
                buildings.remove(b); // On les retire de la liste principale pour les remettre dans l'ordre après
            }
        }
        // Ordonne la liste fences selon la coordonnées x, dans l'ordre croissant peu importe la coordonnée y
        fences.sort((b1, b2) -> Integer.compare(b1.getX(), b2.getX()));

        // Remet les barrières organisées dans la liste principale
        for (Building b : fences) {
            buildings.add(b);
            fences.remove(b);
        }
    }

    public void removeBuilding(Building b) {
        this.buildings.remove(b); // Doit etre la meme liste que celle utilisée par getBuildings()

        // IMPORTANT : Remettre les cases en mode "marchable"
        for (int x = b.getX(); x < b.getX() + b.getWidth(); x++) {
            for (int y = b.getY(); y < b.getY() + b.getHeight(); y++) {
                Tile t = getTile(x, y);
                t.setWalkable(true);
                t.setPlowable(true);
                if (t instanceof PlantTile) {
                    ((PlantTile) t).setPlantingBlocked(false);
                }
            }
        }
    }

    /**
     * Vérifie si un batiment recouvre la case (x, y)
     */
    public boolean hasBuildingAt(int x, int y) {
        for (src.model.buildings.Building b : buildings) {
            // Si la case testée est à l'intérieur de l'empreinte du batiment
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

    /** Retourne le moteur de quetes du monde pour l'UI et la sauvegarde. */
    public src.model.Quests getQuests() {
        return quests;
    }

    /** Permet à la vue de se rafraîchir quand la progression des quetes change. */
    public void setQuestChangeCallback(Runnable callback) {
        if (quests != null) {
            quests.setChangeListener(callback);
        }
    }

    /** Notifie le systeme de quetes qu'une plantation vient de réussir. */
    public void registerPlantEvent(PlantType plantType) {
        if (quests != null) {
            syncLevelMilestones();
            quests.onPlant(plantType, stats);
        }
    }

    /** Notifie le systeme de quetes qu'une récolte vient de réussir. */
    public void registerHarvestEvent(PlantType plantType) {
        if (quests != null) {
            syncLevelMilestones();
            quests.onHarvest(plantType, stats);
        }
    }

    /** Notifie le systeme de quetes qu'un batiment vient d'etre posé. */
    public void registerBuildEvent(Building building) {
        if (quests != null) {
            syncLevelMilestones();
            quests.onBuild(building, stats);
            if (building instanceof Fence) {
                quests.onAction(src.model.Quests.ACTION_PLACE_FENCE, stats);
            }
        }
    }

    public void registerQuestAction(String actionKey) {
        registerQuestAction(actionKey, 1);
    }

    public void registerQuestAction(String actionKey, int amount) {
        if (quests != null) {
            syncLevelMilestones();
            quests.onAction(actionKey, stats, amount);
        }
    }

    /** Nombre total de cases actuellement labourées (PlantTile). */
    public int getPlowedTilesCount() {
        int count = 0;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (tiles[y][x] instanceof PlantTile) {
                    count++;
                }
            }
        }
        return count;
    }

    /** Nombre de cases labourées + réservées (actions en attente). */
    public int getEffectivePlowedTilesCount() {
        return getPlowedTilesCount() + getReservedPlowTilesCount();
    }

    /** Nombre de cases réservées pour labour (actions planifiées). */
    public synchronized int getReservedPlowTilesCount() {
        return reservedPlowTiles;
    }

    /** Réserve immédiatement des cases à labourer pour bloquer la limite. */
    public synchronized void reservePlowTiles(int amount) {
        if (amount <= 0) {
            return;
        }
        reservedPlowTiles += amount;
    }

    /** Libere des réservations de labour (ex: annulation ou fin d'action). */
    public synchronized void releasePlowTiles(int amount) {
        if (amount <= 0) {
            return;
        }
        reservedPlowTiles = Math.max(0, reservedPlowTiles - amount);
    }

    /** Limite de labour : 5 de base + 5 par niveau, plafonné à 100. */
    public int getPlowLimit() {
        int level = (stats != null) ? stats.getLevel() : 1;
        int limit = 5 + 5 * Math.max(1, level);
        return Math.min(100, limit);
    }

    /** Vérifie si on peut encore ajouter additionalTiles cases labourées. */
    public boolean canAddPlowedTiles(int additionalTiles) {
        return getEffectivePlowedTilesCount() + Math.max(0, additionalTiles) <= getPlowLimit();
    }

    /** Enregistre un callback appelé à chaque montée de niveau, avec le nouveau niveau. */
    public void setLevelUpCallback(java.util.function.IntConsumer callback) {
        this.levelUpCallback = callback;
    }

}

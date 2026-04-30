package src.model;

import src.model.buildings.*;

import javax.swing.*;
import java.util.ArrayList;
import java.awt.Point;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * La classe World représente le modèle central (le "coeur") du jeu.
 * Elle orchestre l'ensemble de la simulation logique : la grille spatiale,
 * l'écoulement du temps (horloge), l'intelligence artificielle (Jardiniers, Ennemis)
 * et les données du joueur (Statistiques, Quêtes).
 */
public class World {

    /** Largeur totale de la carte exprimée en nombre de tuiles (cases). */
    public static final int WIDTH = 100;

    /** Hauteur totale de la carte exprimée en nombre de tuiles (cases). */
    public static final int HEIGHT = 100;

    /** * Grille 2D représentant le sol du jeu. Le premier index [y] représente la ligne (hauteur),
     * et le second [x] la colonne (largeur). Contient des objets Tile (herbe, terre, etc.).
     */
    private Tile[][] tiles;

    /** * Registre spatial des objets posés sur la grille (Grange, Barrières, Obstacles naturels).
     * Utilisé pour la gestion des collisions et l'affichage par-dessus le sol.
     */
    private List<Building> buildings = new ArrayList<>();

    /** Texture chargée en RAM utilisée pour dessiner le sol de base (l'herbe) sur les cases. */
    private ImageIcon grassSprite;

    /** * Catalogue contenant toutes les images possibles pour les obstacles naturels (cailloux, buissons).
     * Utilisé lors de la génération aléatoire du terrain pour piocher une image au hasard.
     */
    private List<ImageIcon> obstacleSprites = new ArrayList<>();

    /** * Référence directe historique vers le tout premier jardinier créé.
     * Souvent utilisée par les anciens scripts de test, à conserver pour la rétrocompatibilité.
     */
    private Gardener testGardener;

    /** * Liste dynamique contenant tous les jardiniers actuellement débloqués et actifs sur la carte.
     * Chaque jardinier tourne sur son propre Thread (processus en arrière-plan).
     */
    private ArrayList<Gardener> gardeners = new ArrayList<>();

    /** * Le bâtiment central du jeu. Il fait le pont entre le monde physique (où on dépose les récoltes)
     * et l'inventaire logique du joueur (économie).
     */
    private Barn barn;

    /** Liste thread-safe contenant les poules actuellement vivantes sur la carte. */
    private List<Chicken> enemies;

    /** Liste thread-safe contenant les corbeaux actuellement volants/posés sur la carte. */
    private List<Crow> crows;

    /** Moteur asynchrone responsable de faire apparaître (spawner) des poules à intervalles réguliers. */
    private ChickenSpawner chickenSpawner;

    /** Moteur asynchrone responsable de faire apparaître (spawner) des corbeaux à intervalles réguliers. */
    private CrowSpawner crowSpawner;

    /** * Objet contenant les données chiffrées du joueur : son niveau actuel, son argent (pièces),
     * et ses points d'expérience (XP).
     */
    private Stats stats;

    /** * Moteur logique qui évalue en permanence les actions du joueur pour valider
     * les objectifs des chapitres et donner des récompenses.
     */
    private src.model.Quests quests;

    /** * Fonction (injectée par la Vue) à exécuter à chaque fois que le niveau du joueur change.
     * Elle permet de prévenir l'interface graphique d'afficher un popup "Level Up", par exemple.
     */
    private java.util.function.IntConsumer levelUpCallback;

    /** * Variable de contrôle servant à empêcher le joueur de tricher avec la limite de terrain labourable.
     * Quand le joueur clique pour labourer, on incrémente cette variable avant même que le jardinier n'agisse,
     * ce qui "réserve" la place et bloque les clics suivants si la limite est atteinte.
     */
    private int reservedPlowTiles = 0;

    /** * Service Java gérant l'exécution de tâches planifiées dans le temps sur un thread séparé.
     * C'est lui qui héberge l'horloge interne du jeu.
     */
    private ScheduledExecutorService tickExecutor;

    /** * Objet représentant l'action répétitive planifiée (le "battement de coeur" du jeu).
     * On stocke cette référence pour pouvoir arrêter l'horloge proprement quand on quitte le jeu.
     */
    private ScheduledFuture<?> tickFuture;

    /** * Mémoire (index) utilisée par l'algorithme de distribution du travail ("Round-Robin").
     * Elle retient quel a été le dernier jardinier à recevoir un ordre, pour donner le prochain ordre
     * au jardinier suivant (et non toujours au premier).
     */
    private int lastAssignedIndex = -1;


    /**
     * Constructeur principal du monde.
     * Initialise la mémoire, la géographie, l'économie, et lance les moteurs temporels.
     */
    public World() {
        loadTerrainSprites();
        initializeTiles();
        initalizeStats();
        computeParcels(); // Groupe immédiatement les tuiles collées générées par l'initialisation

        // --- Lancement du premier ouvrier ---
        this.testGardener = new Gardener(WIDTH/2, HEIGHT/2, this);
        this.gardeners.add(testGardener);

        for (Gardener gardener : gardeners) {
            Thread t = new Thread(gardener);
            t.start();
        }

        // --- Lancement des générateurs d'ennemis ---
        // On utilise des "CopyOnWriteArrayList". C'est un type de liste spécial très sécurisé
        // quand plusieurs "Threads" (processus) essaient de lire et modifier la liste en même temps
        // (ex: l'écran veut dessiner la poule pendant que le spawner essaie de la créer).
        this.enemies = new java.util.concurrent.CopyOnWriteArrayList<>();
        this.chickenSpawner = new ChickenSpawner(this);
        this.chickenSpawner.start();

        this.crows = new java.util.concurrent.CopyOnWriteArrayList<>();
        this.crowSpawner = new CrowSpawner(this);
        this.crowSpawner.start();

        // --- Économie ---
        barn = new Barn(stats);
        fstSetBarn();

        // --- Démarrage de l'horloge maître (Le Tick) ---
        // On crée un exécuteur avec 1 seul thread dédié.
        this.tickExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "WorldTickThread");
            // Un thread "Daemon" s'arrêtera de force tout seul si l'utilisateur ferme la fenêtre principale.
            t.setDaemon(true);
            return t;
        });

        // On lui demande d'appeler la fonction "tick()" toutes les 1000 millisecondes (1 seconde).
        this.tickFuture = tickExecutor.scheduleAtFixedRate(
                this::tick,
                1000,
                1000,
                TimeUnit.MILLISECONDS
        );
    }

    private void loadTerrainSprites() {
        try {
            grassSprite = new ImageIcon("src/assets/grass.jpg");
        } catch (Exception e) {
            System.err.println("Erreur : Impossible de charger les sprites ! " + e.getMessage());
        }
    }

    private void loadObstacleSprites() {
        String[] names = {
                "buisson1", "buisson2", "champi1", "champi2", "champi3",
                "buche", "rondin", "rocher1", "rocher2"
        };
        for (String name : names) {
            String path = "src/assets/Obstacles/" + name + ".png";
            obstacleSprites.add(new ImageIcon(path, path));
        }
    }

    private void initalizeStats() {
        stats = new Stats(0);
        quests = new src.model.Quests();
        // Le "this::onLevelUp" indique à la classe Stats d'appeler la fonction onLevelUp de ce World
        // chaque fois qu'un niveau est gagné.
        stats.setLevelUpCallback(this::onLevelUp);
    }

    private void onLevelUp(int newLevel) {
        syncLevelMilestones();
        syncGardenersForLevel(newLevel);

        // Si l'interface graphique a branché une fonction de réaction (levelUpCallback)
        if (levelUpCallback != null) {
            // "SwingUtilities.invokeLater" est TRÈS important ici : il force l'exécution graphique
            // sur le thread principal (EDT). Sans ça, modifier l'UI depuis le thread du jardinier ferait crasher l'affichage.
            SwingUtilities.invokeLater(() -> levelUpCallback.accept(newLevel));
        }
    }

    private void syncLevelMilestones() {
        if (quests != null) {
            int level = stats != null ? stats.getLevel() : 1;
            int activeChapter = quests.getActiveQuestLineIndex() + 1;
            // On n'envoie l'action QUE si on est dans le chapitre qui la demande
            if (level >= 3 && activeChapter == 3) {
                quests.onAction(src.model.Quests.ACTION_REACH_LEVEL_3, stats);
            }
            if (level >= 4 && activeChapter == 4) {
                quests.onAction(src.model.Quests.ACTION_REACH_LEVEL_4, stats);
            }
            if (level >= 5 && activeChapter == 5) {
                quests.onAction(src.model.Quests.ACTION_REACH_LEVEL_5, stats);
            }
        }
    }

    public int getUnlockedGardenersCountForLevel(int level) {
        if (level >= 7) return 3;
        if (level >= 3) return 2;
        return 1; // De base, 1 seul ouvrier
    }

    /**
     * Ajuste le nombre de jardiniers actifs en fonction du niveau.
     * Le mot-clé "synchronized" empêche deux threads d'appeler cette fonction en même temps
     * (ce qui risquerait de créer des jardiniers en double).
     */
    public synchronized void syncGardenersForLevel(int level) {
        int target = getUnlockedGardenersCountForLevel(level);

        while (gardeners.size() < target) {
            int idx = gardeners.size();

            // Calcul des coordonnées d'apparition. On les décale pour qu'ils ne popent pas les uns sur les autres.
            int spawnX = WIDTH / 2;
            int spawnY = HEIGHT / 2;
            if (idx == 1) spawnX = WIDTH / 2 + 1;
            else if (idx == 2) spawnX = WIDTH / 2 - 1;

            Gardener gardener = new Gardener(spawnX, spawnY, this);
            gardeners.add(gardener);

            // On lance le nouveau collègue !
            Thread t = new Thread(gardener, "GardenerThread-" + (idx + 1));
            t.start();
        }
    }

    /**
     * Remplissage initial de la matrice tiles[][].
     */
    private void initializeTiles() {
        loadObstacleSprites();
        this.tiles = new Tile[HEIGHT][WIDTH];

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {

                // EXPLICATION DE LA ZONE DE SÉCURITÉ :
                // On vérifie si les coordonnées X et Y sont proches du centre (55, 55).
                // "x >= 55 - 2" signifie qu'on protège un carré de 5x5 autour du point 55.
                // Le point exact WIDTH/2, HEIGHT/2 est aussi protégé car c'est le point de spawn du joueur.
                if ((x >= 55 - 2 && x <= 55 + 2 && y >= 55 - 2 && y <= 55 + 2) ||
                        (x == WIDTH/2 && y == HEIGHT/2)) {
                    tiles[y][x] = new Tile(x, y, grassSprite);
                    continue; // "continue" saute directement à la case suivante sans lire le reste du code ci-dessous.
                }

                // GÉNÉRATION PROCÉDURALE (Aléatoire)
                // Math.random() génère un nombre entre 0.0 et 1.0.
                // Donc < 0.05 signifie "5% de chances de se réaliser".
                if (Math.random() < 0.05) {
                    // On choisit un sprite au hasard dans notre liste
                    int randomIndex = (int)(Math.random() * obstacleSprites.size());
                    ImageIcon obsSprite = obstacleSprites.get(randomIndex);

                    // On crée la case, mais on lui interdit physiquement de recevoir des actions ou des déplacements.
                    Tile base = new Tile(x, y, grassSprite);
                    base.setWalkable(false);
                    base.setPlowable(false);
                    tiles[y][x] = base;

                    // On crée l'obstacle qui agit comme un bâtiment invisible pour bloquer les collisions 3D/Isométriques.
                    Obstacle obs = new Obstacle(obsSprite);
                    obs.setPosition(x, y);
                    buildings.add(obs);
                }
                else {
                    tiles[y][x] = new Tile(x, y, grassSprite);
                }
            }
        }
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

    public ArrayList<Gardener> getGardeners() {
        return this.gardeners;
    }

    /**
     * ALGORITHME D'ORDONNANCEMENT (Load Balancing).
     * Décide à quel jardinier on va donner le prochain ordre cliqué par le joueur.
     */
    public Gardener getAvailableGardener() {
        if (gardeners == null || gardeners.isEmpty()) return null;
        int n = gardeners.size();

        // RECHERCHE OPTIMALE (Round-Robin)
        // La boucle de 1 à n avec le modulo "% n" permet de parcourir la liste circulairement.
        // Exemple avec 3 jardiniers (n=3) et lastAssignedIndex = 1 :
        // Boucle 1: (1 + 1) % 3 = 2 (On teste le jardinier 2)
        // Boucle 2: (1 + 2) % 3 = 0 (On teste le jardinier 0)
        // Boucle 3: (1 + 3) % 3 = 1 (On teste le jardinier 1)
        // Ainsi, on ne favorise jamais le jardinier 0 s'il n'est pas son tour.
        for (int i = 1; i <= n; i++) {
            int idx = (lastAssignedIndex + i) % n;
            Gardener g = gardeners.get(idx);

            // Si le jardinier attend (WAITING) et qu'il n'a AUCUNE file d'attente (pending = 0), c'est le candidat parfait.
            if (g.getCurrentState() == Gardener.State.WAITING && g.getPendingActionsCount() == 0) {
                lastAssignedIndex = idx;
                return g;
            }
        }

        // RECHERCHE DÉGRADÉE
        // Si tous les jardiniers sont déjà occupés, on cherche celui qui a la plus PETITE file d'attente
        // pour ne pas engorger un seul pauvre ouvrier.
        Gardener best = null;
        int bestPending = Integer.MAX_VALUE; // On initialise au maximum possible pour la comparaison
        int bestIdx = -1;

        for (int i = 1; i <= n; i++) {
            int idx = (lastAssignedIndex + i) % n;
            int pending = gardeners.get(idx).getPendingActionsCount();

            // Si on trouve une file plus petite que notre record actuel, on met à jour notre choix.
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

    public boolean hasBarn() {
        // Retourne VRAI si au moins un des bâtiments de la liste est de type "BarnBuilding".
        return buildings.stream().anyMatch(b -> b instanceof BarnBuilding);
    }

    // Calcule l'accès frontal à la grange (au milieu en bas).
    public int getBarnX() {
        for (Building b : buildings) {
            if (b instanceof BarnBuilding) return b.getX() + (b.getWidth() / 2);
        }
        return -1;
    }

    public int getBarnY() {
        for (Building b : buildings) {
            if (b instanceof BarnBuilding) return b.getY() + (b.getHeight() - 1);
        }
        return -1;
    }

    public boolean isBarnAt(int x, int y) {
        Building b = getBuildingAt(x, y);
        return b instanceof BarnBuilding;
    }

    public boolean isBarnAdjacentOrInside(int x, int y) {
        for (Building b : buildings) {
            if (!(b instanceof BarnBuilding)) continue;
            // On calcule une "Bounding Box" (Boîte de collision) élargie de 1 case dans toutes les directions.
            int minX = b.getX() - 1;
            int maxX = b.getX() + b.getWidth();
            int minY = b.getY() - 1;
            int maxY = b.getY() + b.getHeight();

            if (x >= minX && x <= maxX && y >= minY && y <= maxY) return true;
        }
        return false;
    }

    public boolean isBarnInside(int x, int y) {
        for (Building b : buildings) {
            if (!(b instanceof BarnBuilding)) continue;
            // Pareil qu'au dessus, mais sans la marge (Boîte stricte).
            int minX = b.getX();
            int maxX = b.getX() + b.getWidth() - 1;
            int minY = b.getY();
            int maxY = b.getY() + b.getHeight() - 1;

            if (x >= minX && x <= maxX && y >= minY && y <= maxY) return true;
        }
        return false;
    }

    public List<Chicken> getEnemies() { return enemies; }
    public List<Crow> getCrows() { return crows; }

    public void toPlantTile(int x, int y) {
        // Sauvegarde ce qui traîne au sol (ex: objets)
        ArrayList<Entity> entities = this.tiles[y][x].getEntities();

        // Remplacement physique dans le tableau mémoire
        PlantTile plantTile = new PlantTile(x, y);
        for (Entity entity : entities) {
            plantTile.addEntity(entity);
        }
        this.tiles[y][x] = plantTile;
    }

    public void toNormalTile(int x, int y) {
        Tile current = this.tiles[y][x];
        if (!(current instanceof PlantTile)) return;

        // Sécurité : On refuse de remettre de l'herbe si une plante pousse encore dessus !
        if (((PlantTile) current).getPlant() != null) return;

        Tile normal = new Tile(x, y, grassSprite);
        for (Entity entity : current.getEntities()) {
            normal.addEntity(entity);
        }
        this.tiles[y][x] = normal;

        // Le terrain a changé, il faut recalculer les regroupements de parcelles
        computeParcels();
    }

    private void fstSetBarn() {
        for (PlantType plantType : PlantType.values()) {
            barn.addItem(new ItemPlant(plantType, 0));
            barn.addItem(new ItemSeed(plantType, 0));
        }
        barn.addItem(new ItemSeed(PlantType.CAROTTE, 15));
    }

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
     * LA BOUCLE PRINCIPALE (Appelée 1 fois par seconde).
     */
    public void tick() {
        // 1. Les bâtiments agissent en premier (ex: les Arroseurs arrosent la terre autour d'eux).
        for (Building building : buildings) {
            building.applyEffect(this);
        }

        // 2. On met à jour l'intégralité des 10 000 cases (100x100) du jeu.
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (this.tiles[y][x] != null) {
                    // C'est ici que les graines grandissent en plantes si la case a été arrosée à l'étape 1 !
                    this.tiles[y][x].tick();
                }
            }
        }
    }

    /**
     * ALGORITHME DE PATHFINDING BASIQUE (Recherche de case de contact).
     * Trouve la case la plus proche d'une cible pour s'y tenir à côté (utile pour interagir avec un objet solide).
     */
    public Point findClosestWalkableAdjacent(int tx, int ty, Entity entity) {
        int bestX = -1, bestY = -1;
        int bestDist = Integer.MAX_VALUE;

        if (entity == null) return null;

        // "dirs" est une matrice de vecteurs de direction : {x, y}
        // {1, 0} = Droite | {-1, 0} = Gauche | {0, 1} = Bas | {0, -1} = Haut
        // On ne gère pas les diagonales ({1, 1} par ex.) dans un jeu purement isométrique/orthogonal.
        int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};

        for (int[] d : dirs) {
            // Coordonnées de la case voisine calculée
            int nx = tx + d[0];
            int ny = ty + d[1];

            // On s'assure de ne pas chercher hors de la matrice 100x100 (ce qui ferait un crash IndexOutOfBounds).
            if (nx < 0 || nx >= WIDTH || ny < 0 || ny >= HEIGHT) continue;

            Tile t = getTile(nx, ny);
            if (t.isWalkable()) {
                // CALCUL DE LA DISTANCE DE MANHATTAN
                // Formule : |X1 - X2| + |Y1 - Y2|
                // Idéale pour des déplacements sur une grille car elle calcule le nombre de cases à traverser
                // (contrairement à la distance Euclidienne classique qui trace une ligne droite au compas).
                int dist = Math.abs(nx - entity.getX()) + Math.abs(ny - entity.getY());

                // Si ce voisin est plus proche de notre entité que notre meilleur résultat actuel, on le retient.
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

    public void removeEnemy(Entity enemy) {
        if (enemy instanceof Chicken) this.enemies.remove(enemy);
        else if (enemy instanceof Crow) this.crows.remove(enemy);
    }

    /**
     * PROCÉDURE DE FERMETURE.
     * Très important pour éviter les fuites de mémoire (Memory Leaks) et les blocages
     * lorsque le joueur retourne au menu principal.
     */
    public void stopWorld() {
        // 1. On annule le timer périodique
        if (tickFuture != null) tickFuture.cancel(false);

        // 2. On coupe le gestionnaire de thread de l'horloge
        if (tickExecutor != null) {
            tickExecutor.shutdown();
            try {
                // On lui laisse 2 secondes de politesse pour finir de traiter la frame en cours...
                if (!tickExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    // ...s'il prend trop de temps, on le tue de force.
                    tickExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                tickExecutor.shutdownNow();
                // Règle de bonne pratique Java : si le thread a été interrompu par le système,
                // on restaure l'état d'interruption pour que l'OS le sache.
                Thread.currentThread().interrupt();
            }
        }

        // 3. On ordonne aux boucles 'while(running)' des entités de s'arrêter.
        if (this.chickenSpawner != null) this.chickenSpawner.stop();
        if (this.crowSpawner != null) this.crowSpawner.stop();
        for (Gardener gardener : gardeners) gardener.stopGardener();
        if (this.enemies != null) for (Chicken enemy : enemies) enemy.stop();
        if (this.crows != null) for (Crow crow : crows) crow.stop();
    }

    /**
     * DÉTECTION DES PARCELLES GLOBALES (Méthode de regroupement).
     * Parcourt les 10 000 cases. Dès qu'une case labourée (PlantTile) est trouvée,
     * on lance un algorithme "Flood Fill" pour trouver toutes les cases labourées qui la touchent.
     * L'ensemble formera un objet "Parcel" unique.
     */
    public void computeParcels() {
        // Tableau mémoire (aux mêmes dimensions que la carte) pour marquer "True" sur les cases qu'on a déjà analysées.
        // Cela évite de scanner deux fois la même case et de tourner en boucle infinie.
        boolean[][] visited = new boolean[WIDTH][HEIGHT];

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Tile t = getTile(x, y);

                // Si c'est une terre labourée ET qu'on ne l'a pas encore analysée
                if (t instanceof PlantTile && !visited[x][y]) {
                    ArrayList<PlantTile> parcelTiles = new ArrayList<>();

                    // On lance le scan de contagion à partir de cette coordonnée (x, y)
                    floodFillParcel(x, y, visited, parcelTiles);

                    // Une fois que l'algorithme a rempli 'parcelTiles' avec tout l'amas de terre,
                    // on crée l'objet Parcel logique.
                    new Parcel(parcelTiles);
                }
            }
        }
        System.out.println("Parcelles recalculées automatiquement !");
    }

    /**
     * ALGORITHME FLOOD FILL (Remplissage par diffusion).
     * C'est le même principe que l'outil "Pot de peinture" dans Paint : ça se répand sur les pixels adjacents de même couleur.
     * Plutôt que d'utiliser la "Récursivité" (la fonction qui s'appelle elle-même, ce qui pourrait faire crasher la RAM),
     * on utilise une liste d'attente (Queue).
     */
    private void floodFillParcel(int startX, int startY, boolean[][] visited, ArrayList<PlantTile> parcelTiles) {
        // Création de la file d'attente
        java.util.Queue<Point> queue = new java.util.LinkedList<>();
        queue.add(new Point(startX, startY));
        visited[startX][startY] = true; // On marque immédiatement le départ comme "visité"

        int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}}; // Directions orthogonales

        // Tant qu'il reste des cases dans la file d'attente à inspecter...
        while (!queue.isEmpty()) {
            // "poll()" retire le premier élément de la file d'attente et nous le donne
            Point p = queue.poll();
            Tile t = getTile(p.x, p.y);

            // On ajoute cette case à notre parcelle finale
            parcelTiles.add((PlantTile) t);

            // Pour la case actuelle, on regarde ses 4 voisins (Haut, Bas, Gauche, Droite)
            for (int[] d : dirs) {
                int nx = p.x + d[0];
                int ny = p.y + d[1];

                // Si le voisin est dans la carte...
                if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT) {
                    // ... et que c'est une terre labourée pas encore visitée ...
                    if (!visited[nx][ny] && getTile(nx, ny) instanceof PlantTile) {
                        // ... on le marque comme visité, et on le rajoute dans la file d'attente
                        // pour que l'algorithme aille regarder SES propres voisins plus tard.
                        visited[nx][ny] = true;
                        queue.add(new Point(nx, ny));
                    }
                }
            }
        }
        // La boucle se termine quand la tache d'encre ne trouve plus aucun voisin labouré.
    }

    public void addBuilding(Building b) {
        buildings.add(b);
    }

    /**
     * Méthode appelée avant de charger une sauvegarde.
     * Efface les obstacles aléatoires générés par l'initialisation pour mettre une map "propre"
     * prête à recevoir les objets de la sauvegarde.
     */
    public void prepareForLoad() {
        buildings.clear();

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Tile oldTile = tiles[y][x];
                Tile clean = new Tile(x, y, grassSprite);
                // On réinjecte les items posés au sol dans la nouvelle case propre
                if (oldTile != null) {
                    for (Entity entity : oldTile.getEntities()) {
                        clean.addEntity(entity);
                    }
                }
                tiles[y][x] = clean;
            }
        }
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    /**
     * ASTUCE D'AFFICHAGE ISOMÉTRIQUE (Z-Sorting).
     * Pour que l'affichage 2D simule de la profondeur, il faut dessiner les objets dans un ordre précis.
     * Cette fonction réordonne spécifiquement les barrières par leur axe X, de gauche à droite,
     * pour éviter que le visuel d'une barrière ne coupe celui d'une barrière posée derrière elle.
     */
    public void organizeFences() {
        ArrayList<Building> fences = new ArrayList<>();
        // On extrait toutes les barrières de la liste générale
        for (Building b : buildings) {
            if (b instanceof Fence) fences.add(b);
        }
        buildings.removeAll(fences);

        // On trie mathématiquement notre sous-liste par ordre croissant sur l'axe X
        fences.sort((b1, b2) -> Integer.compare(b1.getX(), b2.getX()));

        // On les remet toutes à la fin de la liste générale
        buildings.addAll(fences);
    }

    public void removeBuilding(Building b) {
        this.buildings.remove(b);

        // Quand on casse un bâtiment, il faut rendre l'herbe en dessous "marchable" (Walkable)
        // et "labourable" (Plowable) à nouveau, sur toute la surface (largeur * hauteur) du bâtiment.
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

    public boolean hasBuildingAt(int x, int y) {
        // Vérifie si le point (x,y) se trouve à l'intérieur du rectangle (Hitbox) formé par les dimensions d'un bâtiment
        for (src.model.buildings.Building b : buildings) {
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
        return null;
    }

    public Stats getStats() { return stats; }
    public src.model.Quests getQuests() { return quests; }

    public void setQuestChangeCallback(Runnable callback) {
        if (quests != null) quests.setChangeListener(callback);
    }

    // ==========================================
    // NOTIFICATEURS D'ÉVÉNEMENTS (MESSAGERS)
    // ==========================================
    // Ces fonctions sont appelées par le reste du jeu (ex: un outil) pour dire à World :
    // "Hé, je viens de réussir ça ! Prévient le moteur de quête !"

    public void registerPlantEvent(PlantType plantType) {
        if (quests != null) {
            syncLevelMilestones();
            quests.onPlant(plantType, stats);
        }
    }

    public void registerHarvestEvent(PlantType plantType) {
        if (quests != null) {
            syncLevelMilestones();
            quests.onHarvest(plantType, stats);
        }
    }

    public void registerBuildEvent(Building building) {
        if (quests != null) {
            syncLevelMilestones();
            quests.onBuild(building, stats);
            // Détection spécifique pour la quête du chapitre 4 (Poser des barrières)
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

    // ==========================================
    // GESTION DU SYSTÈME DE LIMITE DE LABOURAGE
    // ==========================================

    /**
     * Compte mathématiquement sur la grille combien de cases sont déjà labourées.
     */
    public int getPlowedTilesCount() {
        int count = 0;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (tiles[y][x] instanceof PlantTile) count++;
            }
        }
        return count;
    }

    /**
     * Compte hybride (Physique + Virtuel).
     * Additionne ce qui est déjà labouré sur la carte + les ordres cliqués par le joueur
     * qui sont en attente d'être effectués par les jardiniers.
     */
    public int getEffectivePlowedTilesCount() {
        return getPlowedTilesCount() + getReservedPlowTilesCount();
    }

    /** Lecture sécurisée de la mémoire de réservation. */
    public synchronized int getReservedPlowTilesCount() {
        return reservedPlowTiles;
    }

    /** * Bloque de l'espace limite.
     * C'est "synchronized" car le joueur pourrait cliquer très vite, et deux appels
     * pourraient s'écraser l'un l'autre dans la RAM (Race condition).
     */
    public synchronized void reservePlowTiles(int amount) {
        if (amount > 0) reservedPlowTiles += amount;
    }

    /** Libère de l'espace limite (quand l'action a été finalisée ou annulée par erreur). */
    public synchronized void releasePlowTiles(int amount) {
        if (amount > 0) reservedPlowTiles = Math.max(0, reservedPlowTiles - amount);
    }

    /** * Calcule le droit de propriété maximum du joueur.
     * Formule : 5 cases de base + 5 cases supplémentaires offertes à chaque niveau gagné.
     * Un "Math.min(100, ...)" est appliqué pour que la limite absolue du jeu ne dépasse jamais 100.
     */
    public int getPlowLimit() {
        int level = (stats != null) ? stats.getLevel() : 1;
        int limit = 5 + 5 * Math.max(1, level);
        return Math.min(100, limit);
    }

    /** * Fonction de validation.
     * Les outils/menus demandent à cette fonction "Ai-je le droit de faire cette action ?"
     * avant de laisser le joueur cliquer.
     */
    public boolean canAddPlowedTiles(int additionalTiles) {
        return getEffectivePlowedTilesCount() + Math.max(0, additionalTiles) <= getPlowLimit();
    }

    public void setLevelUpCallback(java.util.function.IntConsumer callback) {
        this.levelUpCallback = callback;
    }
}
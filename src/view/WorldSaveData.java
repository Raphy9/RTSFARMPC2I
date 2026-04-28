package src.view;

import src.model.*;
import src.model.buildings.*;

import java.io.Serializable;
import java.util.*;

/**
 * ╔════════════════════════════════════════════════════════════════════════╗
 * ║ WorldSaveData - DTO pour la sérialisation du monde                    ║
 * ╠════════════════════════════════════════════════════════════════════════╣
 * ║                                                                        ║
 * ║ Classe responsable de:                                                ║
 * ║ • Capturer l'état complet du World en constructeur                   ║
 * ║ • Exposer les données via des getters                                ║
 * ║                                                                        ║
 * ║ Architecture MVC:                                                     ║
 * ║ • C'est un DTO (Data Transfer Object) = données seulement             ║
 * ║ • Pas de logique métier                                              ║
 * ║ • La restauration est effectuée par SaveController (contrôleur)       ║
 * ║                                                                        ║
 * ║ Sérialisation:                                                        ║
 * ║ • Classe Serializable pour ObjectOutputStream                        ║
 * ║ • Tous les champs doivent être sérialisables                         ║
 * ╚════════════════════════════════════════════════════════════════════════╝
 */
public class WorldSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    // ────────────────────────────────────────────────────────────────────
    // CHAMPS PRINCIPAUX DE LA CLASSE
    // ────────────────────────────────────────────────────────────────────

    // Statistiques du joueur
    private int level;
    private int money;
    private int exp;
    private long timestamp;

    // Données de la carte et bâtiments
    private List<BuildingSaveData> buildings;           // Tous les bâtiments
    private Map<String, PlantTileSaveData> plantTiles;  // Tuiles labourées

    // Inventaire et quêtes
    private List<ItemSaveData> barnItems;               // Items de la grange
    private List<List<Integer>> questProgresses;        // Progression quêtes
    private int activeQuestLineIndex;                   // Chapitre actif

    private List<GardenerSaveData> gardeners;           // Positions des jardiniers

    /**
     * ╔════════════════════════════════════════════════════════════════════╗
     * ║ Constructeur: Capture l'état complet du World                     ║
     * ║                                                                    ║
     * ║ Appelé depuis SaveController.saveGame()                          ║
     * ║ Extrait UNIQUEMENT les données, pas la logique                   ║
     * ╚════════════════════════════════════════════════════════════════════╝
     */
    public WorldSaveData(World world) {
        // Sauvegarder les statistiques
        this.level = world.getStats().getLevel();
        this.money = world.getStats().getMoney();
        this.exp = world.getStats().getExp();
        this.timestamp = System.currentTimeMillis();

        // Initialiser les collections
        this.buildings = new ArrayList<>();
        this.plantTiles = new HashMap<>();
        this.barnItems = new ArrayList<>();
        this.questProgresses = new ArrayList<>();
        this.activeQuestLineIndex = 0;
        this.gardeners = new ArrayList<>();

        // Sauvegarder tous les bâtiments
        for (Building b : world.getBuildings()) {
            buildings.add(new BuildingSaveData(b));
        }

        // Sauvegarder toutes les PlantTiles
        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                Tile t = world.getTile(x, y);
                if (t instanceof PlantTile) {
                    plantTiles.put(x + "," + y, new PlantTileSaveData((PlantTile) t));
                }
            }
        }

        // Sauvegarder l'inventaire de la grange
        for (Item item : world.getBarn().getItems()) {
            barnItems.add(new ItemSaveData(item));
        }

        // Sauvegarder la progression des quêtes (utilise getProgressSnapshot() du modèle Quests)
        try {
            if (world.getQuests() != null) {
                questProgresses = world.getQuests().getProgressSnapshot();
                activeQuestLineIndex = world.getQuests().getActiveQuestLineIndex();
            }
        } catch (Exception ignored) {}

        // Sauvegarder la position des jardiniers
        for (Gardener gardener : world.getGardeners()) {
            gardeners.add(new GardenerSaveData(gardener));
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // GETTERS POUR ACCÈS AUX DONNÉES
    // ════════════════════════════════════════════════════════════════════

    public int getLevel() { return level; }
    public int getMoney() { return money; }
    public int getExp() { return exp; }
    public long getTimestamp() { return timestamp; }

    public List<BuildingSaveData> getBuildings() { return buildings; }
    public Map<String, PlantTileSaveData> getPlantTiles() { return plantTiles; }
    public List<ItemSaveData> getBarnItems() { return barnItems; }
    public List<List<Integer>> getQuestProgresses() { return questProgresses; }
    public int getActiveQuestLineIndex() { return activeQuestLineIndex; }
    public List<GardenerSaveData> getGardeners() { return gardeners; }

    // ════════════════════════════════════════════════════════════════════
    // CLASSE INTERNE: BuildingSaveData
    // ════════════════════════════════════════════════════════════════════

    /**
     * DTO pour sauvegarder les données d'un bâtiment (classe et position).
     */
    public static class BuildingSaveData implements Serializable {
        private static final long serialVersionUID = 1L;

        public String className;
        public int x, y;
        public String obstacleSpritePath;

        public BuildingSaveData(Building b) {
            this.className = b.getClass().getName();
            this.x = b.getX();
            this.y = b.getY();
            if (b instanceof Obstacle) {
                this.obstacleSpritePath = ((Obstacle) b).getSpritePath();
                if (this.obstacleSpritePath == null && b.getSprite() != null) {
                    this.obstacleSpritePath = b.getSprite().getDescription();
                }
            }
        }

        /**
         * Restaure le bâtiment dans le monde
         */
        public void restoreToWorld(World world) {
            try {
                Building b;

                if (Obstacle.class.getName().equals(this.className)) {
                    String spritePath = (obstacleSpritePath != null)
                            ? obstacleSpritePath
                            : "src/assets/Obstacles/rocher1.png";
                    b = new Obstacle(spritePath);
                } else {
                    Class<?> clazz = Class.forName(this.className);
                    b = (Building) clazz.getDeclaredConstructor().newInstance();
                }

                b.setPosition(x, y);
                world.addBuilding(b);

                // Marquer les tuiles comme inaccessibles
                for (int dx = 0; dx < b.getWidth(); dx++) {
                    for (int dy = 0; dy < b.getHeight(); dy++) {
                        Tile tile = world.getTile(x + dx, y + dy);
                        tile.setPlowable(false);
                        if (!b.isPassable()) tile.setWalkable(false);
                        if (tile instanceof PlantTile) {
                            ((PlantTile) tile).setPlantingBlocked(true);
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Erreur lors de la restauration du bâtiment : " + ex.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // CLASSE INTERNE: PlantTileSaveData
    // ════════════════════════════════════════════════════════════════════

    /**
     * DTO pour sauvegarder les données d'une tuile labourée avec sa plante.
     */
    public static class PlantTileSaveData implements Serializable {
        private static final long serialVersionUID = 1L;

        public boolean hasPlant;
        public int age;
        public float waterLevel;
        public String plantTypeName;
        public String plantStateName;
        public boolean hasFertilizer;
        public int ticksWithoutWater;

        public PlantTileSaveData(PlantTile pt) {
            Plant p = pt.getPlant();
            this.hasPlant = (p != null);
            if (this.hasPlant) {
                this.plantTypeName = p.getType().name();
                this.plantStateName = p.getState().name();
                this.waterLevel = p.getWaterLevel();
                this.hasFertilizer = p.hasFertilizer();
                this.age = p.getAge();
                this.ticksWithoutWater = p.getTicksWithoutWater();
            }
        }

        public void restoreToWorld(World world, int x, int y) {
            try {
                Tile t = world.getTile(x, y);
                PlantTile pt;

                if (!(t instanceof PlantTile)) {
                    world.toPlantTile(x, y);
                    pt = (PlantTile) world.getTile(x, y);
                } else {
                    pt = (PlantTile) t;
                }

                // Compatibilité anciennes sauvegardes
                boolean shouldRestorePlant = hasPlant || plantTypeName != null;
                if (!shouldRestorePlant) {
                    return;
                }

                PlantType type = PlantType.valueOf(plantTypeName);
                if (pt.getPlant() == null) {
                    pt.plant(type);
                }
                Plant p = pt.getPlant();
                if (p != null) {
                    PlantState state = (plantStateName != null)
                        ? PlantState.valueOf(plantStateName)
                        : PlantState.GRAINE;
                    p.restoreState(age, waterLevel, ticksWithoutWater, hasFertilizer, state);
                }
            } catch (Exception ex) {
                System.err.println("Erreur lors de la restauration de la plante : " + ex.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // CLASSE INTERNE: ItemSaveData
    // ════════════════════════════════════════════════════════════════════

    /**
     * DTO pour sauvegarder les données d'un item de grange.
     */
    public static class ItemSaveData implements Serializable {
        private static final long serialVersionUID = 1L;

        public String itemClassName;
        public String plantTypeName;
        public int quantity;

        public ItemSaveData(Item item) {
            this.itemClassName = item.getClass().getName();
            this.plantTypeName = item.getPlantType().name();
            this.quantity = item.getQuantity();
        }

        public void restoreToBarn(World world) {
            try {
                PlantType plantType = PlantType.valueOf(plantTypeName);
                if (ItemSeed.class.getName().equals(itemClassName)) {
                    world.getBarn().addItem(new ItemSeed(plantType, quantity));
                } else if (ItemPlant.class.getName().equals(itemClassName)) {
                    world.getBarn().addItem(new ItemPlant(plantType, quantity));
                }
            } catch (Exception ex) {
                System.err.println("Erreur lors de la restauration d'un item de grange : " + ex.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // CLASSE INTERNE: GardenerSaveData
    // ════════════════════════════════════════════════════════════════════

    /**
     * DTO pour sauvegarder la position et la direction d'un jardinier.
     */
    public static class GardenerSaveData implements Serializable {
        private static final long serialVersionUID = 1L;

        public int x;
        public int y;
        public int facingDirection;

        public GardenerSaveData(Gardener gardener) {
            this.x = gardener.getX();
            this.y = gardener.getY();
            this.facingDirection = gardener.getFacingDirection();
        }
    }
}

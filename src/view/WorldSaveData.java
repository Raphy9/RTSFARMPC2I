package src.view;

import src.model.Item;
import src.model.ItemPlant;
import src.model.ItemSeed;
import src.model.PlantState;
import src.model.PlantType;
import src.model.World;
import src.model.buildings.Building;
import src.model.buildings.Obstacle;
import src.model.Tile;
import src.model.PlantTile;
import src.model.Plant;

import java.io.Serializable;
import java.util.*;

/**
 * Classe pour sérialiser/désérialiser l'état complet du monde
 */
public class WorldSaveData implements Serializable {
    private static final long serialVersionUID = 2L;

    private int level;
    private int money;
    private int exp;
    private long timestamp;
    private List<BuildingSaveData> buildings;
    private Map<String, PlantTileSaveData> plantTiles;
    private List<ItemSaveData> barnItems;

    public WorldSaveData(World world) {
        this.level = world.getStats().getLevel();
        this.money = world.getStats().getMoney();
        this.exp = world.getStats().getExp();
        this.timestamp = System.currentTimeMillis();
        this.buildings = new ArrayList<>();
        this.plantTiles = new HashMap<>();
        this.barnItems = new ArrayList<>();

        // Sauvegarder tous les bâtiments
        for (Building b : world.getBuildings()) {
            buildings.add(new BuildingSaveData(b));
        }

        // Sauvegarder toutes les PlantTile (même vides)
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
    }

    public void applyToWorld(World world) {
        world.getStats().setLevel(this.level);
        world.getStats().setMoney(this.money);
        world.getStats().setExp(this.exp);

        // Restaurer l'inventaire de la grange seulement si la sauvegarde contient ces données.
        // (compatibilité avec anciennes sauvegardes)
        if (barnItems != null) {
            world.getBarn().getItems().clear();
            for (ItemSaveData itemData : barnItems) {
                itemData.restoreToBarn(world);
            }
        }

        // Restaurer d'abord les PlantTile (même vides), puis les bâtiments.
        // Cela permet de conserver les parcelles labourées ; les bâtiments réappliqueront ensuite leurs collisions.
        if (plantTiles != null) {
            for (Map.Entry<String, PlantTileSaveData> entry : plantTiles.entrySet()) {
                String[] coords = entry.getKey().split(",");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                entry.getValue().restoreToWorld(world, x, y);
            }
        }

        if (buildings != null) {
            for (BuildingSaveData bsd : buildings) {
                bsd.restoreToWorld(world);
            }
        }
    }

    // Classes internes pour sérialiser les données
    public static class BuildingSaveData implements Serializable {
        private static final long serialVersionUID = -8617244170660032689L;

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

                // Compatibilité anciennes sauvegardes: si plantTypeName est présent,
                // c'était forcément une case avec plante (même si hasPlant n'existait pas).
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
                    PlantState state = (plantStateName != null) ? PlantState.valueOf(plantStateName) : PlantState.GRAINE;
                    p.restoreState(age, waterLevel, ticksWithoutWater, hasFertilizer, state);
                }
            } catch (Exception ex) {
                System.err.println("Erreur lors de la restauration de la plante : " + ex.getMessage());
            }
        }
    }

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

    public int getLevel() { return level; }
    public int getMoney() { return money; }
    public long getTimestamp() { return timestamp; }
}






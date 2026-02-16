package src.model;

public class Entity {
    private String name;
    private Stats stats;
    private Inventory inventory;

    public Entity(String name, Stats stats, Inventory inventory) {
        this.name = name;
        this.stats = stats;
        this.inventory = inventory;
    }

    public String getName() {
        return name;
    }

    public Stats getStats() {
        return stats;
    }

    public Inventory getInventory() {
        return inventory;
    }
}

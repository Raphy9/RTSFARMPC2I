package src.model;

/**
 * La classe Barn représente une grange dans le jeu, qui peut stocker des items (plantes et graines).
 * Elle hérite de Inventory et ajoute des fonctionnalités spécifiques à la gestion d'une grange.
 */
public class Barn extends Inventory {

    private Stats stats;

    public Barn(Stats stats) {
        super();
        this.stats = stats;
    }

    public int getMoney() {
        return stats.getMoney();
    }

    public int transferToInventory(Inventory target, Item sourceItem, int qty) {
        return super.transferTo(target, sourceItem, qty);
    }

    /**
     * Vend une certaine quantité d'un item de la grange.
     */
    public int sellItem(Item item, int qty) {
        int itemPrice = 0;

        if (item instanceof ItemPlant) {
            switch (item.getPlantType()) {
                case CHOUX -> itemPrice = PlantType.CHOUX.getValue();
                case CAROTTE -> itemPrice = PlantType.CAROTTE.getValue();
                case CITROUILLE -> itemPrice = PlantType.CITROUILLE.getValue();
                case FRAISE -> itemPrice = PlantType.FRAISE.getValue();
            }
        } else if (item instanceof ItemSeed) {
            switch (item.getPlantType()) {
                case CHOUX -> itemPrice = PlantType.CHOUX.getValue()/2;
                case CAROTTE -> itemPrice = PlantType.CAROTTE.getValue()/2;
                case CITROUILLE -> itemPrice = PlantType.CITROUILLE.getValue()/2;
                case FRAISE -> itemPrice = PlantType.FRAISE.getValue()/2;
            }
        }

        if (qty > 0 && getItems().contains(item) && item.getQuantity() >= qty) {
            item.removeQuantity(qty);
            int earned = itemPrice * qty;
            stats.addMoney(earned);
        }

        return itemPrice;
    }

    /**
     * Achète une certaine quantité d'un item.
     */
    public int buyItem(Item item, int qty) {
        int itemPrice = 0;

        if (item instanceof ItemSeed) {
            switch (item.getPlantType()) {
                case CHOUX -> itemPrice = PlantType.CHOUX.getValue()/2 + 1;
                case CAROTTE -> itemPrice = PlantType.CAROTTE.getValue()/2 + 1;
                case CITROUILLE -> itemPrice = PlantType.CITROUILLE.getValue()/2 + 1;
                case FRAISE -> itemPrice = PlantType.FRAISE.getValue()/2 + 1;
            }
        } else if (item instanceof ItemPlant) {
            switch (item.getPlantType()) {
                case CHOUX -> itemPrice = PlantType.CHOUX.getValue() + 1;
                case CAROTTE -> itemPrice = PlantType.CAROTTE.getValue() + 1;
                case CITROUILLE -> itemPrice = PlantType.CITROUILLE.getValue() + 1;
                case FRAISE -> itemPrice = PlantType.FRAISE.getValue() + 1;
            }
        }

        if (qty > 0) {
            int cost = itemPrice * qty;

            if (stats.getMoney() >= cost) {
                stats.removeMoney(cost);

                if (item instanceof ItemSeed) {
                    super.addItem(new ItemSeed(item.getPlantType(), qty));
                } else if (item instanceof ItemPlant) {
                    super.addItem(new ItemPlant(item.getPlantType(), qty));
                }
            }
        }
        return itemPrice;
    }
}
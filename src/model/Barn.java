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

        // 1. Déterminer le prix unitaire
        if (item instanceof ItemPlant) {
            switch (((ItemPlant) item).getPlantType()) {
                case CHOUX -> itemPrice = 4;
                case CAROTTE -> itemPrice = 3;
                case CITROUILLE -> itemPrice = 7;
                case FRAISE -> itemPrice = 9;
            }
        } else if (item instanceof ItemSeed) {
            switch (((ItemSeed) item).getPlantType()) {
                case CHOUX -> itemPrice = 2;
                case CAROTTE -> itemPrice = 1;
                case CITROUILLE -> itemPrice = 3;
                case FRAISE -> itemPrice = 4;
            }
        }

        // 2. Effectuer la transaction UNIQUEMENT si on vend vraiment (qty > 0)
        if (qty > 0 && getItems().contains(item) && item.getQuantity() >= qty) {
            item.removeQuantity(qty);
            stats.addMoney(itemPrice * qty);
        }

        return itemPrice;
    }

    /**
     * Achète une certaine quantité d'un item.
     */
    public int buyItem(Item item, int qty) {
        int itemPrice = 0;

        // 1. Déterminer le prix unitaire
        if (item instanceof ItemSeed) {
            switch (((ItemSeed) item).getPlantType()) {
                case CHOUX -> itemPrice = 3;
                case CAROTTE -> itemPrice = 2;
                case CITROUILLE -> itemPrice = 4;
                case FRAISE -> itemPrice = 5;
            }
        } else if (item instanceof ItemPlant) {
            switch (((ItemPlant) item).getPlantType()) {
                case CHOUX -> itemPrice = 5;
                case CAROTTE -> itemPrice = 4;
                case CITROUILLE -> itemPrice = 8;
                case FRAISE -> itemPrice = 10;
            }
        }

        // 2. Effectuer la transaction UNIQUEMENT si on achète vraiment (qty > 0)
        if (qty > 0) {
            int cost = itemPrice * qty;

            if (stats.getMoney() >= cost) {
                stats.removeMoney(cost);

                if (item instanceof ItemSeed) {
                    super.addItem(new ItemSeed(((ItemSeed) item).getPlantType(), qty));
                } else if (item instanceof ItemPlant) {
                    super.addItem(new ItemPlant(((ItemPlant) item).getPlantType(), qty));
                }
            }
        }
        return itemPrice;
    }
}
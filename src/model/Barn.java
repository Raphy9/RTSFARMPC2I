package src.model;

/**
 * La classe Barn représente la grange du joueur.
 * Elle sert de stockage principal et de centre commercial (achat/vente).
 * Elle hérite de 'Inventory' pour la gestion des objets et interagit avec 'Stats'
 * pour manipuler l'argent du joueur.
 */
public class Barn extends Inventory {

    /** Référence aux statistiques globales (argent, score, etc.) */
    private Stats stats;

    /**
     * Constructeur de la grange.
     * @param stats Les statistiques du joueur pour lier l'inventaire au portefeuille.
     */
    public Barn(Stats stats) {
        super(); // Initialise la liste d'items via Inventory
        this.stats = stats;
    }

    /**
     * @return Le montant actuel de l'argent disponible dans les stats du joueur.
     */
    public int getMoney() {
        return stats.getMoney();
    }

    /**
     * Transfère des objets de la grange vers un autre inventaire (ex: sacoche du joueur).
     * @param target L'inventaire de destination.
     * @param sourceItem L'objet à transférer.
     * @param qty La quantité à déplacer.
     * @return Le résultat du transfert défini dans Inventory.
     */
    public int transferToInventory(Inventory target, Item sourceItem, int qty) {
        return super.transferTo(target, sourceItem, qty);
    }

    /**
     * Vend une certaine quantité d'un item présent dans la grange.
     * Calcule le prix en fonction du type de plante et met à jour les fonds du joueur.
     *
     * @param item L'objet à vendre.
     * @param qty La quantité à vendre.
     * @return Le prix unitaire de l'objet vendu.
     */
    public int sellItem(Item item, int qty) {
        int itemPrice = 0;

        // Détermination du prix de vente
        if (item instanceof ItemPlant) {
            // Les plantes récoltées se vendent à leur valeur nominale
            switch (item.getPlantType()) {
                case CHOUX -> itemPrice = PlantType.CHOUX.getValue();
                case CAROTTE -> itemPrice = PlantType.CAROTTE.getValue();
                case CITROUILLE -> itemPrice = PlantType.CITROUILLE.getValue();
                case FRAISE -> itemPrice = PlantType.FRAISE.getValue();
            }
        } else if (item instanceof ItemSeed) {
            // Les graines se revendent à moitié prix de la plante finale
            switch (item.getPlantType()) {
                case CHOUX -> itemPrice = PlantType.CHOUX.getValue()/2;
                case CAROTTE -> itemPrice = PlantType.CAROTTE.getValue()/2;
                case CITROUILLE -> itemPrice = PlantType.CITROUILLE.getValue()/2;
                case FRAISE -> itemPrice = PlantType.FRAISE.getValue()/2;
            }
        }

        // Vérification de la validité de la vente (quantité positive et stock suffisant)
        if (qty > 0 && getItems().contains(item) && item.getQuantity() >= qty) {
            item.removeQuantity(qty); // Retire les objets de l'inventaire
            int earned = itemPrice * qty;
            stats.addMoney(earned); // Crédite le compte du joueur
        }

        return itemPrice;
    }

    /**
     * Achète une certaine quantité d'un item (principalement des graines).
     * Vérifie si le joueur a assez d'argent avant de valider la transaction.
     *
     * @param item Le modèle de l'objet à acheter.
     * @param qty La quantité souhaitée.
     * @return Le prix unitaire d'achat de l'objet.
     */
    public int buyItem(Item item, int qty) {
        int itemPrice = 0;

        // Détermination du prix d'achat
        if (item instanceof ItemSeed) {
            // Le prix d'achat d'une graine est légèrement supérieur à son prix de revente
            // (Valeur/2 + 1) pour créer une marge commerciale.
            switch (item.getPlantType()) {
                case CHOUX -> itemPrice = PlantType.CHOUX.getValue()/2 + 1;
                case CAROTTE -> itemPrice = PlantType.CAROTTE.getValue()/2 + 1;
                case CITROUILLE -> itemPrice = PlantType.CITROUILLE.getValue()/2 + 1;
                case FRAISE -> itemPrice = PlantType.FRAISE.getValue()/2 + 1;
            }
        }

        if (qty > 0) {
            int cost = itemPrice * qty;

            // Vérification de la solvabilité du joueur
            if (stats.getMoney() >= cost) {
                stats.removeMoney(cost); // Débite le joueur

                // Ajoute l'objet acheté à l'inventaire de la grange
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
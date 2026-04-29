package src.model;

import java.util.ArrayList;

/**
 * Classe gérant une collection d'objets (Items).
 * Utilisée aussi bien pour le sac du jardinier que pour le stock de la grange (Barn).
 */
public class Inventory {

    /** Liste dynamique contenant les objets présents dans l'inventaire. */
    private ArrayList<Item> items;

    /**
     * Constructeur : Initialise un inventaire vide.
     */
    public Inventory() {
        items = new ArrayList<>();
    }

    /**
     * Ajoute un item à l'inventaire.
     * Logique de "Stacking" : Si un item du même type (ex: graines de choux)
     * existe déjà, on augmente simplement sa quantité au lieu de créer une nouvelle ligne.
     *
     * @param item L'objet à ajouter.
     */
    public void addItem(Item item) {
        Item existing = findSameItem(item);
        if (existing != null) {
            // L'item existe déjà, on fusionne les piles
            existing.addQuantity(item.getQuantity());
        } else {
            // Nouvel item, on l'ajoute à la liste
            items.add(item);
        }
    }

    /**
     * Retire complètement un objet de la liste (utilisé si la quantité tombe à 0).
     */
    public void removeItem(Item item) {
        items.remove(item);
    }

    /**
     * @return La liste brute des items pour l'affichage ou le traitement.
     */
    public ArrayList<Item> getItems() {
        return items;
    }

    /**
     * Recherche dans l'inventaire s'il existe un objet identique à celui passé en paramètre.
     * L'identité est définie par : même classe (Graine vs Plante) ET même type (Chou vs Carotte).
     *
     * @param item L'objet de référence.
     * @return L'objet trouvé dans l'inventaire, ou null si aucun ne correspond.
     */
    public Item findSameItem(Item item) {
        for (Item it : items) {
            // Vérification de la classe et de l'énumération PlantType
            if (it.getClass().equals(item.getClass()) && it.getPlantType() == item.getPlantType()) {
                return it;
            }
        }
        return null;
    }

    /**
     * Transfère une quantité spécifique d'un item de cet inventaire vers un autre (target).
     * Très utilisé pour les échanges entre le sac du joueur et la grange.
     *
     * @param target L'inventaire de destination.
     * @param sourceItem L'objet présent dans cet inventaire à transférer.
     * @param qty La quantité souhaitée.
     * @return La quantité réelle qui a pu être transférée.
     */
    public int transferTo(Inventory target, Item sourceItem, int qty) {
        // Sécurités de base
        if (qty <= 0 || sourceItem == null) return 0;

        int available = sourceItem.getQuantity();
        int toTransfer = Math.min(qty, available); // On ne peut pas transférer plus que ce qu'on a

        if (toTransfer <= 0) return 0;

        // 1. Gérer la destination (Target)
        Item targetSame = target.findSameItem(sourceItem);
        if (targetSame != null) {
            // Si la cible possède déjà l'objet, on incrémente sa quantité
            targetSame.addQuantity(toTransfer);
        } else {
            /*
             * Si la cible ne possède pas l'objet, on doit créer une nouvelle instance.
             * On utilise instanceof pour déterminer s'il faut créer une Graine ou une Plante.
             */
            Item newItem = null;
            if (sourceItem instanceof ItemPlant) {
                newItem = new ItemPlant(sourceItem.getPlantType(), toTransfer);
            } else if (sourceItem instanceof ItemSeed) {
                newItem = new ItemSeed(sourceItem.getPlantType(), toTransfer);
            }

            if (newItem != null) {
                target.addItem(newItem);
            }
        }

        // 2. Gérer la source
        // On réduit la quantité dans l'inventaire actuel
        sourceItem.removeQuantity(toTransfer);

        return toTransfer;
    }
}
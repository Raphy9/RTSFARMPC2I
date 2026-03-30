package src.model;

import java.util.ArrayList;

public class Inventory {
    private ArrayList<Item> items;

    public Inventory() {
        items = new ArrayList<>();
    }

    /**
     * Ajoute un item à l'inventaire. Si un item du même type (classe + plantType) existe, fusionne les quantités.
     */
    public void addItem(Item item) {
        Item existing = findSameItem(item);
        if (existing != null) {
            existing.addQuantity(item.getQuantity());
        } else {
            items.add(item);
        }
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    /**
     * Cherche un item dans cet inventaire du même type (même classe et même PlantType).
     * Retourne l'item trouvé ou null.
     */
    public Item findSameItem(Item item) {
        for (Item it : items) {
            if (it.getClass().equals(item.getClass()) && it.getPlantType() == item.getPlantType()) {
                return it;
            }
        }
        return null;
    }

    /**
     * Transfère jusqu'à qty unités de sourceItem depuis cet inventaire vers target.
     * Retourne la quantité effectivement transférée.
     */
    public int transferTo(Inventory target, Item sourceItem, int qty) {
        if (qty <= 0 || sourceItem == null) return 0;
        int available = sourceItem.getQuantity();
        int toTransfer = Math.min(qty, available);
        if (toTransfer <= 0) return 0;

        // Chercher un item similaire dans la cible
        Item targetSame = target.findSameItem(sourceItem);
        if (targetSame != null) {
            targetSame.addQuantity(toTransfer);
        } else {
            // Créer une nouvelle instance du même type avec la quantité à transférer
            Item newItem = null;
            if (sourceItem instanceof ItemPlant) {
                newItem = new ItemPlant(sourceItem.getPlantType(), toTransfer);
            } else if (sourceItem instanceof ItemSeed) {
                newItem = new ItemSeed(sourceItem.getPlantType(), toTransfer);
            } else {
                // Par défaut, tenter d'ajouter l'objet tel quel (peu probable car Item est abstrait)
            }
            if (newItem != null) {
                target.addItem(newItem);
            }
        }
        // Retirer la quantité du source
        sourceItem.removeQuantity(toTransfer);
        return toTransfer;
    }
}

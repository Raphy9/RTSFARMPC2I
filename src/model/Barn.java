package src.model;

import javax.swing.*;
import java.util.ArrayList;

/**
 * La classe Barn représente une grange dans le jeu, qui peut stocker des items (plantes et graines).
 * Elle hérite de Inventory et ajoute des fonctionnalités spécifiques à la gestion d'une grange.
 * L'inventaire de la grange est séparé de ceux des jardiniers, elle ne contient pas ce qui est dans l'inventaire des jardiniers.
 */
public class Barn extends Inventory {

    private Stats stats;

    public Barn() {
        super();
    }

    /**
     * Transfère jusqu'à qty unités de sourceItem depuis cette grange vers target.
     */
    public int transferToInventory(Inventory target, Item sourceItem, int qty) {
        return super.transferTo(target, sourceItem, qty);
    }

    /**
     * Vend une certaine quantité d'un item de la grange, en retirant les items vendus de l'inventaire de la grange.
     */
    public void sellItem(Item item, int qty) {
        if (qty > 0 && item != null && getItems().contains(item) && item.getQuantity() - qty >= 0) {
            item.removeQuantity(qty);
            // Met à jour l'argent gagné du joueur en fonction de la vente
            // Si l'item est une plante
            if (item instanceof ItemPlant) {
                switch (item.getPlantType()) {
                    case CHOUX -> stats.addMoney(5 * qty);
                    case CAROTTE -> stats.addMoney(3 * qty);
                    case CITROUILLE -> stats.addMoney(8 * qty);
                    case FRAISE -> stats.addMoney(10 * qty);
                }
            }
            // Si l'item est une graine
            else if (item instanceof ItemSeed) {
                switch (item.getPlantType()) {
                    case CHOUX -> stats.addMoney(2 * qty);
                    case CAROTTE -> stats.addMoney(1 * qty);
                    case CITROUILLE -> stats.addMoney(3 * qty);
                    case FRAISE -> stats.addMoney(4 * qty);
                }
            }
        }
    }

    /**
     * Achète une certaine quantité d'un item, en ajoutant les items achetés à l'inventaire de la grange.
     * Le prix d'achat est défini en fonction du type de graine (on achète que des graines)
     */
    public void buyItem(PlantType plantType, int qty) {
        if (qty > 0) {
            int cost = 0;
            // On définit le coût d'achat en fonction du type de graine
            switch (plantType) {
                case CHOUX -> cost = 2 * qty;
                case CAROTTE -> cost = qty;
                case CITROUILLE -> cost = 3 * qty;
                case FRAISE -> cost = 4 * qty;
            }
            // Vérifie que le joueur a assez d'argent pour l'achat
            if (stats.getMoney() >= cost) {
                // Le joueur a assez d'argent, on effectue l'achat en retirant l'argent
                stats.removeMoney(cost);
                // On ajoute les graines à l'inventaire de la grange
                ItemSeed item = new ItemSeed(plantType, qty);
                super.addItem(item);
            }
        }
    }
}

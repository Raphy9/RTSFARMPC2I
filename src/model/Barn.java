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
            if (item instanceof ItemPlant) {
                switch (item.getPlantType()) {
                    case CHOUX -> stats.addMoney(5 * qty);
                    case CAROTTE -> stats.addMoney(3 * qty);
                    case CITROUILLE -> stats.addMoney(8 * qty);
                    case FRAISE -> stats.addMoney(10 * qty);
                }
            } else if (item instanceof ItemSeed) {
                switch (item.getPlantType()) {
                    case CHOUX -> stats.addMoney(2 * qty);
                    case CAROTTE -> stats.addMoney(1 * qty);
                    case CITROUILLE -> stats.addMoney(3 * qty);
                    case FRAISE -> stats.addMoney(4 * qty);
                }
            }
        }
    }
}

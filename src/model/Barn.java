package src.model;

import javax.swing.*;
import java.util.ArrayList;

/**
 * La classe Barn représente une grange dans le jeu, qui peut stocker des items (plantes et graines).
 * Elle hérite de Inventory et ajoute des fonctionnalités spécifiques à la gestion d'une grange.
 * L'inventaire de la grange est séparé de ceux des jardiniers, elle ne contient pas ce qui est dans l'inventaire des jardiniers.
 */
public class Barn extends Inventory {

    public Barn() {
        super();
    }

    /**
     * Transfère jusqu'à qty unités de sourceItem depuis cette grange vers target.
     */
    public int transferToInventory(Inventory target, Item sourceItem, int qty) {
        return super.transferTo(target, sourceItem, qty);
    }
}

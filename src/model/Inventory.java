package src.model;

import java.util.ArrayList;

public class Inventory {
    private ArrayList items;

    // ! Regarder quel type on met dans l'ArrayList de l'inventaire !
    public Inventory() {
        items = new ArrayList();
    }

    public void addItem(Plant plant) {
        items.add(plant);
    }
}

package src.view;

import src.model.Inventory;
import src.model.Item;

import javax.swing.*;
import java.awt.*;

/**
 * Classe popup pour afficher l'inventaire de la grange uniquement, on n'affiche pas l'inventaire des jardiniers
 */
public class PopupInventory extends PopupPanel {
    private Inventory inventory;
    private ImageIcon backgroundImage;

    // Nombres d'items affichés dans la grille d'inventaire, on a une grille de 5x3 pour afficher les items.
    private static final int WIDTH = 5;
    private static final int HEIGHT = 3;

    private JPanel gridInventory;

    public PopupInventory(Display display, int width, int height, String title, String text, Inventory inventory) {
        super(display, width, height, title);
        this.inventory = inventory;
        // backgroundImage = new ImageIcon("src/assets/inventory_background.png");
        initializeGrid();
    }

    private void initializeGrid() {
        gridInventory = new JPanel(new GridLayout(HEIGHT, WIDTH));
        // Ajouter les items de l'inventaire à la grille
        for (int i = 0; i < HEIGHT * WIDTH; i++) {
            if (i < inventory.getItems().size()) {
                // Afficher l'item avec son image et sa quantité
                Item item = (Item) inventory.getItems().get(i);
                JButton itemButton = new JButton("x" + item.getQuantity());
                itemButton.setIcon(item.getImage());
                gridInventory.add(itemButton);
            } else {
                // Ajouter une case vide si il n'y a pas d'item à afficher
                gridInventory.add(new JLabel());
            }
        }
        this.add(gridInventory, BorderLayout.CENTER);
    }
}

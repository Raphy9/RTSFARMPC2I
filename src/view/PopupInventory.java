package src.view;

import src.control.popups.InventorySelector;
import src.model.Inventory;
import src.model.Item;
import src.model.actions.ActionBuilder;

import javax.swing.*;
import java.awt.*;

public class PopupInventory extends PopupPanel {
    private Display display;
    private Inventory inventory;
    private Class expectedItemType;

    private static final int WIDTH = 5;
    private static final int HEIGHT = 3;

    public PopupInventory(Display display, Inventory inventory, Class expectedItemType, ActionBuilder builder) {
        super(display, 450, 250, "Inventaire");
        this.display = display;
        this.inventory = inventory;
        this.expectedItemType = expectedItemType;
        initializeGrid(builder);
    }

    private void initializeGrid(ActionBuilder builder) {
        JPanel gridInventory = new JPanel(new GridLayout(HEIGHT, WIDTH, 10, 10));
        gridInventory.setOpaque(false);
        gridInventory.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));

        Color selectLight = new Color(160, 100, 60);
        Color selectDark = new Color(80, 40, 10);

        for (int i = 0; i < HEIGHT * WIDTH; i++) {
            if (i < inventory.getItems().size()) {
                Item item = inventory.getItems().get(i);

                JButton itemButton = new JButton("x" + item.getQuantity());
                itemButton.setFocusable(false);
                itemButton.setBackground(selectLight);
                itemButton.setForeground(Color.WHITE); // Texte blanc
                itemButton.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2));
                itemButton.setHorizontalTextPosition(SwingConstants.CENTER);
                itemButton.setVerticalTextPosition(SwingConstants.BOTTOM);

                if (GameFonts.MINECRAFT_FONT != null) {
                    itemButton.setFont(GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 12f));
                }

                if (item.getImage() != null) {
                    if (item.getRequiredLevel() <= display.getWorld().getStats().getLevel()) {
                        itemButton.setIcon(new ImageIcon(item.getImage().getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH)));
                    } else {
                        ImageIcon lock = new ImageIcon("src/assets/lock.png");
                        itemButton.setIcon(new ImageIcon(lock.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH)));
                    }
                }

                if (item.getQuantity() > 0) {
                    itemButton.addActionListener(new InventorySelector(display, expectedItemType, item, builder));
                    // Effet de survol
                    itemButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseEntered(java.awt.event.MouseEvent evt) { itemButton.setBackground(selectDark); }
                        public void mouseExited(java.awt.event.MouseEvent evt) { itemButton.setBackground(selectLight); }
                    });
                } else {
                    itemButton.setEnabled(false);
                    itemButton.setBackground(new Color(110, 80, 60)); // Brun grise si vide
                }
                gridInventory.add(itemButton);
            } else {
                gridInventory.add(createEmptySlot());
            }
        }
        this.add(gridInventory, BorderLayout.CENTER);
    }

    private JPanel createEmptySlot() {
        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(new Color(230, 180, 110, 150)); // Semi transparent
        emptyPanel.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2));
        return emptyPanel;
    }
}
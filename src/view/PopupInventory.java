package src.view;

import src.control.popups.InventorySelector;
import src.model.Inventory;
import src.model.Item;
import src.model.actions.ActionBuilder;

import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre de type Popup affichant une grille d'inventaire.
 * Utilisée pour permettre au joueur de choisir un objet (graines, outils, etc.)
 * afin de construire une action.
 */
public class PopupInventory extends PopupPanel {
    private Display display;
    private Inventory inventory;
    private Class expectedItemType; // Type d'objet attendu pour filtrer l'action (ex: Graines)

    // Dimensions fixes de la grille (3 lignes de 5 colonnes = 15 slots)
    private static final int WIDTH = 5;
    private static final int HEIGHT = 3;

    /**
     * Constructeur de l'inventaire.
     * @param display Référence vers la vue principale.
     * @param inventory Le modèle d'inventaire à afficher.
     * @param expectedItemType Le type de classe d'Item requis pour cette sélection.
     * @param builder Le constructeur d'action à remplir avec l'objet choisi.
     */
    public PopupInventory(Display display, Inventory inventory, Class expectedItemType, ActionBuilder builder) {
        // Appelle le constructeur de PopupPanel avec titre et dimensions fixes
        super(display, 450, 250, "Inventaire");
        this.display = display;
        this.inventory = inventory;
        this.expectedItemType = expectedItemType;

        // Construction de la grille graphique
        initializeGrid(builder);
    }

    /**
     * Initialise et remplit le panneau central avec les objets de l'inventaire.
     */
    private void initializeGrid(ActionBuilder builder) {
        // Utilisation d'un GridLayout pour organiser les items
        JPanel gridInventory = new JPanel(new GridLayout(HEIGHT, WIDTH, 10, 10));
        gridInventory.setOpaque(false);
        gridInventory.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));

        // Définition des couleurs thématiques (style Stardew Valley)
        Color selectLight = new Color(160, 100, 60);
        Color selectDark = new Color(80, 40, 10);

        // On boucle sur tous les slots de la grille (15 au total)
        for (int i = 0; i < HEIGHT * WIDTH; i++) {
            // Si un objet existe dans la liste à cet index
            if (i < inventory.getItems().size()) {
                Item item = inventory.getItems().get(i);

                // Création du bouton avec le texte de quantité (ex: x5)
                JButton itemButton = new JButton("x" + item.getQuantity());
                itemButton.setFocusable(false);
                itemButton.setBackground(selectLight);
                itemButton.setForeground(Color.WHITE);
                itemButton.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2));

                // Alignement du texte sous l'icône
                itemButton.setHorizontalTextPosition(SwingConstants.CENTER);
                itemButton.setVerticalTextPosition(SwingConstants.BOTTOM);

                // Application de la police Minecraft si chargée
                if (GameFonts.MINECRAFT_FONT != null) {
                    itemButton.setFont(GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 12f));
                }

                // Gestion de l'affichage de l'icône
                if (item.getImage() != null) {
                    // Vérification du niveau requis pour l'objet
                    if (item.getRequiredLevel() <= display.getWorld().getStats().getLevel()) {
                        // Affichage de l'image de l'objet redimensionnée
                        itemButton.setIcon(new ImageIcon(item.getImage().getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH)));
                    } else {
                        // Affichage d'un cadenas si le niveau du joueur est trop bas
                        ImageIcon lock = new ImageIcon("src/assets/lock.png");
                        itemButton.setIcon(new ImageIcon(lock.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH)));
                    }
                }

                // Si l'objet est disponible (quantité > 0)
                if (item.getQuantity() > 0) {
                    // Ajout de l'actionneur de sélection
                    itemButton.addActionListener(new InventorySelector(display, expectedItemType, item, builder));

                    // Ajout des effets visuels au survol (Hover)
                    itemButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseEntered(java.awt.event.MouseEvent evt) { itemButton.setBackground(selectDark); }
                        public void mouseExited(java.awt.event.MouseEvent evt) { itemButton.setBackground(selectLight); }
                    });
                } else {
                    // Désactive le bouton si l'item est épuisé
                    itemButton.setEnabled(false);
                    itemButton.setBackground(new Color(110, 80, 60)); // Brun grisé
                }
                gridInventory.add(itemButton);
            } else {
                // Si pas d'objet à cet index, on ajoute un slot visuellement vide
                gridInventory.add(createEmptySlot());
            }
        }
        // Ajoute la grille complète au centre du panel parent
        this.add(gridInventory, BorderLayout.CENTER);
    }

    /**
     * Crée un panneau décoratif pour représenter un emplacement d'inventaire vide.
     * @return Un JPanel stylisé.
     */
    private JPanel createEmptySlot() {
        JPanel emptyPanel = new JPanel();
        // Couleur beige semi-transparente
        emptyPanel.setBackground(new Color(230, 180, 110, 150));
        emptyPanel.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2));
        return emptyPanel;
    }
}
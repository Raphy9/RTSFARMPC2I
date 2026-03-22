package src.view;

import src.control.popups.InventorySelector;
import src.model.Inventory;
import src.model.Item;
import src.model.actions.ActionBuilder;

import javax.swing.*;
import java.awt.*;

/**
 * Classe popup pour afficher l'inventaire de la grange uniquement, on n'affiche pas l'inventaire des jardiniers
 * Popup recree a chaque fois qu'on en a besoin, car l'inventaire evolue
 */
public class PopupInventory extends PopupPanel {
    private Display display;
    private Inventory inventory; // L'inventaire dont on va afficher les items, dans ce cas c'est l'inventaire de la grange
    private ImageIcon backgroundImage; // Image de fond pour le popup d'inventaire, on peut l'utiliser pour rendre le popup plus joli et plus immersif

    // Nombres d'items affichés dans la grille d'inventaire, on a une grille de 5x3 pour afficher les items.
    private static final int WIDTH = 5;
    private static final int HEIGHT = 3;

    // Panel pour afficher la grille d'inventaire, on utilise un GridLayout pour afficher les items dans une grille de 5x3,
    // avec des cases vides si il n'y a pas d'item à afficher
    private JPanel gridInventory;

    private Class expectedItemType; // Type d'item qu'on doit selectionner

    /**
    Constructeur de la classe PopupInventory, qui prend en paramètre l'inventaire à afficher (Inventory inventory)
    et les paramètres pour le popup (Display display, int width, int height, String title)
     @param display l'affichage de la vue globale, pour pouvoir fermer le popup et revenir à la vue globale
     @param inventory l'inventaire à afficher dans le popup, dans ce cas c'est l'inventaire de la grange
     @param expectedItemType le type d'item qu'on doit selectionner dans le popup (ex: ItemSeed.class pour les graines)
    */
    public PopupInventory(Display display, Inventory inventory, Class expectedItemType, ActionBuilder builder) {
        // Appelle du constructeur de la classe mère PopupPanel pour initialiser le popup avec les paramètres donnés
        super(display, 400, 200, "Selectionner un item");

        this.display = display;
        this.inventory = inventory;
        this.expectedItemType = expectedItemType;

        initializeGrid(builder);
    }

    /**
    Méthode pour initialiser la grille d'inventaire, qui affiche les items de l'inventaire dans une grille de 5x3,
    avec des cases vides si il n'y a pas d'item à afficher

    On peut changer pour afficher par ordre alphabétique ou par type d'item (graines d'abord, puis plantes),
    mais pour l'instant on affiche les items dans l'ordre où ils sont stockés dans la liste d'items de l'inventaire
     */
    private void initializeGrid(ActionBuilder builder) {
        // On définit la grille d'inventaire en utilisant un GridLayout pour afficher les items dans une grille selon les dimensions (HEIGHT x WIDTH)
        gridInventory = new JPanel(new GridLayout(HEIGHT, WIDTH));

        // Ajouter les items de l'inventaire à la grille
        // GridLayout remplit la grille de gauche à droite, de haut en bas, remplit une nouvelle ligne lorsque la ligne précédente est remplie
        // C'est pour cela qu'on fait HEIGHT * WIDTH pour parcourir les premiers items de l'inventaire,
        // et on affiche les items de l'inventaire dans l'ordre où ils sont stockés dans la liste d'items de l'inventaire pour l'instant
        for (int i = 0; i < HEIGHT * WIDTH; i++) {

            // Si on est toujours dans les limites de la liste d'items de l'inventaire, on affiche l'item correspondant à l'index i
            if (i < inventory.getItems().size()) {
                // On récupère l'item à afficher à l'index i de la liste d'items de l'inventaire
                Item item = (Item) inventory.getItems().get(i);

                // Si la quantité de l'item est négative, ne pas l'afficher
                if (item.getQuantity() < 0) {
                    gridInventory.add(new JLabel());
                    continue;
                }

                // On crée un bouton pour afficher l'item, avec la quantité de l'item affichée sur le bouton (ex: "x3" pour 3 items)
                JButton itemButton = new JButton("x" + item.getQuantity());
                // Controleur du bouton
                if (item.getQuantity() > 0) {
                    itemButton.addActionListener(new InventorySelector(display, expectedItemType, item, builder));
                } else {
                    // quantité == 0 : rendre le bouton inactif pour éviter les clics
                    itemButton.setEnabled(false);
                }

                // Définir icône si disponible
                if (item.getImage() != null) {
                    itemButton.setIcon(new ImageIcon(item.getImage().getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH)));
                }

                // L'icon et le texte ne sont pas surlignés quand on clique sur le bouton
                itemButton.setFocusable(false);

                // On centre le texte du bouton et on place le texte en dessous de l'icon
                itemButton.setHorizontalTextPosition(SwingConstants.CENTER); // On centre le texte du bouton
                itemButton.setVerticalTextPosition(SwingConstants.BOTTOM); // On place le texte du bouton en dessous de l'icon

                // On ajoute le bouton à la grille d'inventaire
                gridInventory.add(itemButton);
            }

            // Sinon on ajoute une case vide à la grille pour remplir la grille de 5x3, même si il n'y a pas d'item à afficher
            else {
                gridInventory.add(new JLabel());
            }
        }

        // On ajoute la grille d'inventaire au centre du popup, par dessus l'image de fond }
        this.add(gridInventory, BorderLayout.CENTER);
    }

}

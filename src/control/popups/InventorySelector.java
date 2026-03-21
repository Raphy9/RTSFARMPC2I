package src.control.popups;

import src.model.Item;
import src.model.ItemSeed;
import src.model.Tile;
import src.model.actions.ActionBuilder;
import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Controleur des boutons items dans le popup inventaire
 * Prend en parametre le type d'item attendu ainsi que l'item auquel il est relie
 * Si le bouton est clique et que l'item correspond, enchaine avec l'action associee
 */
public class InventorySelector implements ActionListener {

    private Class itemType;
    private Item item;
    private Display display;
    private ActionBuilder builder;

    /** Constructeur du controleur de selection d'item dans le popup inventaire
     * @param itemType le type d'item attendu pour que le bouton soit actif (ex: ItemSeed.class pour les graines)
     * @param item l'item auquel le bouton est relie, pour verifier son type et acceder a ses proprietes
     */
    public InventorySelector(Display display, Class itemType, Item item, ActionBuilder builder) {
        this.itemType = itemType;
        this.item = item;
        this.display = display;
        this.builder = builder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Verifier que l'item correspond au type attendu pour que le bouton soit actif
        if (itemType.isInstance(item)) {
            // stocker l'item dans le builder pour pouvoir l'utiliser dans l'action finale
            builder.setItem(item);

            if (itemType == ItemSeed.class) {
                // TODO: transferer les graines dans l'inventaire du jardinier
                // Si c'est une graine, enchaine avec la selection d'une case plantable
                display.switchToSelection(Tile::isFarmable, "Selectionner une case plantable", builder);    // critere de selection : case plantable (farmable)
            }
            System.out.println("Item selectionne: " + item);
        }
    }
}

package src.control.popups;

import src.model.Item;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Controleur des boutons items dans le popup inventaire
 * Prend en parametre le type d'item attendu ainsi que l'item auquel il est relie
 * Si le bouton est clique et que l'item correspond, enchaine avec l'action associee
 */
public class InventorySelector implements ActionListener {

    private Class itemType;
    private Item item;

    /** Constructeur du controleur de selection d'item dans le popup inventaire
     * @param itemType le type d'item attendu pour que le bouton soit actif (ex: ItemSeed.class pour les graines)
     * @param item l'item auquel le bouton est relie, pour verifier son type et acceder a ses proprietes
     */
    public InventorySelector(Class itemType, Item item) {
        this.itemType = itemType;
        this.item = item;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Verifier que l'item correspond au type attendu pour que le bouton soit actif
        if (itemType.isInstance(item)) {
            // TODO : enchaine avec l'action associee a l'item selectionne, par exemple planter la graine si c'est une graine
            System.out.println("Item selectionne: " + item);
            // TODO : gerer la quantite d'items
        }
    }
}

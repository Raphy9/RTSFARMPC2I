package src.control.popups;

import src.model.Inventory;
import src.model.Item;
import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controleur pour les boutons d'items dans le popup de la grange.
 * Supporte clic simple = transfert d'une unite
, shift-clic = transfert de toute la pile.
 * Exe
cute un callback apre
s transfert pour rafraîchir l'UI.
 */
public class BarnInventorySelector implements ActionListener {

    private Display display;
    private Inventory source;
    private Inventory target;
    private Item item;
    private int qty; // quantite par defaut à transferer (1 ou -1 pour tout)
    private Runnable afterTransfer; // callback pour rafraîchir le popup

    public BarnInventorySelector(Display display, Inventory source, Inventory target, Item item, int qty, Runnable afterTransfer) {
        this.display = display;
        this.source = source;
        this.target = target;
        this.item = item;
        this.qty = qty;
        this.afterTransfer = afterTransfer;
    }

    /**
     * Lorsque le bouton d'item est clique, cette methode est appelee.
     * Elle doit acheter avec clic gauche de la souris des unites de l'item
     * Elle doit vendre avec clic droit de la souris des unites de l'item
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO : implémenter l'achat (ajout) et la vente (supression) d'items dans l'inventaire
    }
}

package src.control.popups;

import src.model.Inventory;
import src.model.Item;
import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controleur pour les boutons d'items dans le popup de la grange.
 * Supporte clic simple = transfert d'une unité, shift-clic = transfert de toute la pile.
 * Exécute un callback après transfert pour rafraîchir l'UI.
 */
public class BarnInventorySelector implements ActionListener {

    private Display display;
    private Inventory source;
    private Inventory target;
    private Item item;
    private int qty; // quantité par défaut à transférer (1 ou -1 pour tout)
    private Runnable afterTransfer; // callback pour rafraîchir le popup

    public BarnInventorySelector(Display display, Inventory source, Inventory target, Item item, int qty, Runnable afterTransfer) {
        this.display = display;
        this.source = source;
        this.target = target;
        this.item = item;
        this.qty = qty;
        this.afterTransfer = afterTransfer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int amount = qty;
        if (amount == -1) {
            amount = item.getQuantity();
        }
        source.transferTo(target, item, amount);
        if (afterTransfer != null) {
            afterTransfer.run();
        }
        if (display != null) display.repaint();
    }
}

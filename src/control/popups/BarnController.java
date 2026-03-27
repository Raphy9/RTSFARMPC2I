package src.control.popups;

import src.model.Barn;
import src.model.Item;
import src.view.PopupBarn;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BarnController implements ActionListener {
    private final Barn barn;
    private final PopupBarn popupBarn;
    private final Item item;
    private final boolean isBuyAction;

    public BarnController(Barn barn, PopupBarn popupBarn, Item item, boolean isBuyAction) {
        this.barn = barn;
        this.popupBarn = popupBarn;
        this.item = item;
        this.isBuyAction = isBuyAction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Acheter = ajoute 1 graine du type correspondant.
        if (isBuyAction) {
            barn.buyItem(item, 1);
        } else {
            // Vendre = retire 1 unité de l'item affiché.
            barn.sellItem(item, 1);
        }

        // Recharger la grille pour refléter les quantités après action.
        popupBarn.refresh();
    }
}

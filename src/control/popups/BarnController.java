package src.control.popups;

import src.model.Barn;
import src.model.Item;
import src.view.PopupBarn;

import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BarnController implements ActionListener {
    private final Barn barn;
    private final PopupBarn popupBarn;
    private final Item item;
    private final boolean isBuyAction;
    private final JTextComponent quantityInput;

    public BarnController(Barn barn, PopupBarn popupBarn, Item item, boolean isBuyAction, JTextComponent quantityInput) {
        this.barn = barn;
        this.popupBarn = popupBarn;
        this.item = item;
        this.isBuyAction = isBuyAction;
        this.quantityInput = quantityInput;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int qty = parseQuantity();

        // Acheter = ajoute la quantite demandee.
        if (isBuyAction) {
            barn.buyItem(item, qty);
        } else {
            // Vendre = retire la quantite demandee de l'item affiche.
            barn.sellItem(item, qty);
        }

        // Apres validation, on vide la saisie.
        quantityInput.setText("");

        // Recharger la grille pour refléter les quantités après action.
        popupBarn.refresh();
    }

    private int parseQuantity() {
        String raw = quantityInput.getText();
        if (raw == null || raw.trim().isEmpty()) {
            return 1;
        }

        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : 1;
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}

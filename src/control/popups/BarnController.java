package src.control.popups;

import src.model.Barn;
import src.model.Item;
import src.view.PopupBarn;

import javax.swing.*;
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
        int currentMoney = barn.getMoney();

        if (isBuyAction) {
            // On vérifie d'abord si le joueur a assez d'argent
            int unitPrice = barn.buyItem(item, 0);
            if (currentMoney >= unitPrice * qty) {
                barn.buyItem(item, qty);
            } else {
                JOptionPane.showMessageDialog(popupBarn, "Vous n'avez pas assez d'argent !", "Achat impossible", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            // On vérifie d'abord si le joueur a assez de stock
            if (item.getQuantity() >= qty) {
                barn.sellItem(item, qty);
            } else {
                JOptionPane.showMessageDialog(popupBarn, "Vous n'avez pas assez de cet objet en stock !", "Vente impossible", JOptionPane.WARNING_MESSAGE);
            }
        }

        // Après validation, on remet le champ à "1" par défaut (plus agréable pour le joueur)
        quantityInput.setText("1");

        // Recharger la grille pour refléter les quantités et l'argent mis à jour
        popupBarn.refresh();
    }

    private int parseQuantity() {
        String raw = quantityInput.getText();
        if (raw == null || raw.trim().isEmpty()) {
            return 1;
        }

        try {
            int parsed = Integer.parseInt(raw.trim());
            return Math.max(parsed, 1); // Force à au moins 1 (empêche de rentrer 0 ou un nombre négatif)
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}
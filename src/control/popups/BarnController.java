package src.control.popups;

import src.model.Barn;
import src.model.Item;
import src.model.ItemPlant;
import src.model.ItemSeed;
import src.model.Quests;
import src.model.World;
import src.view.GameDialog;
import src.view.PopupBarn;

import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BarnController implements ActionListener {
    private final World world;
    private final Barn barn;
    private final PopupBarn popupBarn;
    private final Item item;
    private final boolean isBuyAction;
    private final JTextComponent quantityInput;

    public BarnController(World world, Barn barn, PopupBarn popupBarn, Item item, boolean isBuyAction, JTextComponent quantityInput) {
        this.world = world;
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
            // On verifie d'abord si le joueur a assez d'argent
            int unitPrice = barn.buyItem(item, 0);
            if (currentMoney >= unitPrice * qty) {
                barn.buyItem(item, qty);
                if (item instanceof ItemSeed && item.getPlantType() == src.model.PlantType.CITROUILLE) {
                    world.registerQuestAction(Quests.ACTION_BUY_SEED_CITROUILLE, qty);
                }
            } else {
                GameDialog.showMessage(popupBarn, "Achat impossible",
                        "Vous n'avez pas assez d'argent !\nCout : " + (unitPrice * qty) + " PO\nPortefeuille : " + currentMoney + " PO");
            }
        } else {
            // On verifie d'abord si le joueur a assez de stock
            if (item.getQuantity() >= qty) {
                barn.sellItem(item, qty);
                if (item instanceof ItemPlant) {
                    switch (item.getPlantType()) {
                        case CAROTTE -> world.registerQuestAction(Quests.ACTION_SELL_CAROTTE, qty);
                        case CHOUX -> world.registerQuestAction(Quests.ACTION_SELL_CHOUX, qty);
                        case CITROUILLE -> world.registerQuestAction(Quests.ACTION_SELL_CITROUILLE, qty);
                        case FRAISE -> world.registerQuestAction(Quests.ACTION_SELL_FRAISE, qty);
                    }
                }
            } else {
                GameDialog.showMessage(popupBarn, "Vente impossible",
                        "Vous n'avez pas assez de cet objet en stock !\nEn stock : " + item.getQuantity() + "\nQuantite demandee : " + qty);
            }
        }

        // Apres validation, on remet le champ à "1" par defaut (plus agreable pour le joueur)
        quantityInput.setText("1");

        // Recharger la grille pour refleter les quantites et l'argent mis à jour
        popupBarn.refresh();
    }

    private int parseQuantity() {
        String raw = quantityInput.getText();
        if (raw == null || raw.trim().isEmpty()) {
            return 1;
        }

        try {
            int parsed = Integer.parseInt(raw.trim());
            return Math.max(parsed, 1); // Force à au moins 1 (empeche de rentrer 0 ou un nombre negatif)
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}
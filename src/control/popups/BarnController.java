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

/**
 * Contrôleur gérant les transactions (Achat/Vente) au sein de la grange (magasin du jeu).
 * Il fait le lien entre les interactions du joueur sur la vue (boutons, champs de texte)
 * et la logique économique du modèle (argent, gestion des stocks, progression des quêtes).
 */
public class BarnController implements ActionListener {

    // Références vers les modèles pour la logique métier et les quêtes
    private final World world;
    private final Barn barn;

    // Référence vers la vue pour demander un rafraîchissement de l'affichage après transaction
    private final PopupBarn popupBarn;

    // L'objet spécifique ciblé par la transaction (ex: graine de carotte, tomate récoltée)
    private final Item item;

    // Drapeau déterminant la nature de la transaction : true pour un Achat, false pour une Vente
    private final boolean isBuyAction;

    // Référence vers le champ de texte de l'interface où le joueur a saisi la quantité désirée
    private final JTextComponent quantityInput;

    /**
     * Constructeur injectant toutes les dépendances nécessaires à une transaction.
     * Chaque bouton "Acheter" ou "Vendre" de l'interface possède sa propre instance de ce contrôleur,
     * paramétrée spécifiquement pour un objet et une action.
     */
    public BarnController(World world, Barn barn, PopupBarn popupBarn, Item item, boolean isBuyAction, JTextComponent quantityInput) {
        this.world = world;
        this.barn = barn;
        this.popupBarn = popupBarn;
        this.item = item;
        this.isBuyAction = isBuyAction;
        this.quantityInput = quantityInput;
    }

    /**
     * Cœur logique de la transaction, appelé lorsque le joueur clique sur le bouton associé.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Sécurisation et extraction de la quantité saisie par le joueur
        int qty = parseQuantity();
        int currentMoney = barn.getMoney();

        if (isBuyAction) {
            // -- LOGIQUE D'ACHAT --

            // On simule un achat de quantité 0 pour interroger le modèle sur le prix unitaire de l'objet
            int unitPrice = barn.buyItem(item, 0);

            // Vérification de solvabilité avant d'effectuer la transaction réelle
            if (currentMoney >= unitPrice * qty) {
                // Transaction validée : on débite l'argent et on ajoute l'item dans le stock du joueur
                barn.buyItem(item, qty);

                // Vérification spécifique pour l'avancement des quêtes (ex: achat de graines de citrouille)
                if (item instanceof ItemSeed && item.getPlantType() == src.model.PlantType.CITROUILLE) {
                    world.registerQuestAction(Quests.ACTION_BUY_SEED_CITROUILLE, qty);
                }
            } else {
                // Fonds insuffisants : on notifie le joueur via une boîte de dialogue d'erreur modale
                GameDialog.showMessage(popupBarn, "Achat impossible",
                        "Vous n'avez pas assez d'argent !\nCoût : " + (unitPrice * qty) + " PO\nPortefeuille : " + currentMoney + " PO");
            }

        } else {
            // -- LOGIQUE DE VENTE --

            // Vérification des stocks physiques avant d'autoriser la vente
            if (item.getQuantity() >= qty) {
                // Transaction validée : on retire l'objet de l'inventaire et on crédite le compte du joueur
                barn.sellItem(item, qty);

                // Moteur de quêtes : si le joueur vend un produit fini (récolte), on enregistre l'action
                // pour potentiellement débloquer le palier suivant d'une quête active.
                if (item instanceof ItemPlant) {
                    switch (item.getPlantType()) {
                        case CAROTTE -> world.registerQuestAction(Quests.ACTION_SELL_CAROTTE, qty);
                        case CHOUX -> world.registerQuestAction(Quests.ACTION_SELL_CHOUX, qty);
                        case CITROUILLE -> world.registerQuestAction(Quests.ACTION_SELL_CITROUILLE, qty);
                        case FRAISE -> world.registerQuestAction(Quests.ACTION_SELL_FRAISE, qty);
                    }
                }
            } else {
                // Stock insuffisant : le joueur tente de vendre plus d'objets qu'il n'en possède
                GameDialog.showMessage(popupBarn, "Vente impossible",
                        "Vous n'avez pas assez de cet objet en stock !\nEn stock : " + item.getQuantity() + "\nQuantité demandée : " + qty);
            }
        }

        // Réinitialisation de l'ergonomie (UX) : on remet le champ de texte à 1 pour faciliter la prochaine interaction
        quantityInput.setText("1");

        // On ordonne à la vue globale de la grange de se redessiner.
        // Cela mettra à jour l'affichage de l'argent total et les compteurs d'inventaire sur tous les boutons.
        popupBarn.refresh();
    }

    /**
     * Méthode utilitaire sécurisant la saisie utilisateur (Sanitization).
     * Elle convertit le texte saisi en un entier valide, gérant les erreurs de format (lettres)
     * et les saisies absurdes (valeurs négatives ou nulles).
     *
     * @return Une quantité stricte et valide, toujours supérieure ou égale à 1.
     */
    private int parseQuantity() {
        String raw = quantityInput.getText();

        // Protection contre les champs vides
        if (raw == null || raw.trim().isEmpty()) {
            return 1;
        }

        try {
            // Tentative de conversion de la chaîne de caractères en nombre entier
            int parsed = Integer.parseInt(raw.trim());

            // Règle de gestion : On interdit formellement de vendre ou d'acheter une quantité de 0 ou négative.
            // Si le joueur tape "-5", la fonction renverra 1.
            return Math.max(parsed, 1);

        } catch (NumberFormatException ex) {
            // Protection contre les caractères non numériques (ex: le joueur tape "dix")
            // On retourne la valeur par défaut pour ne pas crasher le programme.
            return 1;
        }
    }
}
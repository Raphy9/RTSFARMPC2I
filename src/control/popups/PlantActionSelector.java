package src.control.popups;

import src.model.Gardener;
import src.model.Inventory;
import src.model.ItemSeed;
import src.model.World;
import src.model.PlantType;
import src.model.Item;
import src.model.Barn;
import src.model.actions.PlantActionBuilder;
import src.view.Display;
import src.view.PopupInventory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Controleur du bouton planter dans le menu de choix d'action
 * Lance un popup d'inventaire qui affiche toutes les graines disponibles (meme celles dont
 * le jardinier n'a pas de stock mais qui sont presentes dans la grange, avec une quantite de 0 et un bouton desactive)
 * Le popup doit permettre de selectionner une graine, et une fois la graine selectionnee, il doit lancer le mode de
 * sélection de la display pour sélectionner une case à planter, avec un critere de plantation (case doit etre labourée,
 * pas d'obstacle, etc.) et une fois la case sélectionnée, il doit construire une PlantAction avec la graine sélectionnée
 * et la case sélectionnée, et l'ajouter au jardinier.
 * Note : le mode de sélection de la display doit etre lancé à partir du builder pour pouvoir construire l'action une fois la case sélectionnée
 */
public class PlantActionSelector implements ActionListener {

    // Le constructeur reçoit la display pour pouvoir lancer le mode de sélection et
    private Display display;
    private World world;
    private PlantActionBuilder builder;

    /** Le constructeur reçoit la display pour pouvoir lancer le mode de sélection et
     * builder pour construire l'action une fois la case sélectionnée
     * @param display la display pour lancer le mode de sélection
     * @param world le monde pour construire le PlantActionBuilder
     * @param gardener le jardinier pour construire le PlantActionBuilder
     */
    public PlantActionSelector(Display display, World world, Gardener gardener) {
        this.display = display;
        this.world = world;
        this.builder = new PlantActionBuilder(gardener, world);
        this.builder.setDisplay(display); // fournir la display pour le highlight
    }

    /** Lorsque le bouton est cliqué, on lance un popup d'inventaire qui affiche toutes les graines disponibles (meme celles dont
     * le jardinier n'a pas de stock mais qui sont présentes dans la grange, avec une quantité de 0 et un bouton désactivé)
     * Le popup doit permettre de sélectionner une graine, et une fois la graine sélectionnée, il doit lancer le mode de
     * sélection de la display pour sélectionner une case à planter, avec un critere de plantation (case doit etre labourée,
     * pas d'obstacle, etc.) et une fois la case sélectionnée, il doit construire une PlantAction avec la graine sélectionnée
     * et la case sélectionnée, et l'ajouter au jardinier.
     * Note : le mode de sélection de la display doit etre lancé à partir du builder pour pouvoir construire l'action une fois la case sélectionnée
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Lancement du popup de selection de graine");

        // Construire une inventory temporaire qui contient une ItemSeed pour chaque PlantType,
        // quantité = somme(gardener + barn) (peut etre 0)
        Inventory combined = new Inventory();

        Gardener gardener = builder.getGardener();
        Inventory gInv = gardener.getInventory();
        Barn barn = world.getBarn();

        // Pour chaque type de plante, compter la quantité totale dans l'inventaire du jardinier et dans la grange
        for (PlantType pt : PlantType.values()) {
            int total = 0;
            // compter dans le jardinier
            for (Item it1 : gInv.getItems()) {
                if (it1 instanceof ItemSeed) {
                    ItemSeed seed = (ItemSeed) it1;
                    if (seed.getPlantType() == pt) {
                        total += it1.getQuantity();
                    }
                }
            }
            // compter dans la grange
            for (Item it2 : barn.getItems()) {
                if (it2 instanceof ItemSeed) {
                    ItemSeed seed = (ItemSeed) it2;
                    if (seed.getPlantType() == pt) {
                        total += it2.getQuantity();
                    }
                }
            }
            // ajouter un ItemSeed meme si total == 0 pour afficher un bouton désactivé
            combined.addItem(new ItemSeed(pt, total));
        }

        // Afficher le popup en utilisant l'inventory combinée (affiche toutes les graines possibles)
        display.switchToPopup(new PopupInventory(display, combined, ItemSeed.class, builder));
    }
}

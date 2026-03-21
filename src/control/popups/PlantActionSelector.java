package src.control.popups;

import src.model.Gardener;
import src.model.Inventory;
import src.model.ItemSeed;
import src.model.World;
import src.model.actions.PlantActionBuilder;
import src.view.Display;
import src.view.PopupInventory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Controleur du bouton planter dans le menu de choix d'action
 * Lance le popup de selection de graine
 */
public class PlantActionSelector implements ActionListener {

    private Display display;
    private World world;
    private PlantActionBuilder builder;

    public PlantActionSelector(Display display, World world, Gardener gardener) {
        this.display = display;
        this.world = world;
        this.builder = new PlantActionBuilder(gardener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Lancement du popup de selection de graine");
        // Passer en popup inventaire ou on doit selectionner une graine
        display.switchToPopup(new PopupInventory(display, world.getBarn(), ItemSeed.class, builder));
    }
}

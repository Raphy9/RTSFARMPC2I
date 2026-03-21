package src.control.popups;

import src.model.Inventory;
import src.model.ItemSeed;
import src.model.World;
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

    public PlantActionSelector(Display display, World world) {
        this.display = display;
        this.world = world;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Lancement du popup de selection de graine");
        // Passer en popup inventaire ou on doit selectionner une graine
        display.switchToPopup(new PopupInventory(display, world.getBarn(), ItemSeed.class));
    }
}

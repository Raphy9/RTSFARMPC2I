package src.control.popups;

import src.model.Gardener;
import src.model.Tile;
import src.model.actions.PlowActionBuilder;
import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PlowActionSelector implements ActionListener {
    private Display display;
    private PlowActionBuilder builder;

    public PlowActionSelector(Display display, Gardener gardener) {
        this.display = display;
        this.builder = new PlowActionBuilder(gardener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Lancement du mode de sélection pour labourer");
        // Le critère : La case doit être labourable (pas déjà labourée, pas d'obstacle, etc.)
        display.switchToSelection(Tile::isPlowable, "Sélectionnez une case à labourer", builder);
    }
}

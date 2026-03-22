package src.view;

import src.control.popups.HarvestActionSelector;
import src.control.popups.PlantActionSelector;
import src.control.popups.PlowActionSelector;
import src.control.popups.WaterActionSelector;
import src.model.Gardener;
import src.model.World;
import src.model.actions.PlantActionBuilder;

import javax.swing.*;
import java.awt.*;

/** Classe des popups d'action, qui s'affichent lorsqu'on selectionne un jardinier dans la vue globale
 * Permet de donner une action au jardinier selectionne, comme semer, arroser, recolter etc
 * Comprend un bouton annuler pour fermer le popup et revenir a la vue globale
 */
public class ActionsPopup extends PopupPanel {


    public ActionsPopup(Display display, World world, Gardener gardener) {
        super(display, 250, 120, "Actions");

        JPanel actionsPanel = new JPanel(new FlowLayout());

        // Bouton labourer qui lance le mode sélection de case à labourer
        JButton plowButton = new JButton("Labourer");
        plowButton.addActionListener(new PlowActionSelector(display,gardener));
        actionsPanel.add(plowButton);

        // Bouton planter qui lance le popup de selection de graine
        JButton plantButton = new JButton("Planter");;
        plantButton.addActionListener(new PlantActionSelector(display, world, gardener));
        actionsPanel.add(plantButton);

        // Boutons d'arrosage (à implémenter)
        JButton waterButton = new JButton("Arroser");
        waterButton.addActionListener(new WaterActionSelector(display, gardener));
        actionsPanel.add(waterButton);

        // Bouton récolter qui lance le mode sélection de plante à récolter
        JButton harvestButton = new JButton("Récolter");
        harvestButton.addActionListener(new HarvestActionSelector(display, gardener));
        actionsPanel.add(harvestButton);

        this.add(actionsPanel, BorderLayout.CENTER);
    }
}

package src.view;

import src.control.popups.PlantActionSelector;

import javax.swing.*;
import java.awt.*;

/** Classe des popups d'action, qui s'affichent lorsqu'on selectionne un jardinier dans la vue globale
 * Permet de donner une action au jardinier selectionne, comme semer, arroser, recolter etc
 * Comprend un bouton annuler pour fermer le popup et revenir a la vue globale
 */
public class ActionsPopup extends PopupPanel {

    public ActionsPopup(Display display) {
        super(display, 250, 120, "Actions");

        JPanel actionsPanel = new JPanel(new FlowLayout());

        // Bouton planter qui lance le popup de selection de graine
        JButton plantButton = new JButton("Planter");
        plantButton.addActionListener(new PlantActionSelector());
        actionsPanel.add(plantButton);

        // Boutons d'arrosage et de recolte (a implementer plus tard)
        JButton waterButton = new JButton("Arroser");
        actionsPanel.add(waterButton);

        JButton harvestButton = new JButton("Recolter");
        actionsPanel.add(harvestButton);

        this.add(actionsPanel, BorderLayout.CENTER);
    }
}

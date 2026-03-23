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


    /** * Constructeur du popup d'actions, qui crée les boutons pour chaque action possible et leur associe un ActionListener correspondant
     * @param display la display pour pouvoir lancer les modes de sélection des différentes actions
     * @param world le monde pour construire les différents ActionBuilders
     * @param gardener le jardinier pour construire les différents ActionBuilders
     */
    public ActionsPopup(Display display, World world, Gardener gardener) {
        super(display, 250, 120, "Actions");

        JPanel actionsPanel = new JPanel(new FlowLayout());

        // Bouton labourer qui lance le mode sélection de case à labourer
        JButton plowButton = new JButton("Labourer");
        plowButton.addActionListener(new PlowActionSelector(display, world, gardener));
        actionsPanel.add(plowButton);

        // Bouton planter qui lance le popup de selection de graine
        JButton plantButton = new JButton("Planter");;
        plantButton.addActionListener(new PlantActionSelector(display, world, gardener));
        actionsPanel.add(plantButton);

        // Boutons d'arrosage (à implémenter)
        JButton waterButton = new JButton("Arroser");
        waterButton.addActionListener(new WaterActionSelector(display, world, gardener));
        actionsPanel.add(waterButton);

        // Bouton récolter qui lance le mode sélection de plante à récolter
        JButton harvestButton = new JButton("Récolter");
        harvestButton.addActionListener(new HarvestActionSelector(display, gardener, world));
        actionsPanel.add(harvestButton);

        this.add(actionsPanel, BorderLayout.CENTER);
    }
}

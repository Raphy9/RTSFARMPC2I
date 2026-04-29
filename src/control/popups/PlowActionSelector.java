package src.control.popups;

import src.model.Gardener;
import src.model.Tile;
import src.model.World;
import src.model.actions.PlowActionBuilder;
import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Controleur du bouton labourer dans le menu de choix d'action
 * Lance le mode de selection d'une case à labourer
 */
public class PlowActionSelector implements ActionListener {
    private Display display;
    private PlowActionBuilder builder;

    /** Le constructeur reçoit la display pour pouvoir lancer le mode de selection et
     * builder pour construire l'action une fois la case selectionnee
     * @param display la display pour lancer le mode de selection
     * @param world le monde pour construire le PlowActionBuilder
     * @param gardener le jardinier pour construire le PlowActionBuilder
     */
    public PlowActionSelector(Display display, World world, Gardener gardener) {
        this.display = display;
        this.builder = new PlowActionBuilder(gardener, world);
        this.builder.setDisplay(display);
    }

    /** Lorsque le bouton est clique, on lance le mode de selection de la display avec un critere de labourage.
     * La display doit alors permettre à l'utilisateur de cliquer sur une case valide pour labourer, et une fois la case selectionnee,
     * elle doit appeler builder.buildAction() pour construire et ajouter l'action au jardinier.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Lancement du mode de selection pour labourer");
        // Le critere : La case doit etre labourable (pas dejà labourée, pas d'obstacle, etc.)
        display.switchToSelection(Tile::isPlowable, builder.getSelectionMessage(), builder);
    }
}

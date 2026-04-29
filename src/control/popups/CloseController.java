package src.control.popups;

import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/** Controleur du bouton annuler des popups et de la vue selection
 * Permet de revenir a la vue globale au clic d'un bouton ou quand Echap est presse */
public class CloseController implements ActionListener, KeyListener {

    // Le constructeur reçoit la display pour pouvoir switcher à la vue globale
    private Display display;

    /** Le constructeur reçoit la display pour pouvoir switcher à la vue globale
     * @param display la display pour switcher à la vue globale */
    public CloseController(Display display) {
        this.display = display;
    }

    /** Lorsque le bouton est clique, on revient à la vue globale */
    @Override
    public void actionPerformed(ActionEvent e) {
        display.switchToGlobal();   // revenir a la vue globale
    }

    // Implementation de KeyListener pour permettre de fermer le popup ou la vue de selection en appuyant sur Echap
    @Override
    public void keyTyped(KeyEvent e) {

    }

    // Lorsque une touche est pressee, on verifie si c'est Echap, et si c'est le cas, on revient à la vue globale
    @Override
    public void keyPressed(KeyEvent e) {
        // Fermer si la touche echap est appuyee
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            display.switchToGlobal();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

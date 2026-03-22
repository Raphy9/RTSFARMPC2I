package src.control.popups;

import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/** Controleur du bouton annuler des popups et de la vue selection
 * Permet de revenir a la vue globale au clic d'un bouton ou quand Echap est presse */
public class CloseController implements ActionListener, KeyListener {

    private Display display;

    public CloseController(Display display) {
        this.display = display;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        display.switchToGlobal();   // revenir a la vue globale
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

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

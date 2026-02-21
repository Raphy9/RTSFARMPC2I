package src.control.popups;

import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Controleur du bouton annuler des popups */
public class ClosePopup implements ActionListener {

    private Display display;

    public ClosePopup(Display display) {
        this.display = display;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Fermeture du popup");
        display.switchToGlobal();
    }
}

package src.control;

import src.view.Display;
import src.view.Global;
import src.view.PopupPanel;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

/** Classe qui gère les interactions de l'utilisateur avec la vue globale,
 * comme les clics de souris pour sélectionner des cases ou des entités */
public class GlobalController implements MouseListener{

    private Display display;

    public GlobalController(Display display, Global globalView) {
        globalView.addMouseListener(this);
        this.display = display;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println("Clic!");
        // TEMPORAIRE : affiche un popup vide (enlever quand on implementera la vraie fonction clic)
        display.switchToPopup(new PopupPanel(display, "Popup de test"));
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
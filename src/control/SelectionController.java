package src.control;

import src.view.Display;
import src.view.Global;
import src.view.Selection;
import src.view.TextPopup;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/** Classe qui gère les interactions de l'utilisateur avec la vue selection,
 * principalement la selection de l'objet demande */
public class SelectionController implements MouseListener {

    private Display display;

    public SelectionController(Display display, Selection selectionView) {
        selectionView.addMouseListener(this);
        this.display = display;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TEMPORAIRE : revient juste vers la vue globale
        System.out.println("Clic dans la selection! " + this.toString());
        display.switchToGlobalFromSelection();
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
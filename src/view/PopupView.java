package src.view;

import javax.swing.*;
import java.awt.*;

/** Classe qui affiche les differents popups par dessus la vue globale
 * Doit toujours avoir un popup actif, si ce n'est pas le cas, changer vers la vue globale */
public class PopupView extends JPanel {

    private Global globalView;
    private PopupPanel currentPopup;

    public PopupView(Global globalView) {
        super();
        this.globalView = globalView;
        setPreferredSize(globalView.getPreferredSize());
        setLayout(null); // Layout null pour pouvoir positionner les popups manuellement
        setOpaque(false); // Rendre le panel transparent pour voir la vue globale en dessous
    }


    /** Affiche un popup par dessus la vue globale, en le centrant dans la fenetre */
    public void showPopup(PopupPanel popup) {
        hidePopup();
        currentPopup = popup;
        // Centrer le popup dans la fenetre
        int x = (getWidth() - popup.getPreferredSize().width) / 2;
        int y = (getHeight() - popup.getPreferredSize().height) / 2;
        popup.setBounds(x, y, popup.getPreferredSize().width, popup.getPreferredSize().height);

        this.add(currentPopup);
        this.revalidate();
        this.repaint();
    }

    /** Cache et supprime le popup actuellement affiche */
    public void hidePopup() {
        this.removeAll();
        currentPopup = null;
    }
}

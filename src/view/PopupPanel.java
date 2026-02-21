package src.view;

import src.control.popups.ClosePopup;

import javax.swing.*;
import java.awt.*;

/** Classe de base pour les popups d'information et les menus d'action,
 * qui s'affichent lorsqu'on selectionne une case ou une entite dans la vue globale
 * Comprend un bouton annuler pour fermer le popup et revenir a la vue globale
 */
public class PopupPanel extends JPanel {

    private String title;
    private Display display;
    private final int width = 400, height = 250;

        public PopupPanel(Display display, String title) {
            super();
            this.title = title;
            this.display = display;

            this.setLayout(new BorderLayout());
            setPreferredSize(new Dimension(width, height));

            // Panel du haut avec le bouton annuler
            JPanel topPanel = new JPanel(new BorderLayout());
            this.add(topPanel, BorderLayout.NORTH);

            // Bouton annuler pour fermer le popup
            JButton exit = new JButton("X");
            exit.addActionListener(new ClosePopup(display));
            topPanel.add(exit, BorderLayout.EAST);

            // Titre
            topPanel.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.CENTER);
        }
}

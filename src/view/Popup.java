package src.view;

import javax.swing.*;
import java.awt.*;

/** Classe de base pour les popups d'information et les menus d'action,
 * qui s'affichent lorsqu'on selectionne une case ou une entite dans la vue globale
 * Comprend un bouton annuler pour fermer le popup et revenir a la vue globale
 */
public class Popup extends JPanel {

    private String title = "Popup nul";
    private final int width = 400, height = 250;

        public Popup() {
            super();
            this.setLayout(new BorderLayout());
            setPreferredSize(new Dimension(width, height));

            // Panel du haut avec le bouton annuler
            JPanel topPanel = new JPanel(new BorderLayout());
            this.add(topPanel, BorderLayout.NORTH);

            // Bouton annuler pour fermer le popup
            JButton exit = new JButton("X");
            topPanel.add(exit, BorderLayout.EAST);

            // Titre
            topPanel.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.CENTER);
        }
}

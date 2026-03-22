package src.view;

import src.control.popups.CloseController;

import javax.swing.*;
import java.awt.*;

/** Classe de base pour les popups d'information et les menus d'action,
 * qui s'affichent lorsqu'on selectionne une case ou une entite dans la vue globale
 * Comprend un bouton annuler pour fermer le popup et revenir a la vue globale
 */
public class PopupPanel extends JPanel {

    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private String title;
    protected final int width, height;

        public PopupPanel(Display display, int width, int height, String title) {
            super();
            this.title = title;
            this.width = width;
            this.height = height;

            this.setLayout(new BorderLayout());
            setPreferredSize(new Dimension(width, height));
            setFocusable(true);

            // Panel du haut avec le bouton annuler
            JPanel topPanel = new JPanel(new BorderLayout());
            this.add(topPanel, BorderLayout.NORTH);

            // Bouton annuler pour fermer le popup
            JButton exit = new JButton("X");
            exit.setFocusable(false);
            CloseController closeController = new CloseController(display);
            exit.addActionListener(closeController);   // pour pouvoir fermer le popup au clic du bouton
            this.addKeyListener(closeController);   // pour pouvoir fermer le popup avec Echap
            topPanel.add(exit, BorderLayout.EAST);

            // Titre
            JLabel t = new JLabel(title, SwingConstants.CENTER);
            t.setFont(TITLE_FONT);
            topPanel.add(t, BorderLayout.CENTER);
        }
}

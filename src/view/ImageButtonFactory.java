package src.view;

import javax.swing.*;
import java.awt.*;

public class ImageButtonFactory {

    public static JButton createImageButton(String idlePath, String hoverPath, String pressedPath) {
        // On charge les images en memoire
        final ImageIcon idleIcon = new ImageIcon(idlePath);
        final ImageIcon hoverIcon = hoverPath != null ? new ImageIcon(hoverPath) : null;
        final ImageIcon pressedIcon = pressedPath != null ? new ImageIcon(pressedPath) : null;

        // On cree un bouton personnalise qui redefinit son propre dessin
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Image imgToDraw = idleIcon.getImage();

                // On choisit l'image selon l'etat de la souris
                if (getModel().isPressed() && pressedIcon != null) {
                    imgToDraw = pressedIcon.getImage();
                } else if (getModel().isRollover() && hoverIcon != null) {
                    imgToDraw = hoverIcon.getImage();
                }

                // LE SECRET EST ICI : On dessine l'image en forçant la taille a getWidth() et getHeight()
                if (imgToDraw != null) {
                    g.drawImage(imgToDraw, 0, 0, getWidth(), getHeight(), this);
                }

                // On appelle le super pour dessiner le texte par-dessus si tu en as mis un
                super.paintComponent(g);
            }
        };

        // --- Configuration pour rendre le bouton "invisible" ---
        button.setBorderPainted(false);
        button.setContentAreaFilled(false); // Retire le fond gris
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setFocusable(false); // Protege le focus de la camera

        // Optionnel : Retire le texte par defaut si tu n'utilises que l'image
        button.setText("");

        return button;
    }
}
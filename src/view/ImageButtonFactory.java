package src.view;

import javax.swing.*;
import java.awt.*;

/**
 * Factory utilitaire pour créer des boutons basés sur des images.
 * Gère les états visuels (repos, survol, clic) en redéfinissant le rendu Swing.
 */
public class ImageButtonFactory {

    /**
     * Crée un JButton personnalisé qui affiche des images différentes selon l'interaction.
     *
     * @param idlePath    Chemin vers l'image au repos (état normal).
     * @param hoverPath   Chemin vers l'image lors du survol par la souris (peut être null).
     * @param pressedPath Chemin vers l'image lors du clic (peut être null).
     * @return Un JButton configuré pour le rendu d'images.
     */
    public static JButton createImageButton(String idlePath, String hoverPath, String pressedPath) {
        // Chargement des icônes en mémoire depuis les fichiers assets
        final ImageIcon idleIcon = new ImageIcon(idlePath);
        final ImageIcon hoverIcon = hoverPath != null ? new ImageIcon(hoverPath) : null;
        final ImageIcon pressedIcon = pressedPath != null ? new ImageIcon(pressedPath) : null;

        // Création d'une classe anonyme étendant JButton pour surcharger le dessin (rendering)
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                // Par défaut, on prépare l'image "repos"
                Image imgToDraw = idleIcon.getImage();

                // Logique de changement d'image basée sur l'état du bouton (ButtonModel)
                if (getModel().isPressed() && pressedIcon != null) {
                    // État : Clic enfoncé
                    imgToDraw = pressedIcon.getImage();
                } else if (getModel().isRollover() && hoverIcon != null) {
                    // État : Souris sur le bouton (survol)
                    imgToDraw = hoverIcon.getImage();
                }

                // LE SECRET DU RENDU :
                // On dessine l'image en l'étirant/réduisant pour qu'elle remplisse
                // exactement la taille actuelle du composant (getWidth/getHeight).
                if (imgToDraw != null) {
                    g.drawImage(imgToDraw, 0, 0, getWidth(), getHeight(), this);
                }

                // Appelle la méthode parente pour permettre le dessin du texte (setForeground)
                // ou d'autres effets standards par-dessus l'image.
                super.paintComponent(g);
            }
        };

        // --- CONFIGURATION DU BOUTON POUR LE STYLE "PIXEL ART / HUD" ---

        // Empêche Swing de dessiner la bordure rectangulaire standard
        button.setBorderPainted(false);

        // Empêche Swing de dessiner le rectangle gris par défaut derrière l'image
        button.setContentAreaFilled(false);

        // Empêche de dessiner le petit liseré pointillé de focus (souvent moche sur des boutons images)
        button.setFocusPainted(false);

        // Rend le composant transparent (pour ne pas masquer ce qu'il y a derrière les bords de l'image)
        button.setOpaque(false);

        // Empêche le bouton de prendre le focus clavier, évitant ainsi d'intercepter
        // les commandes de la Caméra ou du Jardinier.
        button.setFocusable(false);

        // Assure que le bouton n'a pas de texte résiduel qui viendrait polluer l'image
        button.setText("");

        return button;
    }
}
package src.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class SpriteUtils {

    /** Rend l'image parfaitement carrée (sans l'écraser) et enlève le fond noir */
    public static ImageIcon processFenceImage(String path) {
        try {
            BufferedImage original = ImageIO.read(new File(path));
            // Trouve la plus grande dimension pour créer un carré parfait
            int maxDim = Math.max(original.getWidth(), original.getHeight());

            // Crée une image carrée vide (transparente)
            BufferedImage squared = new BufferedImage(maxDim, maxDim, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = squared.createGraphics();

            // On prend la couleur du tout premier pixel (en haut à gauche) comme référence pour le fond (noir)
            int bgColor = original.getRGB(0, 0);

            // On supprime ce fond noir pour le rendre transparent
            for (int x = 0; x < original.getWidth(); x++) {
                for (int y = 0; y < original.getHeight(); y++) {
                    if (original.getRGB(x, y) == bgColor) {
                        original.setRGB(x, y, 0x00000000); // Pixel 100% transparent
                    }
                }
            }

            // Dessine l'image originale bien au CENTRE du carré
            int offsetX = (maxDim - original.getWidth()) / 2;
            int offsetY = (maxDim - original.getHeight()) / 2;
            g2d.drawImage(original, offsetX, offsetY, null);
            g2d.dispose();

            return new ImageIcon(squared);
        } catch (Exception e) {
            System.err.println("Erreur traitement image : " + path);
            return new ImageIcon(path); // Fallback en cas d'erreur
        }
    }

    /** Fait une rotation mathématique de l'image */
    public static ImageIcon rotateImageIcon(ImageIcon icon, double angleDegrees) {
        Image img = icon.getImage();
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();

        BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bimg.createGraphics();

        // On tourne l'image depuis son centre
        g2d.rotate(Math.toRadians(angleDegrees), w / 2.0, h / 2.0);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return new ImageIcon(bimg);
    }
}
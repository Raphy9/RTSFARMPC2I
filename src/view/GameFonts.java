package src.view;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;

public class GameFonts {
    public static Font MINECRAFT_FONT;

    public static void loadFonts() {
        try {
            // Chargement du fichier ttf
            MINECRAFT_FONT = Font.createFont(Font.TRUETYPE_FONT, new File("src/assets/Minecraft.ttf"));
            // Enregistrement dans l'environnement graphique
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(MINECRAFT_FONT);
        } catch (IOException | FontFormatException e) {
            System.err.println("Erreur : Impossible de charger Minecraft.ttf. Utilisation de la police par défaut.");
            MINECRAFT_FONT = new Font("Arial", Font.PLAIN, 14);
        }
    }

    /** Applique la police à TOUS les composants Swing par défaut */
    public static void applyGlobalFont(Font font) {
        FontUIResource resource = new FontUIResource(font);
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, resource);
            }
        }
    }
}
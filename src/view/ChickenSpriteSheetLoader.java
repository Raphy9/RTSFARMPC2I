package src.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import src.model.Entity;

/**
 * Classe specifique pour charger et decouper les sprites de la poule.
 * Contrairement au jardinier qui a une grille, la poule utilise 6 fichiers separes de 4 frames horizontales.
 */
public class ChickenSpriteSheetLoader {

    // Structure : Action, Direction, FrameIndex
    // Actions : 0=IDLE, 1=RUN, 2=EATING
    // Directions : 0=LEFT, 1=RIGHT
    private BufferedImage[][][] animations;
    private final int nbFrames = 4; // Car chaque fichier de poule n'a que 4 frames

    // Constructeur : charge tous les sprites des l'instanciation
    public ChickenSpriteSheetLoader() {
        animations = new BufferedImage[3][2][nbFrames];
        loadAllSheets();
    }

    // Charge tous les fichiers de sprites et les decoupe en frames
    private void loadAllSheets() {
        try {
            // Idle : la poule se repose
            animations[0][0] = parseHorizontalSheet("src/assets/chicken/chicken_idle_left-Sheet.png");
            animations[0][1] = parseHorizontalSheet("src/assets/chicken/chicken_idle_right-Sheet-Sheet.png");

            //Run : la poule court
            animations[1][0] = parseHorizontalSheet("src/assets/chicken/chicken_run_left-Sheet.png");
            animations[1][1] = parseHorizontalSheet("src/assets/chicken/chicken_run_right-Sheet.png");

            // eating : la poule mange les plantes
            animations[2][0] = parseHorizontalSheet("src/assets/chicken/chicken_eating_left-Sheet.png");
            animations[2][1] = parseHorizontalSheet("src/assets/chicken/chicken_eating_right-Sheet.png");

        } catch (IOException e) {
            System.err.println("Erreur critique : Impossible de charger les sprites de la poule.");
            e.printStackTrace();
        }
    }

    /** Decoupe une bande horizontale d'image en 4 frames carrees */
    private BufferedImage[] parseHorizontalSheet(String path) throws IOException {
        BufferedImage sheet = ImageIO.read(new File(path));
        BufferedImage[] frames = new BufferedImage[nbFrames];

        // On suppose que la hauteur de l'image correspond a la taille d'une frame carrée
        int frameSize = sheet.getHeight();

        for (int i = 0; i < nbFrames; i++) {
            // Cut(x, y, width, height)
            frames[i] = sheet.getSubimage(i * frameSize, 0, frameSize, frameSize);
        }
        return frames;
    }

    /**
     * Récupere la bonne image d'animation.
     * @param stateAction L'état (src.model.Chicken.State) converti en int (0,1,2)
     * @param direction La direction (Entity.LEFT ou RIGHT)
     * @param frameIndex L'index de la frame (0-3)
     */
    public BufferedImage getFrame(int stateAction, int direction, int frameIndex) {
        // Conversion de Entity.LEFT(2)/RIGHT(3) vers 0/1 pour le tableau
        int dirIndex = (direction == Entity.RIGHT) ? 1 : 0;

        // Sécurité si l'état ou la direction est invalide (ex: UP/DOWN non gérés ici)
        if (stateAction < 0 || stateAction >= 3) stateAction = 0; // Défaut Idle

        return animations[stateAction][dirIndex][frameIndex % nbFrames];
    }
}
package src.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

import src.model.Entity; // Pour utiliser Entity.LEFT et Entity.RIGHT

/**
 * Chargeur de spritesheet pour le nouvel ennemi "Corbeau".
 * Gère les états IDLE, FLYING, EATING et le miroir automatique pour les directions.
 */
public class CrowSpriteSheetLoader {

    private static final String PATH_IDLE = "src/assets/crow/Crow_idle.png";
    private static final String PATH_FLYING = "src/assets/crow/Crow_flying.png";
    private static final String PATH_EATING = "src/assets/crow/Crow_eating.png";

    // --- Configuration des spritesheets ---
    private static final int NB_FRAMES = 4; // 4 images d'animation par ligne

    // --- Stockage des sprites découpés ---
    // [Action (0=Idle, 1=Flying/Fleeing, 2=Eating)][Direction (0=RIGHT, 1=LEFT)][Frame (0-3)]
    private BufferedImage[][][] frames;

    public CrowSpriteSheetLoader() {
        // Initialisation de la matrice
        frames = new BufferedImage[3][2][NB_FRAMES];
        loadAndProcessSprites();
    }

    /**
     * Charge les images, les découpe et crée les versions miroir (LEFT).
     */
    private void loadAndProcessSprites() {
        try {
            System.out.println("Chargement des sprites du corbeau...");

            // 1. Charger et découper l'action IDLE (Action index 0)
            BufferedImage idleSheet = ImageIO.read(new File(PATH_IDLE));
            processSheet(idleSheet, 0);

            // 2. Charger et découper l'action FLYING/FLEEING (Action index 1)
            BufferedImage flyingSheet = ImageIO.read(new File(PATH_FLYING));
            processSheet(flyingSheet, 1);

            // 3. Charger et découper l'action EATING (Action index 2)
            BufferedImage eatingSheet = ImageIO.read(new File(PATH_EATING));
            processSheet(eatingSheet, 2);

            System.out.println("Sprites du corbeau chargés avec succès !");

        } catch (Exception e) {
            System.err.println("ERREUR : Impossible de charger les sprites du corbeau.");
            System.err.println("Vérifie les chemins d'accès : " + PATH_IDLE + ", " + PATH_FLYING + ", " + PATH_EATING);
            e.printStackTrace();
            // Création de sprites de secours vides pour éviter les crashs
            createPlaceholderSprites();
        }
    }

    /**
     * Découpe une spritesheet d'une seule ligne et génère les versions miroir.
     */
    private void processSheet(BufferedImage sheet, int actionIndex) {
        if (sheet == null) return;

        // Calcul de la largeur d'une frame (on assume que la feuille est une seule ligne de 4 images)
        int frameWidth = sheet.getWidth() / NB_FRAMES;
        int frameHeight = sheet.getHeight();

        for (int f = 0; f < NB_FRAMES; f++) {
            // Découpage de la frame originale (regarde à DROITE par défaut si c'est comme tes autres assets)
            BufferedImage originalFrame = sheet.getSubimage(f * frameWidth, 0, frameWidth, frameHeight);

            // Stockage pour la direction DROITE
            frames[actionIndex][0][f] = originalFrame;

            // Création et stockage de la version miroir pour la direction GAUCHE
            frames[actionIndex][1][f] = createFlippedImage(originalFrame);
        }
    }

    /**
     * Utilitaire pour retourner horizontalement (miroir) une image.
     */
    private BufferedImage createFlippedImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage flipped = new BufferedImage(w, h, image.getType());
        Graphics2D g2d = flipped.createGraphics();
        // Dessine l'image en inversant les coordonnées X (miroir)
        g2d.drawImage(image, w, 0, 0, h, 0, 0, w, h, null);
        g2d.dispose();
        return flipped;
    }

    /**
     * Crée des images colorées de secours si le chargement échoue.
     */
    private void createPlaceholderSprites() {
        for (int a = 0; a < 3; a++) {
            Color c = (a == 0) ? Color.GRAY : (a == 1) ? Color.CYAN : Color.YELLOW;
            for (int d = 0; d < 2; d++) {
                for (int f = 0; f < NB_FRAMES; f++) {
                    frames[a][d][f] = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = frames[a][d][f].createGraphics();
                    g.setColor(c);
                    g.fillRect(0, 0, 24, 24);
                    g.dispose();
                }
            }
        }
    }

    /**
     * Récupère la frame exacte pour dessiner le corbeau.
     * * @param actionIndex Index de l'action : 0=IDLE/LANDED, 1=FLYING, 2=EATING.
     * (Correspondant à crow.getCurrentStateActionIndex())
     * @param direction   Direction de l'entité (Entity.RIGHT ou Entity.LEFT).
     * @param frameIndex   Frame actuelle de l'animation (0 à 3).
     * @return L'image (BufferedImage) à dessiner.
     */
    public BufferedImage getFrame(int actionIndex, int direction, int frameIndex) {
        // Validation des indices pour éviter les erreurs
        if (actionIndex < 0 || actionIndex >= 3) actionIndex = 0;
        if (frameIndex < 0 || frameIndex >= NB_FRAMES) frameIndex = 0;

        // Convertit Entity.RIGHT (souvent 1) et Entity.LEFT (souvent -1) en indices 0 et 1
        int dirIndex = (direction == Entity.LEFT) ? 1 : 0;

        return frames[actionIndex][dirIndex][frameIndex];
    }
}
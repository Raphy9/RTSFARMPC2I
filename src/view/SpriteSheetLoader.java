package src.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SpriteSheetLoader {

    // Dimensions exactes calculees a partir de votre image 192x72
    private static final int FRAME_WIDTH = 24;
    private static final int FRAME_HEIGHT = 24;
    private static final int NB_ROWS = 3;
    private static final int FRAMES_PER_ANIM = 4; // 4 images pour l'attente, 4 pour la marche

    // Tableaux pour stocker : [Direction (Ligne)][Frame]
    private BufferedImage[][] idleSprites;
    private BufferedImage[][] walkSprites;

    public SpriteSheetLoader(String path) {
        try {
            BufferedImage sheet = ImageIO.read(new File(path));

            idleSprites = new BufferedImage[NB_ROWS][FRAMES_PER_ANIM];
            walkSprites = new BufferedImage[NB_ROWS][FRAMES_PER_ANIM];

            for (int row = 0; row < NB_ROWS; row++) {
                for (int col = 0; col < 8; col++) {
                    BufferedImage frame = sheet.getSubimage(
                            col * FRAME_WIDTH,
                            row * FRAME_HEIGHT,
                            FRAME_WIDTH,
                            FRAME_HEIGHT
                    );

                    if (col < 4) {
                        // Les 4 premieres colonnes sont pour l'IDLE (Attente)
                        idleSprites[row][col] = frame;
                    } else {
                        // Les 4 dernieres colonnes sont pour le WALK (Marche)
                        walkSprites[row][col - 4] = frame;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de chargement de l'image : " + path);
            e.printStackTrace();
        }
    }

    /**
     * Convertit la direction de l'Entite (0=Bas, 1=Gauche, 2=Droite, 3=Haut)
     * en numero de ligne dans l'image (0=Bas, 1=Profil, 2=Haut).
     */
    private int getRowIndex(int direction) {
        if (direction == src.model.Entity.UP) return 2;
        if (direction == src.model.Entity.LEFT || direction == src.model.Entity.RIGHT) return 1;
        if (direction == src.model.Entity.DOWN) return 2;
        return 0; //
    }

    public BufferedImage getIdleFrame(int direction, int frameIndex) {
        return idleSprites[getRowIndex(direction)][frameIndex % FRAMES_PER_ANIM];
    }

    public BufferedImage getWalkFrame(int direction, int frameIndex) {
        return walkSprites[getRowIndex(direction)][frameIndex % FRAMES_PER_ANIM];
    }

    public int getNbFrames() {
        return FRAMES_PER_ANIM;
    }
}
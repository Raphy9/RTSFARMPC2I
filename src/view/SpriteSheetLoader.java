package src.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Gestionnaire de découpage de planches de sprites (SpriteSheets).
 * Cette classe permet d'extraire des animations spécifiques à partir d'un fichier image unique,
 * facilitant la gestion des mouvements du personnage (Idle et Walk).
 */
public class SpriteSheetLoader {

    // --- Configuration des dimensions (basée sur une grille 24x24) ---
    private static final int FRAME_WIDTH = 24;  // Largeur d'une vignette
    private static final int FRAME_HEIGHT = 24; // Hauteur d'une vignette
    private static final int NB_ROWS = 3;       // Nombre de lignes (Bas, Profil, Haut)
    private static final int FRAMES_PER_ANIM = 4; // Nombre d'images par cycle d'animation

    // Tableaux à deux dimensions : [Ligne de direction][Index de l'image]
    private BufferedImage[][] idleSprites; // Stocke les images d'attente
    private BufferedImage[][] walkSprites; // Stocke les images de marche

    /**
     * Constructeur : charge l'image et procède au découpage automatique.
     * @param path Chemin vers le fichier image (ex: .png).
     */
    public SpriteSheetLoader(String path) {
        try {
            // Lecture du fichier image source
            BufferedImage sheet = ImageIO.read(new File(path));

            // Initialisation des tableaux de stockage
            idleSprites = new BufferedImage[NB_ROWS][FRAMES_PER_ANIM];
            walkSprites = new BufferedImage[NB_ROWS][FRAMES_PER_ANIM];

            // Double boucle pour parcourir la grille de l'image
            for (int row = 0; row < NB_ROWS; row++) {
                for (int col = 0; col < 8; col++) { // 8 colonnes au total (4 idle + 4 walk)

                    // Extraction d'une sous-image (une seule frame)
                    BufferedImage frame = sheet.getSubimage(
                            col * FRAME_WIDTH,
                            row * FRAME_HEIGHT,
                            FRAME_WIDTH,
                            FRAME_HEIGHT
                    );

                    // Répartition selon la colonne
                    if (col < 4) {
                        // Colonnes 0 à 3 : Animation d'attente (Idle)
                        idleSprites[row][col] = frame;
                    } else {
                        // Colonnes 4 à 7 : Animation de marche (Walk)
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
     * Mappe la direction logique de l'Entité vers l'index réel de la ligne dans l'image.
     * @param direction Constante de direction (UP, DOWN, LEFT, RIGHT).
     * @return L'index de la ligne (0, 1 ou 2).
     */
    private int getRowIndex(int direction) {
        // Ligne 0 : Bas
        // Ligne 1 : Profil (Gauche/Droite via effet miroir dans Global)
        // Ligne 2 : Haut
        if (direction == src.model.Entity.UP) return 2;
        if (direction == src.model.Entity.LEFT || direction == src.model.Entity.RIGHT) return 1;
        if (direction == src.model.Entity.DOWN) return 0; // Correction logique ici
        return 0;
    }

    /**
     * Récupère la frame correspondante pour l'état "immobile".
     * @param direction Direction du regard.
     * @param frameIndex Index de l'animation (généralement calculé avec le temps).
     */
    public BufferedImage getIdleFrame(int direction, int frameIndex) {
        return idleSprites[getRowIndex(direction)][frameIndex % FRAMES_PER_ANIM];
    }

    /**
     * Récupère la frame correspondante pour l'état "en mouvement".
     * @param direction Direction du déplacement.
     * @param frameIndex Index de l'animation.
     */
    public BufferedImage getWalkFrame(int direction, int frameIndex) {
        return walkSprites[getRowIndex(direction)][frameIndex % FRAMES_PER_ANIM];
    }

    /** Retourne le nombre d'images maximum par animation (ici 4) */
    public int getNbFrames() {
        return FRAMES_PER_ANIM;
    }
}
package src.model;

import src.view.Display;
import java.awt.*;

/**
 * La classe Camera définit la zone visible du monde de jeu.
 * Elle permet de gérer le défilement (scrolling) et la conversion de coordonnées.
 */
public class Camera {

    /** Largeur de la vue (fenêtre) en nombre de tuiles. */
    public static final int WIDTH = 22;

    /** Hauteur de la vue (fenêtre) en nombre de tuiles. */
    public static final int HEIGHT = 13;

    // Position flottante pour permettre des mouvements fluides (sub-tuile)
    private float x;
    private float y;

    /**
     * Constructeur par défaut.
     * Initialise la caméra au centre du monde.
     */
    public Camera() {
        // Le calcul inclut un décalage de +0.7 pour ajuster visuellement le centrage
        this.x = (float) ((float)World.WIDTH/2 - (float) WIDTH/2 + 0.7);
        this.y = (float) ((float) World.HEIGHT /2 - (float) HEIGHT/2 + 0.7);
    }

    /**
     * Constructeur avec position spécifique.
     * @param x Position X initiale.
     * @param y Position Y initiale.
     */
    public Camera(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    /**
     * Déplace la caméra selon un vecteur de changement (delta).
     * Inclut une validation des bordures (clamping) pour éviter de voir le "vide"
     * en dehors des limites du monde.
     *
     * @param deltaX Déplacement horizontal.
     * @param deltaY Déplacement vertical.
     */
    public void move(float deltaX, float deltaY) {
        // Vérification des limites horizontales
        if (this.x + deltaX >= 0 && this.x + deltaX < World.WIDTH - WIDTH) {
            this.x += deltaX;
        }
        // Vérification des limites verticales
        if (this.y + deltaY >= 0 && this.y + deltaY < World.HEIGHT - HEIGHT) {
            this.y += deltaY;
        }
    }

    /**
     * Convertit une position en pixels (clic de souris) en position de tuile dans le monde.
     * Essentiel pour savoir sur quel légume le joueur a cliqué.
     *
     * @param screenX Position X en pixels sur la fenêtre.
     * @param screenY Position Y en pixels sur la fenêtre.
     * @return Un objet Point contenant les coordonnées (int) de la tuile visée.
     */
    public Point screenToWorld(float screenX, float screenY) {
        // On ajoute la position de la caméra au ratio de pixels par tuile
        float worldX = this.x + (screenX / Display.RATIO_X);
        float worldY = this.y + (screenY / Display.RATIO_Y);

        // Retourne les coordonnées entières (tronquage vers la tuile la plus proche)
        return new Point((int)worldX, (int)worldY);
    }
}
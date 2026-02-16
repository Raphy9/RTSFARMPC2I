package src.model;

public class Camera {
    // Constantes de la taille de la caméra (en nombre de tuiles à afficher)
    public static final int WIDTH = 28;
    public static final int HEIGHT = 18;

    // Position de la caméra dans le monde du jeu
    private float x;
    private float y;

    public Camera() {
        this.x = (float) ((float)World.WIDTH/2 - (float) WIDTH/2+0.7); // Centrer la caméra sur le monde
        this.y = (float) ((float) World.HEIGHT /2 - (float) HEIGHT/2+0.7);
    }

    public Camera(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    /**
     * Déplace la caméra en fonction des changements de position
     * Assure que la caméra ne dépasse pas les limites du monde
     * @param deltaX Le changement de position en X
     * @param deltaY Le changement de position en Y
     */
    public void move(float deltaX, float deltaY) {
        if (this.x + deltaX > 0 && this.x + deltaX < World.WIDTH - WIDTH) {
            this.x += deltaX; // Empêche de dépasser les bords gauche et droit
        }
        if (this.y + deltaY > 0 && this.y + deltaY < World.HEIGHT - HEIGHT) {
            this.y += deltaY; // Empêche de dépasser les bords haut et bas
        }
    }
}

package src.model;

public class Camera {
    // Constantes de la taille de la caméra (en nombre de tuiles à afficher)
    public static final int WIDTH = 30;
    public static final int HEIGHT = 20;

    // Position de la caméra dans le monde du jeu
    private int x;
    private int y;

    public Camera() {
        this.x = 0;
        this.y = 0;
    }

    public Camera(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Déplace la caméra en fonction des changements de position
     * @param deltaX Le changement de position en X
     * @param deltaY Le changement de position en Y
     */
    public void move(int deltaX, int deltaY) {
        this.x += deltaX;
        this.y += deltaY;
    }
}

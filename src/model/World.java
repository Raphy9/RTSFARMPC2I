package src.model;

/**
 * La classe World représente le monde du jeu, qui est une grille de tiles.
 * Cette classe gère la structure du monde et les interactions avec les cases.
 */
public class World {
    // Constantes des dimensions du monde (en nombre de cases)
    public static final int WIDTH = 100;
    public static final int HEIGHT = 100;

    // Les cases du monde, représentées par une matrice de Tile
    private Tile[][] tiles;

    public World() {

    }

    /* Initialise les cases du monde, en créant une nouvelle instance de Tile pour chaque position */
    private void initializeTiles() {
        this.tiles = new Tile[HEIGHT][WIDTH];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                // this.tiles[x][y] = new Tile(...);
            }
        }
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            throw new IndexOutOfBoundsException("Position hors du monde : (" + x + ", " + y + ")");
        }
        return this.tiles[y][x];
    }

    public Tile[][] getTiles() {
        return this.tiles;
    }
}
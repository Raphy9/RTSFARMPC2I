package src.model;

import javax.swing.*;

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
        initializeTiles();
    }

    /* Initialise les cases du monde, en créant une nouvelle instance de Tile pour chaque position */
    private void initializeTiles() {
        this.tiles = new Tile[HEIGHT][WIDTH];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (y == HEIGHT/2 && x == WIDTH/2) {
                    this.tiles[x][y] = new Tile(x, y, new ImageIcon("src/assets/parcel.png"));
                } else {
                    this.tiles[x][y] = new Tile(x, y, new ImageIcon("src/assets/grass.png"));
                }
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

    public Gardener getGardenerTest() {
        return new Gardener(WIDTH/2, HEIGHT/2, this);
    }
}
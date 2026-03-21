package src.model;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * La classe World représente le monde du jeu, qui est une grille de tiles.
 */
public class World {
    public static final int WIDTH = 100;
    public static final int HEIGHT = 100;

    private Tile[][] tiles;

    // Plus besoin de tableau, on stocke juste l'unique image d'herbe choisie
    private ImageIcon grassSprite;
    private ImageIcon defaultParcel; // Image de la parcelle
    private Gardener testGardener;
    private Barn barn;

    public World() {
        loadTerrainSprites();
        initializeTiles();
        this.testGardener = new Gardener(WIDTH/2, HEIGHT/2, this);

        // Initialisation de la grange et remplissage de départ pour les tests
        barn = new Barn();
        fstSetBarn();

        Thread t = new Thread(this.testGardener);
        t.start();
    }

    /**
     * Charge et découpe la Sprite Sheet du terrain.
     */
    private void loadTerrainSprites() {
        try {
            // chargement de l'image de la Sprite Sheet
            BufferedImage sheet = ImageIO.read(new File("src/assets/Tiny Wonder Farm Free/tilemaps/spring farm tilemap.png"));

            int tileWidth = 32;
            int tileHeight = 32;

            // --- CHANGEMENT ICI ---
            // On veut la 2ème image (index 1) de la 1ère ligne (Y = 0)
            int indexColonne = 1;
            int coordX = indexColonne * tileWidth;
            int coordY = 0; // 1ère ligne

            BufferedImage subImage = sheet.getSubimage(coordX, coordY, tileWidth, tileHeight);
            grassSprite = new ImageIcon(subImage);

            // Charger aussi l'image de la parcelle labourée
            this.defaultParcel = new ImageIcon("src/assets/parcel.png");

        } catch (IOException e) {
            System.err.println("Erreur : Impossible de charger les sprites du terrain !");
            e.printStackTrace();

            // Fallback de sécurité si l'image plante
            grassSprite = new ImageIcon("src/assets/grass.png");
            this.defaultParcel = new ImageIcon("src/assets/parcel.png");
        }
    }

    /**
     * Initialise les cases du monde avec l'herbe choisie.
     */
    private void initializeTiles() {
        this.tiles = new Tile[HEIGHT][WIDTH];

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {

                // Temporaire : on met des cases plantables aléatoirement pour tester
                Random rand = new Random();
                if (rand.nextInt(10) < 1) {  // 10% de chances d'avoir une case plantable
                    this.tiles[y][x] = new CasePlantable(x, y);
                } else {
                    // Partout ailleurs : on met l'unique herbe découpée
                    this.tiles[y][x] = new Tile(x, y, grassSprite);
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
        return this.testGardener;
    }

    public Barn getBarn() {
        return this.barn;
    }

    /*
     Méthode pour remplir la grange de départ avec quelques items, à appeler dans le constructeur du monde.
     On peut la modifier pour tester différents items dans la grange.
     */
    private void fstSetBarn() {
        barn.addItem(new ItemPlant(PlantType.CAROTTE, 10));
        barn.addItem(new ItemPlant(PlantType.CHOUX, 20));
        barn.addItem(new ItemPlant(PlantType.FRAISE, 20));
        barn.addItem(new ItemSeed(PlantType.CAROTTE, 20));
        barn.addItem(new ItemSeed(PlantType.CHOUX, 20));
        barn.addItem(new ItemSeed(PlantType.FRAISE, 20));
    }
}
package src.model;

import java.util.ArrayList;

/**
 * La classe Tile représente une tuile dans le jeu.
 * Chaque tuile a des coordonnées (x, y) indiquant sa position dans le monde du jeu.
 * Chaque tuile peut contenir différents éléments (parcel, entités, etc.).
 */
public class Tile {
    private int x;
    private int y;

    ArrayList<Entity> entities;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
        entities = new ArrayList<>();
    }

    public boolean isWalkable() {
        return true;
    }

    public boolean isFarmable() {
        return false;
    }

}


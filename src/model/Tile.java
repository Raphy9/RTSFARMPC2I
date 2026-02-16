package src.model;

import javax.swing.*;
import java.util.ArrayList;

/**
 * La classe Tile représente une tuile dans le jeu.
 * Chaque tuile a des coordonnées (x, y) indiquant sa position dans le monde du jeu.
 * Chaque tuile peut contenir différents éléments (parcel, entités, etc.).
 */
public class Tile {
    private int x;
    private int y;

    private ImageIcon sprite; // L'image représentant la tuile


    ArrayList<Entity> entities;

    public Tile(int x, int y, ImageIcon s) {
        this.x = x;
        this.y = y;
        entities = new ArrayList<>();
        this.sprite = s;
    }

    public ImageIcon getSprite() {
        return sprite;
    }

    public boolean isWalkable() {
        return true;
    }

    public boolean isFarmable() {
        return false;
    }

}


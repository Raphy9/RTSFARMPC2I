package src.model;

import javax.swing.*;
import java.util.ArrayList;

/**
 * La classe Tile represente une tuile dans le jeu.
 * Chaque tuile a des coordonnees (x, y) indiquant sa position dans le monde du jeu.
 * Chaque tuile peut contenir differents elements (parcel, entites, etc.).
 */
public class Tile {
    private int x;
    private int y;

    private ImageIcon sprite; // L'image representant la tuile

    private ArrayList<Entity> entities;

    // Par defaut la tuile est franchissable
    private boolean walkable = true;
    private boolean plowable = true;

    public Tile(int x, int y, ImageIcon s) {
        this.x = x;
        this.y = y;
        entities = new ArrayList<>();
        this.sprite = s;
    }

    public ImageIcon getSprite() {
        return sprite;
    }

    /**
     * Methode pour changer le sprite de la tuile.
     * @param sprite
     */
    public void setSprite(ImageIcon sprite) {
        this.sprite = sprite;
    }

    /**
     * Methode appelee a chaque cycle de jeu.
     * Par defaut, une tuile normale ne fait rien.
     * Les enfants (comme CasePlantable) surchargeront cette méthode.
     */
    public void tick() {
        // Rien par défaut
    }

    public boolean isWalkable() {
        return walkable;
    }

    public void setWalkable(boolean walkable) {
        this.walkable = walkable;
    }

    public boolean isFarmable() {
        return false;
    }

    // Modifie ta méthode existante
    public boolean isPlowable() {
        return plowable;
    }

//    public boolean isPlantable() {return false;}

    /**
     * Vérifie si cette case contient déja une poule.
     * Utile pour gérer les collisions entre ennemis.
     */
    public boolean hasChicken() {
        // On parcourt les entités présentes sur la case
        for (Entity e : entities) {
            if (e instanceof Chicken) {
                return true;
            }
        }
        return false;
    }
    public int getX() { return x; }
    public int getY() { return y; }
    public ArrayList<Entity> getEntities() { return entities; }

    public void setPlowable(boolean plowable) {
        this.plowable = plowable;
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public void removeEntity(Entity e) {
        entities.remove(e);
    }

}

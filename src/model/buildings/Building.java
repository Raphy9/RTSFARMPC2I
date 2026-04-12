package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

public abstract class Building {
    // Enumération pour définir les règles de placement
    public enum PlacementRule {
        NORMAL_ONLY,    // Uniquement sur l'herbe normale (ex: Grange, Épouvantail)
        PLANTABLE_ONLY, // Uniquement sur la terre labourée (ex: Arroseur automatique)
        ANYWHERE        // N'importe où
    }

    protected int anchorX;
    protected int anchorY;
    protected int width;
    protected int height;
    protected boolean isPassable;
    protected PlacementRule placementRule;
    protected ImageIcon sprite;

    public Building(int width, int height, boolean isPassable, PlacementRule rule, ImageIcon sprite) {
        this.width = width;
        this.height = height;
        this.isPassable = isPassable;
        this.placementRule = rule;
        this.sprite = sprite;
    }

    public void setPosition(int x, int y) {
        this.anchorX = x;
        this.anchorY = y;
    }

    public abstract void applyEffect(World world);

    // Getters
    public int getX() { return anchorX; }
    public int getY() { return anchorY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isPassable() { return isPassable; }
    public PlacementRule getPlacementRule() { return placementRule; }
    public ImageIcon getSprite(World world, int x, int y) {
        return this.sprite; // Par défaut, un bâtiment normal garde son sprite fixe
    }
}
package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

public abstract class Building {

    public enum PlacementRule {
        NORMAL_ONLY,
        PLANTABLE_ONLY,
        ANYWHERE
    }

    protected int anchorX;
    protected int anchorY;
    protected int width;
    protected int height;
    protected boolean isPassable;
    protected PlacementRule placementRule;
    protected ImageIcon sprite;
    protected int buyPrice = 0;
    /** Niveau requis pour débloquer ce batiment dans le shop (0 = disponible des le départ) */
    protected int levelRequirement = 0;

    public Building(int width, int height, boolean isPassable, PlacementRule rule, ImageIcon sprite) {
        this.width = width;
        this.height = height;
        this.isPassable = isPassable;
        this.placementRule = rule;
        this.sprite = sprite;
    }

    public void setPosition(int x, int y) { this.anchorX = x; this.anchorY = y; }

    public abstract void applyEffect(World world);

    public int getX()                       { return anchorX; }
    public int getY()                       { return anchorY; }
    public int getWidth()                   { return width; }
    public int getHeight()                  { return height; }
    public boolean isPassable()             { return isPassable; }
    public PlacementRule getPlacementRule() { return placementRule; }
    public ImageIcon getSprite()            { return sprite; }

    /** Variante contextuelle : sous-classes peuvent surcharger pour un sprite dépendant des voisins. */
    public ImageIcon getSprite(World world, int x, int y) {
        return getSprite();
    }

    public int getBuyPrice() { return buyPrice; }
    public int getLevelRequirement() { return levelRequirement; }

    public int getSellPrice() { return (int) Math.ceil(buyPrice * 0.4); }

    /** Retourne true si ce batiment est une porte franchissable. */
    public boolean isGate() { return false; }
}
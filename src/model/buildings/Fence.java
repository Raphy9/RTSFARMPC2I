package src.model.buildings;

import src.model.World;
import src.view.SpriteUtils;
import javax.swing.ImageIcon;

public class Fence extends Building {

    private static ImageIcon baseSprite, face, side;
    private static boolean loaded = false;

    public Fence() {
        super(1, 1, false, PlacementRule.NORMAL_ONLY, null);
        this.buyPrice = 10;
        this.levelRequirement = 1;
        if (!loaded) loadSprites();
        this.sprite = baseSprite;

    }

    private static void loadSprites() {
        if (loaded) return;
        // Image de base pour la barriere de face
        baseSprite = SpriteUtils.processFenceImage("src/assets/Obstacles/fence.png");
        face = SpriteUtils.processFenceImage("src/assets/Obstacles/fence_face.png");
        side = SpriteUtils.processFenceImage("src/assets/Obstacles/fence_side.png");
        loaded = true;
    }

    // Petite méthode pour détecter si un voisin est une barriere (face ou coté)
    private boolean isFence(Building b) {
        return b instanceof Fence;
    }

    public boolean hasFenceRight(World world, int x, int y) {
        // Renvoie vrai s'il y a une autre barrière à droite
        return isFence(world.getBuildingAt(x + 1, y));
    }

    public boolean hasFenceBelow(World world, int x, int y) {
        // Renvoie vrai s'il y a une autre barrière en dessous
        return isFence(world.getBuildingAt(x, y + 1));
    }

    public ImageIcon getFaceSprite() {
        return face;
    }

    public ImageIcon getSideSprite() {
        return side;
    }

    @Override
    public ImageIcon getSprite() {
        return baseSprite;
    }

    @Override
    public void applyEffect(World world) {}

}
package src.model.buildings;

import src.model.World;
import src.view.SpriteUtils;
import javax.swing.ImageIcon;

public class FenceFace extends Building {

    private static ImageIcon baseSprite;
    private static ImageIcon cornerTR, cornerBR, cornerBL, cornerTL;
    private static boolean loaded = false;

    public FenceFace() {
        super(1, 1, false, PlacementRule.NORMAL_ONLY, null);
        this.buyPrice = 10;
        this.levelRequirement = 1;
        if (!loaded) loadSprites();
        this.sprite = baseSprite;
    }

    private static void loadSprites() {
        if (loaded) return;

        // Image de base pour la barriere de face
        baseSprite = SpriteUtils.processFenceImage("src/assets/Obstacles/fence_face.png");

        // On charge manuellement les 4 angles des barrieres
        cornerTR = SpriteUtils.processFenceImage("src/assets/Obstacles/corner_top_right.png");
        cornerBR = SpriteUtils.processFenceImage("src/assets/Obstacles/corner_bottom_right.png");
        cornerBL = SpriteUtils.processFenceImage("src/assets/Obstacles/corner_bottom_left.png");
        cornerTL = SpriteUtils.processFenceImage("src/assets/Obstacles/corner_top_left.png");

        loaded = true;
    }

    // Petite méthode pour détecter si un voisin est une barriere (face ou coté)
    private boolean isFence(Building b) {
        return b instanceof FenceFace || b instanceof FenceSide;
    }

    @Override
    public ImageIcon getSprite(World world, int x, int y) {
        if (world == null) return sprite;

        // On regarde les 4 cases adjacentes
        boolean hasN = isFence(world.getBuildingAt(x, y - 1));
        boolean hasS = isFence(world.getBuildingAt(x, y + 1));
        boolean hasE = isFence(world.getBuildingAt(x + 1, y));
        boolean hasW = isFence(world.getBuildingAt(x - 1, y));

        // Si ça forme EXCLUSIVEMENT un angle droit, on renvoie l'image d'angle pivotée
        if (hasW && hasS && !hasN && !hasE) return cornerTR; // Angle en Haut a Droite
        if (hasN && hasW && !hasS && !hasE) return cornerBR; // Angle en Bas a Droite
        if (hasN && hasE && !hasS && !hasW) return cornerBL; // Angle en Bas a Gauche
        if (hasS && hasE && !hasN && !hasW) return cornerTL; // Angle en Haut a Gauche

        // Si ce n'est pas un angle droit (ligne droite, cul-de-sac ou croisement), on reste de Face
        return baseSprite;
    }

    @Override
    public void applyEffect(World world) {}

}
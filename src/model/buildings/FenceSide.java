package src.model.buildings;

import src.model.World;
import src.view.SpriteUtils;
import javax.swing.ImageIcon;

public class FenceSide extends Building {

    private static ImageIcon baseSprite;
    private static ImageIcon cornerTR, cornerBR, cornerBL, cornerTL;
    private static boolean loaded = false;

    public FenceSide() {
        super(1, 1, false, PlacementRule.NORMAL_ONLY, null);
        this.buyPrice = 10;
        if (!loaded) loadSprites();
        this.sprite = baseSprite;
    }

    private static void loadSprites() {
        if (loaded) return;
        baseSprite = SpriteUtils.processFenceImage("src/assets/Obstacles/fence_side.png");

        cornerTR = SpriteUtils.processFenceImage("src/assets/Obstacles/fence_corner.png");
        cornerBR = SpriteUtils.rotateImageIcon(cornerTR, 90);
        cornerBL = SpriteUtils.rotateImageIcon(cornerTR, 180);
        cornerTL = SpriteUtils.rotateImageIcon(cornerTR, 270);

        loaded = true;
    }

    private boolean isFence(Building b) {
        return b instanceof FenceFace || b instanceof FenceSide;
    }

    @Override
    public ImageIcon getSprite(World world, int x, int y) {
        if (world == null) return sprite;

        boolean hasN = isFence(world.getBuildingAt(x, y - 1));
        boolean hasS = isFence(world.getBuildingAt(x, y + 1));
        boolean hasE = isFence(world.getBuildingAt(x + 1, y));
        boolean hasW = isFence(world.getBuildingAt(x - 1, y));

        // Détection d'angle droit (même logique que pour la face)
        if (hasW && hasS && !hasN && !hasE) return cornerTR;
        if (hasN && hasW && !hasS && !hasE) return cornerBR;
        if (hasN && hasE && !hasS && !hasW) return cornerBL;
        if (hasS && hasE && !hasN && !hasW) return cornerTL;

        // Sinon, on reste sur l'image de Côté
        return baseSprite;
    }

    @Override
    public void applyEffect(World world) {}
}
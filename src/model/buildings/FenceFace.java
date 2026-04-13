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
        if (!loaded) loadSprites();
        this.sprite = baseSprite;
    }

    private static void loadSprites() {
        if (loaded) return;
        baseSprite = SpriteUtils.processFenceImage("src/assets/Obstacles/fence_face.png");

        // L'image de base (Haut-Droite) a ses poutres qui partent vers la GAUCHE (Ouest) et le BAS (Sud)
        cornerTR = SpriteUtils.processFenceImage("src/assets/Obstacles/fence_corner.png");

        // On génère les autres angles mathématiquement
        cornerBR = SpriteUtils.rotateImageIcon(cornerTR, 90);  // Bas-Droite (Poutres vers Nord et Ouest)
        cornerBL = SpriteUtils.rotateImageIcon(cornerTR, 180); // Bas-Gauche (Poutres vers Nord et Est)
        cornerTL = SpriteUtils.rotateImageIcon(cornerTR, 270); // Haut-Gauche (Poutres vers Sud et Est)

        loaded = true;
    }

    // Petite méthode pour détecter si un voisin est une barrière (face ou côté)
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
        if (hasW && hasS && !hasN && !hasE) return cornerTR; // Angle en Haut à Droite
        if (hasN && hasW && !hasS && !hasE) return cornerBR; // Angle en Bas à Droite
        if (hasN && hasE && !hasS && !hasW) return cornerBL; // Angle en Bas à Gauche
        if (hasS && hasE && !hasN && !hasW) return cornerTL; // Angle en Haut à Gauche

        // Si ce n'est pas un angle droit (ligne droite, cul-de-sac ou croisement), on reste de Face
        return baseSprite;
    }

    @Override
    public void applyEffect(World world) {}
}
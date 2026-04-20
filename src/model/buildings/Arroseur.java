package src.model.buildings;

import src.model.PlantTile;
import src.model.Tile;
import src.model.World;

import javax.swing.ImageIcon;

public class Arroseur extends Building {

    private static final int RANGE = 2;

    public Arroseur() {
        super(1, 1, false, PlacementRule.PLANTABLE_ONLY, new ImageIcon("src/assets/arroseur.png"));
        this.buyPrice = 120;
        this.levelRequirement = 3;
    }

    @Override
    public void applyEffect(World world) {
        int centerX = getX();
        int centerY = getY();

        for (int dx = -RANGE; dx <= RANGE; dx++) {
            for (int dy = -RANGE; dy <= RANGE; dy++) {
                int x = centerX + dx;
                int y = centerY + dy;

                if (x < 0 || y < 0 || x >= World.WIDTH || y >= World.HEIGHT) {
                    continue;
                }

                Tile tile = world.getTile(x, y);
                if (tile instanceof PlantTile) {
                    ((PlantTile) tile).water();
                }
            }
        }
    }
}

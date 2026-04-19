package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

public class Rock1 extends Building {

    public Rock1() {
        super(1, 1, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/rock1.png"));
        this.buyPrice = 10;
    }

    @Override
    public void applyEffect(World world) {
    }
}

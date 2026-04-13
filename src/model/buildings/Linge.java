package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

public class Linge extends Building {

    public Linge() {
        super(2, 1, true, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/linge.png"));
        this.buyPrice = 120;
    }

    @Override
    public void applyEffect(World world) {
    }
}

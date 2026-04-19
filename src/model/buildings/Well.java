package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

public class Well extends Building {

    public Well() {
        super(1, 1, false, PlacementRule.NORMAL_ONLY,  new ImageIcon("src/assets/Buildings/well.png"));
        this.buyPrice = 40;
    }

    @Override
    public void applyEffect(World world) {
    }
}

package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

public class Well extends Building {

    public Well() {
        super(2, 2, false, PlacementRule.NORMAL_ONLY,  new ImageIcon("src/assets/Buildings/well.png"));
    }

    @Override
    public void applyEffect(World world) {
    }
}

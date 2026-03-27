package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

public class Tree1 extends Building {

    public Tree1() {
        super(1, 1, false, PlacementRule.NORMAL_ONLY,  new ImageIcon("src/assets/Buildings/tree1.png"));
    }

    @Override
    public void applyEffect(World world) {
    }
}
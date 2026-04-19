package src.model.buildings;

import src.model.World;

import javax.swing.*;

public class strawberrysign extends Building {

    public strawberrysign() {
        super(1, 1, true, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/strawberrysign.png"));
        this.buyPrice = 10;
    }

    @Override
    public void applyEffect(World world) {
    }
}

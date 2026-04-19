package src.model.buildings;

import src.model.World;

import javax.swing.*;

public class pumpkinsign extends Building {

    public pumpkinsign() {
        super(1, 1, true, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/pumpkinsign.png"));
        this.buyPrice = 10;
    }

    @Override
    public void applyEffect(World world) {
    }
}

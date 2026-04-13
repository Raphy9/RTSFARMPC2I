package src.model.buildings;

import src.model.World;

import javax.swing.*;

public class Barrel2 extends Building {

    public Barrel2() {
        super(1, 1, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/barrel2.png"));
        this.buyPrice = 20;
    }

    @Override
    public void applyEffect(World world) {
    }
}

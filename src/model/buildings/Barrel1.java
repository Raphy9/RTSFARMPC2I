package src.model.buildings;

import src.model.World;

import javax.swing.*;

public class Barrel1 extends Building {

    public Barrel1() {
        super(1, 1, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/barrel1.png"));
        this.buyPrice = 20;
    }

    @Override
    public void applyEffect(World world) {
    }
}
package src.model.buildings;

import src.model.World;

import javax.swing.*;

public class Tree2 extends Building {

    public Tree2() {
        super(2, 2, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/tree2.png"));
        this.buyPrice = 35;
    }

    @Override
    public void applyEffect(World world) {
    }
}
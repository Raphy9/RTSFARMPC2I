package src.model.buildings;

import src.model.World;

import javax.swing.*;

public class Bigsign extends Building {

    public Bigsign() {
        super(1, 1, true, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/bigsign.png"));
        this.buyPrice = 40;
    }

    @Override
    public void applyEffect(World world) {
    }
}
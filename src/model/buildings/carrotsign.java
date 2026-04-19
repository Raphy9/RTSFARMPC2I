package src.model.buildings;

import src.model.World;

import javax.swing.*;

public class carrotsign extends Building {

    public carrotsign() {
        super(1, 1, true, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/carrotsign.png"));
        this.buyPrice = 10;
    }

    @Override
    public void applyEffect(World world) {
    }
}

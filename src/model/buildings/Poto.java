package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

public class Poto extends Building {

    public Poto() {
        super(1, 1, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/poto.png"));
        this.buyPrice = 30;
    }

    @Override
    public void applyEffect(World world) {
    }
}
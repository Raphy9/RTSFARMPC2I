package src.model.buildings;

import src.model.World;

import javax.swing.*;

public class Path extends Building {

    public Path(){
        super(1,1, true, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/path.png    "));
         this.buyPrice = 5;
    }

    @Override
    public void applyEffect(World world) {

    }
}

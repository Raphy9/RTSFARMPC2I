package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

public class Linge extends Building {

    public Linge() {
        // Linge est un bâtiment de 3x1 cases, traversable, qui n'a pas d'effet particulier
        super(2, 1, true, PlacementRule.NORMAL_ONLY,  new ImageIcon("src/assets/Buildings/linge.png"));
    }

    @Override
    public void applyEffect(World world) {
    }
}
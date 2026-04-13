package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

public class Mailbox1 extends Building {

    public Mailbox1() {
        super(1, 1, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/mailbox1.png"));
        this.buyPrice = 50;
    }

    @Override
    public void applyEffect(World world) {
    }
}

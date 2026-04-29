package src.model.buildings;

import src.model.World;

import javax.swing.*;

public class Gate extends Fence {
    private ImageIcon gateLeft;
    private ImageIcon gateRight;

    public Gate() {
        super();
        this.buyPrice = 15;
        this.levelRequirement = 4; // Porte plus chere que la simple barriere
        gateLeft = new ImageIcon("src/assets/Obstacles/gate_left.png");
        gateRight = new ImageIcon("src/assets/Obstacles/gate_right.png");
    }

    public ImageIcon getGateLeftSprite() {
        return gateLeft;
    }
    public ImageIcon getGateRightSprite() {
        return gateRight;
    }

    @Override
    public boolean isGate() {
        return true; // C'est une porte !
    }
}
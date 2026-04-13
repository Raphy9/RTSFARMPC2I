package src.model.buildings;

public class GateSide extends FenceSide {
    public GateSide() {
        super();
        this.buyPrice = 15;
        this.levelRequirement = 4;
    }

    @Override
    public boolean isGate() {
        return true; // C'est une porte !
    }
}
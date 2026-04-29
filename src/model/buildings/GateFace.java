package src.model.buildings;

public class GateFace extends FenceFace {
    public GateFace() {
        super();
        this.buyPrice = 15;
        this.levelRequirement = 4; // Porte plus chere que la simple barriere
    }

    @Override
    public boolean isGate() {
        return true; // C'est une porte !
    }
}
package src.model.buildings;

public class GateFace extends FenceFace {
    public GateFace() {
        super();
        this.buyPrice = 15; // Porte plus chère que la simple barrière
    }

    @Override
    public boolean isGate() {
        return true; // C'est une porte !
    }
}
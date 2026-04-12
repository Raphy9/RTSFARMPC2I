package src.model.buildings;

public class GateFace extends FenceFace {
    @Override
    public boolean isGate() {
        return true; // C'est une porte !
    }
}
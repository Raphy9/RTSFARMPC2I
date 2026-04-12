package src.model.buildings;

public class GateSide extends FenceSide {
    @Override
    public boolean isGate() {
        return true; // C'est une porte !
    }
}
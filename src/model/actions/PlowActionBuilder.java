package src.model.actions;

import src.model.Gardener;
import src.model.World;
import java.awt.Point;

public class PlowActionBuilder extends ActionBuilder {
    private World world;

    public PlowActionBuilder(Gardener gardener, World world) {
        super(gardener);
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public int getPlowLimit() {
        return world.getPlowLimit();
    }

    public int getCurrentPlowedTilesCount() {
        return world.getPlowedTilesCount();
    }

    /** Message affiché en mode sélection labour. */
    public String getSelectionMessage() {
        int current = getCurrentPlowedTilesCount();
        int limit = getPlowLimit();
        int queued = getSelectedPoints().size();
        return "Ajoutez une case à labourer " + current + "/"
                + limit + " (+5 par niveau) | En attente : " + queued;
    }

    @Override
    public void buildAction() {

        for (Point p : getSelectedPoints()) {
            Point adjacent = world.findClosestWalkableAdjacent(p.x, p.y, getGardener());
            int execX = (adjacent != null) ? adjacent.x : p.x;
            int execY = (adjacent != null) ? adjacent.y : p.y;

            if (getDisplay() != null) {
                getDisplay().getGlobalView().setHighlight(p.x, p.y);
            }
            Runnable clearHighlight = () -> {
                if (getDisplay() != null) {
                    getDisplay().getGlobalView().clearHighlight(p.x, p.y);
                }
            };

            getGardener().addAction(new MoveAction(execX, execY, clearHighlight));
            getGardener().addAction(new PlowAction(execX, execY, p.x, p.y));
        }
    }
}
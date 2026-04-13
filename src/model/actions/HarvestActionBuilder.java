package src.model.actions;

import src.model.Gardener;
import src.model.World;
import java.awt.Point;

public class HarvestActionBuilder extends ActionBuilder {
    private World world;

    public HarvestActionBuilder(Gardener gardener, World world) {
        super(gardener);
        this.world = world;
    }

    @Override
    public void buildAction() {

        for (Point p : getSelectedPoints()) {
            if (getDisplay() != null) {
                getDisplay().getGlobalView().setHighlight(p.x, p.y);
            }
            Runnable clearHighlight = () -> {
                if (getDisplay() != null) {
                    getDisplay().getGlobalView().clearHighlight(p.x, p.y);
                }
            };

            Point adjacent = world.findClosestWalkableAdjacent(p.x, p.y, getGardener());
            int execX = (adjacent != null) ? adjacent.x : p.x;
            int execY = (adjacent != null) ? adjacent.y : p.y;

            getGardener().addAction(new MoveAction(execX, execY, clearHighlight));
            getGardener().addAction(new HarvestAction(p.x, p.y));
        }

        // Après avoir tout récolté, on va à la grange
        Point barnPos = world.findClosestWalkableAdjacent(world.getBarnX(), world.getBarnY(), getGardener());
        if (barnPos != null) {
            getGardener().addAction(new MoveAction(barnPos.x, barnPos.y));
            getGardener().addAction(new StoreAction(barnPos.x, barnPos.y));
        }
    }
}
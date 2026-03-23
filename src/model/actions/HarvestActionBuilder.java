package src.model.actions;

import src.model.Gardener;
import src.model.World;

import java.awt.*;

public class HarvestActionBuilder extends ActionBuilder {

    private World world;

    public HarvestActionBuilder(Gardener gardener, World world) {
        super(gardener);
        this.world = world;
    }

    @Override
    public void buildAction() {
        // Recolter à la case cible (getX(), getY())
        HarvestAction harvestAction = new HarvestAction(getX(), getY());
        // Après la récolte, on veut que le jardinier aille à la grange pour stocker les items récoltés
        Point pos = world.findClosestWalkableAdjacent(world.getBarnX(), world.getBarnY(), getGardener());
        MoveAction moveAction = new MoveAction(pos.x, pos.y);
        // Après le déplacement vers la grange, on veut que le jardinier stocke les items
        StoreAction storeAction = new StoreAction(pos.x, pos.y);

        getGardener().interruptGardener();
        getGardener().addAction(harvestAction);
        getGardener().addAction(moveAction);
        getGardener().addAction(storeAction);

        System.out.println("Ordre de récolter envoyé en (" + getX() + ", " + getY() + ") !");
    }
}
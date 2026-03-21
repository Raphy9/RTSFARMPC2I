package src.model.actions;

import src.model.Gardener;

public class HarvestActionBuilder extends ActionBuilder {

    public HarvestActionBuilder(Gardener gardener) {
        super(gardener);
    }

    @Override
    public void buildAction() {
        HarvestAction action = new HarvestAction(getX(), getY());

        getGardener().interruptGardener();
        getGardener().addAction(action);

        System.out.println("Ordre de récolter envoyé en (" + getX() + ", " + getY() + ") !");
    }
}
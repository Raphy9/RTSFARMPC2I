package src.model.actions;

import src.model.Gardener;

public class WaterActionBuilder extends ActionBuilder {

    public WaterActionBuilder(Gardener gardener) {
        super(gardener);
    }

    @Override
    public void buildAction() {
        WaterAction action = new WaterAction(getX(), getY());

        getGardener().interruptGardener();
        getGardener().addAction(action);

        System.out.println("Ordre d'arroser envoyé en (" + getX() + ", " + getY() + ") !");
    }
}

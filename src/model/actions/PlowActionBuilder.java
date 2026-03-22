package src.model.actions;

import src.model.Gardener;

public class PlowActionBuilder extends ActionBuilder {

    public PlowActionBuilder(Gardener gardener) {
        super(gardener);
    }

    @Override
    public void buildAction() {
        PlowAction action = new PlowAction(getX(), getY());

        getGardener().interruptGardener();
        getGardener().addAction(action);

        System.out.println("Ordre de labourer envoyé en (" + getX() + ", " + getY() + ") !");
    }
}

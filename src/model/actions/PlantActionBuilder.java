package src.model.actions;

import src.model.Gardener;
import src.model.ItemSeed;

public class PlantActionBuilder extends ActionBuilder {

    public PlantActionBuilder(Gardener gardener) {
        super(gardener);
    }

    @Override
    public void buildAction() {
        if (getItem() != null && getItem() instanceof src.model.ItemSeed) {
            src.model.ItemSeed seed = (src.model.ItemSeed) getItem();

            src.model.actions.PlantAction action = new src.model.actions.PlantAction(getX(), getY(), seed);

            getGardener().interruptGardener();
            getGardener().addAction(action);
            System.out.println("Ordre de planter envoyé !");
        }
    }
}
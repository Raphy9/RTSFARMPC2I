package src.model.actions;

import src.model.Gardener;

public class PlantActionBuilder extends ActionBuilder {

    public PlantActionBuilder(Gardener gardener) {
        super(gardener);
    }

    @Override
    public void buildAction() {
        // temporaire
        getGardener().addAction(new MoveAction(getX(), getY()));
        // TODO faire le vrai truc
    }
}

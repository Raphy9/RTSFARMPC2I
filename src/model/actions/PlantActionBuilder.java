package src.model.actions;

import src.model.*;
import java.awt.Point;

public class PlantActionBuilder extends ActionBuilder {
    private World world;

    public PlantActionBuilder(Gardener gardener, World world) {
        super(gardener);
        this.world = world;
    }

    @Override
    public void buildAction() {
        if (getItem() instanceof ItemSeed) {
            int totalSeedsNeeded = getSelectedPoints().size();
            PlantType type = getItem().getPlantType();

            // 1. VÉRIFICATION DES GRAINES DISPONIBLES (Inventaire + Grange)
            int available = 0;
            Item invItem = getGardener().getInventory().findSameItem(new ItemSeed(type));
            if (invItem != null) available += invItem.getQuantity();

            Item barnItem = world.getBarn().findSameItem(new ItemSeed(type));
            if (barnItem != null) available += barnItem.getQuantity();

            // S'il n'y a pas assez de graines, on annule tout AVANT d'ajouter le moindre jaune
            if (available < totalSeedsNeeded) {
                if (getDisplay() != null) {
                    getDisplay().switchToPopup(new src.view.TextPopup(getDisplay(), 350, 150, "Graines insuffisantes",
                            "<div style='text-align: center;'>Il vous manque " + (totalSeedsNeeded - available) + " graine(s) de " + type.getName() + ".<br>Achetez-en à la grange !</div>"));
                }
                getDisplay().getGlobalView().clearAllHighlights();
                return; // Annulation totale
            }

            // 2. L'ACTION EST VALIDE

            ItemSeed seedToFetch = new ItemSeed(type, totalSeedsNeeded);
            Point barnAdj = world.findClosestWalkableAdjacent(world.getBarnX(), world.getBarnY(), getGardener());
            if (barnAdj != null) {
                getGardener().addAction(new MoveAction(barnAdj.x, barnAdj.y));
                getGardener().addAction(new FetchSeedAction(barnAdj.x, barnAdj.y, seedToFetch));
            }

            // 3. Boucle de plantation
            for (Point p : getSelectedPoints()) {
                if (getDisplay() != null) {
                    getDisplay().getGlobalView().setHighlight(p.x, p.y); // Ajoute le jaune
                }
                Runnable clearHighlight = () -> {
                    if (getDisplay() != null) {
                        getDisplay().getGlobalView().clearHighlight(p.x, p.y); // Retire le jaune à l'arrivée
                    }
                };

                Point adjacent = world.findClosestWalkableAdjacent(p.x, p.y, getGardener());
                int execX = (adjacent != null) ? adjacent.x : p.x;
                int execY = (adjacent != null) ? adjacent.y : p.y;

                getGardener().addAction(new MoveAction(execX, execY, clearHighlight));
                getGardener().addAction(new PlantAction(execX, execY, p.x, p.y, type));
            }
        }
    }
}
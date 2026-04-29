package src.model.actions;

import src.model.*;
import java.awt.Point;

/**
 * Monteur (Builder) orchestrant la séquence complexe de plantation.
 * Valide les ressources (inventaire + grange) avant d'ordonner au jardinier de se déplacer.
 */
public class PlantActionBuilder extends ActionBuilder {
    private World world;

    public PlantActionBuilder(Gardener gardener, World world) {
        super(gardener);
        this.world = world;
    }

    @Override
    public void buildAction() {
        // Clause de garde : Vérification de la présence de la grange
        if (world.getBarnY() == -1 || world.getBarnX() == -1) {
            if (getDisplay() != null) {
                getDisplay().switchToPopup(new src.view.TextPopup(getDisplay(), 350, 150, "Grange manquante",
                        "<div style='text-align: center;'>Vous devez construire une grange pour planter des graines !</div>"));
            }
            return; // Annulation totale
        }

        if (getItem() instanceof ItemSeed) {
            int totalSeedsNeeded = getSelectedPoints().size();
            PlantType type = getItem().getPlantType();

            // 1. VÉRIFICATION DES GRAINES DISPONIBLES (Inventaire + Grange)
            // On calcule intelligemment ce que l'agent possède déjà sur lui + ce qu'il y a en stock.
            int available = 0;

            Item invItem = getGardener().getInventory().findSameItem(new ItemSeed(type));
            if (invItem != null) available += invItem.getQuantity();

            Item barnItem = world.getBarn().findSameItem(new ItemSeed(type));
            if (barnItem != null) available += barnItem.getQuantity();

            // S'il n'y a pas assez de graines, on annule tout AVANT d'ajouter le moindre jaune
            // Cela empêche le jardinier de commencer une tâche qu'il ne pourra pas finir.
            if (available < totalSeedsNeeded) {
                if (getDisplay() != null) {
                    getDisplay().switchToPopup(new src.view.TextPopup(getDisplay(), 350, 150, "Graines insuffisantes",
                            "<div style='text-align: center;'>Il vous manque " + (totalSeedsNeeded - available) + " graine(s) de " + type.getName() + ".<br>Achetez-en a la grange !</div>"));
                }
                return; // Annulation totale
            }

            // 2. L'ACTION EST VALIDE

            // On prévoit une étape de logistique : ordonner au jardinier d'aller piocher dans la grange.
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

                // Création d'un callback exécuté uniquement quand le jardinier arrivera sur la case
                Runnable clearHighlight = () -> {
                    if (getDisplay() != null) {
                        getDisplay().getGlobalView().clearHighlight(p.x, p.y); // Retire le jaune a l'arrivée
                    }
                };

                // On cherche une case libre à côté pour que le jardinier puisse planter sans marcher sur la terre cultivable
                Point adjacent = world.findClosestWalkableAdjacent(p.x, p.y, getGardener());
                int execX = (adjacent != null) ? adjacent.x : p.x;
                int execY = (adjacent != null) ? adjacent.y : p.y;

                // Ajout des instructions finales dans la file d'attente de l'agent
                getGardener().addAction(new MoveAction(execX, execY, clearHighlight));
                getGardener().addAction(new PlantAction(execX, execY, p.x, p.y, type));
            }
        }
    }
}
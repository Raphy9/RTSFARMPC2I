package src.model.actions;

import src.model.*;

import java.awt.Point;

/**
 * PlantActionBuilder est responsable de construire une séquence d'actions pour planter une graine à une position cible (getX(), getY())).
 * Il gère la logique de vérification de l'inventaire du jardinier, de déplacement vers la grange si nécessaire, et de plantation.
 * Il utilise également le Display pour mettre en évidence la case cible pendant le processus.
 */
public class PlantActionBuilder extends ActionBuilder {

    // Le monde est nécessaire pour trouver les tuiles adjacentes walkable (pour se déplacer avant de planter, et pour aller à la grange si besoin)
    private World world;

    /** Constructeur de PlantActionBuilder.
     * @param gardener Le jardinier qui va exécuter les actions construites par ce builder.
     * @param world Le monde dans lequel le jardinier évolue, nécessaire pour trouver les tuiles adjacentes walkable.
     */
    public PlantActionBuilder(Gardener gardener, World world) {
        super(gardener);
        this.world = world;
    }

    /**
     * Cette méthode construit la séquence d'actions pour planter une graine à la position cible (getX(), getY()).
     * La logique est la suivante :
     * 1. Vérifier que l'item sélectionné est une ItemSeed et obtenir le PlantType.
     * 2. Vérifier si le jardinier a déjà une graine de ce type dans son inventaire.
     * 3. Trouver la tuile adjacente walkable la plus proche de la cible pour se déplacer avant de planter.
     * 4. Si le jardinier a déjà la graine, planifier un MoveAction vers la tuile adjacente et ensuite un PlantAction.
     * 5. Sinon, planifier un MoveAction vers la grange, un FetchSeedAction pour récupérer la graine, puis un MoveAction vers la tuile adjacente de la cible, et enfin un PlantAction.
     * 6. Utiliser le Display pour mettre en évidence la case cible pendant le processus, et effacer le highlight une fois arrivé sur la tuile d'exécution.
     */
    @Override
    public void buildAction() {
        if (getItem() != null && getItem() instanceof src.model.ItemSeed) {
            int nbTiles = getParcel().getSize();  // nombre de cases dans la parcelle ciblée
            getGardener().interruptGardener();  // interrompre l'action en cours

            // Etape 1 : chercher les graines dans la grange

            Point barnAdj = world.findClosestWalkableAdjacent(world.getBarnX(), world.getBarnY(), getGardener());
            if (barnAdj == null) throw new RuntimeException("Aucune tuile adjacente walkable à la grange, planter est impossible !");
            // Aller a la grange
            getGardener().addAction(new MoveAction(barnAdj.x, barnAdj.y));
            // Recuperer les graines
            ItemSeed seedToFetch = new ItemSeed(getItem().getPlantType(), nbTiles);  // type et quantité de graines
            FetchSeedAction fetchAction = new FetchSeedAction(barnAdj.x, barnAdj.y, seedToFetch );
            getGardener().addAction(fetchAction);

            // Etape 2 : Planter sur chaque case de la parcelle
            for (PlantTile tile : getParcel().getTiles()) {
                // Mettre en évidence la case cible pendant le processus de planification
                getDisplay().getGlobalView().setHighlight(tile.getX(), tile.getY());
                // Préparer le callback qui efface le highlight (on efface lorsque le jardinier arrive)
                Runnable clearHighlight = null;
                clearHighlight = () -> getDisplay().getGlobalView().clearHighlight();
                // Déterminer la tuile adjacente walkable la plus proche de la cible (pour y aller avant d'agir)
                Point adjacent = world.findClosestWalkableAdjacent(tile.getX(), tile.getY(), getGardener());
                // se déplacer vers la tuile adjacente de la cible, avec le callback pour effacer le highlight une fois arrivé
                getGardener().addAction(new MoveAction(adjacent.x, adjacent.y, clearHighlight));
                // planter une graine sur la case
                getGardener().addAction(new PlantAction(adjacent.x, adjacent.y, tile.getX(), tile.getY(), getItem().getPlantType()));
            }
        }
    }
}
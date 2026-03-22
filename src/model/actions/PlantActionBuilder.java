package src.model.actions;

import src.model.Gardener;
import src.model.ItemSeed;
import src.model.Item;
import src.model.World;
import src.model.PlantType;
import src.model.Tile;
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
            src.model.ItemSeed seed = (src.model.ItemSeed) getItem();
            PlantType plantType = seed.getPlantType();

            Gardener gardener = getGardener();
            // Vérifier si le jardinier a au moins 1 graine de ce type dans son inventaire
            Item prototype = new ItemSeed(plantType);
            Item own = gardener.getInventory().findSameItem(prototype);

            // Déterminer la tuile adjacente walkable la plus proche de la cible (pour y aller avant d'agir)
            Point adjacent = world.findClosestWalkableAdjacent(getX(), getY(), gardener);

            // calculer coords d'execution
            int execX = (adjacent != null) ? adjacent.x : getX();
            int execY = (adjacent != null) ? adjacent.y : getY();

            // Préparer le callback qui efface le highlight si nécessaire (on efface le highlight de la CASE CIBLE
            // lorsque le jardinier arrive sur la case d'exécution juste avant de planter)
            Runnable clearHighlight = null;
            if (getDisplay() != null) {
                // Highlight sur la case cible (getX(), getY()) — cohérent pour tous les builders
                getDisplay().getGlobalView().setHighlight(getX(), getY());
                clearHighlight = () -> getDisplay().getGlobalView().clearHighlight();
            }

            // Si le jardinier a déjà la graine sur lui -> se déplacer à la case adjacente et planter
            if (own != null && own.getQuantity() > 0) {
                src.model.actions.PlantAction action = new src.model.actions.PlantAction(execX, execY, getX(), getY(), plantType);
                gardener.interruptGardener();
                if (clearHighlight != null) {
                    gardener.addAction(new src.model.actions.MoveAction(execX, execY, clearHighlight));
                } else {
                    gardener.addAction(new src.model.actions.MoveAction(execX, execY));
                }
                gardener.addAction(action);
                System.out.println("Ordre de planter envoyé directement (graine en poche) !");
            } else {
                // Sinon, planifier : aller près de la grange (tuile adjacente marchable), récupérer 1 graine, puis aller à la tuile adjacente de la cible et planter
                int bx = world.getBarnX();
                int by = world.getBarnY();

                // trouver la meilleure tuile adjacente qui est walkable
                Point barnAdj = world.findClosestWalkableAdjacent(bx, by, gardener);

                gardener.interruptGardener();
                if (barnAdj != null) {
                    gardener.addAction(new src.model.actions.MoveAction(barnAdj.x, barnAdj.y));
                    gardener.addAction(new src.model.actions.FetchSeedAction(barnAdj.x, barnAdj.y, new ItemSeed(plantType)));
                } else {
                    gardener.addAction(new src.model.actions.MoveAction(bx, by));
                    gardener.addAction(new src.model.actions.FetchSeedAction(bx, by, new ItemSeed(plantType)));
                }

                // Aller à la tuile adjacente de la cible, avec onArrival qui efface le highlight (sur la case cible)
                if (clearHighlight != null) {
                    gardener.addAction(new src.model.actions.MoveAction(execX, execY, clearHighlight));
                } else {
                    gardener.addAction(new src.model.actions.MoveAction(execX, execY));
                }

                // Planter (action exécutée depuis execX,execY mais plantX/Y = target)
                gardener.addAction(new src.model.actions.PlantAction(execX, execY, getX(), getY(), plantType));
                System.out.println("Ordre de planter envoyé avec récupération de graine depuis la grange !");
            }
        }
    }
}
package src.model.actions;

import src.model.Gardener;
import src.model.World;
import src.model.ItemSeed;
import src.model.Item;

/** * Action pour récupérer une graine spécifique depuis la grange.
 * Cette action est planifiée après que le jardinier se soit déplacé à la tuile
 * adjacente de la grange, et elle suppose que le jardinier est déjà sur place au moment de l'exécution.
 * La perform() va chercher dans la grange une pile d'ItemSeed correspondant au type
 * de graine demandé (seedPrototype), et transférer 1 unité de cette graine dans l'inventaire du jardinier.
 * Si la grange ne contient pas de graine de ce type, ou si le transfert échoue, un message d'erreur est affiché dans la console.
 * Note : cette action ne gère pas le déplacement vers la grange, elle doit être utilisée en combinaison avec un MoveAction qui
 * amène le jardinier à la bonne position avant d'exécuter cette action.
 */
public class FetchSeedAction extends Action {

    private ItemSeed seedPrototype; // type de graine à récupérer

    /**
     * Constructeur de FetchSeedAction.
     * @param targetX Les coordonnées x de la tuile où le jardinier doit être pour exécuter cette action (généralement une tuile adjacente à la grange).
     * @param targetY Les coordonnées y de la tuile où le jardinier doit être pour exécuter cette action.
     * @param seedPrototype Un objet ItemSeed qui sert de prototype pour identifier le type de graine à récupérer (ex: graine de chou, carotte, etc.).
     */
    public FetchSeedAction(int targetX, int targetY, ItemSeed seedPrototype) {
        super(targetX, targetY);
        this.seedPrototype = seedPrototype;
    }

    /**
     * Méthode perform() qui est appelée lorsque le jardinier arrive sur la tuile cible.
     * Elle va chercher dans la grange une pile d'ItemSeed correspondant au type de graine demandé,
     * et transférer 1 unité de cette graine dans l'inventaire du jardinier.
     * @param gardener Le jardinier qui exécute cette action.
     * @param world Le monde dans lequel le jardinier évolue, utilisé pour accéder à la grange.
     */
    @Override
    public void perform(Gardener gardener, World world) {
        // On suppose que cette méthode est appelée quand le jardinier est arrivé sur la tuile de la grange
        src.model.Barn barn = world.getBarn();
        // Chercher la pile correspondante dans la grange
        Item found = barn.findSameItem(seedPrototype);
        if (found != null && found instanceof ItemSeed) {
            int transferred = barn.transferTo(gardener.getInventory(), found, 1);
            if (transferred > 0) {
                System.out.println("Le jardinier a récupéré 1 graine de " + seedPrototype.getPlantType());
            } else {
                System.out.println("Erreur lors du transfert de la graine depuis la grange.");
            }
        } else {
            System.out.println("Pas de graine disponible dans la grange pour " + seedPrototype.getPlantType());
        }
    }
}

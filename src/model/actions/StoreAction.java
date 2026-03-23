package src.model.actions;

import src.model.Barn;
import src.model.Gardener;
import src.model.Item;
import src.model.World;

/** Action de stockage des items dans l'inventaire du jardinier dans la grange
 * Cette action est déclenchée lorsque le jardinier arrive à la grange après un MoveAction vers la grange.
 * Elle suppose que le jardinier est déjà sur place au moment de l'exécution.
 * perform() va transférer tous les items de l'inventaire du jardinier vers la grange
 */
public class StoreAction extends Action {


    /** Constructeur de StoreAction.
     * @param targetX Les coordonnées x de la tuile où le jardinier doit être pour exécuter cette action (généralement une tuile adjacente à la grange).
     * @param targetY Les coordonnées y de la tuile où le jardinier doit être pour exécuter cette action (généralement une tuile adjacente à la grange).
     */
    public StoreAction(int targetX, int targetY) {
        super(targetX, targetY);
    }

    @Override
    public void perform(Gardener gardener, World world) {
        // On suppose que cette méthode est appelée quand le jardinier est arrivé sur la tuile de la grange
        Barn barn = world.getBarn();
        System.out.println("Deposer les items");

        // Transférer tous les items de l'inventaire du jardinier vers la grange
        for (Item item : gardener.getInventory().getItems()) {
            if (item != null) {
                gardener.getInventory().transferTo(barn, item, item.getQuantity());
                System.out.println("Item transfere");
            }
        }
    }


}

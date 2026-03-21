package src.model.actions;

import src.model.Gardener;
import src.model.Item;

/** Classe utilitaire pour construire des actions complexes en plusieurs etapes
 * Un objet de ce type est cree lors de la selection d'action et promene dans les differentes vues (selection, popup)
 * jusqu'a ce que l'action soit complete. Elle est ensuite construite et donnee au jardinier pour execution
 * Creer une sous-classe pour chaque type d'action, qui override la methode buildAction() pour construire l'action finale
 */
public abstract class ActionBuilder {

    // Jardinier qui effectue l'action
    private Gardener gardener;
    // coordonnees de la case cible de l'action
    private int x, y;    // Elargir une fois qu'on aura les parcelles
    // Item associe a l'action, par exemple la graine a planter. Laisser a null si l'action n'implique pas d'item
    private Item item = null;

    // rajouter d'autres parametres si besoin

    public ActionBuilder(Gardener gardener) {
        this.gardener = gardener;
    }

    public Gardener getGardener() {
        return gardener;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    public void setTarget(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    /** Construit l'action finale a partir des parametres stockes dans le builder et la donne au jardinier.
     * A appeler une seule fois, lorsque tous les parametres ont ete definis
     * @return l'action construite a partir des parametres du builder, prete a etre executee par le jardinier
     */
    public abstract Action buildAction();

}

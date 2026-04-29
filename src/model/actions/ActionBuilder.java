package src.model.actions;

import src.model.Gardener;
import src.model.Item;
import src.model.Parcel;
import src.view.Display;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/** Classe utilitaire pour construire des actions complexes en plusieurs etapes
 * Un objet de ce type est cree lors de la selection d'action et promene dans les differentes vues (selection, popup)
 * jusqu'a ce que l'action soit complete. Elle est ensuite construite et donnee au jardinier pour execution
 * Creer une sous-classe pour chaque type d'action, qui override la methode buildAction() pour construire l'action finale
 *
 * NOTE: un Display peut etre fourni au builder (via setDisplay) pour lui permettre de demander
 * un feedback visuel (ex: surbrillance de la case adjacente choisie) au moment de planifier
 * les deplacements. Le Display est optionnel pour garder la separations logiques model/view.
 */
public abstract class ActionBuilder {

    // Jardinier qui effectue l'action
    private final Gardener gardener;
    // coordonnees de la case cible de l'action
    private int x, y;    // Elargir une fois qu'on aura les parcelles
    // Parcelle cible de l'action
    private Parcel parcel;
    // Item associe a l'action, par exemple la graine a planter. Laisser a null si l'action n'implique pas d'item
    private Item item = null;

    // Affichage (optionnel) pour feedback visuel (highlight)
    private Display display = null;

    // Liste de points sélectionnés pour l'action, par exemple les cases ciblées pour une action qui implique plusieurs cases (ex: planter sur une parcelle entiere)
    private List<Point> selectedPoints = new ArrayList<>();

    public void addTarget(Point p) {
        if (!selectedPoints.contains(p)) selectedPoints.add(p);
    }

    public void removeTarget(Point p) {
        selectedPoints.remove(p);
    }

    public void clearTargets() {
        selectedPoints.clear();
    }

    public List<Point> getSelectedPoints() {
        return selectedPoints;
    }

    // rajouter d'autres parametres si besoin

    /** Constructeur de base du builder, qui prend le jardinier comme parametre obligatoire,
     * les autres parametres de l'action seront definis au fur et a mesure de la planification de l'action (ex: selection de la case cible, selection de l'item, etc.)
     * @param gardener
     */
    public ActionBuilder(Gardener gardener) {
        this.gardener = gardener;
    }

    /**
     * Getter pour le jardinier qui effectue l'action, qui peut etre utilisé par les sous-classes pour accéder au jardinier lors de la construction de l'action finale.
     * Ce jardinier est celui qui a initié la planification de l'action, et c'est a lui que l'action finale sera donnée pour exécution une fois construite.
     * @return
     */
    public Gardener getGardener() {
        return gardener;
    }

    /** Getter pour les coordonnées x de la case cible de l'action, par exemple la case a planter ou la case de la grange pour récupérer une graine.
     * Ces coordonnées seront utilisées pour planifier le déplacement du jardinier vers la case cible avant d'exécuter l'action.
     * @return les coordonnées x de la case cible
     */
    public int getX() {
        return x;
    }

    /** Getter pour les coordonnées y de la case cible de l'action, par exemple la case a planter ou la case de la grange pour récupérer une graine.
     * Ces coordonnées seront utilisées pour planifier le déplacement du jardinier vers la case cible avant d'exécuter l'action.
     * @return les coordonnées y de la case cible
     */
    public int getY() {
        return y;
    }

    /** Setter pour les coordonnées de la case cible de l'action, par exemple la case a planter ou la case de la grange pour récupérer une graine.
     * Ces coordonnées seront utilisées pour planifier le déplacement du jardinier vers la case cible avant d'exécuter l'action.
     * @param x les coordonnées x de la case cible
     * @param y les coordonnées y de la case cible
     */
    public void setTarget(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Setter pour la parcelle cible de l'action, utilisée pour des actions qui impliquent une parcelle entiere
     * @param parcel la parcelle cible de l'action
     */
    public void setTarget(Parcel parcel) {
        this.parcel = parcel;
    }

    /** Getter pour la parcelle cible de l'action, utilisée pour des actions qui impliquent une parcelle entiere
     * @return la parcelle cible de l'action, ou null si aucune parcelle n'est ciblée
     */
    public Parcel getParcel() {
        return parcel;
    }

    /** Getter pour l'item associé à l'action, par exemple la graine à planter. Peut etre null si l'action n'implique pas d'item.
     * @return l'item associé à l'action, ou null si aucun item n'est impliqué
     */
    public Item getItem() {
        return item;
    }

    /** Setter pour l'item associé à l'action, par exemple la graine à planter. Laisser à null si l'action n'implique pas d'item.
     * @param item l'item à associer à l'action, ou null si aucun item n'est impliqué
     */
    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * Fournit une référence à la Display pour que le builder puisse demander
     * des effets visuels (surbrillance) lors de la planification des actions.
     * Cette référence est optionnelle et n'est pas obligatoire pour les tests unitaires.
     */
    public void setDisplay(Display display) {
        this.display = display;
    }

    /** Getter pour la Display, qui peut etre utilisée par les sous-classes pour demander des effets visuels lors de la planification des actions.
     * @return la Display associée à ce builder, ou null si aucune n'a été fournie
     */
    public Display getDisplay() {
        return this.display;
    }

    /** Construit l'action finale a partir des parametres stockes dans le builder et la donne au jardinier.
     * A appeler une seule fois, lorsque tous les parametres ont ete definis
     */
    public abstract void buildAction();

}

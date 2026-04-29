package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

/**
 * Classe représentant un arbre décoratif (Tree1).
 * Cet objet permet de boiser la ferme. Bien qu'il s'agisse d'un arbre,
 * il est géré comme un bâtiment statique pour bénéficier du système de collision.
 */
public class Tree1 extends Building {

    /**
     * Constructeur de l'arbre.
     * Définit son encombrement au sol et son coût de plantation.
     */
    public Tree1() {
        // Appel au constructeur de la classe parente (Building) :
        // - 1, 1 : Occupe une seule case sur la grille (le tronc).
        // - false : 'isPassable' est à false. L'arbre bloque le passage,
        //           le jardinier ne peut pas traverser le tronc.
        // - PlacementRule.NORMAL_ONLY : Se plante uniquement sur l'herbe.
        // - new ImageIcon(...) : Charge le visuel de l'arbre depuis les assets.
        super(1, 1, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/tree1.png"));

        // Prix d'achat fixé à 10 pièces d'or.
        this.buyPrice = 10;
    }

    /**
     * Méthode de mise à jour logique appelée à chaque tick.
     * Pour cet arbre décoratif simple, aucune action de croissance ou de récolte
     * n'est implémentée ici.
     *
     * @param world Référence au moteur de simulation.
     */
    @Override
    public void applyEffect(World world) {
        // Pas d'effet particulier (objet passif).
    }
}
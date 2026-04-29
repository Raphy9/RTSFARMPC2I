package src.model.buildings;

import src.model.World;
import javax.swing.*;

/**
 * Classe représentant un grand arbre décoratif (Tree2).
 * Contrairement au Tree1, cet arbre occupe un espace plus important sur la grille,
 * ce qui le rend idéal pour créer des zones boisées denses ou des parcs.
 */
public class Tree2 extends Building {

    /**
     * Constructeur du grand arbre.
     * Définit une zone de collision plus large et un coût proportionnel à sa taille.
     */
    public Tree2() {
        // Appel au constructeur de la classe parente (Building) :
        // - 2, 2 : Dimensions sur la grille. Il occupe un carré de 4 cases (2x2).
        // - false : 'isPassable' est à false. L'arbre est un obstacle massif
        //           que les entités ne peuvent pas traverser.
        // - PlacementRule.NORMAL_ONLY : Ne peut être posé que sur de l'herbe libre.
        // - new ImageIcon(...) : Charge le visuel du grand arbre.
        super(2, 2, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/tree2.png"));

        // Prix d'achat de 25 pièces d'or (plus cher que la version 1x1).
        this.buyPrice = 25;
    }

    /**
     * Méthode de mise à jour logique.
     * Comme il s'agit d'un objet de décoration statique, aucune action n'est
     * effectuée sur le monde à chaque cycle.
     *
     * @param world Référence au moteur de simulation.
     */
    @Override
    public void applyEffect(World world) {
        // Pas d'effet logique particulier.
    }
}
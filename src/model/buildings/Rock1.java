package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

/**
 * Classe représentant un rocher décoratif (Rock1).
 * Cet objet permet d'ajouter du relief visuel à la ferme tout en bloquant
 * physiquement le passage sur la case où il est posé.
 */
public class Rock1 extends Building {

    /**
     * Constructeur du rocher.
     * Initialise les dimensions, la collision et le coût.
     */
    public Rock1() {
        // Appel au constructeur de la classe parente (Building) :
        // - 1, 1 : Occupe une seule case sur la grille de jeu.
        // - false : 'isPassable' est à false. Le rocher est un obstacle infranchissable
        //           pour le jardinier et les entités mobiles.
        // - PlacementRule.NORMAL_ONLY : Ne peut être placé que sur l'herbe (pas sur les cultures).
        // - new ImageIcon(...) : Charge l'image du rocher depuis le dossier des assets.
        super(1, 1, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/rock1.png"));

        // Prix d'achat fixé à 10 pièces d'or, ce qui en fait une décoration abordable.
        this.buyPrice = 10;
    }

    /**
     * Méthode de mise à jour logique appelée à chaque cycle de simulation.
     * Comme pour la plupart des éléments de décor, ce rocher n'a pas d'effet actif.
     *
     * @param world Référence au modèle du monde.
     */
    @Override
    public void applyEffect(World world) {
        // L'objet est statique et n'interagit pas avec les mécaniques de ressources.
    }
}
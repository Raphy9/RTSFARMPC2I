package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

/**
 * Représente la Grange physique sur la carte.
 * Taille 3x3, gratuite, max 1 exemplaire.
 */
public class BarnBuilding extends Building {

    public BarnBuilding() {
        // Dimensions: 3 (largeur) x 3 (hauteur)
        // isPassable: false (on ne peut pas marcher dessus)
        // Regle: NORMAL_ONLY (se pose sur l'herbe)
        super(3, 3, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/barn.png"));
        this.buyPrice = 0; // Gratuite !
        this.levelRequirement = 1; // Disponible des le début
    }

    @Override
    public void applyEffect(World world) {
        // Pas d'effet continu particulier pour la grange
    }

    // On s'assure que la suppression de la grange ne donne pas d'argent (car elle est gratuite)
    @Override
    public int getSellPrice() {
        return 0;
    }
}
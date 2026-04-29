package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

/**
 * Classe représentant un étendoir à linge (Linge).
 * C'est un objet de décoration horizontal qui occupe plusieurs cases
 * tout en permettant aux entités de circuler librement dessous.
 */
public class Linge extends Building {

    /**
     * Constructeur de l'objet Linge.
     * Définit une empreinte au sol large et des propriétés de collision permissives.
     */
    public Linge() {
        // Appel au constructeur parent (Building) :
        // - 2 : Largeur de 2 cases (width).
        // - 1 : Hauteur de 1 case (height).
        // - true : isPassable est à true. Le jardinier peut marcher "sous" le linge.
        // - PlacementRule.NORMAL_ONLY : Ne peut être placé que sur l'herbe.
        // - new ImageIcon(...) : Charge l'image représentant l'étendoir.
        super(2, 1, true, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/linge.png"));

        // Prix d'achat fixé à 50 pièces d'or (objet de décoration plus imposant).
        this.buyPrice = 50;
    }

    /**
     * Méthode de mise à jour logique.
     * Comme pour les autres éléments de décoration statiques, aucune action n'est requise.
     *
     * @param world Référence au moteur de simulation.
     */
    @Override
    public void applyEffect(World world) {
        // Pas d'effet systémique sur le monde.
    }
}
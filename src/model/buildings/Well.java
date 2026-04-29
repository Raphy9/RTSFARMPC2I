package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

/**
 * Classe représentant un puits (Well).
 * Un élément de décor emblématique de la ferme qui occupe une case
 * et bloque physiquement le passage.
 */
public class Well extends Building {

    /**
     * Constructeur du puits.
     * Initialise les paramètres de collision, de placement et le coût.
     */
    public Well() {
        // Appel au constructeur de la classe parente (Building) :
        // - 1, 1 : Occupe une seule case sur la grille.
        // - false : 'isPassable' est à false. Le puits est un obstacle solide ;
        //           les entités doivent le contourner.
        // - PlacementRule.NORMAL_ONLY : Ne peut être construit que sur l'herbe.
        // - new ImageIcon(...) : Charge le visuel du puits depuis le dossier des assets.
        super(1, 1, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/well.png"));

        // Prix d'achat fixé à 40 pièces d'or.
        this.buyPrice = 40;
    }

    /**
     * Méthode de mise à jour logique appelée à chaque tick.
     * Actuellement, le puits n'a pas d'effet actif sur le monde.
     *
     * @param world Référence au moteur de simulation.
     */
    @Override
    public void applyEffect(World world) {
        // Pas d'effet particulier (objet passif).
    }
}
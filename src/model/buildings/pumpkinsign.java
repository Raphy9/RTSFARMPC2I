package src.model.buildings;

import src.model.World;
import javax.swing.*;

/**
 * Classe représentant un petit panneau indicateur décoratif avec une icône de citrouille (pumpkinsign).
 * Utile pour organiser visuellement les différentes zones de culture de la ferme.
 */
public class pumpkinsign extends Building {

    /**
     * Constructeur du panneau "Citrouille".
     * Initialise l'objet avec des paramètres favorisant la décoration sans entrave.
     */
    public pumpkinsign() {
        // Appel au constructeur de la classe parente (Building) :
        // - 1, 1 : Occupe une seule tuile sur la grille.
        // - true : L'objet est "passable" (isPassable). Le jardinier peut marcher
        //          sur la même case que le panneau sans être bloqué.
        // - PlacementRule.NORMAL_ONLY : Doit être posé sur l'herbe uniquement.
        // - new ImageIcon(...) : Charge le sprite spécifique représentant une citrouille.
        super(1, 1, true, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/pumpkinsign.png"));

        // Prix d'achat très abordable fixé à 10 pièces d'or.
        this.buyPrice = 10;
    }

    /**
     * Méthode appelée à chaque cycle de mise à jour du jeu.
     * Comme pour les autres panneaux, il n'y a aucun effet dynamique sur le monde.
     *
     * @param world Référence au moteur de simulation.
     */
    @Override
    public void applyEffect(World world) {
        // Pas d'effet logique particulier.
    }
}
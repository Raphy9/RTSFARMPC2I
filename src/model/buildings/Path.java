package src.model.buildings;

import src.model.World;
import javax.swing.*;

/**
 * Classe représentant une dalle ou un segment de chemin (Path).
 * Le chemin est un bâtiment utilitaire permettant de décorer le sol
 * tout en restant totalement transparent pour le système de collision.
 */
public class Path extends Building {

    /**
     * Constructeur du chemin.
     * Configure l'objet pour qu'il soit peu coûteux et facile à placer.
     */
    public Path(){
        // Appel au constructeur de la classe parente (Building) :
        // - 1, 1 : Occupe une seule case sur la grille.
        // - true : L'objet est "passable" (isPassable = true). Le jardinier et
        //          les animaux peuvent marcher dessus sans ralentissement ni blocage.
        // - PlacementRule.NORMAL_ONLY : Se place uniquement sur l'herbe (terrain non-labouré).
        // - new ImageIcon(...) : Charge la texture visuelle du chemin.
        super(1, 1, true, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/path.png"));

        // Prix d'achat minimal (5 PO) pour permettre au joueur d'en placer en grande quantité.
        this.buyPrice = 5;
    }

    /**
     * Méthode de mise à jour logique appelée à chaque cycle.
     * Pour un segment de chemin, aucune action dynamique n'est nécessaire.
     *
     * @param world Référence au modèle du monde.
     */
    @Override
    public void applyEffect(World world) {
        // Le chemin n'a pas d'effet sur les ressources ou les statistiques.
    }
}
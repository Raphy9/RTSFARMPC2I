package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

/**
 * Classe représentant un poteau ou un pilier décoratif (Poto).
 * Contrairement aux barrières (Fence), cet objet est statique et n'essaie pas
 * de se connecter visuellement à ses voisins.
 */
public class Poto extends Building {

    /**
     * Constructeur du poteau.
     * Définit ses propriétés de collision et son coût.
     */
    public Poto() {
        // Appel au constructeur de la classe parente (Building) :
        // - 1, 1 : Occupe une seule case sur la grille.
        // - false : 'isPassable' est à false. Cet objet est un obstacle physique
        //           qui bloque le passage des agents.
        // - PlacementRule.NORMAL_ONLY : Ne peut être posé que sur l'herbe standard.
        // - new ImageIcon(...) : Charge le visuel du poteau depuis les assets.
        super(1, 1, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/poto.png"));

        // Prix d'achat fixé à 20 pièces d'or.
        this.buyPrice = 20;
    }

    /**
     * Méthode de mise à jour logique appelée à chaque tick.
     * Pour un poteau décoratif, aucune action n'est effectuée sur le monde.
     *
     * @param world Référence au modèle de simulation.
     */
    @Override
    public void applyEffect(World world) {
        // Objet purement décoratif et physique, sans effet sur les ressources.
    }
}
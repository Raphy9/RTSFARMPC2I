package src.model.buildings;

import src.model.World;
import javax.swing.*;

/**
 * Classe représentant un objet de décoration de type "Tonneau".
 * Hérite de Building pour s'intégrer au système de construction et de rendu.
 */
public class Barrel1 extends Building {

    /**
     * Constructeur initialisant les propriétés visuelles et physiques du tonneau.
     */
    public Barrel1() {
        // Appel au constructeur de la classe parente (Building) :
        // 1, 1 : Le bâtiment occupe une surface de 1x1 tuile sur la grille.
        // false : L'objet n'est pas "passable" (isPassable = false), il bloque donc le mouvement du jardinier.
        // PlacementRule.NORMAL_ONLY : Interdiction de le poser sur une parcelle labourée (PlantTile).
        // new ImageIcon(...) : Charge l'image spécifique pour le rendu graphique.
        super(1, 1, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/barrel1.png"));

        // Coût d'achat en pièces d'or (PO) prélevé lors de la construction via le BuildingManager.
        this.buyPrice = 10;

        // Niveau minimum requis dans les statistiques du joueur pour débloquer cet objet.
        this.levelRequirement = 1;
    }

    /**
     * Méthode appelée à chaque cycle de mise à jour (tick) du jeu.
     * Pour ce tonneau décoratif, aucun effet n'est appliqué au monde.
     *
     * @param world Référence au moteur de simulation.
     */
    @Override
    public void applyEffect(World world) {
        // Cet objet est purement esthétique, il ne modifie pas l'état du monde.
    }
}
package src.model.buildings;

import src.model.World;
import javax.swing.*;

/**
 * Classe représentant une variante esthétique du tonneau (Barrel2).
 * Ce bâtiment est un objet purement décoratif qui utilise le système de collision
 * par défaut pour bloquer le passage des entités.
 */
public class Barrel2 extends Building {

    /**
     * Constructeur de la décoration.
     * Définit l'apparence, l'encombrement et les règles de pose.
     */
    public Barrel2() {
        // super(...) appelle le constructeur de la classe Building :
        // - 1, 1 : Dimensions sur la grille (1 case sur 1).
        // - false : Définit 'isPassable' à false. Le moteur de pathfinding (A*)
        //           considérera cette case comme un obstacle infranchissable.
        // - PlacementRule.NORMAL_ONLY : Interdit la construction sur des parcelles de culture (PlantTile).
        // - new ImageIcon(...) : Chemin vers la texture spécifique de cette variante de tonneau.
        super(1, 1, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/barrel2.png"));

        // Prix d'achat utilisé par le BuildingManager lors de la transaction.
        this.buyPrice = 10;
    }

    /**
     * Logique de mise à jour du bâtiment.
     * Pour un objet décoratif statique, cette méthode reste vide.
     *
     * @param world Référence au modèle du monde pour d'éventuelles interactions.
     */
    @Override
    public void applyEffect(World world) {
        // Pas d'effet sur le gameplay (ne produit ni ressource, ni bonus).
    }
}
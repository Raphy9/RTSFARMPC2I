package src.model.buildings;

import src.model.World;
import javax.swing.*;

/**
 * Classe représentant un petit panneau décoratif avec une icône de carotte (carrotsign).
 * Cet objet est idéal pour baliser les zones de culture dans la ferme.
 */
public class carrotsign extends Building {

    /**
     * Constructeur du panneau "Carotte".
     * Définit les paramètres spécifiques à cet objet décoratif.
     */
    public carrotsign() {
        // Appel au constructeur de la classe parente (Building) :
        // - 1, 1 : Dimensions sur la grille (occupe une seule tuile).
        // - true : L'objet est "passable" (isPassable). Le jardinier et les animaux
        //          peuvent marcher dessus/à travers sans collision.
        // - PlacementRule.NORMAL_ONLY : Ce panneau doit être posé sur de l'herbe
        //                               (pas directement sur une parcelle labourée).
        // - new ImageIcon(...) : Charge l'image visuelle du panneau carotte.
        super(1, 1, true, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/carrotsign.png"));

        // Coût d'acquisition pour le joueur (10 pièces d'or).
        this.buyPrice = 10;
    }

    /**
     * Méthode de mise à jour logique appelée à chaque tick du jeu.
     * Étant un panneau purement informatif et décoratif, il n'exerce aucune influence
     * sur les mécaniques du monde.
     *
     * @param world Référence au moteur de simulation.
     */
    @Override
    public void applyEffect(World world) {
        // Aucune logique métier particulière à appliquer.
    }
}
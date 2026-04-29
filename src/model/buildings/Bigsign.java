package src.model.buildings;

import src.model.World;
import javax.swing.*;

/**
 * Classe représentant une grande enseigne ou un panneau décoratif (Bigsign).
 * Contrairement aux autres objets de décoration, ce bâtiment est traversable par les entités.
 */
public class Bigsign extends Building {

    /**
     * Constructeur de l'enseigne.
     * Configure les spécifications de construction et de collision.
     */
    public Bigsign() {
        // super(...) appelle le constructeur de la classe parente Building :
        // - 1, 1 : Dimensions sur la grille (occupe une seule case).
        // - true : Définit 'isPassable' à true. Le jardinier et les animaux peuvent
        //          marcher "à travers" ou "derrière" ce panneau sans être bloqués.
        // - PlacementRule.NORMAL_ONLY : Interdiction de le poser sur des parcelles de culture.
        // - new ImageIcon(...) : Charge le visuel haute résolution du panneau.
        super(1, 1, true, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/bigsign.png"));

        // Prix d'achat pour le joueur (20 pièces d'or).
        this.buyPrice = 20;
    }

    /**
     * Méthode de mise à jour logique du bâtiment.
     * Comme pour les autres objets de décoration, aucune action n'est effectuée sur le monde.
     *
     * @param world Référence au modèle du monde.
     */
    @Override
    public void applyEffect(World world) {
        // Objet purement cosmétique, aucun effet systémique.
    }
}
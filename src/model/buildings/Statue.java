package src.model.buildings;

import javax.swing.*;

/**
 * Statue : un bâtiment décoratif 2x1, non traversable.
 * Coûte 300 PO.
 * Débloqué au chapitre 6.
 */
public class Statue extends Building {

    public Statue() {
        super(1, 2, false, PlacementRule.NORMAL_ONLY, loadStatueSprite());
        this.buyPrice = 300;
        this.levelRequirement = 6;  // Débloqué au niveau 6 (ou chapitre 6)
    }

    private static ImageIcon loadStatueSprite() {
        try {
            return new ImageIcon("src/assets/Buildings/statue.png");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la sprite Statue : " + e.getMessage());
            return new ImageIcon();
        }
    }

    @Override
    public void applyEffect(src.model.World world) {
        // Pas d'effet particulier pour la statue
    }
}


package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

/**
 * Représente un obstacle décoratif placé sur la carte au démarrage (ex: rocher, buisson).
 * Les obstacles sont considérés comme des bâtiments pour simplifier la logique (empêchent le placement dessus,
 * sont rendus via la liste des bâtiments, etc.).
 */
public class Obstacle extends Building {

    public Obstacle(ImageIcon sprite) {
        // Taille 1x1, non franchissable, règle de placement NORMAL_ONLY
        super(1, 1, false, PlacementRule.NORMAL_ONLY, sprite);
        buyPrice = 5;
    }

    @Override
    public void applyEffect(World world) {
        // Pas d'effet particulier
    }
}

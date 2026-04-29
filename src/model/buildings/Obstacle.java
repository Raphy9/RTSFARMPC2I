package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

/**
 * Représente un obstacle décoratif placé sur la carte au démarrage (ex: rocher, buisson).
 * Les obstacles sont considérés comme des batiments pour simplifier la logique (empechent le placement dessus,
 * sont rendus via la liste des batiments, etc.).
 */
public class Obstacle extends Building {

    private String spritePath;

    public Obstacle(String spritePath) {
        this(new ImageIcon(spritePath));
        this.spritePath = spritePath;
    }

    public Obstacle(ImageIcon sprite) {
        // Taille 1x1, non franchissable, regle de placement NORMAL_ONLY
        super(1, 1, false, PlacementRule.NORMAL_ONLY, sprite);
        buyPrice = 5;
        this.spritePath = (sprite != null) ? sprite.getDescription() : null;
    }

    @Override
    public void applyEffect(World world) {
        // Pas d'effet particulier
    }

    public String getSpritePath() {
        return spritePath;
    }
}

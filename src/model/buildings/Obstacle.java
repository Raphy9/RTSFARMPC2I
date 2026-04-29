package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

/**
 * Représente un obstacle décoratif placé sur la carte au démarrage (ex: rocher, buisson).
 * Les obstacles sont considérés comme des bâtiments pour simplifier la logique :
 * - Ils empêchent le placement d'autres objets sur la même case.
 * - Ils sont rendus via la liste standard des bâtiments.
 * - Ils bloquent le passage du jardinier.
 */
public class Obstacle extends Building {

    // Stockage du chemin du fichier image pour faciliter la sauvegarde/chargement
    private String spritePath;

    /**
     * Constructeur prenant un chemin de fichier.
     * @param spritePath Le chemin vers l'image de l'obstacle (ex: "assets/Obstacles/rock.png").
     */
    public Obstacle(String spritePath) {
        this(new ImageIcon(spritePath));
        this.spritePath = spritePath;
    }

    /**
     * Constructeur principal initialisant les propriétés physiques.
     * @param sprite L'image à afficher.
     */
    public Obstacle(ImageIcon sprite) {
        // Paramètres hérités de Building :
        // - 1, 1 : Occupe une seule tuile.
        // - false : Non franchissable (isPassable = false). Le jardinier doit le contourner.
        // - PlacementRule.NORMAL_ONLY : Définit où l'objet peut exister (herbe standard).
        super(1, 1, false, PlacementRule.NORMAL_ONLY, sprite);

        // Prix symbolique (utile si tu décides de permettre au joueur de les déplacer ou les vendre).
        buyPrice = 5;

        // Récupération de la description de l'image comme chemin si elle n'est pas déjà définie
        this.spritePath = (sprite != null) ? sprite.getDescription() : null;
    }

    /**
     * Méthode appelée à chaque cycle de jeu.
     * Les obstacles étant statiques et naturels, ils n'ont aucun effet sur le monde.
     */
    @Override
    public void applyEffect(World world) {
        // Pas d'effet particulier (objet passif)
    }

    /**
     * Accesseur pour le chemin du sprite.
     * Utile pour le système de persistance (sauvegarde JSON/XML par exemple).
     */
    public String getSpritePath() {
        return spritePath;
    }
}
package src.model.buildings;

import javax.swing.*;

/**
 * Classe représentant une Statue décorative.
 * C'est un objet de prestige occupant un espace vertical (1x2) sur la grille.
 * Contrairement au 'Linge' (2x1), la statue est un obstacle physique total.
 */
public class Statue extends Building {

    /**
     * Constructeur de la Statue.
     * Configure l'encombrement, le coût élevé et les conditions de déblocage.
     */
    public Statue() {
        // super(...) appelle le constructeur de la classe Building :
        // - 1 : Largeur (width) d'une seule case.
        // - 2 : Hauteur (height) de deux cases.
        // - false : 'isPassable' est à false. La statue bloque le passage.
        // - PlacementRule.NORMAL_ONLY : Ne peut être posée que sur l'herbe.
        // - loadStatueSprite() : Méthode privée pour charger l'image de manière sécurisée.
        super(1, 2, false, PlacementRule.NORMAL_ONLY, loadStatueSprite());

        // Prix d'achat prestigieux (300 PO).
        this.buyPrice = 300;

        // Condition de progression : disponible uniquement au niveau 6.
        this.levelRequirement = 6;
    }

    /**
     * Charge l'icône de la statue avec une gestion d'erreur rudimentaire.
     * @return ImageIcon de la statue ou une icône vide en cas d'échec.
     */
    private static ImageIcon loadStatueSprite() {
        try {
            return new ImageIcon("src/assets/Buildings/statue.png");
        } catch (Exception e) {
            // Log l'erreur dans la console pour faciliter le débogage si le fichier est manquant
            System.err.println("Erreur lors du chargement de la sprite Statue : " + e.getMessage());
            return new ImageIcon();
        }
    }

    /**
     * Méthode de mise à jour logique.
     * Étant un monument statique, elle ne produit aucun effet sur les mécaniques du monde.
     *
     * @param world Référence au modèle du monde.
     */
    @Override
    public void applyEffect(src.model.World world) {
        // Pas d'effet particulier pour la statue (objet cosmétique).
    }
}
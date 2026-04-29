package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

/**
 * Classe représentant une boîte aux lettres décorative (Mailbox1).
 * Cet objet enrichit l'esthétique de la ferme tout en agissant comme un obstacle physique.
 */
public class Mailbox1 extends Building {

    /**
     * Constructeur de la boîte aux lettres.
     * Configure les dimensions, la collision et le coût de l'objet.
     */
    public Mailbox1() {
        // Appel au constructeur de la classe parente (Building) :
        // - 1, 1 : Occupe une seule case sur la grille de jeu.
        // - false : 'isPassable' est à false. Cet objet bloque le mouvement,
        //           le jardinier devra le contourner.
        // - PlacementRule.NORMAL_ONLY : Ne peut être posé que sur l'herbe (pas sur les parcelles).
        // - new ImageIcon(...) : Charge le visuel de la boîte aux lettres depuis les assets.
        super(1, 1, false, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/mailbox1.png"));

        // Prix d'achat fixé à 30 pièces d'or.
        this.buyPrice = 30;
    }

    /**
     * Méthode de mise à jour logique appelée à chaque cycle de jeu.
     * Pour une boîte aux lettres décorative, aucune action n'est effectuée sur le monde.
     *
     * @param world Référence au modèle de simulation.
     */
    @Override
    public void applyEffect(World world) {
        // Pas d'effet logique particulier (objet passif).
    }
}
package src.model.buildings;

import src.model.World;
import javax.swing.*;

/**
 * Classe représentant un panneau indicateur décoratif avec une icône de fraise (strawberrysign).
 * Ce bâtiment permet au joueur de marquer visuellement ses zones de petits fruits.
 */
public class strawberrysign extends Building {

    /**
     * Constructeur du panneau "Fraise".
     * Initialise l'objet comme un élément de décor traversable.
     */
    public strawberrysign() {
        // Appel au constructeur de la classe parente (Building) :
        // - 1, 1 : Occupe une seule case sur la grille.
        // - true : 'isPassable' est à true. Le jardinier peut passer à travers,
        //          ce qui est idéal pour ne pas encombrer les allées de circulation.
        // - PlacementRule.NORMAL_ONLY : Se place uniquement sur les cases d'herbe.
        // - new ImageIcon(...) : Charge l'image spécifique de la fraise.
        super(1, 1, true, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/strawberrysign.png"));

        // Prix d'achat standard pour la signalétique (10 pièces d'or).
        this.buyPrice = 10;
    }

    /**
     * Méthode de mise à jour logique.
     * Comme pour les autres panneaux décoratifs, aucune action n'est requise à chaque tick.
     *
     * @param world Référence au modèle du monde.
     */
    @Override
    public void applyEffect(World world) {
        // Objet purement cosmétique, sans effet sur les mécaniques de jeu.
    }
}
package src.model.buildings;

import src.model.World;
import javax.swing.*;

/**
 * Classe représentant un panneau indicateur décoratif avec une icône de chou (chouxsign).
 * Ce bâtiment sert principalement de repère visuel pour le joueur dans sa ferme.
 */
public class chouxsign extends Building {

    /**
     * Constructeur du panneau "Chou".
     * Initialise les propriétés physiques, économiques et visuelles du bâtiment.
     */
    public chouxsign() {
        // Appel au constructeur de la classe parente (Building) :
        // - 1, 1 : Dimensions sur la grille (occupe une seule case).
        // - true : L'objet est "passable". Les jardiniers et animaux peuvent circuler
        //          librement sur la case sans être bloqués par le panneau.
        // - PlacementRule.NORMAL_ONLY : Ce bâtiment ne peut être posé que sur de l'herbe standard.
        // - new ImageIcon(...) : Charge le visuel spécifique représentant un chou.
        super(1, 1, true, PlacementRule.NORMAL_ONLY, new ImageIcon("src/assets/Buildings/chouxsign.png"));

        // Prix d'achat défini à 10 pièces d'or dans la boutique.
        this.buyPrice = 10;
    }

    /**
     * Méthode appelée à chaque mise à jour du monde pour appliquer les effets du bâtiment.
     * Étant un objet de décoration statique, il n'a aucune influence sur les mécaniques de jeu.
     *
     * @param world Référence au moteur de simulation du monde.
     */
    @Override
    public void applyEffect(World world) {
        // Aucune logique particulière n'est exécutée à chaque tick.
    }
}
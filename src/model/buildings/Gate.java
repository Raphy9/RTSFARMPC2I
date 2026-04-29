package src.model.buildings;

import src.model.World;
import javax.swing.*;

/**
 * Représente un portillon ou une porte de clôture.
 * Cette classe étend 'Fence', héritant ainsi de ses propriétés physiques et
 * de sa logique de voisinage, tout en ajoutant des visuels spécifiques.
 */
public class Gate extends Fence {

    // Sprites spécifiques pour représenter l'ouverture ou les battants de la porte
    private ImageIcon gateLeft;
    private ImageIcon gateRight;

    /**
     * Constructeur de la porte.
     * Appelle le constructeur de Fence pour initialiser les bases (1x1, non-passable).
     */
    public Gate() {
        // super() initialise les constantes de la classe Fence (prix de base, images statiques)
        super();

        // On surcharge les valeurs économiques pour refléter la valeur supérieure d'une porte
        this.buyPrice = 15;
        this.levelRequirement = 4; // Accès restreint aux joueurs plus expérimentés

        // Chargement des ressources graphiques propres au portillon
        gateLeft = new ImageIcon("src/assets/Obstacles/gate_left.png");
        gateRight = new ImageIcon("src/assets/Obstacles/gate_right.png");
    }

    /**
     * Accesseur pour le sprite du battant gauche.
     */
    public ImageIcon getGateLeftSprite() {
        return gateLeft;
    }

    /**
     * Accesseur pour le sprite du battant droit.
     */
    public ImageIcon getGateRightSprite() {
        return gateRight;
    }

    /**
     * Spécialisation de la méthode de la classe mère (Building).
     * Permet au moteur de jeu d'identifier rapidement cet objet comme une porte
     * sans avoir à utiliser l'opérateur 'instanceof'.
     *
     * @return true systématiquement pour cette classe.
     */
    @Override
    public boolean isGate() {
        return true; // Identification explicite du type d'objet
    }
}
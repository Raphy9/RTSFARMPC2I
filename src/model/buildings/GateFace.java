package src.model.buildings;

/**
 * Représente la variante frontale (vue de face) du portillon.
 * Cette classe spécialise 'Gate' pour permettre au moteur de rendu ou au
 * système de placement de distinguer l'orientation de la porte.
 */
public class GateFace extends Gate {

    /**
     * Constructeur de la porte frontale.
     * Initialise les paramètres économiques et de progression.
     */
    public GateFace() {
        // Appelle le constructeur de Gate (qui charge les sprites gate_left/right)
        super();

        // Redéfinition du prix et du niveau requis pour correspondre aux spécifications de Gate
        this.buyPrice = 15;
        this.levelRequirement = 4;
    }

    /**
     * Confirmation explicite du type d'objet.
     * Bien que déjà défini dans la classe mère 'Gate', cette surcharge garantit
     * que l'objet est reconnu comme un point de passage franchissable.
     *
     * @return true systématiquement.
     */
    @Override
    public boolean isGate() {
        return true; // C'est une porte !
    }
}
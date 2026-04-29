package src.model.buildings;

/**
 * Représente la variante latérale (vue de profil) du portillon.
 * Cette classe permet de distinguer graphiquement et logiquement les portes
 * placées sur les axes verticaux de ta clôture.
 */
public class GateSide extends Gate {

    /**
     * Constructeur de la porte latérale.
     * Initialise l'objet avec les mêmes contraintes économiques que sa variante frontale.
     */
    public GateSide() {
        // Appelle le constructeur de la classe parente 'Gate'
        super();

        // Coût d'achat (15 pièces d'or)
        this.buyPrice = 15;

        // Niveau requis pour l'obtention (Niveau 4)
        this.levelRequirement = 4;
    }

    /**
     * Identifie explicitement cet objet comme étant une porte.
     * Cette information est cruciale pour le moteur de jeu, notamment pour
     * autoriser le passage à travers cette case de bâtiment.
     *
     * @return true systématiquement.
     */
    @Override
    public boolean isGate() {
        return true; // C'est une porte !
    }
}
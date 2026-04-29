package src.model.buildings;

import src.model.Plant;
import src.model.PlantTile;
import src.model.Tile;
import src.model.World;
import javax.swing.ImageIcon;

/**
 * Classe représentant un arroseur automatique (Sprinkler).
 * Ce bâtiment automatise l'arrosage des plantes dans un rayon défini,
 * permettant au joueur de gagner du temps et d'optimiser ses cultures.
 */
public class Sprinkler extends Building {

    /**
     * Portée de l'arroseur (2 cases).
     * Cela crée une zone d'arrosage de 5x5 tuiles autour du bâtiment.
     */
    private static final int RANGE = 2;

    /**
     * Constructeur de l'arroseur.
     */
    public Sprinkler() {
        // - 1, 1 : Occupe une seule case.
        // - false : Obstacle physique (le jardinier ne peut pas marcher dessus).
        // - PlacementRule.PLANTABLE_ONLY : Contrainte forte ! L'arroseur ne peut être
        //   posé que sur une parcelle labourée (PlantTile) pour être au plus près des plantes.
        super(1, 1, false, PlacementRule.PLANTABLE_ONLY, new ImageIcon("src/assets/arroseur.png"));

        // Coût élevé (120 PO) car il s'agit d'un outil d'automatisation puissant.
        this.buyPrice = 120;

        // Débloqué au niveau 3, offrant une aide au moment où la ferme s'agrandit.
        this.levelRequirement = 3;
    }

    /**
     * Logique d'arrosage automatique exécutée à chaque tick du jeu.
     * Parcourt toutes les tuiles dans le rayon d'action et hydrate les plantes si nécessaire.
     *
     * @param world Référence au monde pour accéder aux tuiles et à leurs états.
     */
    @Override
    public void applyEffect(World world) {
        int centerX = getX();
        int centerY = getY();

        // Double boucle pour parcourir la zone 5x5 centrée sur l'arroseur
        for (int dx = -RANGE; dx <= RANGE; dx++) {
            for (int dy = -RANGE; dy <= RANGE; dy++) {
                int x = centerX + dx;
                int y = centerY + dy;

                // Sécurité : on vérifie que les coordonnées sont bien à l'intérieur de la carte
                if (x < 0 || y < 0 || x >= World.WIDTH || y >= World.HEIGHT) {
                    continue;
                }

                Tile tile = world.getTile(x, y);

                // On vérifie si la tuile est une parcelle de culture
                if (tile instanceof PlantTile) {
                    Plant plant = ((PlantTile) tile).getPlant();

                    /*
                     * Condition intelligente : On n'arrose que si :
                     * 1. Il y a effectivement une plante sur la tuile.
                     * 2. L'ajout d'eau ne dépasse pas le seuil critique (MAX_WATER_LEVEL).
                     * Cela évite que l'arroseur automatique ne fasse mourir (pourrir) les plantes.
                     */
                    if (plant != null && plant.getWaterLevel() + Plant.WATERING_AMOUNT <= Plant.MAX_WATER_LEVEL) {
                        ((PlantTile) tile).water(); // Applique l'hydratation
                    }
                }
            }
        }
    }
}
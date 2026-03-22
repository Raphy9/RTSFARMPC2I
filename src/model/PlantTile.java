package src.model;

import javax.swing.*;

/**
 * La classe CasePlantable représente une tuile de terrain qui peut être cultivée.
 * Elle hérite de Tile et ajoute des fonctionnalités spécifiques à l'agriculture (planter, arroser, fertiliser, récolter).
 */
public class PlantTile extends Tile {

    private static final ImageIcon DRY_SPRITE = new ImageIcon("src/assets/parcel.png");
    private static final ImageIcon WET_SPRITE = new ImageIcon("src/assets/parcel_wet.jpg");
    // --- Attributs spécifiques à l'agriculture ---
    private Plant plant;// La plante (null si vide)

    /**
     * Constructeur de CasePlantable.
     *
     * @param x La coordonnée x de la tuile
     * @param y La coordonnée y de la tuile
     */
    public PlantTile(int x, int y) {
        super(x, y, DRY_SPRITE); // Appelle le constructeur de Tile avec l'image de base
        this.plant = null;
    }

    // --- SURCHARGE (OVERRIDE) ---

    /**
     * Une case plantable est considérée comme "farmable" si elle ne contient pas déjà une plante.
     * Contrairement à une tuile normale, on peut planter directement dessus sans devoir labourer.
     *
     * @return true si la case est prête à être plantée, false sinon
     */
    @Override
    public boolean isFarmable() {
        // On peut planter SEULEMENT s'il n'y a pas déjà une plante
        return this.plant == null;
    }

    /**
     * Une case plantable ne peut pas être labourée, car elle est déjà prête à être plantée.
     *
     * @return false, car on ne peut pas labourer une case plantable
     */
    @Override
    public boolean isPlowable() {
        // On ne peut pas labourer une case plantable, elle est déjà prête à être plantée
        return false;
    }

    /**
     * La méthode tick() est appelée à chaque cycle de jeu.
     * Elle fait grandir la plante si elle existe.
     */
    @Override
    public void tick() {
        // Fait grandir la plante si elle existe
        if (this.plant != null) {
            this.plant.tick();
        }
    }

    // --- MÉTHODES AGRICOLES ---

    /**
     * Tente de planter une graine de type donné sur cette case.
     *
     * @param type Le type de plante à planter
     * @return true si la plantation a réussi, false si la case est déjà occupée
     */
    public boolean plant(PlantType type) {
        if (this.plant != null) {
            return false;
        }
        this.plant = new Plant(type);
        return true;
    }

    /**
     * Arrose la plante présente sur cette case, si elle existe.
     * L'arrosage ajoute une quantité d'eau fixe (33.0f) à la plante.
     */
    public void water() {
        if (this.plant != null) {
            this.plant.water(33.0f);
        }
    }

    /**
     * Applique de l'engrais à la plante présente sur cette case, si elle existe.
     * L'engrais accélère la croissance de la plante.
     *
     * @return true si l'engrais a été appliqué, false sinon (ex: pas de plante ou déjà mature)
     */
    public boolean fertilizer() {
        if (this.plant != null) {
            return this.plant.applyFertilizer();
        }
        return false;
    }

    /**
     * Tente de récolter la plante présente sur cette case.
     * Si la plante est mûre, elle est récoltée et la case se vide.
     *
     * @return le gain en argent de la récolte, ou 0 si la plante n'était pas récoltable
     */
    public int harvest() {
        if (this.plant != null && this.plant.isHarvestable()) {
            int gain = this.plant.getType().getValue();
            this.plant = null; // La case se vide après la récolte
            return gain;
        }
        return 0;
    }

    /**
     * Nettoie la case en supprimant la plante morte ou pourrie.
     * Cette méthode peut être appelée après une récolte ratée ou pour préparer la case à une nouvelle plantation.
     */
    public void clean() {
        if (this.plant != null &&
                (this.plant.getState() == PlantState.MORT)) {
            this.plant = null;
        }
    }

    // --- GETTERS SPÉCIFIQUES ---

    public ImageIcon getSprite() {
        // Si la case contient une plante ET que cette plante a de l'eau
        if (this.plant != null && this.plant.isIrrigated()) {
            return WET_SPRITE; // On affiche la terre sombre
        }

        // Dans tous les autres cas
        return DRY_SPRITE; // On affiche la terre claire
    }

    public Plant getPlant() {
        return plant;
    }
}
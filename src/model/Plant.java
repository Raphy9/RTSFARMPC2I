package src.model;

/**
 * La classe Plant gère la logique de croissance, d'eau, de pourriture et de mort d'une plante.
 * Elle est indépendante de la vue et ne connaît que son type et son état.
 * C'est la "source de vérité" pour tout ce qui concerne une plante.
 */
public class Plant {

     // --- Constantes ---
    private static final float MAX_WATER_LEVEL = 100.0f;
    private static final int TIME_BEFORE_ROT = 500;   // Temps (ticks) avant de pourrir une fois mature
    private static final int TIME_BEFORE_DEATH = 100; // Temps (ticks) sans eau avant de mourir

    // --- Attributs ---
    private final PlantType type; // Le type de plante (Salade, Carotte, Tomate, etc.)
    private PlantState state; // L'état actuel de la plante (GRAINE, POUSSE, MATURE, MORT, POURRIE)

    private float currentWaterLevel; // Niveau d'eau actuel (0 à MAX_WATER_LEVEL)
    private int age;             // Progression de la croissance
    private int ticksWithoutWater; // Compteur pour la mort de soif
    private int timeSinceMature;   // Compteur pour la pourriture
    private boolean hasFertilizer; // Si de l'engrais a été mis

    // --- Constructeur ---
    public Plant(PlantType type) {
        this.type = type;
        this.state = PlantState.GRAINE;
        this.currentWaterLevel = 50.0f; // Humidité initiale
        this.age = 0;
        this.ticksWithoutWater = 0;
        this.timeSinceMature = 0;
        this.hasFertilizer = false;
    }

    /**
     * Méthode appelée à chaque cycle de jeu (Tick).
     * Gère l'eau, la croissance, la mort et la pourriture.
     */
    public void tick() {
        // Si la plante est morte ou pourrie, elle ne fait rien (mais l'eau s'évapore quand même)
        if (state == PlantState.MORT || state == PlantState.POURRIE) {
            if (currentWaterLevel > 0) currentWaterLevel -= 0.5f; // Évaporation naturelle
            return;
        }

        // 1. Gestion de l'Eau
        if (currentWaterLevel > 0) {
            currentWaterLevel -= type.getWaterConsumption();
            ticksWithoutWater = 0; // La plante boit, tout va bien
        } else {
            currentWaterLevel = 0;
            ticksWithoutWater++; // Le danger augmente (Sol sec)
        }

        // 2. Vérification des conditions de Mort (Soif)
        if (ticksWithoutWater >= TIME_BEFORE_DEATH) {
            state = PlantState.MORT;
            return; // Fin de la mise à jour
        }

        // 3. Gestion de la Croissance et des États
        if (state != PlantState.MATURE) {
            // La plante ne grandit que si elle a de l'eau
            if (currentWaterLevel > 0) {
                int croissance = 1;
                if (hasFertilizer) croissance = 2; // Bonus Engrais

                age += croissance;
                updateState();
            }
        } else {
            // Si elle est MATURE, on gère la pourriture
            timeSinceMature++;
            if (timeSinceMature >= TIME_BEFORE_ROT) {
                state = PlantState.POURRIE;
            }
        }
    }

    /** Mise à jour de l'état de la plante en fonction de son âge et de son type.
     * GRAINE -> POUSSE -> MATURE
     */
    private void updateState() {
        int duration = type.getGrowthDuration();

        if (state == PlantState.GRAINE) {
            if (age > duration / 3) {
                state = PlantState.POUSSE;
            }
        } else if (state == PlantState.POUSSE) {
            if (age >= duration) {
                state = PlantState.MATURE;
                timeSinceMature = 0;
            }
        }
    }

    // --- Actions ---

    /** Arrose la plante d'une certaine quantité d'eau.
     * @param amount Quantité d'eau à ajouter (en pourcentage, ex: 20.0f)
     */
    public void water(float amount) {
        this.currentWaterLevel += amount;
        if (this.currentWaterLevel > MAX_WATER_LEVEL) {
            this.currentWaterLevel = MAX_WATER_LEVEL;
        }
        // Si on arrose, on reset le compteur de mort de soif (sauvetage in extremis)
        if (state != PlantState.MORT && state != PlantState.POURRIE) {
            ticksWithoutWater = 0;
        }
    }

    /** Applique de l'engrais à la plante, ce qui accélère sa croissance.
     * @return true si l'engrais a été appliqué, false sinon (ex: déjà mature ou morte)
     */
    public boolean applyFertilizer() {
        // On ne peut mettre de l'engrais que sur une pousse ou graine, pas une plante mature
        if (this.hasFertilizer || this.state == PlantState.MATURE ||
                this.state == PlantState.MORT || this.state == PlantState.POURRIE) {
            return false;
        }
        this.hasFertilizer = true;
        return true;
    }

    // --- Getters & Helpers pour la Vue ---

    /**
     * INDISPENSABLE POUR L'AFFICHAGE
     * @return true si le sol est humide (Terre foncée), false si sec (Terre claire)
     */
    public boolean isIrrigated() {
        return currentWaterLevel > 0;
    }

    /** INDISPENSABLE POUR LA RÉCOLTE
     * @return true si la plante est mûre et peut être récoltée, false sinon
     */
    public boolean isHarvestable() {
        return state == PlantState.MATURE;
    }

    // Getters pour la Vue (type, état, niveau d'eau)
    public PlantType getType() { return type; }
    public PlantState getState() { return state; }
    public float getWaterLevel() { return currentWaterLevel; }

    // Pour debug ou barre de vie
    public float getGrowthPercentage() {
        return Math.min((float) age / type.getGrowthDuration(), 1.0f);
    }
}
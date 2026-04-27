package src.model;

/**
 * Enumération représentant les différents types de plantes que le joueur peut cultiver.
 * Chaque type de plante a des caractéristiques spécifiques (durée de croissance, consommation d'eau, valeur de vente).
 */
public enum PlantType {
    // Nom, Temps croissance (ticks), Soif (eau/tick), Prix vente
    CHOUX("Choux",       75, 0.3f,  4, 3),
    CAROTTE("Carotte",   100, 0.6f, 3, 3),
    CITROUILLE("Citrouille", 125, 1f, 7, 4, 3),
    FRAISE("Fraise",     100, 2f,  9, 5, 4);

    // --- Attributs ---
    private final String name;
    private final int growthDuration;   // Durée pour devenir mature
    private final float waterConsumption; // Eau bue par tick
    private final int value; // Prix de vente
    private int levelRequirement = 0; // Niveau requis pour débloquer cette plante
    private int expGain;

    /**
     * Constructeur de l'énumération PlantType.
     *
     * @param name              Le nom de la plante (ex: "Salade")
     * @param growthDuration    Le nombre de ticks nécessaires pour que la plante devienne mature
     * @param waterConsumption  La quantité d'eau consommée par tick
     * @param value             La valeur de vente de la plante une fois récoltée
     */
    PlantType(String name, int growthDuration, float waterConsumption, int value, int expGain) {
        this.name = name;
        this.growthDuration = growthDuration;
        this.waterConsumption = waterConsumption;
        this.value = value;
        this.levelRequirement = 0;
        this.expGain = expGain;
    }

    /**
     * Constructeur de l'énumération PlantType.
     *
     * @param name              Le nom de la plante (ex: "Salade")
     * @param growthDuration    Le nombre de ticks nécessaires pour que la plante devienne mature
     * @param waterConsumption  La quantité d'eau consommée par tick
     * @param value             La valeur de vente de la plante une fois récoltée
     */
    PlantType(String name, int growthDuration, float waterConsumption, int value, int expGain, int levelRequirement) {
        this.name = name;
        this.growthDuration = growthDuration;
        this.waterConsumption = waterConsumption;
        this.value = value;
        this.levelRequirement = levelRequirement;
        this.expGain = expGain;
    }

    // --- Getters ---
    public String getName() { return name; }
    public int getGrowthDuration() { return growthDuration; }
    public float getWaterConsumption() { return waterConsumption; }
    public int getValue() { return value; }
    public int getLevelRequirement() { return levelRequirement; }
    public int getExpGain() { return expGain; }
}
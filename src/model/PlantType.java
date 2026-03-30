package src.model;

/**
 * Enumération représentant les différents types de plantes que le joueur peut cultiver.
 * Chaque type de plante a des caractéristiques spécifiques (durée de croissance, consommation d'eau, valeur de vente).
 */
public enum PlantType {
    // Nom, Temps croissance (ticks), Soif (eau/tick), Prix vente
    CHOUX("Choux", 75, 0.5f, 6),
    CAROTTE("Carotte", 75, 0.5f, 10),
    CITROUILLE("Citrouille", 75, 0.5f, 25,5),
    FRAISE("Fraise", 75, 0.5f, 8,5);

    // --- Attributs ---
    private final String name;
    private final int growthDuration;   // Durée pour devenir mature
    private final float waterConsumption; // Eau bue par tick
    private final int value; // Prix de vente
    private int levelRequirement = 0; // Niveau requis pour débloquer cette plante

    /**
     * Constructeur de l'énumération PlantType.
     *
     * @param name              Le nom de la plante (ex: "Salade")
     * @param growthDuration    Le nombre de ticks nécessaires pour que la plante devienne mature
     * @param waterConsumption  La quantité d'eau consommée par tick
     * @param value             La valeur de vente de la plante une fois récoltée
     */
    PlantType(String name, int growthDuration, float waterConsumption, int value) {
        this.name = name;
        this.growthDuration = growthDuration;
        this.waterConsumption = waterConsumption;
        this.value = value;
        this.levelRequirement = 0;
    }

    /**
     * Constructeur de l'énumération PlantType.
     *
     * @param name              Le nom de la plante (ex: "Salade")
     * @param growthDuration    Le nombre de ticks nécessaires pour que la plante devienne mature
     * @param waterConsumption  La quantité d'eau consommée par tick
     * @param value             La valeur de vente de la plante une fois récoltée
     */
    PlantType(String name, int growthDuration, float waterConsumption, int value, int levelRequirement) {
        this.name = name;
        this.growthDuration = growthDuration;
        this.waterConsumption = waterConsumption;
        this.value = value;
        this.levelRequirement = levelRequirement;
    }

    // --- Getters ---
    public String getName() { return name; }
    public int getGrowthDuration() { return growthDuration; }
    public float getWaterConsumption() { return waterConsumption; }
    public int getValue() { return value; }
    public int getLevelRequirement() { return levelRequirement; }
}
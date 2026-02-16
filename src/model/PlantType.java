package src.model;

/**
 * Enumération représentant les différents types de plantes que le joueur peut cultiver.
 * Chaque type de plante a des caractéristiques spécifiques (durée de croissance, consommation d'eau, valeur de vente).
 */
public enum PlantType {
    // Nom, Temps croissance (ticks), Soif (eau/tick), Prix vente
    SALADE("Salade", 100, 20.0f, 5),   // Nom, Temps croissance (ticks), Soif (eau/tick), Prix vente
    CAROTTE("Carotte", 200, 15.0f, 10),
    TOMATE("Tomate", 300, 25.0f, 20);

    // --- Attributs ---
    private final String name;
    private final int growthDuration;   // Durée pour devenir mature
    private final float waterConsumption; // Eau bue par tick
    private final int value; // Prix de vente

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
    }

    // --- Getters ---
    public String getName() { return name; }
    public int getGrowthDuration() { return growthDuration; }
    public float getWaterConsumption() { return waterConsumption; }
    public int getValue() { return value; }
}
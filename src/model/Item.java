package src.model;

public abstract class Item {
    private PlantType plantType;
    private int quantity;

    /*
    Constructeur par défaut de tous les items
     */
    public Item(PlantType pType) {
        quantity = 0;
        plantType = plantType;
    }

    /*
    Méthode pour récupérer la quantité de l'item
     */
    public int getQuantity() {
        // Renvoie la quantité
        return quantity;
    }

    /*
    Méthode pour récupérer le type de l'item
     */
    public PlantType getPlantType() {
        // Renvoie le type de la graine/plante
        return plantType;
    }
}

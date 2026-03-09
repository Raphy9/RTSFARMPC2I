package src.model;

import javax.swing.*;

public abstract class Item {
    private ImageIcon image;
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
    Méthode pour ajouter une certaine quantité de cet item
     */
    public void addQuantity(int x) {
        quantity += x;
    }

    /*
    Méthode pour retirer une certaine quantité de cet item
     */
    public void removeQuantity(int x) {
        quantity -= x;
    }

    /*
    Méthode pour récupérer le type de l'item
     */
    public PlantType getPlantType() {
        // Renvoie le type de la graine/plante
        return plantType;
    }

    public ImageIcon getImage() {
        return image;
    }
}

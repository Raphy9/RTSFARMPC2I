package src.model;

import javax.swing.*;

public abstract class Item {
    private ImageIcon image; // Image de l'item, définie dans les classes filles en fonction du type de plante (image différente pour les graines et les plantes)
    private PlantType plantType; // Type de l'item, qui correspond au type de plante (choux, carotte, citrouille, fraise)
    private int quantity; // Quantité de l'item, qui peut être modifiée en ajoutant ou retirant des items (ex: récolter une plante ajoute une plante à l'inventaire et retire une graine, planter une graine retire une graine de l'inventaire)

    /*
    Constructeur par défaut de tous les items
     */
    public Item(PlantType pType) {
        quantity = 0;
        plantType = pType;
    }

    /*
    Constructeur pour créer un item avec une quantité définie (uitilisé pour tester l'inventaire sans gameplay pour l'instant)
     */
    public Item(PlantType pType, int quantity) {
        this.quantity = quantity;
        this.plantType = pType;
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

    /*
    Méthode pour récupérer l'image de l'item
     */
    public ImageIcon getImage() {
        return image;
    }

    /*
    Méthode pour définir l'image de l'item, utilisée dans les classes filles pour définir l'image en fonction du type de plante
     */
    public void defineImage(ImageIcon image) {
        this.image = image;
    }
}

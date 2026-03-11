package src.model;

import javax.swing.*;

public class ItemPlant extends Item {
    /*
    Constructeur (par défaut) de la classe ItemPlant, qui prend en paramètre le type de plante (PlantType) et initialise l'image correspondante.
     */
    public ItemPlant(PlantType plantType) {
        // Appelle du constructeur de la classe mère Item pour initialiser le type de plante et la quantité (0 par défaut)
        super(plantType);

        initializeImage();
    }

    /*
    Constructeur de la classe ItemPlant, qui prend en paramètre le type de plante (PlantType) et la quantité de plantes (int quantity) et initialise l'image correspondante,
    pour permettre de créer un item avec une quantité définie (uitilisé pour tester l'inventaire sans gameplay pour l'instant)
     */
    public ItemPlant(PlantType plantType, int quantity) {
        // Appelle du constructeur de la classe mère Item pour initialiser le type de plante et la quantité
        super(plantType, quantity);

        initializeImage();
    }

    /*
    Méthode pour définir l'image de l'item en fonction du type (PlantType)
    */
    private void initializeImage() {
        // On utilise une image différente pour les plantes que pour les graines, on affiche l'image de la plante prête à être récoltée
        // On utilise un switch pour définir l'image en fonction du type de plante
        switch (getPlantType()) {
            case CHOUX -> super.defineImage(new javax.swing.ImageIcon("src/assets/CropSprites/choux/done.png"));
            case CAROTTE -> super.defineImage(new javax.swing.ImageIcon("src/assets/CropSprites/carotte/done.png"));
            case CITROUILLE -> super.defineImage(new javax.swing.ImageIcon("src/assets/CropSprites/citrouille/done.png"));
            case FRAISE -> super.defineImage(new javax.swing.ImageIcon("src/assets/CropSprites/fraise/done.png"));
        }
    }

}

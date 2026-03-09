package src.model;

import javax.swing.*;

public class ItemPlant extends Item {
    public ItemPlant(PlantType plantType) {
        super(plantType);
        // Ajouter l'image en fonction du PlantType
    }

    /*
    Méthode pour définir l'image de l'item en fonction du type (PlantType)
     */
    private void defineImage() {
        switch (getPlantType()) {
            // définir les images en fonction du plantType
        }
    }

}

package src.model;

public class ItemSeed extends Item {

    /*
    Constructeur de la classe ItemSeed, qui prend en paramètre le type de plante (PlantType) et initialise l'image correspondante.
     */
    public ItemSeed(PlantType plantType) {
        super(plantType);
        initializeImage();
    }

    /*
    Constructeur de la classe ItemSeed, qui prend en paramètre le type de plante (PlantType) et la quantité de graines (int quantity) et initialise l'image correspondante,
    pour permettre de créer un item avec une quantité définie (uitilisé pour tester l'inventaire sans gameplay pour l'instant)
     */
    public ItemSeed(PlantType plantType, int quantity) {
        super(plantType, quantity);
        initializeImage();
    }

    /*
    Méthode pour définir l'image de l'item en fonction du type (PlantType)
    */
    private void initializeImage() {
        // On utilise une image différente pour les graines que pour les plantes, on affiche l'image du paquet de graines
        // On utilise un switch pour définir l'image en fonction du type de plante
        switch (getPlantType()) {
            case CHOUX -> super.defineImage(new javax.swing.ImageIcon("src/assets/CropSprites/choux/seedpack.png"));
            case CAROTTE -> super.defineImage(new javax.swing.ImageIcon("src/assets/CropSprites/carotte/seedpack.png"));
            case CITROUILLE -> super.defineImage(new javax.swing.ImageIcon("src/assets/CropSprites/citrouille/seedpack.png"));
            case FRAISE -> super.defineImage(new javax.swing.ImageIcon("src/assets/CropSprites/fraise/seedpack.png"));
        }
    }
}

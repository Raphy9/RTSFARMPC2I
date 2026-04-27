package src.model.actions;

import src.model.*;

/**
 * Action concrète exécutée par le jardinier pour planter une graine sur le terrain.
 * Cette action est conçue pour s'exécuter depuis une case adjacente (execX,execY) :
 * le MoveAction précédent amènera le jardinier sur execX/execY, puis cette action
 * plantera sur plantX/plantY sans déplacer le jardinier.
 */
public class PlantAction extends Action {

    // Informations nécessaires pour planter : le type de plante et les coordonnées de la parcelle à planter
    private PlantType plantType;
    private int plantX, plantY; // coordonnées de la parcelle à planter

    /**
     * Constructeur de PlantAction.
     * @param execX Les coordonnées x de la tuile d'exécution (adjacente à la parcelle à planter).
     * @param execY Les coordonnées y de la tuile d'exécution.
     * @param plantX Les coordonnées x de la parcelle où planter.
     * @param plantY Les coordonnées y de la parcelle où planter.
     * @param plantType Le type de plante à planter (ex: chou, carotte, etc.).
     */
    public PlantAction(int execX, int execY, int plantX, int plantY, PlantType plantType) {
        super(execX, execY);
        this.plantX = plantX;
        this.plantY = plantY;
        this.plantType = plantType;
    }

    /**
     * Cette méthode est appelée lorsque le jardinier arrive sur la tuile d'exécution (execX, execY).
     * Elle vérifie que la tuile ciblée pour planter (plantX, plantY) est une PlantTile et qu'elle est encore libre (farmable).
     * Si c'est le cas, elle plante la graine de type plantType dans cette parcelle, ce qui crée un objet Plant dans la case.
     * Ensuite, elle déduit 1 unité de la graine utilisée depuis l'inventaire du jardinier.
     * Si la parcelle n'est pas plantable ou si elle est déjà occupée au moment de l'exécution, un message d'erreur est affiché dans la console.
     */
    @Override
    public void perform(Gardener gardener, World world) {
        if (world.hasBuildingAt(plantX, plantY)) {
            System.out.println("Erreur : Impossible de planter, un bâtiment occupe la case ciblée.");
            return;
        }

        // On verifie que le jardinier a bien la graine dans son inventaire au moment de planter
        Inventory inv = gardener.getInventory();
        if (inv.findSameItem(new ItemSeed(plantType)) == null) {
            System.out.println("Attention : Le jardinier n'a plus de graines, impossible de planter !");
            return;
        }
        //  On vérifie que la parcelle est plantable
        Tile tile = world.getTile(plantX, plantY);
        if (tile instanceof PlantTile) {
            PlantTile parcel = (PlantTile) tile;

            // On vérifie que la case est toujours libre (farmable) au moment où l'action est exécutée
            if (parcel.isFarmable()) {

                // On plante la graine ! (Cela va créer l'objet Plant dans la case)
                boolean success = parcel.plant(plantType);

                if (success) {
                    System.out.println("Succès : Le jardinier a planté " + plantType.getName() + " !");

                    SoundManager.playSound(SoundManager.PLANT);

                    //  On déduit la graine utilisée depuis l'inventaire du jardinier
                    // Créer un item prototype pour rechercher la pile correspondante
                    ItemSeed prototype = new ItemSeed(plantType);
                    src.model.Item same = inv.findSameItem(prototype);
                    if (same instanceof ItemSeed) {
                        same.removeQuantity(1);
                        if (same.getQuantity() <= 0) {
                            inv.removeItem(same);
                        }
                    } else {
                        System.out.println("Attention : pas de graine dans l'inventaire du jardinier au moment de planter.");
                    }

                }
            } else {
                System.out.println("Trop tard ! La case est déjà occupée.");
            }
        } else {
            System.out.println("Erreur : Ce n'est pas une case plantable.");
        }
    }
}
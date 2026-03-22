package src.test;

import org.junit.jupiter.api.Test;
import src.model.Inventory;
import src.model.ItemSeed;
import src.model.ItemPlant;

import static org.junit.jupiter.api.Assertions.*;

/** * Test de transfert d'items entre deux inventaires, en vérifiant que les quantités sont correctement mises à jour et que les items sont fusionnés ou supprimés si nécessaire.
 */
public class InventoryTransferTest {

    /** Test de transfert d'items entre deux inventaires, en vérifiant que les quantités sont correctement mises à jour et que les items sont fusionnés ou supprimés si nécessaire.
     * On teste le transfert de graines entre la grange et un jardinier, en vérifiant que les quantités sont mises à jour dans les deux inventaires,
     * que les items sont fusionnés dans l'inventaire du jardinier si il a déjà des graines du même type, et que les items sont supprimés de l'inventaire de la grange si il n'en reste plus.
     */
    @Test
    public void testTransferMergeAndRemove() {
        Inventory barn = new Inventory();
        Inventory gardener = new Inventory();

        ItemSeed seedsBarn = new ItemSeed(src.model.PlantType.CAROTTE, 5);
        ItemSeed seedsGard = new ItemSeed(src.model.PlantType.CAROTTE, 2);

        barn.addItem(seedsBarn);
        gardener.addItem(seedsGard);

        // Transfer 3 seeds from barn to gardener
        int transferred = barn.transferTo(gardener, seedsBarn, 3);
        assertEquals(3, transferred);

        // Gardener should now have 5 (2+3)
        ItemSeed gItem = (ItemSeed) gardener.findSameItem(new ItemSeed(src.model.PlantType.CAROTTE));
        assertNotNull(gItem);
        assertEquals(5, gItem.getQuantity());

        // Barn should have 2 left
        ItemSeed bItem = (ItemSeed) barn.findSameItem(new ItemSeed(src.model.PlantType.CAROTTE));
        assertNotNull(bItem);
        assertEquals(2, bItem.getQuantity());

        // Transfer more than available -> only transfer available
        transferred = barn.transferTo(gardener, bItem, 5);
        assertEquals(2, transferred);

        // Now barn should no longer have the item
        assertNull(barn.findSameItem(new ItemSeed(src.model.PlantType.CAROTTE)));

        // Gardener should have 7
        gItem = (ItemSeed) gardener.findSameItem(new ItemSeed(src.model.PlantType.CAROTTE));
        assertEquals(7, gItem.getQuantity());
    }
}

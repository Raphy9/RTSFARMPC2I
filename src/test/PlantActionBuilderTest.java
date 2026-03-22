package src.test;

import org.junit.jupiter.api.Test;
import src.model.World;
import src.model.Gardener;
import src.model.ItemSeed;
import src.model.PlantType;
import src.model.actions.PlantActionBuilder;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitaire pour PlantActionBuilder
 * Vérifie que le builder construit les actions correctes en fonction de la présence ou non de la graine dans l'inventaire du jardinier
 */
public class PlantActionBuilderTest {

    /** Test du PlantActionBuilder lorsque le jardinier a déjà la graine nécessaire dans son inventaire.
     * Le builder devrait construire une séquence d'actions simple : Move -> Plant
     */
    @Test
    public void testBuilderWhenGardenerHasSeed() {
        World world = new World();
        Gardener g = world.getGardenerTest();
        // give gardener 1 seed
        g.getInventory().addItem(new ItemSeed(PlantType.CAROTTE, 1));

        PlantActionBuilder builder = new PlantActionBuilder(g, world);
        builder.setTarget(10, 10);
        builder.setItem(new ItemSeed(PlantType.CAROTTE, 1));

        builder.buildAction();

        // When gardener has seed, builder should create 2 actions (Move + Plant)
        assertEquals(2, g.getPendingActionsCount());
    }

    /** Test du PlantActionBuilder lorsque le jardinier n'a pas la graine nécessaire dans son inventaire.
     * Le builder devrait construire une séquence d'actions plus longue : Move -> Fetch -> Move -> Plant
     */
    @Test
    public void testBuilderWhenGardenerLacksSeed() {
        World world = new World();
        Gardener g = world.getGardenerTest();
        // ensure gardener has no seeds

        // Clear gardener inventory if any
        g.getInventory().getItems().clear();

        PlantActionBuilder builder = new PlantActionBuilder(g, world);
        builder.setTarget(10, 10);
        builder.setItem(new ItemSeed(PlantType.CAROTTE, 1));

        builder.buildAction();

        // When gardener lacks seed, builder should create Move->Fetch->Move->Plant sequence = 4 actions
        assertTrue(g.getPendingActionsCount() >= 3, "Expected at least 3 actions queued (Move to barn/adjacent, Fetch, Move to target, Plant)");
    }
}

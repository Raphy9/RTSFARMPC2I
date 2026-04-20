package src.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.model.PlantTile;
import src.model.PlantState;
import src.model.PlantType;

import static org.junit.jupiter.api.Assertions.*;

class CasePlantableTest {

    private PlantTile tuile;

    @BeforeEach
    void setUp() {
        tuile = new PlantTile(0, 0);
    }

    @Test
    void testInitialProperties() {
        assertNull(tuile.getPlant(), "Il ne doit pas y avoir de plante au début");
        assertTrue(tuile.isFarmable(), "Une case plantable doit être prête à planter dès le début (pas besoin de labourer)");
    }

    @Test
    void testPlanterRules() {
        boolean success = tuile.plant(PlantType.CAROTTE);

        assertTrue(success, "La plantation doit réussir immédiatement");
        assertNotNull(tuile.getPlant(), "La plante doit être présente après plantation");
        assertEquals(PlantType.CAROTTE, tuile.getPlant().getType(), "Le type de plante doit être celui qui a été planté");

        assertFalse(tuile.isFarmable(), "La case ne doit plus accepter de nouvelles graines une fois occupée");

        boolean doublePlant = tuile.plant(PlantType.CHOUX);
        assertFalse(doublePlant, "Impossible de planter si la case est déjà occupée");

        assertEquals(PlantType.CAROTTE, tuile.getPlant().getType(), "La plante originale ne doit pas être remplacée");
    }

    @Test
    void testRecolteLogic() {
        tuile.plant(PlantType.CHOUX);
        int duration = PlantType.CHOUX.getGrowthDuration();

        int gain = tuile.harvest();
        assertEquals(0, gain, "On ne gagne rien sur une plante pas mûre");
        assertNotNull(tuile.getPlant(), "La plante doit rester là tant qu'elle n'est pas récoltée");

        for (int i = 0; i < duration + 1; i++) {
            tuile.water();
            tuile.tick();
        }

        assertNotNull(tuile.getPlant());
        assertEquals(PlantState.MATURE, tuile.getPlant().getState());

        gain = tuile.harvest();
        assertEquals(PlantType.CHOUX.getValue(), gain, "Le gain doit correspondre à la valeur de la plante");
        assertNull(tuile.getPlant(), "La case doit être vide après récolte");
        assertTrue(tuile.isFarmable(), "La case doit être de nouveau plantable après la récolte");
    }

    @Test
    void testNettoyagePlanteMorte() {
        tuile.plant(PlantType.CHOUX);

        for (int i = 0; i < 200; i++) {
            tuile.tick();
        }

        assertNotNull(tuile.getPlant());
        assertEquals(PlantState.MORT, tuile.getPlant().getState());
        assertEquals(0, tuile.harvest());

        tuile.clean();

        assertNull(tuile.getPlant(), "La plante morte doit être retirée après nettoyage");
        assertTrue(tuile.isFarmable(), "La case doit être de nouveau disponible");
    }

    @Test
    void testMettreEngrais() {
        assertFalse(tuile.fertilizer(), "Impossible de mettre de l'engrais sur une case vide");

        tuile.plant(PlantType.CHOUX);
        int duration = PlantType.CHOUX.getGrowthDuration();

        assertTrue(tuile.fertilizer(), "L'application d'engrais doit réussir sur une jeune plante");
        assertFalse(tuile.fertilizer(), "Impossible de mettre de l'engrais deux fois");

        int ticksToMatureFertilized = duration / 2;
        for (int i = 0; i < ticksToMatureFertilized; i++) {
            tuile.water();
            tuile.tick();
        }

        assertNotNull(tuile.getPlant());
        assertEquals(PlantState.MATURE, tuile.getPlant().getState(),
                "La plante fertilisée devrait être mature en moitié moins de temps");

        assertTrue(tuile.harvest() > 0);
    }

    @Test
    void testCaseBloqueeParBatimentNePeutPasPlanter() {
        tuile.setPlantingBlocked(true);

        assertFalse(tuile.isFarmable(), "Une case plantable occupée par un bâtiment ne doit plus être farmable");
        assertFalse(tuile.plant(PlantType.CAROTTE), "La plantation doit être refusée si la case est bloquée");

        tuile.setPlantingBlocked(false);
        assertTrue(tuile.plant(PlantType.CAROTTE), "La plantation doit redevenir possible après retrait du bâtiment");
    }
}
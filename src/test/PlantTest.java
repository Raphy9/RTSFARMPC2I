package src.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.model.Plant;
import src.model.PlantState;
import src.model.PlantType;

import static org.junit.jupiter.api.Assertions.*;

class PlantTest {

    private Plant plant;

    @BeforeEach
    void setUp() {
        // On initialise un CHOUX avant chaque test (Durée: 120 ticks, Eau: 20.0f/tick)
        plant = new Plant(PlantType.CHOUX);
    }

    @Test
    void testInitialState() {
        assertEquals(PlantState.GRAINE, plant.getState(), "La plante doit commencer à l'état GRAINE");
        assertEquals(50.0f, plant.getWaterLevel(), "L'eau initiale doit être de 50");
        assertTrue(plant.isIrrigated(), "Le sol doit être considéré comme irrigué au début");
        assertNotNull(plant.getSprite(), "Le sprite initial ne doit pas être null");
    }

    @Test
    void testWaterConsumption() {
        plant.tick();
        // Choux consomme 20 par tick. 50 - 20 = 30.
        assertEquals(30.0f, plant.getWaterLevel(), 0.01, "L'eau doit baisser selon la consommation du type");
    }

    @Test
    void testGrowthCycleNormal() {
        // Le chou met 120 ticks pour être mature.
        int duration = plant.getType().getGrowthDuration(); // Récupère la durée (120)

        // 1. Passage de GRAINE à POUSSE (âge > 33% de 120, soit 39.6) -> Atteint à 40 ticks
        int ticksToPousse = (int) (duration * 0.33f) + 1; // 40 ticks
        for (int i = 0; i < ticksToPousse; i++) {
            plant.water(Plant.MAX_WATER_LEVEL); // On garde l'eau à fond
            plant.tick();
        }
        assertEquals(PlantState.POUSSE, plant.getState(), "Après " + ticksToPousse + " ticks, le chou doit être une POUSSE");

        // 2. Passage de POUSSE à CROISSANCE (âge > 66% de 120, soit 79.2) -> Atteint à 80 ticks
        // On est déjà à 40 ticks, il en faut 40 de plus.
        int ticksToCroissance = (int) (duration * 0.66f) + 1 - ticksToPousse; // 80 - 40 = 40 ticks
        for (int i = 0; i < ticksToCroissance; i++) {
            plant.water(Plant.MAX_WATER_LEVEL);
            plant.tick();
        }
        assertEquals(PlantState.CROISSANCE, plant.getState(), "Après " + (ticksToPousse + ticksToCroissance) + " ticks, le chou doit être en CROISSANCE");

        // 3. Passage de CROISSANCE à MATURE (âge >= 100% de 120) -> Atteint à 120 ticks
        // On est à 80 ticks, il en faut 40 de plus.
        int ticksToMature = duration - (ticksToPousse + ticksToCroissance); // 120 - 80 = 40 ticks
        for (int i = 0; i < ticksToMature; i++) {
            plant.water(Plant.MAX_WATER_LEVEL);
            plant.tick();
        }
        assertEquals(PlantState.MATURE, plant.getState(), "Après " + duration + " ticks, le chou doit être MATURE");
        assertTrue(plant.isHarvestable(), "La plante doit être récoltable");
    }

    @Test
    void testDeathByThirst() {
        // 50 eau / 20 conso = 3 ticks d'autonomie.
        // Ensuite, il faut 100 ticks sans eau pour mourir. Le test avec 200 ticks est donc largement suffisant.
        for (int i = 0; i < 200; i++) {
            plant.tick(); // On ne remet JAMAIS d'eau
        }

        assertEquals(0.0f, plant.getWaterLevel(), "L'eau doit être à 0");
        assertFalse(plant.isIrrigated(), "Le sol doit être sec");
        assertEquals(PlantState.MORT, plant.getState(), "La plante doit être MORTE de soif");
        assertNotNull(plant.getSprite(), "Le sprite de la plante morte ne doit pas être null");
    }

    @Test
    void testFertilizer() {
        boolean success = plant.applyFertilizer();
        assertTrue(success, "L'application d'engrais doit réussir sur une graine");

        // Avec engrais, la croissance est double (+2 age par tick)
        // Choux : 120 ticks normalement. Avec engrais => 60 ticks.
        int duration = plant.getType().getGrowthDuration();
        int ticksToMatureFertilized = duration / 2;

        for (int i = 0; i < ticksToMatureFertilized; i++) {
            plant.water(Plant.MAX_WATER_LEVEL);
            plant.tick();
        }

        assertEquals(PlantState.MATURE, plant.getState(), "La plante fertilisée doit être mature après " + ticksToMatureFertilized + " ticks");
    }

    @Test
    void testWaterCap() {
        plant.water(1000.0f);
        assertEquals(Plant.MAX_WATER_LEVEL, plant.getWaterLevel(), "L'eau ne doit jamais dépasser 100");
    }
}
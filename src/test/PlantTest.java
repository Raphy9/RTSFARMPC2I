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
        // On initialise une Salade avant chaque test (Durée: 100 ticks, Eau: 20.0f/tick)
        plant = new Plant(PlantType.SALADE);
    }

    @Test
    void testInitialState() {
        assertEquals(PlantState.GRAINE, plant.getState(), "La plante doit commencer à l'état GRAINE");
        assertEquals(50.0f, plant.getWaterLevel(), "L'eau initiale doit être de 50");
        assertTrue(plant.isIrrigated(), "Le sol doit être considéré comme irrigué au début");
    }

    @Test
    void testWaterConsumption() {
        plant.tick();
        // Salade consomme 20 par tick. 50 - 20 = 30.
        assertEquals(30.0f, plant.getWaterLevel(), 0.01, "L'eau doit baisser selon la consommation du type");
    }

    @Test
    void testGrowthCycleNormal() {
        // La salade met 100 ticks pour être mature.
        // On garde l'eau à fond pour éviter la mort

        // 1. Passage de GRAINE à POUSSE (> 33 ticks)
        for (int i = 0; i < 35; i++) {
            plant.water(20); // On recharge l'eau
            plant.tick();
        }
        assertEquals(PlantState.POUSSE, plant.getState(), "Après ~35 ticks, la salade doit être une POUSSE");

        // 2. Passage de POUSSE à MATURE (>= 100 ticks total)
        for (int i = 0; i < 70; i++) {
            plant.water(20);
            plant.tick();
        }
        assertEquals(PlantState.MATURE, plant.getState(), "Après 100 ticks, la salade doit être MATURE");
        assertTrue(plant.isHarvestable(), "La plante doit être récoltable");
    }

    @Test
    void testDeathByThirst() {
        // On vide l'eau manuellement ou on attend
        // 50 eau / 20 conso = 3 ticks d'autonomie.
        // Ensuite, il faut 100 ticks sans eau pour mourir.

        for (int i = 0; i < 200; i++) {
            plant.tick(); // On ne remet JAMAIS d'eau
        }

        assertEquals(0.0f, plant.getWaterLevel(), "L'eau doit être à 0");
        assertFalse(plant.isIrrigated(), "Le sol doit être sec");
        assertEquals(PlantState.MORT, plant.getState(), "La plante doit être MORTE de soif");
    }

    @Test
    void testRotting() {
        // 1. On amène la plante à maturité rapidement
        for (int i = 0; i < 110; i++) {
            plant.water(100);
            plant.tick();
        }
        assertEquals(PlantState.MATURE, plant.getState());

        // 2. On attend 500 ticks pour qu'elle pourrisse
        for (int i = 0; i < 501; i++) {
            plant.water(100);
            plant.tick();
        }

        assertEquals(PlantState.POURRIE, plant.getState(), "La plante doit pourrir si on ne la récolte pas");
        assertFalse(plant.isHarvestable(), "Une plante pourrie ne se récolte pas");
    }

    @Test
    void testFertilizer() {
        // On applique l'engrais
        boolean success = plant.applyFertilizer();
        assertTrue(success, "L'application d'engrais doit réussir sur une graine");

        // Avec engrais, la croissance est double (+2 age par tick)
        // Salade : 100 ticks normalement. Avec engrais => 50 ticks.

        for (int i = 0; i < 51; i++) {
            plant.water(100);
            plant.tick();
        }

        assertEquals(PlantState.MATURE, plant.getState(), "La plante fertilisée doit pousser 2x plus vite");
    }

    @Test
    void testWaterCap() {
        plant.water(1000.0f);
        assertEquals(100.0f, plant.getWaterLevel(), "L'eau ne doit jamais dépasser 100");
    }
}
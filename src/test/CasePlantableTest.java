package src.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.model.CasePlantable;
import src.model.PlantState;
import src.model.PlantType;

import static org.junit.jupiter.api.Assertions.*;

class CasePlantableTest {

    private CasePlantable tuile;

    @BeforeEach
    void setUp() {
        tuile = new CasePlantable(0, 0);
    }

    @Test
    void testInitialProperties() {
        // Vérifie que la case est vide
        assertNull(tuile.getPlant(), "Il ne doit pas y avoir de plante au début");

        // CHANGEMENT : La case doit être cultivable IMMÉDIATEMENT
        assertTrue(tuile.isFarmable(), "Une case plantable doit être prête à planter dès le début (pas besoin de labourer)");
    }

    @Test
    void testPlanterRules() {
        // 1. Essai de plantation direct (doit fonctionner maintenant)
        boolean success = tuile.planter(PlantType.TOMATE);

        assertTrue(success, "La plantation doit réussir immédiatement");
        assertNotNull(tuile.getPlant(), "La plante doit être présente après plantation");
        assertEquals(PlantType.TOMATE, tuile.getPlant().getType());

        // Une fois plantée, la case ne doit plus être 'farmable' (car occupée)
        assertFalse(tuile.isFarmable(), "La case ne doit plus accepter de nouvelles graines une fois occupée");

        // 2. Essai par dessus une plante existante (Double plantation)
        boolean doublePlant = tuile.planter(PlantType.CAROTTE);
        assertFalse(doublePlant, "Impossible de planter si la case est déjà occupée");

        // Vérifie que c'est toujours une tomate et pas une carotte
        assertEquals(PlantType.TOMATE, tuile.getPlant().getType());
    }

    @Test
    void testRecolteLogic() {
        // On plante directement
        tuile.planter(PlantType.SALADE); // Prix supposé : 5 (selon ton Enum)

        // Essai de récolte immédiate (pas mûre)
        int gain = tuile.recolter();
        assertEquals(0, gain, "On ne gagne rien sur une plante pas mûre");
        assertNotNull(tuile.getPlant(), "La plante doit rester là tant qu'elle n'est pas récoltée");

        // On fait vieillir la plante via la tuile (Simulation du temps)
        // Supposons que Salade = 100 ticks pour maturité
        for (int i = 0; i < 110; i++) {
            tuile.arroser(); // On garde la plante en vie
            tuile.tick();
        }

        // Vérification maturité
        assertEquals(PlantState.MATURE, tuile.getPlant().getState());

        // Récolte valide
        gain = tuile.recolter();
        assertTrue(gain > 0, "Le gain doit être positif pour une récolte réussie");
        assertNull(tuile.getPlant(), "La case doit être vide après récolte");
        assertTrue(tuile.isFarmable(), "La case doit être de nouveau plantable après la récolte");
    }

    @Test
    void testNettoyagePlanteMorte() {
        tuile.planter(PlantType.SALADE);

        // On tue la plante (pas d'eau pendant > 100 ticks + autonomie)
        for (int i = 0; i < 200; i++) {
            tuile.tick(); // Pas d'arrosage -> Mort de soif
        }

        assertNotNull(tuile.getPlant());
        assertEquals(PlantState.MORT, tuile.getPlant().getState());

        // On essaie de récolter une plante morte -> Ça doit échouer (return 0)
        assertEquals(0, tuile.recolter());

        // On nettoie (coup de pelle)
        tuile.nettoyer();

        assertNull(tuile.getPlant(), "La plante morte doit être retirée après nettoyage");
        assertTrue(tuile.isFarmable(), "La case doit être de nouveau disponible");
    }

    @Test
    void testMettreEngrais() {
        // 1. Essai sur case vide
        assertFalse(tuile.mettreEngrais(), "Impossible de mettre de l'engrais sur une case vide");

        // On plante une Salade (Durée normale : 100 ticks)
        tuile.planter(PlantType.SALADE);

        // 2. Essai valide
        assertTrue(tuile.mettreEngrais(), "L'application d'engrais doit réussir sur une jeune plante");

        // 3. Essai double (Interdit)
        assertFalse(tuile.mettreEngrais(), "Impossible de mettre de l'engrais deux fois");

        // 4. Vérification de l'accélération (Logique F3)
        // Normalement, une salade met 100 ticks. Avec engrais (+2 age/tick), elle doit mettre 50 ticks.

        for (int i = 0; i < 51; i++) {
            tuile.arroser(); // Toujours arroser pour éviter la mort
            tuile.tick();
        }

        // Au bout de 51 ticks, elle doit être MATURE (alors que sans engrais, elle serait encore POUSSE)
        assertEquals(PlantState.MATURE, tuile.getPlant().getState(),
                "La plante fertilisée devrait être mature en moitié moins de temps");

        // Et bien sûr, on peut la récolter
        assertTrue(tuile.recolter() > 0);
    }
}
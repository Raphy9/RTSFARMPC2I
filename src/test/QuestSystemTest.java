package src.test;

import org.junit.jupiter.api.Test;
import src.model.PlantType;
import src.model.Quests;
import src.model.Quest;
import src.model.Stats;
import src.model.buildings.Sprinkler;
import src.model.buildings.Well;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests du système de quêtes.
 * On vérifie ici les points importants: progression, récompense, déblocage de chapitre et restauration.
 */
public class QuestSystemTest {

	@Test
	void questProgressAndRewardWork() {
		Stats stats = new Stats(100);
		Quest quest = Quest.createPlantQuest(
				"Q_TEST",
				"Planter des carottes",
				"Planter 2 carottes.",
				PlantType.CAROTTE,
				2,
				15,
				10
		);

		assertTrue(quest.matchesPlantEvent(PlantType.CAROTTE));
		assertFalse(quest.matchesHarvestEvent(PlantType.CAROTTE));

		assertFalse(quest.addProgress(1));
		assertEquals(1, quest.getProgress());
		assertFalse(quest.isCompleted());

		assertTrue(quest.addProgress(1));
		assertTrue(quest.isCompleted());

		assertTrue(quest.grantReward(stats));
		assertEquals(115, stats.getMoney());
		assertEquals(10, stats.getExp());
		assertFalse(quest.grantReward(stats));
	}

	@Test
	void questLinesUnlockInOrder() {
		Quests quests = new Quests();
		Stats stats = new Stats(0);

		assertNotNull(quests.getActiveQuestLine());
		assertEquals(0, quests.getActiveQuestLineIndex());

		// Chapitre 1
		quests.onBuild(new Well(), stats);
		quests.onAction(Quests.ACTION_PLOW_TILE, stats, 10);
		for (int i = 0; i < 5; i++) {
			quests.onPlant(PlantType.CAROTTE, stats);
		}
		quests.onAction(Quests.ACTION_WATER_TILE, stats, 5);
		for (int i = 0; i < 3; i++) {
			quests.onHarvest(PlantType.CAROTTE, stats);
		}
		assertEquals(1, quests.getActiveQuestLineIndex(), "Le chapitre 2 doit s'ouvrir après le chapitre 1");

		// Chapitre 2
		quests.onAction(Quests.ACTION_SELL_CAROTTE, stats, 3);
		quests.onAction(Quests.ACTION_CHASE_CHICKEN, stats, 3);
		quests.onAction(Quests.ACTION_DESTROY_OBSTACLE, stats, 7);
		quests.onBuild(new Well(), stats);
		quests.onBuild(new Well(), stats);
		quests.onAction(Quests.ACTION_DESTROY_BUILDING, stats);
		assertEquals(2, quests.getActiveQuestLineIndex(), "Le chapitre 3 doit s'ouvrir après le chapitre 2");

		// Chapitre 3
		quests.onAction(Quests.ACTION_PLOW_TILE, stats, 5);
		for (int i = 0; i < 5; i++) {
			quests.onPlant(PlantType.CHOUX, stats);
		}
		for (int i = 0; i < 3; i++) {
			quests.onHarvest(PlantType.CHOUX, stats);
		}
		quests.onAction(Quests.ACTION_SELL_CHOUX, stats, 3);
		quests.onAction(Quests.ACTION_REACH_LEVEL_3, stats);
		assertEquals(3, quests.getActiveQuestLineIndex(), "Le chapitre 4 doit s'ouvrir après le chapitre 3");

		// Chapitre 4
		quests.onAction(Quests.ACTION_PLOW_TILE, stats, 5);
		quests.onAction(Quests.ACTION_BUY_SEED_CITROUILLE, stats, 10);
		quests.onAction(Quests.ACTION_SELL_CITROUILLE, stats, 8);
		quests.onAction(Quests.ACTION_PLACE_FENCE, stats, 5);
		quests.onAction(Quests.ACTION_REACH_LEVEL_4, stats);
		assertEquals(4, quests.getActiveQuestLineIndex(), "Le chapitre 5 doit s'ouvrir après le chapitre 4");

		// Chapitre 5
		for (int i = 0; i < 20; i++) {
			quests.onHarvest(PlantType.FRAISE, stats);
		}
		quests.onAction(Quests.ACTION_SELL_FRAISE, stats, 20);
		quests.onBuild(new Sprinkler(), stats);
		quests.onBuild(new Well(), stats);
		quests.onBuild(new Well(), stats);
		quests.onBuild(new Well(), stats);
		quests.onBuild(new Well(), stats);
		quests.onBuild(new Well(), stats);
		quests.onBuild(new Well(), stats);
		quests.onBuild(new Well(), stats);
		quests.onBuild(new Well(), stats);
		quests.onBuild(new Well(), stats);
		quests.onAction(Quests.ACTION_REACH_LEVEL_5, stats);
		assertTrue(quests.isFinished(), "Toutes les lignes doivent être terminées après la dernière série de quêtes");
	}

	@Test
	void expThresholdsFollowConfiguredProgression() {
		Stats stats = new Stats();
		stats.addExp(100);
		assertEquals(2, stats.getLevel());
		assertEquals(0, stats.getExp());

		stats.addExp(150);
		assertEquals(3, stats.getLevel());
		assertEquals(0, stats.getExp());

		stats.addExp(150);
		assertEquals(4, stats.getLevel());
		assertEquals(0, stats.getExp());

		stats.addExp(200);
		assertEquals(5, stats.getLevel());
		assertEquals(0, stats.getExp());

		stats.addExp(500);
		assertEquals(7, stats.getLevel());
		assertEquals(0, stats.getExp());
	}

	@Test
	void questProgressSnapshotCanBeRestored() {
		Quests quests = new Quests();
		Stats stats = new Stats();

		quests.onPlant(PlantType.CAROTTE, stats);
		quests.onPlant(PlantType.CAROTTE, stats);

		List<List<Integer>> snapshot = quests.getProgressSnapshot();
		int activeLineIndex = quests.getActiveQuestLineIndex();

		Quests restored = new Quests();
		restored.restoreProgress(snapshot, activeLineIndex);

		assertEquals(activeLineIndex, restored.getActiveQuestLineIndex());
		assertEquals(snapshot, restored.getProgressSnapshot());
	}
}


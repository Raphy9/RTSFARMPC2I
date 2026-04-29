package src.model;

import src.model.buildings.Sprinkler;

public class Quests {
    public static final String ACTION_PLOW_TILE = "PLOW_TILE";
    public static final String ACTION_WATER_TILE = "WATER_TILE";
    public static final String ACTION_SELL_CAROTTE = "SELL_CAROTTE";
    public static final String ACTION_SELL_CHOUX = "SELL_CHOUX";
    public static final String ACTION_SELL_CITROUILLE = "SELL_CITROUILLE";
    public static final String ACTION_SELL_FRAISE = "SELL_FRAISE";
    public static final String ACTION_BUY_SEED_CITROUILLE = "BUY_SEED_CITROUILLE";
    public static final String ACTION_CHASE_CHICKEN = "CHASE_CHICKEN";
    public static final String ACTION_DESTROY_OBSTACLE = "DESTROY_OBSTACLE";
    public static final String ACTION_DESTROY_BUILDING = "DESTROY_BUILDING";
    public static final String ACTION_PLACE_FENCE = "PLACE_FENCE";
    public static final String ACTION_REACH_LEVEL_3 = "REACH_LEVEL_3";
    public static final String ACTION_REACH_LEVEL_4 = "REACH_LEVEL_4";
    public static final String ACTION_REACH_LEVEL_5 = "REACH_LEVEL_5";
    public static final String ACTION_CLICK_CROW = "CLICK_CROW";

    /**
     * Une ligne de quetes represente un chapitre : elle contient plusieurs quetes et se debloque en bloc.
     */
    public static class QuestLine {
        private final String title;
        private final String description;
        private final java.util.List<Quest> quests;
        private boolean unlocked;

        public QuestLine(String title, String description, java.util.List<Quest> quests) {
            this.title = title;
            this.description = description;
            this.quests = new java.util.ArrayList<>(quests);
            this.unlocked = false;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public java.util.List<Quest> getQuests() {
            return java.util.Collections.unmodifiableList(quests);
        }

        public boolean isUnlocked() {
            return unlocked;
        }

        private void setUnlocked(boolean unlocked) {
            this.unlocked = unlocked;
        }

        public boolean isCompleted() {
            for (Quest quest : quests) {
                if (!quest.isCompleted()) {
                    return false;
                }
            }
            return true;
        }

        public int getCompletedCount() {
            int count = 0;
            for (Quest quest : quests) {
                if (quest.isCompleted()) {
                    count++;
                }
            }
            return count;
        }

        public int getQuestCount() {
            return quests.size();
        }
    }

    private final java.util.List<QuestLine> questLines = new java.util.ArrayList<>();
    private int activeLineIndex = 0;
    private Runnable changeListener = null;

    public Quests() {
        buildDefaultQuestLines();
        if (!questLines.isEmpty()) {
            questLines.get(0).setUnlocked(true);
        }
        notifyChange();
    }

    /**
     * Chapitres simples et lisibles : le joueur voit une vraie progression par paliers.
     */
    private void buildDefaultQuestLines() {
        questLines.clear();

        questLines.add(new QuestLine(
                "Chapitre 1 - Premiers pas",
                "Lancer la ferme et maitriser les bases.",
                java.util.List.of(
                        Quest.createBuildQuest("Q1_BUILD_BARN", "Poser une grange", "Poser 1 grange.", null, 1, 20, 10),
                        Quest.createActionQuest("Q1_PLOW_10", "Labourer des parcelles", "Labourer 10 plant tiles.", ACTION_PLOW_TILE, 10, 10, 10),
                        Quest.createPlantQuest("Q1_PLANT_CAROTTE", "Planter des carottes", "Planter 5 carottes.", PlantType.CAROTTE, 5, 10, 10),
                        Quest.createActionQuest("Q1_WATER_5", "Arroser des cases", "Arroser 5 cases.", ACTION_WATER_TILE, 5, 10, 10),
                        Quest.createActionQuest("Q1_CHASE_CHICKEN", "Chasser des poules", "Chasser 3 poules.", ACTION_CHASE_CHICKEN, 3, 15, 15)
                )
        ));

        questLines.add(new QuestLine(
                "Chapitre 2 - Nettoyage de la ferme",
                "Vendre, chasser et amenager le terrain.",
                java.util.List.of(
                        Quest.createHarvestQuest("Q2_HARVEST_CAROTTE", "Recolter des carottes", "Recolter 3 carottes mures.", PlantType.CAROTTE, 3, 10, 15),
                        Quest.createActionQuest("Q2_SELL_CAROTTE", "Vendre des carottes", "Vendre 3 carottes.", ACTION_SELL_CAROTTE, 3, 20, 10),
                        Quest.createActionQuest("Q2_DESTROY_OBSTACLE", "Detruire des obstacles", "Detruire 7 obstacles.", ACTION_DESTROY_OBSTACLE, 7, 10, 20),
                        Quest.createBuildQuest("Q2_BUILD_2", "Poser des batiments", "Poser 2 batiments.", null, 2, 20, 15),
                        Quest.createActionQuest("Q2_DESTROY_BUILDING", "Detruire un batiment", "Detruire 1 batiment.", ACTION_DESTROY_BUILDING, 1, 15, 20)
                )
        ));

        questLines.add(new QuestLine(
                "Chapitre 3 - Filiere choux",
                "Specialisation choux et progression de niveau.",
                java.util.List.of(
                        Quest.createActionQuest("Q3_PLOW_5", "Labourer des parcelles", "Labourer 5 plant tiles.", ACTION_PLOW_TILE, 5, 10, 20),
                        Quest.createPlantQuest("Q3_PLANT_CHOUX", "Planter des choux", "Planter 5 choux.", PlantType.CHOUX, 5, 10, 20),
                        Quest.createHarvestQuest("Q3_HARVEST_CHOUX", "Recolter des choux", "Recolter 3 choux.", PlantType.CHOUX, 3, 15, 20),
                        Quest.createActionQuest("Q3_SELL_CHOUX", "Vendre des choux", "Vendre 3 choux a la grange.", ACTION_SELL_CHOUX, 3, 10, 20),
                        Quest.createActionQuest("Q3_LEVEL_3", "Passer niveau 3", "Atteindre le niveau 3.", ACTION_REACH_LEVEL_3, 1, 30, 30)
                )
        ));

        questLines.add(new QuestLine(
                "Chapitre 4 - Filiere citrouille",
                "Monter en puissance avec les citrouilles.",
                java.util.List.of(
                        Quest.createActionQuest("Q4_PLOW_5", "Labourer des parcelles", "Labourer 5 plant tiles.", ACTION_PLOW_TILE, 5, 15, 20),
                        Quest.createActionQuest("Q4_BUY_SEED_PUMPKIN", "Acheter des graines", "Acheter 10 graines de citrouille a la grange.", ACTION_BUY_SEED_CITROUILLE, 10, 15, 20),
                        Quest.createActionQuest("Q4_SELL_PUMPKIN", "Vendre des citrouilles", "Vendre 8 citrouilles a la grange.", ACTION_SELL_CITROUILLE, 8, 15, 25),
                        Quest.createActionQuest("Q4_PLACE_FENCE", "Poser des barrieres", "Poser 5 barrieres.", ACTION_PLACE_FENCE, 5, 20, 20),
                        Quest.createActionQuest("Q4_LEVEL_4", "Passer niveau 4", "Atteindre le niveau 4.", ACTION_REACH_LEVEL_4, 1, 30, 30)
                )
        ));

        questLines.add(new QuestLine(
                "Chapitre 5 - Maitrise totale",
                "Finaliser la ferme fraise et automatiser.",
                java.util.List.of(
                        Quest.createHarvestQuest("Q5_HARVEST_FRAISE", "Recolter des fraises", "Recolter 20 fraises.", PlantType.FRAISE, 20, 30, 30),
                        Quest.createActionQuest("Q5_CLICK_CROW", "Chasser des corbeaux", "Cliquer sur 5 corbeaux pour les chasser.", ACTION_CLICK_CROW, 5, 20, 30),
                        Quest.createBuildQuest("Q5_BUILD_SPRINKLER", "Poser un arroseur", "Poser un arroseur automatique.", Sprinkler.class, 1, 40, 35),
                        Quest.createBuildQuest("Q5_BUILD_10", "Poser des batiments", "Poser 10 batiments.", null, 10, 40, 35),
                        Quest.createActionQuest("Q5_LEVEL_5", "Passer niveau 5", "Atteindre le niveau 5.", ACTION_REACH_LEVEL_5, 1, 50, 40)
                )
        ));
    }

    public java.util.List<QuestLine> getQuestLines() {
        return java.util.Collections.unmodifiableList(questLines);
    }

    /** Permet a l'interface de se rafraîchir des qu'une quete evolue. */
    public void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
    }

    public QuestLine getActiveQuestLine() {
        if (activeLineIndex < 0 || activeLineIndex >= questLines.size()) {
            return null;
        }
        return questLines.get(activeLineIndex);
    }

    public int getActiveQuestLineIndex() {
        return activeLineIndex;
    }

    public boolean isFinished() {
        return activeLineIndex >= questLines.size();
    }

    public void onPlant(PlantType plantType, Stats stats) {
        handleEvent(Quest.Type.PLANT, plantType, null, stats);
    }

    public void onHarvest(PlantType plantType, Stats stats) {
        handleEvent(Quest.Type.HARVEST, plantType, null, stats);
    }

    public void onBuild(src.model.buildings.Building building, Stats stats) {
        handleEvent(Quest.Type.BUILD, null, building, stats);
    }

    public void onAction(String actionKey, Stats stats) {
        onAction(actionKey, stats, 1);
    }

    public void onAction(String actionKey, Stats stats, int amount) {
        handleEvent(Quest.Type.ACTION, null, null, actionKey, stats, amount);
    }

    /**
     * Fait avancer la premiere quete compatible du chapitre courant.
     * Quand toutes les quetes d'une ligne sont finies, le chapitre suivant se debloque.
     */
    private void handleEvent(Quest.Type type, PlantType plantType, src.model.buildings.Building building, Stats stats) {
        handleEvent(type, plantType, building, null, stats, 1);
    }

    private void handleEvent(Quest.Type type, PlantType plantType, src.model.buildings.Building building, String actionKey, Stats stats, int amount) {
        QuestLine line = getActiveQuestLine();
        if (line == null || !line.isUnlocked()) {
            return;
        }

        for (Quest quest : line.quests) {
            boolean matches = switch (type) {
                case PLANT -> quest.matchesPlantEvent(plantType);
                case HARVEST -> quest.matchesHarvestEvent(plantType);
                case BUILD -> quest.matchesBuildEvent(building);
                case ACTION -> quest.matchesActionEvent(actionKey);
            };

            if (!matches || quest.isCompleted()) {
                continue;
            }

            if (quest.addProgress(amount)) {
                SoundManager.playSound(SoundManager.QUEST_COMPLETE);
                if (quest.grantReward(stats)) {
                    notifyChange();
                }
            }

            if (line.isCompleted()) {
                unlockNextLine();
            }
            notifyChange();
            return;
        }
    }

    private void unlockNextLine() {
        if (activeLineIndex < questLines.size()) {
            activeLineIndex++;
            questLines.get(activeLineIndex).setUnlocked(true);
        } else {
            activeLineIndex = questLines.size();
        }
    }

    /**
     * Retourne un instantane simple des progres pour la sauvegarde.
     * La structure est : ligne de quetes -> liste des progressions de chaque quete.
     */
    public java.util.List<java.util.List<Integer>> getProgressSnapshot() {
        java.util.List<java.util.List<Integer>> snapshot = new java.util.ArrayList<>();
        for (QuestLine line : questLines) {
            java.util.List<Integer> lineProgress = new java.util.ArrayList<>();
            for (Quest quest : line.quests) {
                lineProgress.add(quest.getProgress());
            }
            snapshot.add(lineProgress);
        }
        return snapshot;
    }

    /**
     * Restaure les progres et le chapitre actif depuis une sauvegarde.
     */
    public void restoreProgress(java.util.List<java.util.List<Integer>> progresses, int restoredActiveLineIndex) {
        for (int i = 0; i < questLines.size(); i++) {
            QuestLine line = questLines.get(i);
            line.setUnlocked(i == 0);

            java.util.List<Integer> lineProgress = (progresses != null && i < progresses.size()) ? progresses.get(i) : java.util.Collections.emptyList();
            for (int q = 0; q < line.quests.size(); q++) {
                Quest quest = line.quests.get(q);
                int restoredProgress = (q < lineProgress.size()) ? lineProgress.get(q) : 0;
                boolean completed = restoredProgress >= quest.getGoal();
                quest.restoreState(restoredProgress, completed, completed);
            }
        }

        if (questLines.isEmpty()) {
            activeLineIndex = 0;
            notifyChange();
            return;
        }

        activeLineIndex = Math.max(0, Math.min(restoredActiveLineIndex, questLines.size()));
        for (int i = 0; i < activeLineIndex && i < questLines.size(); i++) {
            questLines.get(i).setUnlocked(true);
        }
        if (activeLineIndex < questLines.size()) {
            questLines.get(activeLineIndex).setUnlocked(true);
        }
        notifyChange();
    }

    private void notifyChange() {
        if (changeListener != null) {
            changeListener.run();
        }
    }
}

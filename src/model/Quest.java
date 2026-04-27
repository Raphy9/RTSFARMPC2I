package src.model;

import src.model.buildings.Building;

public class Quest {
    /**
     * Type d'événement qui peut faire avancer une quête.
     * Chaque quête ne répond qu'à un seul type pour garder les règles lisibles.
     */
    public enum Type {
        PLANT,
        HARVEST,
        BUILD,
        ACTION
    }

    private final String id;
    private final String title;
    private final String description;
    private final Type type;
    private final PlantType targetPlantType;
    private final String targetBuildingClassName;
    private final String targetActionKey;
    private final int goal;
    private final int rewardMoney;
    private final int rewardExp;

    private int progress;
    private boolean completed;
    private boolean rewardClaimed;

    private Quest(
            String id,
            String title,
            String description,
            Type type,
            PlantType targetPlantType,
            String targetBuildingClassName,
            String targetActionKey,
            int goal,
            int rewardMoney,
            int rewardExp
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.targetPlantType = targetPlantType;
        this.targetBuildingClassName = targetBuildingClassName;
        this.targetActionKey = targetActionKey;
        this.goal = Math.max(1, goal);
        this.rewardMoney = Math.max(0, rewardMoney);
        this.rewardExp = Math.max(0, rewardExp);
        this.progress = 0;
        this.completed = false;
        this.rewardClaimed = false;
    }

    /** Crée une quête liée à une plantation. */
    public static Quest createPlantQuest(String id, String title, String description, PlantType plantType, int goal, int rewardMoney, int rewardExp) {
        return new Quest(id, title, description, Type.PLANT, plantType, null, null, goal, rewardMoney, rewardExp);
    }

    /** Crée une quête liée à une récolte. */
    public static Quest createHarvestQuest(String id, String title, String description, PlantType plantType, int goal, int rewardMoney, int rewardExp) {
        return new Quest(id, title, description, Type.HARVEST, plantType, null, null, goal, rewardMoney, rewardExp);
    }

    /** Crée une quête liée à une construction. Si targetBuildingClass est null, n'importe quel bâtiment valide. */
    public static Quest createBuildQuest(String id, String title, String description, Class<? extends Building> targetBuildingClass, int goal, int rewardMoney, int rewardExp) {
        return new Quest(id, title, description, Type.BUILD, null, targetBuildingClass != null ? targetBuildingClass.getName() : null, null, goal, rewardMoney, rewardExp);
    }

    /** Crée une quête pilotée par un événement de gameplay (labour, vente, niveau, etc.). */
    public static Quest createActionQuest(String id, String title, String description, String actionKey, int goal, int rewardMoney, int rewardExp) {
        return new Quest(id, title, description, Type.ACTION, null, null, actionKey, goal, rewardMoney, rewardExp);
    }

    public boolean matchesPlantEvent(PlantType plantType) {
        return type == Type.PLANT && targetPlantType == plantType;
    }

    public boolean matchesHarvestEvent(PlantType plantType) {
        return type == Type.HARVEST && targetPlantType == plantType;
    }

    public boolean matchesBuildEvent(Building building) {
        if (type != Type.BUILD || building == null) {
            return false;
        }
        return targetBuildingClassName == null || targetBuildingClassName.equals(building.getClass().getName());
    }

    public boolean matchesActionEvent(String actionKey) {
        if (type != Type.ACTION || actionKey == null) {
            return false;
        }
        return actionKey.equals(targetActionKey);
    }

    /**
     * Avance la quête d'une quantité donnée et renvoie true uniquement lorsqu'elle passe à l'état terminé.
     */
    public boolean addProgress(int amount) {
        if (completed || amount <= 0) {
            return false;
        }

        progress = Math.min(goal, progress + amount);
        if (progress >= goal) {
            completed = true;
            return true;
        }
        return false;
    }

    /**
     * Verse la récompense une seule fois.
     */
    public boolean grantReward(Stats stats) {
        if (!completed || rewardClaimed || stats == null) {
            return false;
        }
        if (rewardMoney > 0) {
            stats.addMoney(rewardMoney);
        }
        if (rewardExp > 0) {
            stats.addExp(rewardExp);
        }
        rewardClaimed = true;
        return true;
    }

    /**
     * Restaure l'état exact de la quête depuis une sauvegarde.
     */
    public void restoreState(int restoredProgress, boolean restoredCompleted, boolean restoredRewardClaimed) {
        this.progress = Math.max(0, Math.min(goal, restoredProgress));
        this.completed = restoredCompleted || this.progress >= goal;
        this.rewardClaimed = restoredRewardClaimed || this.completed;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    public int getProgress() {
        return progress;
    }

    public int getGoal() {
        return goal;
    }

    public int getRewardMoney() {
        return rewardMoney;
    }

    public int getRewardExp() {
        return rewardExp;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public PlantType getTargetPlantType() {
        return targetPlantType;
    }

    public String getTargetBuildingClassName() {
        return targetBuildingClassName;
    }

    public String getTargetActionKey() {
        return targetActionKey;
    }
}

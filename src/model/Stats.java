package src.model;

import java.util.function.IntConsumer;

public class Stats {
    private int money;
    private int exp;
    private int level;

    /** Appelé chaque fois qu'un niveau est atteint, avec le nouveau niveau comme argument. */
    private IntConsumer levelUpCallback = null;

    public void setLevelUpCallback(IntConsumer callback) {
        this.levelUpCallback = callback;
    }

    public Stats() {
        this.money = 0;
        this.exp = 0;
        this.level = 1;
    }

    public Stats(int money) {
        this.money = money;
        this.exp = 0;
        this.level = 1;
    }

    public int getMoney() {
        return money;
    }

    public void addMoney(int m) {
        this.money += m;
    }

    public void removeMoney(int m) {
        this.money -= m;
    }

    public int getExp() {
        return exp;
    }

    public void addExp(int e) {
        this.exp += e;
        checkLevelUp();
    }

    public int getLevel() {
        return level;
    }

    public void setMoney(int m) {
        this.money = m;
    }

    public void setLevel(int lvl) {
        this.level = lvl;
    }

    public void setExp(int e) {
        this.exp = e;
    }

    /** XP nécessaire pour passer au niveau suivant (toujours 100 dans ce système simple) */
    public int getExpForNextLevel() {
        return 100;
    }

    private void checkLevelUp() {
        // Simple système de level up : chaque 100 exp, on gagne un niveau
        while (exp >= 100) {
            exp -= 100;
            level++;
            if (levelUpCallback != null) {
                final int lvl = level;
                levelUpCallback.accept(lvl);
            }
        }
    }
}

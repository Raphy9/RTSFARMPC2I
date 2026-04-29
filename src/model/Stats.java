package src.model;

import java.util.function.IntConsumer;

public class Stats {
    private int money;
    private int exp;
    private int level;

    /** Appele chaque fois qu'un niveau est atteint, avec le nouveau niveau comme argument. */
    private IntConsumer levelUpCallback = null;
    private java.util.function.IntConsumer moneyGainCallback = null;
    private java.util.function.IntConsumer expGainCallback = null;

    public void setLevelUpCallback(IntConsumer callback) {
        this.levelUpCallback = callback;
    }

    public void setMoneyGainCallback(java.util.function.IntConsumer callback) {
        this.moneyGainCallback = callback;
    }

    public void setExpGainCallback(java.util.function.IntConsumer callback) {
        this.expGainCallback = callback;
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
        if (m > 0 && moneyGainCallback != null) {
            moneyGainCallback.accept(m);
        }
        SoundManager.playSound(SoundManager.SELL);
    }

    public void removeMoney(int m) {
        this.money -= m;
        SoundManager.playSound(SoundManager.BUY);
    }

    public int getExp() {
        return exp;
    }

    public void addExp(int e) {
        this.exp += e;
        if (e > 0 && expGainCallback != null) {
            expGainCallback.accept(e);
        }
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

    /** XP nécessaire pour passer au niveau suivant. */
    public int getExpForNextLevel() {
        return switch (level) {
            case 1 -> 100;
            case 2 -> 150;
            case 3 -> 150;
            case 4 -> 200;
            default -> 250;
        };
    }

    private void checkLevelUp() {
        while (exp >= getExpForNextLevel()) {
            exp -= getExpForNextLevel();
            level++;
            if (levelUpCallback != null) {
                final int lvl = level;
                levelUpCallback.accept(lvl);
            }
        }
    }
}

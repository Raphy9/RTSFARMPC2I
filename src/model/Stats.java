package src.model;

public class Stats {
    private int money;
    private int exp;
    private int level;

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

    private void checkLevelUp() {
        // Simple système de level up : chaque 100 exp, on gagne un niveau
        while (exp >= 100) {
            exp -= 100;
            level++;
        }
    }
}

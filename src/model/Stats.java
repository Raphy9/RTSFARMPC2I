package src.model;

import java.util.function.IntConsumer;

/**
 * Gère les statistiques globales du joueur : argent, expérience et niveau.
 * Utilise un système de callbacks pour notifier d'autres classes (comme la vue)
 * lors de changements importants.
 */
public class Stats {
    private int money;
    private int exp;
    private int level;

    /**
     * Callbacks (fonctions de rappel) appelés lors d'événements spécifiques.
     * L'argument entier correspond à la valeur gagnée ou au nouveau niveau atteint.
     */
    private IntConsumer levelUpCallback = null;
    private java.util.function.IntConsumer moneyGainCallback = null;
    private java.util.function.IntConsumer expGainCallback = null;

    // --- Configuration des Callbacks ---

    public void setLevelUpCallback(IntConsumer callback) {
        this.levelUpCallback = callback;
    }

    public void setMoneyGainCallback(java.util.function.IntConsumer callback) {
        this.moneyGainCallback = callback;
    }

    public void setExpGainCallback(java.util.function.IntConsumer callback) {
        this.expGainCallback = callback;
    }

    // --- Constructeurs ---

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

    // --- Gestion de l'Argent ---

    public int getMoney() {
        return money;
    }

    /**
     * Ajoute de l'argent et déclenche le son et le callback de gain.
     */
    public void addMoney(int m) {
        this.money += m;
        // Notifie l'UI si un callback est enregistré
        if (m > 0 && moneyGainCallback != null) {
            moneyGainCallback.accept(m);
        }
        SoundManager.playSound(SoundManager.SELL); // Son de vente
    }

    /**
     * Retire de l'argent (achat).
     */
    public void removeMoney(int m) {
        this.money -= m;
        SoundManager.playSound(SoundManager.BUY); // Son d'achat
    }

    // --- Gestion de l'Expérience et des Niveaux ---

    public int getExp() {
        return exp;
    }

    /**
     * Ajoute de l'expérience et vérifie si le joueur change de niveau.
     */
    public void addExp(int e) {
        this.exp += e;
        if (e > 0 && expGainCallback != null) {
            expGainCallback.accept(e);
        }
        checkLevelUp(); // Vérification immédiate du passage de niveau
    }

    public int getLevel() {
        return level;
    }

    // --- Setters simples (utiles pour la sauvegarde ou l'initialisation) ---

    public void setMoney(int m) { this.money = m; }
    public void setLevel(int lvl) { this.level = lvl; }
    public void setExp(int e) { this.exp = e; }

    /**
     * Définit le seuil d'XP requis pour chaque niveau.
     * Utilise une switch expression moderne pour une lecture rapide.
     */
    public int getExpForNextLevel() {
        return switch (level) {
            case 1 -> 100;
            case 2 -> 150;
            case 3 -> 150;
            case 4 -> 200;
            default -> 250;
        };
    }

    /**
     * Logique de passage de niveau.
     * Utilise une boucle 'while' au cas où le gain d'XP soit assez massif
     * pour faire passer plusieurs niveaux d'un coup.
     */
    private void checkLevelUp() {
        while (exp >= getExpForNextLevel()) {
            exp -= getExpForNextLevel(); // On soustrait le coût du niveau actuel
            level++;

            // Notification du passage de niveau
            if (levelUpCallback != null) {
                final int lvl = level;
                levelUpCallback.accept(lvl);
            }
        }
    }
}
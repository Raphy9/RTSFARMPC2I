package src.view;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread qui gère le rendu de la vue globale et des popups,
 * en appelant la méthode repaint() de la classe Display.
 * Il hérite de TimerTask pour être planifié par un java.util.Timer.
 */
public class Rendering extends TimerTask {

    // Nombre d'images par seconde (Rafraîchissement de l'écran)
    public static final int FPS = 30;

    // Intervalle pour la sauvegarde automatique (60 000 ms = 1 minute)
    private static final long SAVE_INTERVAL_MS = 60000;

    private Display display; // Référence vers le gestionnaire de vue principal

    // Utilisation d'AtomicLong pour garantir une lecture/écriture sécurisée du temps
    // entre les différents threads.
    private AtomicLong lastSaveTime = new AtomicLong(0);

    /**
     * Constructeur de la boucle de rendu.
     * @param display L'instance de Display à rafraîchir.
     */
    public Rendering(Display display) {
        this.display = display;
    }

    /**
     * Méthode exécutée à chaque cycle du Timer.
     */
    @Override
    public void run() {
        // 1. Demande au gestionnaire de vue de redessiner tous les composants (Terrain, Entités, HUD)
        display.repaint();

        // 2. Gestion de la sauvegarde automatique
        long now = System.currentTimeMillis();

        // Vérifie si une minute s'est écoulée depuis la dernière sauvegarde
        if (now - lastSaveTime.get() >= SAVE_INTERVAL_MS) {
            display.saveGame(); // Appelle la logique de sauvegarde
            lastSaveTime.set(now); // Met à jour le marqueur temporel
        }
    }
}
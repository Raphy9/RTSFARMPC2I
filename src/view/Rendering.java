package src.view;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/** Thread qui gère le rendu de la vue globale et des popups,
 * en appelant la méthode repaint() de la classe Display */
public class Rendering extends TimerTask {

    public static final int FPS = 30;
    private static final long SAVE_INTERVAL_MS = 3000;

    private Display display;
    private AtomicLong lastSaveTime = new AtomicLong(0);

    public Rendering(Display display) {
        this.display = display;
    }

    @Override
    public void run() {
        display.repaint();
        long now = System.currentTimeMillis();
        if (now - lastSaveTime.get() >= SAVE_INTERVAL_MS) {
            display.saveGame();
            lastSaveTime.set(now);
        }
    }
}
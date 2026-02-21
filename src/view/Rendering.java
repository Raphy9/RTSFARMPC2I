package src.view;

import java.util.TimerTask;

/** Thread qui gère le rendu de la vue globale et des popups,
 * en appelant la méthode repaint() de la classe Display */
public class Rendering extends TimerTask {

    public static final int FPS = 30;

    private Display display;

    public Rendering(Display display) {
        this.display = display;
    }

    @Override
    public void run() {
        display.repaint();
    }
}

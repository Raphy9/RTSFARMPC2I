package src.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Floating text feedback with a dedicated update thread.
 */
public class FloatingTextManager {
    public static final Color COIN_COLOR = new Color(255, 220, 0);
    public static final Color EXP_COLOR = new Color(175, 90, 255);

    private static final int DEFAULT_FPS = 30;
    private static final int DEFAULT_DURATION_MS = 900;
    private static final float DEFAULT_RISE_PX = 24f;

    private final Object lock = new Object();
    private final List<FloatingText> texts = new ArrayList<>();
    private final int fps;
    private final int durationMs;
    private final float risePx;

    private volatile Runnable repaintCallback;
    private Thread thread;

    public FloatingTextManager() {
        this(DEFAULT_FPS, DEFAULT_DURATION_MS, DEFAULT_RISE_PX);
    }

    public FloatingTextManager(int fps, int durationMs, float risePx) {
        this.fps = Math.max(1, fps);
        this.durationMs = Math.max(100, durationMs);
        this.risePx = Math.max(1f, risePx);
    }

    public void setRepaintCallback(Runnable repaintCallback) {
        this.repaintCallback = repaintCallback;
    }

    public void start() {
        if (thread != null && thread.isAlive()) return;
        thread = new Thread(this::runLoop, "FloatingTextThread");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void addText(String text, int screenX, int screenY, Color color) {
        if (text == null || text.isEmpty()) return;
        if (color == null) color = Color.WHITE;
        FloatingText ft = new FloatingText(text, screenX, screenY, color, System.currentTimeMillis());
        synchronized (lock) {
            texts.add(ft);
        }
        Runnable cb = repaintCallback;
        if (cb != null) {
            SwingUtilities.invokeLater(cb);
        }
    }

    public void addMoney(int amount, int screenX, int screenY) {
        addText("+" + amount + "PO", screenX, screenY, COIN_COLOR);
    }

    public void addExp(int amount, int screenX, int screenY) {
        addText("+" + amount + "XP", screenX, screenY, EXP_COLOR);
    }

    public void clear() {
        synchronized (lock) {
            texts.clear();
        }
    }

    public void draw(Graphics2D g) {
        long now = System.currentTimeMillis();
        List<FloatingText> snapshot;
        synchronized (lock) {
            if (texts.isEmpty()) return;
            snapshot = new ArrayList<>(texts);
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (GameFonts.MINECRAFT_FONT != null) {
            g2.setFont(GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 18f));
        } else {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
        }

        for (FloatingText ft : snapshot) {
            float progress = Math.min(1f, (now - ft.startTimeMs) / (float) durationMs);
            float alpha = 1f - progress;
            int drawX = ft.x;
            int drawY = Math.round(ft.y - (risePx * progress));

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(new Color(0, 0, 0, 160));
            g2.drawString(ft.text, drawX + 1, drawY + 1);

            g2.setColor(ft.color);
            g2.drawString(ft.text, drawX, drawY);
        }
        g2.dispose();
    }

    private void runLoop() {
        int sleepMs = Math.max(1, 1000 / fps);
        try {
            while (!Thread.currentThread().isInterrupted()) {
                boolean hasActive = false;
                long now = System.currentTimeMillis();
                synchronized (lock) {
                    Iterator<FloatingText> it = texts.iterator();
                    while (it.hasNext()) {
                        FloatingText ft = it.next();
                        if (now - ft.startTimeMs >= durationMs) {
                            it.remove();
                        } else {
                            hasActive = true;
                        }
                    }
                }
                if (hasActive && repaintCallback != null) {
                    SwingUtilities.invokeLater(repaintCallback);
                }
                Thread.sleep(sleepMs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class FloatingText {
        private final String text;
        private final int x;
        private final int y;
        private final Color color;
        private final long startTimeMs;

        private FloatingText(String text, int x, int y, Color color, long startTimeMs) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
            this.startTimeMs = startTimeMs;
        }
    }
}

package src.model;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Classe utilitaire pour gérer les sons du jeu : chargement, lecture, boucles, et fondu enchaîné.
 * Utilise javax.sound.sampled pour la lecture de clips audio.
 * Les sons sont chargés en mémoire sous forme de SoundData (format + données brutes) pour permettre une lecture rapide.
 * Permet de jouer des sons ponctuels (playSound) et des boucles identifiées par une clé (playLoop/stopLoop).
 * Gère également les fondues d'apparition et de disparition pour les boucles.
 */
public class SoundManager {

    public static class SoundData {
        public final AudioFormat format;
        public final byte[] data;

        public SoundData(AudioFormat format, byte[] data) {
            this.format = format;
            this.data = data;
        }
    }

    // --- Variables statiques publiques pour accès direct depuis le reste du code ---
    public static SoundData BG;
    public static SoundData MENU;
    public static SoundData LEVEL_UP;

    public static SoundData PLANT;
    public static SoundData WATER;
    public static SoundData PLOW;
    public static SoundData HARVEST;

    // Clips actifs et boucles
    private static final List<Clip> activeClips = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, Clip> loopClips = Collections.synchronizedMap(new HashMap<>());

    // Scheduler pour les fades
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "SoundManager-Fade");
        t.setDaemon(true);
        return t;
    });
    private static final Map<String, ScheduledFuture<?>> fadeFutures = Collections.synchronizedMap(new HashMap<>());

    /**
     * Charge plusieurs sons d'exemple ; adaptez les chemins/cles selon votre projet.
     */
    public static void loadSounds() {
        // Exemple: chargez chaque son directement dans sa variable statique
        BG = loadSound("src/assets/sounds/background_3.wav");
        MENU = loadSound("src/assets/sounds/menu_1-1.wav");
        LEVEL_UP = loadSound("src/assets/sounds/level_up.wav");

        PLANT = loadSound("src/assets/sounds/planting.wav");
        WATER = loadSound("src/assets/sounds/watering.wav");
        PLOW = loadSound("src/assets/sounds/till.wav");
        HARVEST = loadSound("src/assets/sounds/pop_2.wav");
    }

    /**
     * Charge un son depuis un chemin (fichier sur disque ou ressource classpath) et retourne la SoundData.
     * Retourne null si le chargement échoue.
     */
    public static SoundData loadSound(String path) {
        if (path == null) return null;
        try {
            AudioInputStream ais = getAudioInputStreamForPath(path);
            AudioFormat baseFormat = ais.getFormat();

            // Convert to PCM_SIGNED if necessary (Clip.open(byte[]) attend souvent des données PCM)
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                    baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

            try (AudioInputStream dais = AudioSystem.getAudioInputStream(decodedFormat, ais);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = dais.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                }
                return new SoundData(decodedFormat, baos.toByteArray());
            } finally {
                try { ais.close(); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            System.err.println("Failed to load sound from " + path + " : " + e.getMessage());
            return null;
        }
    }

    private static AudioInputStream getAudioInputStreamForPath(String path) throws IOException, UnsupportedAudioFileException {
        // try file
        File f = new File(path);
        if (f.exists()) {
            return AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(f)));
        }
        // try classpath resource
        InputStream res = SoundManager.class.getResourceAsStream(path.startsWith("/") ? path : "/" + path);
        if (res != null) {
            return AudioSystem.getAudioInputStream(new BufferedInputStream(res));
        }
        throw new FileNotFoundException("Sound not found: " + path);
    }

    /**
     * Joue une instance à partir d'une SoundData. Retourne le Clip (ou null si échec).
     */
    public static Clip playSound(SoundData sd) {
        if (sd == null) return null;
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(sd.format, sd.data, 0, sd.data.length);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP || event.getType() == LineEvent.Type.CLOSE) {
                    try { clip.close(); } catch (Exception ignored) {}
                    activeClips.remove(clip);
                }
            });
            activeClips.add(clip);
            clip.start();
            return clip;
        } catch (LineUnavailableException e) {
            System.err.println("Failed to play provided SoundData : " + e.getMessage());
            return null;
        }
    }

    /**
     * Démarre une boucle identifiée par keyLoop en utilisant directement une SoundData.
     * Si une boucle existe déjà sous keyLoop, elle est arrêtée.
     */
    public static Clip playLoop(String keyLoop, SoundData sd) {
        return playLoop(keyLoop, sd, 0L);
    }

    /**
     * Démarre une boucle identifiée par keyLoop en utilisant directement une SoundData.
     * Si une boucle existe déjà sous keyLoop, elle est arrêtée. Le fondu d'apparition (fade-in)
     * se fait sur fadeMs millisecondes si > 0.
     */
    public static Clip playLoop(String keyLoop, SoundData sd, long fadeMs) {
        if (keyLoop == null || sd == null) return null;
        stopLoop(keyLoop, 0L); // stop existing without fade (we'll start new)
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(sd.format, sd.data, 0, sd.data.length);

            // If fade requested and control exists, set initial volume to minimum then fade up
            FloatControl gain = getGainControl(clip);
            if (fadeMs > 0 && gain != null) {
                float min = gain.getMinimum();
                gain.setValue(min);
            }

            clip.loop(Clip.LOOP_CONTINUOUSLY);
            loopClips.put(keyLoop, clip);
            activeClips.add(clip);
            clip.start();

            if (fadeMs > 0) {
                FloatControl gain2 = getGainControl(clip);
                if (gain2 != null) {
                    scheduleFade(keyLoop, clip, /*from*/gain2.getMinimum(), /*to*/0f, fadeMs);
                }
            }

            return clip;
        } catch (LineUnavailableException e) {
            System.err.println("Failed to start loop '" + keyLoop + "' from SoundData : " + e.getMessage());
            return null;
        }
    }

    /**
     * Stoppe la boucle immédiatement (version existante)
     */
    public static void stopLoop(String keyLoop) {
        stopLoop(keyLoop, 0L);
    }

    /**
     * Stoppe la boucle identifiée par keyLoop ; si fadeMs > 0, on fait un fondu de sortie pendant fadeMs ms
     * puis on arrête et ferme le clip.
     */
    public static void stopLoop(String keyLoop, long fadeMs) {
        if (keyLoop == null) return;

        Clip c = loopClips.get(keyLoop);
        if (c == null) return;

        // Cancel any existing fade for this key
        ScheduledFuture<?> existing = fadeFutures.remove(keyLoop);
        if (existing != null) existing.cancel(true);

        if (fadeMs <= 0) {
            // stop immediately
            loopClips.remove(keyLoop);
            try { c.stop(); } catch (Exception ignored) {}
            try { c.close(); } catch (Exception ignored) {}
            activeClips.remove(c);
            return;
        }

        // If gain control available, fade out; else stop immediately
        FloatControl gain = getGainControl(c);
        if (gain == null) {
            loopClips.remove(keyLoop);
            try { c.stop(); } catch (Exception ignored) {}
            try { c.close(); } catch (Exception ignored) {}
            activeClips.remove(c);
            return;
        }

        // schedule fade down from current value to minimum
        float start = gain.getValue();
        float end = gain.getMinimum();
        scheduleFade(keyLoop, c, start, end, fadeMs, () -> {
            // after fade finished, fully stop and remove
            loopClips.remove(keyLoop);
            try { c.stop(); } catch (Exception ignored) {}
            try { c.close(); } catch (Exception ignored) {}
            activeClips.remove(c);
        });
    }

    private static FloatControl getGainControl(Clip clip) {
        if (clip == null) return null;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                return (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            }
            // some mixers use VOLUME control
            if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
                return (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
            }
        } catch (IllegalArgumentException ignored) {}
        return null;
    }

    /**
     * Schedule a fade from 'fromDb' to 'toDb' over durationMs; associates the scheduled future with key
     * and optionally runs onComplete when finished.
     */
    private static void scheduleFade(String key, Clip clip, float fromDb, float toDb, long durationMs) {
        scheduleFade(key, clip, fromDb, toDb, durationMs, null);
    }

    private static void scheduleFade(String key, Clip clip, float fromDb, float toDb, long durationMs, Runnable onComplete) {
        // Cancel previous fade for key if any
        ScheduledFuture<?> prev = fadeFutures.remove(key);
        if (prev != null) prev.cancel(true);

        final int stepMs = 50;
        final int steps = Math.max(1, (int) (durationMs / stepMs));
        final float delta = (toDb - fromDb) / (float) steps;

        FloatControl gain = getGainControl(clip);
        if (gain == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        // Ensure starting value
        try {
            gain.setValue(fromDb);
        } catch (IllegalArgumentException ignored) {}

        final Runnable task = new Runnable() {
            int currentStep = 0;
            float current = fromDb;

            @Override
            public void run() {
                try {
                    if (!clip.isOpen()) {
                        ScheduledFuture<?> f = fadeFutures.remove(key);
                        if (f != null) f.cancel(false);
                        return;
                    }
                    if (currentStep >= steps) {
                        // final set
                        try { gain.setValue(toDb); } catch (IllegalArgumentException ignored) {}
                        ScheduledFuture<?> f = fadeFutures.remove(key);
                        if (f != null) f.cancel(false);
                        if (onComplete != null) onComplete.run();
                        return;
                    }
                    current += delta;
                    try { gain.setValue(current); } catch (IllegalArgumentException ignored) {}
                    currentStep++;
                } catch (Throwable t) {
                    ScheduledFuture<?> f = fadeFutures.remove(key);
                    if (f != null) f.cancel(false);
                }
            }
        };

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, stepMs, stepMs, TimeUnit.MILLISECONDS);
        fadeFutures.put(key, future);
    }

    public static void stopSound(Clip clip) {
        if (clip == null) return;
        try { clip.stop(); } catch (Exception ignored) {}
        try { clip.close(); } catch (Exception ignored) {}
        activeClips.remove(clip);
    }

    public static void stopAll() {
        List<Clip> copy;
        synchronized (activeClips) {
            copy = new ArrayList<>(activeClips);
        }
        for (Clip c : copy) {
            try { c.stop(); } catch (Exception ignored) {}
            try { c.close(); } catch (Exception ignored) {}
        }
        activeClips.clear();
        loopClips.clear();
    }

}

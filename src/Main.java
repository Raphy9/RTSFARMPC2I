package src;

import src.model.Camera;
import src.model.SoundManager;
import src.view.Display;
import src.view.HomeScreenPanel;
import src.view.Rendering;
import src.view.SaveManager;

import javax.swing.*;
import java.awt.Dimension;
import java.util.Timer;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Saclay Valley");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setResizable(false);

            // Charger les sons
            final SoundManager soundManager = new SoundManager();
            SoundManager.loadSounds();
            SoundManager.playLoop("menu", SoundManager.MENU);   // Musique de background en boucle

            final Display[] displayHolder = new Display[1];

            final HomeScreenPanel[] homeHolder = new HomeScreenPanel[1];
            homeHolder[0] = new HomeScreenPanel(() -> {
                // Charger ou créer la sauvegarde sélectionnée
                String selectedSave = homeHolder[0].getSelectedSave();
                if (selectedSave == null) return;

                Timer timer = new Timer();
                Display display = new Display(f);
                displayHolder[0] = display;
                Rendering renderer = new Rendering(display);
                timer.schedule(renderer, 0, 1000 / Rendering.FPS);

                // Charger la sauvegarde si elle existe
                if (SaveManager.savExists(selectedSave)) {
                    SaveManager.loadGame(selectedSave, display.getWorld());
                }
                display.setCurrentSaveName(selectedSave);
                // Lancer le son de jeu
                SoundManager.stopLoop("menu", 2000); // Arrêter la musique de menu avec un fondu de 2 secondes
                SoundManager.playLoop("bg", SoundManager.BG, 1000); // Lancer la musique de jeu en boucle avec un fondu de 2 secondes
            });

            f.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    if (displayHolder[0] != null) {
                        displayHolder[0].saveGame();
                    }
                }
            });

            homeHolder[0].setParentFrame(f);

            f.setContentPane(homeHolder[0]);
            f.setPreferredSize(new Dimension(Camera.WIDTH * Display.RATIO_X, Camera.HEIGHT * Display.RATIO_Y));
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}



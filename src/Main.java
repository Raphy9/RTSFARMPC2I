package src;

import src.model.Camera;
import src.model.SoundManager;
import src.view.Display;
import src.view.HomeScreenPanel;
import src.view.Rendering;
import src.view.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;

public class Main {

    private static JFrame frame;
    private static Display currentDisplay;
    private static Timer currentTimer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Saclay Valley");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            // On fixe la taille de la fenêtre dès le départ
            frame.setPreferredSize(new Dimension(Camera.WIDTH * Display.RATIO_X, Camera.HEIGHT * Display.RATIO_Y));

            // Charger les sons en mémoire
            SoundManager.loadSounds();

            // Gestion de la sauvegarde automatique quand on ferme le jeu avec la croix rouge
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    if (currentDisplay != null) {
                        currentDisplay.saveGame();
                    }
                }
            });

            // Afficher le menu principal au lancement
            showMainMenu();

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /**
     * Affiche l'écran d'accueil et gère la musique du menu.
     */
    private static void showMainMenu() {
        // Arrêter le timer de rendu du jeu si on vient de quitter une partie
        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer = null;
        }
        currentDisplay = null;

        // Transition musicale : on coupe la musique du jeu (bg) et on lance celle du menu
        SoundManager.stopLoop("bg");
        SoundManager.playLoop("menu", SoundManager.MENU);

        // Créer l'écran d'accueil avec son comportement au clic sur "Jouer"
        HomeScreenPanel homeScreen = new HomeScreenPanel(() -> {
            // Cette action s'exécute quand le joueur valide le popup de sauvegarde
            String selectedSave = null;
            // On doit tricher un peu pour récupérer la variable car on est dans une lambda
            Component currentPane = frame.getContentPane();
            if (currentPane instanceof HomeScreenPanel) {
                selectedSave = ((HomeScreenPanel) currentPane).getSelectedSave();
            }

            if (selectedSave != null) {
                launchGame(selectedSave);
            }
        });

        homeScreen.setParentFrame(frame);

        // Remplacer le contenu de la fenêtre par le menu
        frame.setContentPane(homeScreen);
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Initialise le monde, charge la sauvegarde, lance le thread de rendu et gère le retour au menu.
     */
    private static void launchGame(String saveName) {
        // Transition musicale : on coupe le menu et on lance la musique de fond du jeu
        SoundManager.stopLoop("menu", 1000);
        SoundManager.playLoop("bg", SoundManager.BG, 1000);

        // Création du Display (le monde, la caméra, les menus s'initialisent ici)
        currentDisplay = new Display(frame);

        // Définir l'action pour retourner au menu (appelée depuis le Menu Pause de Display)
        currentDisplay.setReturnToMenuCallback(() -> {
            showMainMenu(); // Retourne au menu principal !
        });

        // Lancement de la boucle de rendu (Rendering)
        currentTimer = new Timer();
        Rendering renderer = new Rendering(currentDisplay);
        currentTimer.schedule(renderer, 0, 1000 / Rendering.FPS);

        // Chargement de la sauvegarde si elle existe
        if (SaveManager.savExists(saveName)) {
            SaveManager.loadGame(saveName, currentDisplay.getWorld());
        }
        currentDisplay.setCurrentSaveName(saveName);

        // C'est le Display qui s'occupe désormais d'utiliser les calques et d'afficher le terrain
        // L'appel de revalidate() n'est pas nécessaire ici car Display manipule la frame en interne.
    }
}
package src.view;

import src.model.SoundManager;
import javax.swing.*;
import java.awt.*;

/**
 * Menu de Pause du jeu.
 * Hérite de PopupPanel pour bénéficier du fond et de la structure stylisée.
 * Permet de reprendre la partie, de gérer les réglages audio et de quitter.
 */
public class PauseMenuPopup extends PopupPanel {

    // --- Configuration des couleurs thématiques ---
    private final Color COLOR_ON = new Color(160, 100, 60);  // Marron standard (Actif)
    private final Color COLOR_OFF = new Color(200, 60, 50); // Rouge Stardew (Désactivé/Quitter)
    private final Color COLOR_HOVER_ON = new Color(120, 70, 40); // Marron foncé au survol
    private final Color COLOR_HOVER_OFF = new Color(150, 40, 30); // Rouge foncé au survol

    public PauseMenuPopup(Display display) {
        // Appelle le constructeur parent : dimensions 300x320, titre "Menu Pause"
        super(display, 300, 320, "Menu Pause");

        // Panneau de contenu organisé en grille verticale (4 lignes, 1 colonne)
        JPanel content = new JPanel(new GridLayout(4, 1, 0, 15));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // --- 1. Bouton Reprendre ---
        JButton btnResume = createButton("Reprendre", COLOR_ON);
        btnResume.addActionListener(e -> display.switchToGlobal());

        // --- 2. Bouton Musique (Toggle ON/OFF) ---
        JButton btnMusic = createButton("Musique", COLOR_ON);
        // Initialisation du texte et de la couleur selon l'état actuel du SoundManager
        updateButtonState(btnMusic, SoundManager.isMusicMuted(), "Musique");
        btnMusic.addActionListener(e -> {
            SoundManager.toggleMusic();
            updateButtonState(btnMusic, SoundManager.isMusicMuted(), "Musique");
        });

        // --- 3. Bouton Bruitages (Toggle ON/OFF) ---
        JButton btnSFX = createButton("Bruitages", COLOR_ON);
        updateButtonState(btnSFX, SoundManager.isSfxMuted(), "Bruitages");
        btnSFX.addActionListener(e -> {
            SoundManager.toggleSFX();
            updateButtonState(btnSFX, SoundManager.isSfxMuted(), "Bruitages");
        });

        // --- 4. Bouton Quitter ---
        JButton btnQuit = createButton("Sauvegarder & Quitter", COLOR_OFF);
        btnQuit.addActionListener(e -> {
            // Affiche une boîte de dialogue de confirmation avant de quitter
            boolean confirm = GameDialog.showConfirm(display.getGlobalView(), "Quitter", "Voulez-vous sauvegarder et quitter ?");
            if (confirm) {
                display.returnToMainMenu();
            }
        });

        // Ajout des boutons au conteneur
        content.add(btnResume);
        content.add(btnMusic);
        content.add(btnSFX);
        content.add(btnQuit);

        // Positionnement au centre du popup
        this.add(content, BorderLayout.CENTER);
    }

    /**
     * Met à jour dynamiquement le texte et la couleur du bouton selon l'état Mute.
     * @param btn Le bouton à modifier.
     * @param isMuted État actuel du son.
     * @param label Nom de l'option (Musique ou Bruitages).
     */
    private void updateButtonState(JButton btn, boolean isMuted, String label) {
        btn.setText(label + " : " + (isMuted ? "OFF" : "ON"));

        // Applique la couleur de survol car l'utilisateur vient de cliquer sur le bouton
        btn.setBackground(isMuted ? COLOR_HOVER_OFF : COLOR_HOVER_ON);

        // Demande un rafraîchissement graphique immédiat
        btn.repaint();
    }

    /**
     * Crée un bouton stylisé avec gestion personnalisée du survol.
     */
    private JButton createButton(String text, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);

        // Indispensable pour que le fond coloré soit visible sur tous les systèmes
        btn.setOpaque(true);

        // Police Minecraft si disponible
        btn.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 14f) : new Font("Arial", Font.BOLD, 14));
        btn.setBackground(baseColor);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2));

        // --- Gestion des effets visuels au survol (Hover) ---
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                String currentText = btn.getText();
                // On adapte la couleur de survol selon le type de bouton et son état ON/OFF
                if (currentText.contains("Musique")) {
                    btn.setBackground(SoundManager.isMusicMuted() ? COLOR_HOVER_OFF : COLOR_HOVER_ON);
                } else if (currentText.contains("Bruitages")) {
                    btn.setBackground(SoundManager.isSfxMuted() ? COLOR_HOVER_OFF : COLOR_HOVER_ON);
                } else if (currentText.contains("Quitter")) {
                    btn.setBackground(COLOR_HOVER_OFF);
                } else {
                    btn.setBackground(COLOR_HOVER_ON); // Cas "Reprendre"
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                String currentText = btn.getText();
                // On restaure la couleur de base selon l'état ON/OFF
                if (currentText.contains("Musique")) {
                    btn.setBackground(SoundManager.isMusicMuted() ? COLOR_OFF : COLOR_ON);
                } else if (currentText.contains("Bruitages")) {
                    btn.setBackground(SoundManager.isSfxMuted() ? COLOR_OFF : COLOR_ON);
                } else if (currentText.contains("Quitter")) {
                    btn.setBackground(COLOR_OFF);
                } else {
                    btn.setBackground(COLOR_ON); // Cas "Reprendre"
                }
            }
        });
        return btn;
    }
}
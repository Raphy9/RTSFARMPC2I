package src.view;

import src.model.SoundManager;
import javax.swing.*;
import java.awt.*;

public class PauseMenuPopup extends PopupPanel {

    // Couleurs pour les états
    private final Color COLOR_ON = new Color(160, 100, 60);  // Marron (Actif)
    private final Color COLOR_OFF = new Color(200, 60, 50); // Rouge (Désactivé)
    private final Color COLOR_HOVER_ON = new Color(120, 70, 40);
    private final Color COLOR_HOVER_OFF = new Color(150, 40, 30);

    public PauseMenuPopup(Display display) {
        super(display, 300, 320, "Menu Pause");

        JPanel content = new JPanel(new GridLayout(4, 1, 0, 15));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Bouton Reprendre
        JButton btnResume = createButton("Reprendre", COLOR_ON);
        btnResume.addActionListener(e -> display.switchToGlobal());

        // Bouton Musique
        JButton btnMusic = createButton("Musique", COLOR_ON);
        updateButtonState(btnMusic, SoundManager.isMusicMuted(), "Musique");
        btnMusic.addActionListener(e -> {
            SoundManager.toggleMusic();
            updateButtonState(btnMusic, SoundManager.isMusicMuted(), "Musique");
        });

        // Bouton Bruitages
        JButton btnSFX = createButton("Bruitages", COLOR_ON);
        updateButtonState(btnSFX, SoundManager.isSfxMuted(), "Bruitages");
        btnSFX.addActionListener(e -> {
            SoundManager.toggleSFX();
            updateButtonState(btnSFX, SoundManager.isSfxMuted(), "Bruitages");
        });

        // Bouton Quitter
        JButton btnQuit = createButton("Sauvegarder & Quitter", COLOR_OFF);
        btnQuit.addActionListener(e -> {
            boolean confirm = GameDialog.showConfirm(display.getGlobalView(), "Quitter", "Voulez-vous sauvegarder et quitter ?");
            if (confirm) {
                display.returnToMainMenu();
            }
        });

        content.add(btnResume);
        content.add(btnMusic);
        content.add(btnSFX);
        content.add(btnQuit);

        this.add(content, BorderLayout.CENTER);
    }

    /**
     * Met à jour dynamiquement le texte et la couleur du bouton selon l'état Mute.
     */
    private void updateButtonState(JButton btn, boolean isMuted, String label) {
        btn.setText(label + " : " + (isMuted ? "OFF" : "ON"));

        // Au moment du clic, la souris est forcément SUR le bouton, on met donc directement la couleur Hover appropriée
        btn.setBackground(isMuted ? COLOR_HOVER_OFF : COLOR_HOVER_ON);

        // Force l'interface graphique à redessiner instantanément la nouvelle couleur
        btn.repaint();
    }

    private JButton createButton(String text, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);

        // TRÈS IMPORTANT : OBLIGATOIRE sur certains OS (Mac/Win) pour que le setBackground fonctionne !
        btn.setOpaque(true);

        btn.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 14f) : new Font("Arial", Font.BOLD, 14));
        btn.setBackground(baseColor);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2));

        // Gestion du survol dynamique basée sur le texte ACTUEL du bouton
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                String currentText = btn.getText();
                if (currentText.contains("Musique")) {
                    btn.setBackground(SoundManager.isMusicMuted() ? COLOR_HOVER_OFF : COLOR_HOVER_ON);
                } else if (currentText.contains("Bruitages")) {
                    btn.setBackground(SoundManager.isSfxMuted() ? COLOR_HOVER_OFF : COLOR_HOVER_ON);
                } else if (currentText.contains("Quitter")) {
                    btn.setBackground(COLOR_HOVER_OFF);
                } else {
                    btn.setBackground(COLOR_HOVER_ON); // Pour le bouton Reprendre
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                String currentText = btn.getText();
                if (currentText.contains("Musique")) {
                    btn.setBackground(SoundManager.isMusicMuted() ? COLOR_OFF : COLOR_ON);
                } else if (currentText.contains("Bruitages")) {
                    btn.setBackground(SoundManager.isSfxMuted() ? COLOR_OFF : COLOR_ON);
                } else if (currentText.contains("Quitter")) {
                    btn.setBackground(COLOR_OFF);
                } else {
                    btn.setBackground(COLOR_ON); // Pour le bouton Reprendre
                }
            }
        });
        return btn;
    }
}
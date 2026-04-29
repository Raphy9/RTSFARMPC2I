package src.view;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * HomeScreenPanel représente l'écran titre du jeu.
 * Il affiche une image de fond et un bouton central pour accéder aux sauvegardes.
 */
public class HomeScreenPanel extends JPanel {

    private final Image backgroundImage; // Image de fond de l'écran d'accueil
    private String selectedSave = null;  // Stocke le nom de la sauvegarde choisie
    private JFrame parentFrame;          // Référence à la fenêtre principale pour centrer les popups

    /**
     * Constructeur de l'écran d'accueil.
     * @param onPlayWithSave Callback (Runnable) exécuté quand une partie est lancée.
     */
    public HomeScreenPanel(Runnable onPlayWithSave) {
        // Initialisation des polices personnalisées
        GameFonts.loadFonts();
        // Chargement de l'image de fond
        this.backgroundImage = loadBackground("src/assets/background.png");

        // Utilisation d'un GridBagLayout pour centrer facilement le bouton au milieu
        setLayout(new GridBagLayout());

        // --- Configuration du bouton "Jouer" ---
        JButton playButton = new JButton("Jouer");
        playButton.setFocusPainted(false); // Retire le rectangle de focus moche autour du texte

        // Assure que le bouton est bien opaque pour afficher sa couleur de fond marron
        playButton.setOpaque(true);
        playButton.setContentAreaFilled(true);

        playButton.setForeground(Color.WHITE); // Texte blanc
        playButton.setBackground(new Color(160, 100, 60)); // Couleur marron "terre"

        // Bordure composée : une ligne foncée (style Stardew Valley) + marges internes (padding)
        playButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 3),
                BorderFactory.createEmptyBorder(12, 28, 12, 28)
        ));

        // Application de la police Minecraft si disponible, sinon Arial
        Font buttonFont = GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 18f)
                : new Font("Arial", Font.BOLD, 18);
        playButton.setFont(buttonFont);

        // --- Gestion des effets visuels au survol de la souris ---
        playButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (playButton.isEnabled()) {
                    playButton.setBackground(new Color(120, 70, 40)); // Marron plus foncé
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                playButton.setBackground(new Color(160, 100, 60)); // Retour au marron normal
            }
        });

        // Action au clic : ouvre le dialogue de sélection de sauvegarde
        playButton.addActionListener(e -> {
            playButton.setEnabled(false); // Désactive pour éviter les doubles clics
            showSaveSelectionDialog(onPlayWithSave, playButton);
        });

        // Ajout du bouton au centre du panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(playButton, gbc);
    }

    /** Permet de définir la fenêtre parente pour le positionnement des popups */
    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
    }

    /**
     * Ouvre une boîte de dialogue modale pour choisir ou créer une sauvegarde.
     */
    private void showSaveSelectionDialog(Runnable onPlayWithSave, JButton playButton) {
        // JDialog modal (bloque l'interaction avec l'arrière-plan)
        JDialog popupFrame = new JDialog(parentFrame, "Charger/Creer Partie", true);
        selectedSave = null;

        // Instanciation de la popup de sélection
        SaveSelectionPopup popup = new SaveSelectionPopup(selectedSaveName -> {
            selectedSave = selectedSaveName; // On récupère le nom choisi
            popupFrame.dispose();            // On ferme la fenêtre de dialogue
            onPlayWithSave.run();            // On lance le jeu via le callback
        });

        popupFrame.setContentPane(popup);
        popupFrame.setSize(500, 600);
        popupFrame.setLocationRelativeTo(parentFrame); // Centre la popup par rapport au jeu
        popupFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        popupFrame.setVisible(true);

        // Si l'utilisateur a fermé la popup sans choisir de sauvegarde, on réactive le bouton
        if (selectedSave == null) {
            playButton.setEnabled(true);
        }
    }

    public String getSelectedSave() {
        return selectedSave;
    }

    /**
     * Dessine l'image de fond sur tout le panneau.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        if (backgroundImage != null) {
            // Améliore la qualité du redimensionnement de l'image de fond
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Couleur de secours si l'image ne charge pas
            g2.setColor(PopupPanel.SDV_BG);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.dispose();
    }

    /** Utilitaire pour charger l'image disque */
    private Image loadBackground(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (Exception ex) {
            return null; // Retourne null si le fichier est manquant
        }
    }
}
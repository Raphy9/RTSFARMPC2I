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

public class HomeScreenPanel extends JPanel {

    private final Image backgroundImage;
    private String selectedSave = null;
    private JFrame parentFrame;

    public HomeScreenPanel(Runnable onPlayWithSave) {
        GameFonts.loadFonts();
        this.backgroundImage = loadBackground("src/assets/background.png");

        setLayout(new GridBagLayout());

        JButton playButton = new JButton("Jouer");
        playButton.setFocusPainted(false);
        playButton.setForeground(Color.WHITE);
        playButton.setBackground(new Color(160, 100, 60));
        playButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 3),
                BorderFactory.createEmptyBorder(12, 28, 12, 28)
        ));

        Font buttonFont = GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 18f)
                : new Font("Arial", Font.BOLD, 18);
        playButton.setFont(buttonFont);

        playButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (playButton.isEnabled()) {
                    playButton.setBackground(new Color(120, 70, 40));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                playButton.setBackground(new Color(160, 100, 60));
            }
        });

        playButton.addActionListener(e -> {
            playButton.setEnabled(false);
            showSaveSelectionDialog(onPlayWithSave, playButton);
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(playButton, gbc);
    }

    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
    }

    private void showSaveSelectionDialog(Runnable onPlayWithSave, JButton playButton) {
        JDialog popupFrame = new JDialog(parentFrame, "Charger/Créer Partie", true);
        selectedSave = null;

        SaveSelectionPopup popup = new SaveSelectionPopup(selectedSaveName -> {
            selectedSave = selectedSaveName;
            popupFrame.dispose();
            onPlayWithSave.run();
        });

        popupFrame.setContentPane(popup);
        popupFrame.setSize(500, 600);
        popupFrame.setLocationRelativeTo(parentFrame);
        popupFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        popupFrame.setVisible(true);

        if (selectedSave == null) {
            playButton.setEnabled(true);
        }
    }

    public String getSelectedSave() {
        return selectedSave;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        if (backgroundImage != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2.setColor(PopupPanel.SDV_BG);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.dispose();
    }

    private Image loadBackground(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            return img;
        } catch (Exception ex) {
            return null;
        }
    }
}




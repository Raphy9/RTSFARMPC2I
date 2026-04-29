package src.view;

import src.model.*;
import src.model.buildings.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Popup de felicitations affiche a chaque montee de niveau.
 * Montre les objets nouvellement debloques (plantes ET batiments) avec un badge "new.png".
 */
public class LevelUpPopup {

    private static final Color SDV_BG          = PopupPanel.SDV_BG;
    private static final Color SDV_BORDER_DARK  = PopupPanel.SDV_BORDER_DARK;
    private static final Color SDV_BORDER_LIGHT = PopupPanel.SDV_BORDER_LIGHT;
    private static final Color SDV_TEXT         = PopupPanel.SDV_TEXT;

    /** Affiche le popup de level-up (bloquant). */
    public static void show(Component parent, World world, int newLevel) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth(), h = getHeight(), b = 4;
                g2.setColor(SDV_BORDER_DARK);  g2.fillRect(0, 0, w, h);
                g2.setColor(SDV_BORDER_LIGHT); g2.fillRect(b, b, w-b*2, h-b*2);
                g2.setColor(SDV_BG);           g2.fillRect(b*2, b*2, w-b*4, h-b*4);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- Titre ---
        Font font = GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT : new Font("Arial", Font.BOLD, 14);

        JLabel title = new JLabel("Niveau " + newLevel + " atteint !", SwingConstants.CENTER);
        title.setFont(font.deriveFont(Font.BOLD, 26f));
        title.setForeground(new Color(200, 150, 255));
        root.add(title, BorderLayout.NORTH);

        // --- Corps : items debloques ---
        List<UnlockEntry> unlocked = getUnlocksForLevel(newLevel);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        if (unlocked.isEmpty()) {
            JLabel noNew = new JLabel("Continuez a progresser pour debloquer de nouveaux objets !", SwingConstants.CENTER);
            noNew.setFont(font.deriveFont(14f));
            noNew.setForeground(SDV_TEXT);
            centerPanel.add(noNew, BorderLayout.CENTER);
        } else {
            JLabel subtitle = new JLabel("Nouveaux objets debloques :", SwingConstants.CENTER);
            subtitle.setFont(font.deriveFont(16f));
            subtitle.setForeground(SDV_TEXT);
            centerPanel.add(subtitle, BorderLayout.NORTH);

            JPanel grid = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
            grid.setOpaque(false);
            for (UnlockEntry entry : unlocked) {
                grid.add(createUnlockCard(entry, font));
            }
            centerPanel.add(grid, BorderLayout.CENTER);
        }
        root.add(centerPanel, BorderLayout.CENTER);

        // --- Bouton OK ---
        JButton ok = new JButton("Super !");
        ok.setFont(font.deriveFont(Font.BOLD, 14f));
        ok.setBackground(new Color(80, 140, 70));
        ok.setForeground(Color.WHITE);
        ok.setFocusPainted(false);
        ok.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SDV_BORDER_DARK, 2),
                BorderFactory.createEmptyBorder(6, 24, 6, 24)
        ));
        ok.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { ok.setBackground(new Color(50, 100, 45)); }
            public void mouseExited (java.awt.event.MouseEvent e) { ok.setBackground(new Color(80, 140, 70)); }
        });
        ok.addActionListener(e -> dialog.dispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setOpaque(false);
        south.add(ok);
        root.add(south, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    // -------------------------------------------------------

    private static JPanel createUnlockCard(UnlockEntry entry, Font font) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setOpaque(true);
        card.setBackground(new Color(235, 185, 120));
        card.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 2));
        card.setPreferredSize(new Dimension(110, 120));

        // Image chargee de façon synchrone + badge "new" superpose
        JLabel imgLabel = new JLabel(buildBadgedIcon(entry.iconPath, 64)) {
            @Override public Dimension getPreferredSize() { return new Dimension(80, 80); }
        };
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(imgLabel, BorderLayout.CENTER);

        JLabel name = new JLabel("<html><center>" + entry.name + "</center></html>", SwingConstants.CENTER);
        name.setFont(font.deriveFont(11f));
        name.setForeground(SDV_TEXT);
        name.setBorder(new EmptyBorder(0, 4, 4, 4));
        card.add(name, BorderLayout.SOUTH);

        return card;
    }

    /** Charge une image de façon SYNCHRONE via ImageIO (evite les artefacts asynchrones de ImageIcon). */
    private static java.awt.image.BufferedImage loadSync(String path) {
        try {
            java.io.File f = new java.io.File(path);
            if (f.exists()) return javax.imageio.ImageIO.read(f);
        } catch (Exception ignored) {}
        return null;
    }

    /** Cree une icone avec le badge "new.png" en haut a droite. */
    private static ImageIcon buildBadgedIcon(String imagePath, int size) {
        java.awt.image.BufferedImage baseImg = loadSync(imagePath);
        java.awt.image.BufferedImage badgeImg = loadSync("src/assets/new.png");

        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (baseImg != null) {
            g2.drawImage(baseImg, 0, 0, size, size, null);
        } else {
            // Fallback : carre gris avec "?"
            g2.setColor(new Color(180, 140, 80));
            g2.fillRect(0, 0, size, size);
            g2.setColor(new Color(110, 45, 15));
            g2.setFont(new Font("Arial", Font.BOLD, size / 2));
            g2.drawString("?", size / 3, size * 2 / 3);
        }
        if (badgeImg != null) {
            int badgeSize = size / 3;
            g2.drawImage(badgeImg, size - badgeSize, 0, badgeSize, badgeSize, null);
        }
        g2.dispose();
        return new ImageIcon(out);
    }

    // -------------------------------------------------------
    // Donnees : quoi est debloque a chaque niveau
    // -------------------------------------------------------

    public static List<UnlockEntry> getUnlocksForLevel(int level) {
        List<UnlockEntry> list = new ArrayList<>();

        // Plantes
        for (PlantType pt : PlantType.values()) {
            if (pt.getLevelRequirement() == level) {
                String seedPath  = "src/assets/CropSprites/" + pt.getName().toLowerCase() + "/seedpack.png";
                String plantPath = "src/assets/CropSprites/" + pt.getName().toLowerCase() + "/step4.png";
                list.add(new UnlockEntry(pt.getName() + " (Graine)", seedPath));
                list.add(new UnlockEntry(pt.getName() + " (Plante)", plantPath));
            }
        }

        // Batiments
        for (BuildingEntry b : ALL_BUILDINGS) {
            if (b.levelRequirement() == level) {
                list.add(new UnlockEntry(b.name(), b.iconPath()));
            }
        }

        return list;
    }

    // -------------------------------------------------------
    // Catalogue statique de tous les batiments avec leurs niveaux
    // -------------------------------------------------------

    public record BuildingEntry(String name, String iconPath, int levelRequirement) {}

    public static final List<BuildingEntry> ALL_BUILDINGS = List.of(
        new BuildingEntry("Barriere (Face)",  "src/assets/Obstacles/fence_face.png", 1),
        new BuildingEntry("Barriere (Cote)",  "src/assets/Obstacles/fence_side.png", 1),
        new BuildingEntry("Tonneau 1",        "src/assets/Buildings/barrel1.png",    1),
        new BuildingEntry("Tonneau 2",        "src/assets/Buildings/barrel2.png",    1),
        new BuildingEntry("Rocher",           "src/assets/Buildings/rock1.png",      1),
        new BuildingEntry("Arbre 1",          "src/assets/Buildings/tree1.png",      1),
        new BuildingEntry("Poteau",           "src/assets/Buildings/poto.png",       2),
        new BuildingEntry("Arbre 2",          "src/assets/Buildings/tree2.png",      3),
        new BuildingEntry("Grande enseigne",  "src/assets/Buildings/bigsign.png",    3),
        new BuildingEntry("Puits",            "src/assets/Buildings/well.png",       3),
        new BuildingEntry("Arroseur auto",    "src/assets/arroseur.png",             3),
        new BuildingEntry("Boite aux lettres","src/assets/Buildings/mailbox1.png",   4),
        new BuildingEntry("Porte (Face)",     "src/assets/Obstacles/fence_face.png", 4),
        new BuildingEntry("Porte (Cote)",     "src/assets/Obstacles/fence_side.png", 4),
        new BuildingEntry("Linge",            "src/assets/Buildings/linge.png",      5),
        new BuildingEntry("Enseigne carrote",  "src/assets/Buildings/carrotsign.png",   2),
        new BuildingEntry("Enseigne fraise",  "src/assets/Buildings/strawberrysign.png",   4),
        new BuildingEntry("Enseigne choux",  "src/assets/Buildings/chouxsign.png",  2),
        new BuildingEntry("Enseigne citrouille",  "src/assets/Buildings/pumpkinsign.png",  3),
        new BuildingEntry("Epouvantail",      "src/assets/Buildings/scarecrow.png",  4),
        new BuildingEntry("Statue",           "src/assets/Buildings/statue.png",     6)
    );

    public record UnlockEntry(String name, String iconPath) {}
}

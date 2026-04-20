package src.view;

import src.control.popups.BuildingManager;
import src.model.World;
import src.model.buildings.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BuildingSidePanel extends JPanel {

    private final BuildingManager manager;
    private final Display display;
    private final World world;
    private final JPanel itemsPanel;
    private JScrollPane scrollPane; // champ pour pouvoir revalider depuis showCategory
    private Runnable onClose;
    private String currentCategory = "Bâtiments";

    public BuildingSidePanel(BuildingManager manager, Display display, World world, Runnable onClose) {
        this.manager = manager;
        this.display = display;
        this.world = world;
        this.onClose = onClose;

        this.setLayout(new BorderLayout(0, 0));
        this.setOpaque(false); // On dessine le fond nous-mêmes

        // --- Barre du haut : Onglets + Fermeture ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(15, 15, 5, 15));

        // Conteneur pour les catégories
        JPanel categories = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        categories.setOpaque(false);

        // Onglets — noms AVEC accents, cohérents avec getEntriesFor()
        String[] cats = new String[]{"Bâtiments", "Décoration", "Nature", "Chemin"};
        for (String c : cats) {
            categories.add(createTabButton(c));
        }
        topBar.add(categories, BorderLayout.WEST);

        // Bouton FERMER (le "X" rouge Stardew)
        JButton btnClose = new JButton("X");
        btnClose.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 16f) : new Font("Arial", Font.BOLD, 16));
        btnClose.setBackground(new Color(210, 60, 50));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        btnClose.addActionListener(e -> {
            this.setVisible(false);
            display.onBuildingPanelClose(); // Restaure l'icône de construction
            if (this.onClose != null) this.onClose.run();
        });

        JPanel closeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closeWrap.setOpaque(false);
        closeWrap.add(btnClose);
        topBar.add(closeWrap, BorderLayout.EAST);

        this.add(topBar, BorderLayout.NORTH);

        // Zone des items — UN SEUL JScrollPane
        itemsPanel = new JPanel();
        itemsPanel.setOpaque(false);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(itemsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.add(scrollPane, BorderLayout.CENTER);

        // CORRECTION DE L'ACCENT : Ouvre bien l'onglet par défaut !
        showCategory("Bâtiments");
    }

    private JButton createTabButton(String text) {
        JButton b = new JButton(text);
        b.setFocusable(false);
        b.setBackground(PopupPanel.SDV_BORDER_LIGHT);
        b.setForeground(PopupPanel.SDV_TEXT);
        b.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 12f) : new Font("Arial", Font.BOLD, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        b.addActionListener(e -> showCategory(text));
        return b;
    }

    public void refresh() {
        // SwingUtilities assure que l'UI se met à jour sans lag ni glitch
        SwingUtilities.invokeLater(() -> {
            if (currentCategory != null) {
                showCategory(currentCategory);
            }
            // On force le panneau entier à recalculer ses dimensions et à se repeindre
            this.revalidate();
            this.repaint();
        });
    }

    private void showCategory(String category) {
        this.currentCategory = category;
        itemsPanel.removeAll();
        List<Entry> list = getEntriesFor(category);
        for (Entry e : list) {
            itemsPanel.add(createCard(e));
            itemsPanel.add(Box.createVerticalStrut(10));
        }

        // On force la mise à jour en cascade
        itemsPanel.revalidate();
        itemsPanel.repaint();

        if (scrollPane != null) {
            scrollPane.revalidate();
            scrollPane.getViewport().revalidate(); // On force le rafraîchissement de la vue interne
            scrollPane.repaint();
        }
    }

    private static ImageIcon LOCK_ICON = null;
    private static ImageIcon NEW_BADGE_ICON = null;

    /** Charge une image de façon synchrone via ImageIO (évite les artefacts de chargement asynchrone). */
    private static java.awt.image.BufferedImage loadSync(String path) {
        try {
            return javax.imageio.ImageIO.read(new java.io.File(path));
        } catch (Exception ex) {
            return null;
        }
    }

    private int countBuiltInstances(Entry e) {
        int count = 0;
        // Vérifiez que getBuildings() correspond bien à la méthode de votre classe World
        if (world.getBuildings() != null) {
            for (Building b : world.getBuildings()) {
                if (b.getClass().equals(e.creator.get().getClass())) {
                    count++;
                }
            }
        }
        return count;
    }


    private JComponent createCard(Entry e) {
        int currentLevel = world.getStats().getLevel();
        boolean locked = e.levelRequirement > currentLevel;
        boolean isNew  = e.levelRequirement == currentLevel;

        int builtCount = countBuiltInstances(e);
        String countText = (e.maxCount != -1) ? " (" + builtCount + "/" + e.maxCount + ")" : " (" + builtCount + ")";

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setMaximumSize(new Dimension(350, 80));
        card.setBackground(locked ? new Color(170, 160, 150) : new Color(235, 185, 120));
        card.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2));

        // Image à gauche
        int iconSz = 50;
        ImageIcon rawIcon;
        if (locked) {
            if (LOCK_ICON == null) LOCK_ICON = new ImageIcon("src/assets/lock.png");
            rawIcon = new ImageIcon(LOCK_ICON.getImage().getScaledInstance(iconSz, iconSz, Image.SCALE_SMOOTH));
        } else if (isNew) {
            // Chargement synchrone pour éviter l'image vide (ImageIcon asynchrone)
            java.awt.image.BufferedImage baseImg = loadSync(e.iconPath);
            if (NEW_BADGE_ICON == null) NEW_BADGE_ICON = new ImageIcon("src/assets/new.png");
            java.awt.image.BufferedImage badgeImg = loadSync("src/assets/new.png");

            java.awt.image.BufferedImage buf = new java.awt.image.BufferedImage(iconSz, iconSz, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D bg = buf.createGraphics();
            if (baseImg != null) bg.drawImage(baseImg, 0, 0, iconSz, iconSz, null);
            if (badgeImg != null) {
                int bsz = iconSz / 3;
                bg.drawImage(badgeImg, iconSz - bsz, 0, bsz, bsz, null);
            }
            bg.dispose();
            rawIcon = new ImageIcon(buf);
        } else {
            Image img = new ImageIcon(e.iconPath).getImage().getScaledInstance(iconSz, iconSz, Image.SCALE_SMOOTH);
            rawIcon = new ImageIcon(img);
        }

        JLabel imgLabel = new JLabel(rawIcon);
        imgLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        card.add(imgLabel, BorderLayout.WEST);

        // Infos au centre
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel title = new JLabel(locked ? "???" : e.title + countText);
        title.setForeground(PopupPanel.SDV_TEXT);
        title.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 14f) : new Font("Arial", Font.BOLD, 14));
        JLabel sub = new JLabel(locked ? "Niveau " + e.levelRequirement : e.price + " PO");
        sub.setForeground(locked ? new Color(100, 80, 150) : new Color(110, 60, 20));
        sub.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(12f) : new Font("Arial", Font.PLAIN, 12));
        info.add(title);
        info.add(sub);
        card.add(info, BorderLayout.CENTER);

        // Bouton Poser (grisé et désactivé si bloqué)
        JButton place = new JButton("Poser");
        place.setFocusable(false);
        place.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 12f) : new Font("Arial", Font.BOLD, 12));
        place.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 1));

        if (locked) {
            place.setEnabled(false);
            place.setBackground(new Color(150, 145, 140));
            place.setForeground(new Color(100, 95, 90));
        } else if (e.maxCount != -1 && builtCount >= e.maxCount) {
            // Le bâtiment est débloqué mais la limite est atteinte
            place.setEnabled(false);
            place.setText("Max atteint");
            place.setBackground(new Color(150, 145, 140));
            place.setForeground(new Color(100, 95, 90));
        } else {
            // Comportement normal
            Color selLight = new Color(160, 100, 60);
            Color selDark  = new Color(80, 40, 10);
            place.setBackground(selLight);
            place.setForeground(Color.WHITE);
            place.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { place.setBackground(selDark); }
                public void mouseExited (java.awt.event.MouseEvent evt) { place.setBackground(selLight); }
            });
            place.addActionListener(a -> {
                manager.startPlacement(e.creator.get(), e.maxCount, () -> this.refresh());
                display.getGlobalView().requestFocusInWindow();
            });
        }

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrap.setOpaque(false);
        btnWrap.setBorder(new EmptyBorder(10, 0, 0, 10));
        btnWrap.add(place);
        card.add(btnWrap, BorderLayout.EAST);

        return card;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        int b = 4;

        g2.setColor(PopupPanel.SDV_BORDER_DARK);
        g2.fillRect(0, 0, w, h);
        g2.setColor(PopupPanel.SDV_BORDER_LIGHT);
        g2.fillRect(b, b, w - b*2, h - b*2);
        g2.setColor(PopupPanel.SDV_BG);
        g2.fillRect(b*2, b*2, w - b*4, h - b*4);
        g2.dispose();
    }

    private List<Entry> getEntriesFor(String category) {
        List<Entry> out = new ArrayList<>();
        switch (category) {
            case "Bâtiments":
                out.add(new Entry("Grange", "src/assets/Buildings/barn.png", () -> new BarnBuilding(), 0, 1, 1));
                out.add(new Entry("Puits",            "src/assets/Buildings/well.png",      () -> new Well(),      40, 3, 2));
                out.add(new Entry("Arroseur auto",    "src/assets/arroseur.png",            () -> new Arroseur(), 120, 3));
                out.add(new Entry("Boîte aux lettres","src/assets/Buildings/mailbox1.png",  () -> new Mailbox1(),  30, 4, 1));
                out.add(new Entry("Linge",            "src/assets/Buildings/linge.png",    () -> new Linge(),    50, 5,2));

                break;
            case "Décoration":

                out.add(new Entry("Tonneau 1", "src/assets/Buildings/barrel1.png", () -> new Barrel1(), 10, 1,30));
                out.add(new Entry("Tonneau 2", "src/assets/Buildings/barrel2.png", () -> new Barrel2(), 10, 1,30));
                out.add(new Entry("Enseigne carrote",  "src/assets/Buildings/carrotsign.png",   () -> new carrotsign(),   10, 2,3));
                out.add(new Entry("Poteau",    "src/assets/Buildings/poto.png",    () -> new Poto(),    20, 2,30));
                out.add(new Entry("Enseigne choux",  "src/assets/Buildings/chouxsign.png",   () -> new chouxsign(),   10, 2,3));
                out.add(new Entry("Grande enseigne",  "src/assets/Buildings/bigsign.png",   () -> new Bigsign(),   20, 3,3));
                out.add(new Entry("Enseigne citrouille",  "src/assets/Buildings/pumpkinsign.png",   () -> new pumpkinsign(),   10, 3,3));
                out.add(new Entry("Enseigne fraise",  "src/assets/Buildings/strawberrysign.png",   () -> new strawberrysign(),   10, 4,3));
                break;
            case "Nature":
                out.add(new Entry("Rocher",  "src/assets/Buildings/rock1.png", () -> new Rock1(), 10, 1,30));
                out.add(new Entry("Arbre 1", "src/assets/Buildings/tree1.png", () -> new Tree1(), 10, 2,30));
                out.add(new Entry("Arbre 2", "src/assets/Buildings/tree2.png", () -> new Tree2(), 25, 3,15));
                break;
            case "Chemin":
                out.add(new Entry("Barrière (Face)", "src/assets/Obstacles/fence_face.png", () -> new FenceFace(), 10, 1));
                out.add(new Entry("Barrière (Côté)", "src/assets/Obstacles/fence_side.png", () -> new FenceSide(), 10, 1));
                out.add(new Entry("Porte (Face)",    "src/assets/Obstacles/fence_face.png", () -> new GateFace(),  15, 4));
                out.add(new Entry("Porte (Côté)",    "src/assets/Obstacles/fence_side.png", () -> new GateSide(),  15, 4));
                break;
        }
        return out;
    }

    private static class Entry {
        final String title, iconPath;
        final Supplier<Building> creator;
        final int price, levelRequirement, maxCount;;
        Entry(String t, String i, Supplier<Building> c, int p, int lvl) {
            this.title = t; this.iconPath = i; this.creator = c; this.price = p; this.levelRequirement = lvl; maxCount = -1;;
        }

        Entry(String t, String i, Supplier<Building> c, int p, int lvl, int maxCount) {
            this.title = t; this.iconPath = i; this.creator = c; this.price = p; this.levelRequirement = lvl; this.maxCount = maxCount;
        }
    }

    public void setOnClose(Runnable onClose) { this.onClose = onClose; }
}
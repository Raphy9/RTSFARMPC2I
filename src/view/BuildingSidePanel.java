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

    private void showCategory(String category) {
        itemsPanel.removeAll();
        List<Entry> list = getEntriesFor(category);
        for (Entry e : list) {
            itemsPanel.add(createCard(e));
            itemsPanel.add(Box.createVerticalStrut(10));
        }
        itemsPanel.revalidate();
        itemsPanel.repaint();
        // Revalide aussi le scroll pour éviter le bug "items qui disparaissent au 2e clic"
        if (scrollPane != null) {
            scrollPane.revalidate();
            scrollPane.repaint();
        }
    }

    private JComponent createCard(Entry e) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setMaximumSize(new Dimension(350, 80));
        card.setBackground(new Color(235, 185, 120)); // Orange sable
        card.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2));

        // Image à gauche
        ImageIcon rawIcon = new ImageIcon(e.iconPath);
        Image img = rawIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel imgLabel = new JLabel(new ImageIcon(img));
        imgLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        card.add(imgLabel, BorderLayout.WEST);

        // Infos au centre
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel title = new JLabel(e.title);
        title.setForeground(PopupPanel.SDV_TEXT);
        title.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 14f) : new Font("Arial", Font.BOLD, 14));
        JLabel price = new JLabel(e.price + " PO");
        price.setForeground(new Color(110, 60, 20));
        price.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(12f) : new Font("Arial", Font.PLAIN, 12));
        info.add(title);
        info.add(price);
        card.add(info, BorderLayout.CENTER);

        // Bouton Poser à droite
        JButton place = new JButton("Poser");
        place.setFocusable(false);
        Color selLight = new Color(160, 100, 60);
        Color selDark = new Color(80, 40, 10);
        place.setBackground(selLight);
        place.setForeground(Color.WHITE);
        place.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 12f) : new Font("Arial", Font.BOLD, 12));
        place.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 1));

        place.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { place.setBackground(selDark); }
            public void mouseExited(java.awt.event.MouseEvent evt) { place.setBackground(selLight); }
        });

        place.addActionListener(a -> {
            manager.startPlacement(e.creator.get());
            display.getGlobalView().requestFocusInWindow();
        });

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
                out.add(new Entry("Linge",           "src/assets/Buildings/linge.png",   () -> new Linge(),   120));
                out.add(new Entry("Boîte aux lettres","src/assets/Buildings/mailbox1.png",() -> new Mailbox1(), 50));
                out.add(new Entry("Puits",           "src/assets/Buildings/well.png",    () -> new Well(),     80));
                out.add(new Entry("Grande enseigne", "src/assets/Buildings/bigsign.png", () -> new Bigsign(),  40));
                break;
            case "Décoration":
                out.add(new Entry("Poteau",   "src/assets/Buildings/poto.png",   () -> new Poto(),    30));
                out.add(new Entry("Tonneau 1","src/assets/Buildings/barrel1.png",() -> new Barrel1(), 20));
                out.add(new Entry("Tonneau 2","src/assets/Buildings/barrel2.png",() -> new Barrel2(), 20));
                break;
            case "Nature":
                out.add(new Entry("Arbre 1", "src/assets/Buildings/tree1.png", () -> new Tree1(), 25));
                out.add(new Entry("Arbre 2", "src/assets/Buildings/tree2.png", () -> new Tree2(), 35));
                out.add(new Entry("Rocher",  "src/assets/Buildings/rock1.png", () -> new Rock1(), 15));
                break;
            case "Chemin":
                out.add(new Entry("Barriere (Face)", "src/assets/Obstacles/fence_face.png", () -> new FenceFace(), 10));
                out.add(new Entry("Barriere (Cote)", "src/assets/Obstacles/fence_side.png", () -> new FenceSide(), 10));
                out.add(new Entry("Porte (Face)", "src/assets/Obstacles/fence_face.png", () -> new GateFace(), 15));
                out.add(new Entry("Porte (Cote)", "src/assets/Obstacles/fence_side.png", () -> new GateSide(), 15));
                break;
        }
        return out;
    }

    private static class Entry {
        final String title, iconPath;
        final Supplier<Building> creator;
        final int price;
        Entry(String t, String i, Supplier<Building> c, int p) {
            this.title = t; this.iconPath = i; this.creator = c; this.price = p;
        }
    }

    public void setOnClose(Runnable onClose) { this.onClose = onClose; }
}
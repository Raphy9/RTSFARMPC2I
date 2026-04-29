package src.view;

import src.control.popups.BuildingManager;
import src.model.World;
import src.model.buildings.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
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
    private String currentCategory = "Batiments";

    public BuildingSidePanel(BuildingManager manager, Display display, World world, Runnable onClose) {
        this.manager = manager;
        this.display = display;
        this.world = world;
        this.onClose = onClose;

        this.setLayout(new BorderLayout(0, 0));
        this.setOpaque(false); // On dessine le fond nous-memes

        // --- Barre du haut : Onglets + Fermeture ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(15, 15, 5, 15));

        // Conteneur pour les categories
        JPanel categories = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        categories.setOpaque(false);

        // Onglets — noms SANS accents, coherents avec getEntriesFor()
        String[] cats = new String[]{"Batiments", "Decoration", "Nature", "Chemin"};
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
            display.onBuildingPanelClose(); // Restaure l'icone de construction
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

        // CORRECTION DE L'ACCENT : Ouvre bien l'onglet par defaut !
        showCategory("Batiments");
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
        // SwingUtilities assure que l'UI se met a jour sans lag ni glitch
        SwingUtilities.invokeLater(() -> {
            if (currentCategory != null) {
                showCategory(currentCategory);
            }
            // On force le panneau entier a recalculer ses dimensions et a se repeindre
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

        // On force la mise a jour en cascade
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

    /** Charge une image de façon synchrone via ImageIO (evite les artefacts de chargement asynchrone). */
    private static java.awt.image.BufferedImage loadSync(String path) {
        try {
            return javax.imageio.ImageIO.read(new java.io.File(path));
        } catch (Exception ex) {
            return null;
        }
    }

    private int countBuiltInstances(Entry e) {
        int count = 0;
        // Verifiez que getBuildings() correspond bien a la methode de votre classe World
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
        int builtCount = countBuiltInstances(e);
        int currentLevel = world.getStats().getLevel();
        boolean locked = e.levelRequirement > currentLevel;
        boolean isNew  = e.levelRequirement == currentLevel && builtCount == 0;

        String countText = (e.maxCount != -1) ? " (" + builtCount + "/" + e.maxCount + ")" : " (" + builtCount + ")";

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setMaximumSize(new Dimension(350, 80));
        card.setBackground(locked ? new Color(170, 160, 150) : new Color(235, 185, 120));
        card.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2));

        // Image a gauche
        int iconSz = 50;
        ImageIcon rawIcon;
        if (locked) {
            if (LOCK_ICON == null) LOCK_ICON = new ImageIcon("src/assets/lock.png");
            rawIcon = new ImageIcon(LOCK_ICON.getImage().getScaledInstance(iconSz, iconSz, Image.SCALE_SMOOTH));
        } else if (isNew) {
            // Chargement synchrone pour eviter l'image vide (ImageIcon asynchrone)
            java.awt.image.BufferedImage baseImg = loadSync(e.iconPath);
            if (NEW_BADGE_ICON == null) NEW_BADGE_ICON = new ImageIcon("src/assets/new.png");
            BufferedImage badgeImg = loadSync("src/assets/new.png");
            BufferedImage buf = new BufferedImage(iconSz, iconSz, BufferedImage.TYPE_INT_ARGB);
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
        JPanel info = new JPanel(new GridLayout(3, 1));
        info.setOpaque(false);
        JLabel title = new JLabel(locked ? "???" : e.title + countText);
        title.setForeground(PopupPanel.SDV_TEXT);
        title.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 14f) : new Font("Arial", Font.BOLD, 14));
        JLabel sub = new JLabel(locked ? "Niveau " + e.levelRequirement : e.price + " PO");
        sub.setForeground(locked ? new Color(100, 80, 150) : new Color(110, 60, 20));
        sub.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(12f) : new Font("Arial", Font.PLAIN, 12));
        info.add(title);
        JButton btnInfo = getJButton(e);
        if (locked) {
            btnInfo.setEnabled(false);
        }
        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        flowPanel.setOpaque(false);
        flowPanel.add(btnInfo);
        info.add(flowPanel);
        info.add(sub);
        card.add(info, BorderLayout.CENTER);


        // Bouton Poser (grise et desactive si bloque)
        JButton place = new JButton("Poser");
        place.setFocusable(false);
        place.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 12f) : new Font("Arial", Font.BOLD, 12));
        place.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 1));

        if (locked) {
            place.setEnabled(false);
            place.setBackground(new Color(150, 145, 140));
            place.setForeground(new Color(100, 95, 90));
        } else if (e.maxCount != -1 && builtCount >= e.maxCount) {
            // Le batiment est debloque mais la limite est atteinte
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

    private JButton getJButton(Entry e) {
        JButton btnInfo = new JButton();
        btnInfo.setIcon(new ImageIcon(new ImageIcon("src/assets/btninfo.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        btnInfo.setFocusable(false);
        btnInfo.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 8f) : new Font("Arial", Font.BOLD, 10));
        btnInfo.setPreferredSize(new Dimension(20,20    ));
        btnInfo.setOpaque(false);
        btnInfo.setContentAreaFilled(false);
        btnInfo.setBorder(BorderFactory.createEmptyBorder());
        btnInfo.addActionListener(a -> {createDescriptionPanel(e);});
        return btnInfo;
    }

    /**
     * Methode qui construit un panel au milieu de l'ecran pour afficher la description de l'entree
    */
    private void createDescriptionPanel(Entry e) {
        GameDialog.showMessage(display.getGlobalView(), e.title, e.description);
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
            case "Batiments":
                out.add(new Entry("Grange", "src/assets/Buildings/barn.png", "Le batiment principal de votre ferme qui permet de \nstocker/vendre/acheter les objets/plantes", () -> new BarnBuilding(), 0, 1, 1));
                out.add(new Entry("Arroseur auto",    "src/assets/arroseur.png", "Le meilleur ami du fermier paresseux. \nIl arrose automatiquement toutes les plantations dans un rayon de 2 cases. (zone de 5x5)", () -> new Sprinkler(), 120, 3));
                out.add(new Entry("Epouvantail",      "src/assets/Buildings/scarecrow.png", "Le protecteur de vos champs. Il effraie et fait \nfuir instantanement les corbeaux dans un rayon de 3 cases (zone de 7x7).",() -> new Scarecrow(), 100, 5));
                break;
            case "Decoration":

                out.add(new Entry("Tonneau 1", "src/assets/Buildings/barrel1.png", "De simples tonneaux en bois concu pour decorer votre terrain.", () -> new Barrel1(), 10, 1,30));
                out.add(new Entry("Tonneau 2", "src/assets/Buildings/barrel2.png", "De simples tonneaux en bois concu pour decorer votre terrain.", () -> new Barrel2(), 10, 1,30));
                out.add(new Entry("Enseigne carrote",  "src/assets/Buildings/carrotsign.png", "Ideal pour organiser votre potager et ne plus \nconfondre vos plantations de carottes avec le reste.",  () -> new carrotsign(),   10, 2,3));
                out.add(new Entry("Poteau",    "src/assets/Buildings/poto.png",  "Un simple poteau decoratif",  () -> new Poto(),    20, 2,30));
                out.add(new Entry("Enseigne choux",  "src/assets/Buildings/chouxsign.png", "Une petite pancarte artisanale pour indiquer \nfierement ou poussent vos plus beaux specimens de choux.",  () -> new chouxsign(),   10, 2,3));
                out.add(new Entry("Puits",            "src/assets/Buildings/well.png",  "Un puits traditionnel  qui ajoute une touche \nde charme indispensable a toute ferme.",    () -> new Well(),      40, 3, 2));
                out.add(new Entry("Grande enseigne",  "src/assets/Buildings/bigsign.png", "Un grand panneau imposant \npour marquer votre territoire.",  () -> new Bigsign(),   20, 3,3));
                out.add(new Entry("Enseigne citrouille",  "src/assets/Buildings/pumpkinsign.png", "Une petite pancarte pour baliser \nvos plantations de citrouilles et preparer sereinement Halloween.",  () -> new pumpkinsign(),   10, 3,3));
                out.add(new Entry("Enseigne fraise",  "src/assets/Buildings/strawberrysign.png", "Pour ne jamais perdre de vue \nvos precieux plants de fraises.",  () -> new strawberrysign(),   10, 4,3));
                out.add(new Entry("Boite aux lettres","src/assets/Buildings/mailbox1.png", "Indispensable pour recevoir les nouvelles \ndu voisinage (ou juste pour le style).", () -> new Mailbox1(),  30, 4, 1));
                out.add(new Entry("Linge",            "src/assets/Buildings/linge.png",  "Un element decoratif qui donne un air de \ncampagne habitee a votre ferme.",  () -> new Linge(),    50, 5,2));
                out.add(new Entry("Statue",            "src/assets/Buildings/statue.png",  "Une statue dorée.",  () -> new Statue(),    300, 6));
                break;
            case "Nature":
                out.add(new Entry("Rocher",  "src/assets/Buildings/rock1.png", "Un element naturel et robuste pour decorer votre \nterrain avec un aspect sauvage.",() -> new Rock1(), 10, 1,30));
                out.add(new Entry("Arbre 1", "src/assets/Buildings/tree1.png", "Un petit arbre decoratif qui apporte un peu de verdure \net d'ombre a votre terrain.",() -> new Tree1(), 10, 2,30));
                out.add(new Entry("Arbre 2", "src/assets/Buildings/tree2.png", "Plusieurs petits arbres decoratifs qui apportent un peu \nde verdure et d'ombre a votre terrain.",() -> new Tree2(), 25, 3,15));
                break;
            case "Chemin":
                out.add(new Entry("Barriere (Face)", "src/assets/Obstacles/fence_face.png", "Des clotures modulables qui s'adaptent et forment des angles \nautomatiquement pour bloquer les entites terrestres.",() -> new FenceFace(), 10, 1));
                out.add(new Entry("Barriere (Cote)", "src/assets/Obstacles/fence_side.png", "Des clotures modulables qui s'adaptent et forment des angles \nautomatiquement pour bloquer les entites terrestres.",() -> new FenceSide(), 10, 1));
                out.add(new Entry("Porte (Face)",    "src/assets/Obstacles/fence_face.png", "Une version amelioree de la barriere qui permet a \nvos jardiniers de circuler librement dans vos enclos.", () -> new GateFace(),  15, 4));
                out.add(new Entry("Porte (Cote)",    "src/assets/Obstacles/fence_side.png", "Une version amelioree de la barriere qui permet a \nvos jardiniers de circuler librement dans vos enclos.",() -> new GateSide(),  15, 4));
                break;
        }
        return out;
    }

    private static class Entry {
        final String title, iconPath, description;
        final Supplier<Building> creator;
        final int price, levelRequirement, maxCount;
        Entry(String t, String i, String d, Supplier<Building> c, int p, int lvl) {
            this.title = t; this.iconPath = i; this.creator = c; this.price = p; this.levelRequirement = lvl; maxCount = -1;
            this.description = d;
        }

        Entry(String t, String i, String d, Supplier<Building> c, int p, int lvl, int maxCount) {
            this.title = t; this.iconPath = i; this.creator = c; this.price = p; this.levelRequirement = lvl; this.maxCount = maxCount;
            this.description = d;
        }
    }

    public void setOnClose(Runnable onClose) { this.onClose = onClose; }
}
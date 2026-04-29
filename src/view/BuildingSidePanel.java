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

/**
 * Panneau latéral de construction (Shop/Catalogue).
 * Permet de naviguer par catégories et de choisir des bâtiments à placer dans le monde.
 */
public class BuildingSidePanel extends JPanel {

    // --- Champs de données et contrôleurs ---
    private final BuildingManager manager; // Gère la logique de placement (fantôme de construction)
    private final Display display;         // Référence à la vue principale
    private final World world;             // Référence au modèle pour vérifier les stats (niveau, or)
    private final JPanel itemsPanel;       // Conteneur vertical pour les "cartes" de bâtiments
    private JScrollPane scrollPane;        // Barre de défilement pour naviguer dans la liste
    private Runnable onClose;              // Action à exécuter lors de la fermeture (callback)
    private String currentCategory = "Batiment"; // Catégorie d'affichage active

    /**
     * Constructeur : Initialise la structure du panneau, les onglets et la zone de défilement.
     */
    public BuildingSidePanel(BuildingManager manager, Display display, World world, Runnable onClose) {
        this.manager = manager;
        this.display = display;
        this.world = world;
        this.onClose = onClose;

        this.setLayout(new BorderLayout(0, 0));
        this.setOpaque(false); // Fond personnalisé via paintComponent

        // --- Barre du haut : Onglets + Bouton de Fermeture ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(15, 15, 5, 15));

        // Panneau contenant les boutons d'onglets (catégories)
        JPanel categories = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        categories.setOpaque(false);

        // Définition des catégories de construction
        String[] cats = new String[]{"Batiment", "Decoration", "Nature", "Divers"};
        for (String c : cats) {
            categories.add(createTabButton(c));
        }
        topBar.add(categories, BorderLayout.WEST);

        // Bouton FERMER : Style "X" rouge inspiré de Stardew Valley
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
            display.onBuildingPanelClose(); // Restaure l'icône de construction dans l'UI principale
            if (this.onClose != null) this.onClose.run();
        });

        JPanel closeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closeWrap.setOpaque(false);
        closeWrap.add(btnClose);
        topBar.add(closeWrap, BorderLayout.EAST);

        this.add(topBar, BorderLayout.NORTH);

        // --- Zone centrale : Liste défilante des articles ---
        itemsPanel = new JPanel();
        itemsPanel.setOpaque(false);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS)); // Alignement vertical des cartes

        scrollPane = new JScrollPane(itemsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Fluidité du scroll
        this.add(scrollPane, BorderLayout.CENTER);

        // Chargement initial de la catégorie par défaut
        showCategory("Batiment");
    }

    /**
     * Crée un bouton d'onglet stylisé.
     */
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

    /**
     * Rafraîchit l'affichage actuel (utile en cas de level-up ou changement de ressources).
     */
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            if (currentCategory != null) {
                showCategory(currentCategory);
            }
            this.revalidate();
            this.repaint();
        });
    }

    /**
     * Filtre et affiche les bâtiments de la catégorie sélectionnée.
     */
    private void showCategory(String category) {
        this.currentCategory = category;
        itemsPanel.removeAll();
        List<Entry> list = getEntriesFor(category);
        for (Entry e : list) {
            itemsPanel.add(createCard(e)); // Ajoute la carte visuelle de l'article
            itemsPanel.add(Box.createVerticalStrut(10)); // Espacement
        }

        itemsPanel.revalidate();
        itemsPanel.repaint();

        if (scrollPane != null) {
            scrollPane.revalidate();
            scrollPane.getViewport().revalidate();
            scrollPane.repaint();
        }
    }

    // --- Ressources statiques pour les icônes d'état ---
    private static ImageIcon LOCK_ICON = null;
    private static ImageIcon NEW_BADGE_ICON = null;

    /**
     * Chargement synchrone d'image pour éviter les scintillements à l'affichage.
     */
    private static java.awt.image.BufferedImage loadSync(String path) {
        try {
            return javax.imageio.ImageIO.read(new java.io.File(path));
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Compte combien d'exemplaires de ce bâtiment existent déjà dans le monde.
     */
    private int countBuiltInstances(Entry e) {
        int count = 0;
        if (world.getBuildings() != null) {
            for (Building b : world.getBuildings()) {
                // Comparaison par classe pour identifier le type de bâtiment
                if (b.getClass().equals(e.creator.get().getClass())) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Crée le composant visuel (Card) pour un article du catalogue.
     * Gère les états : Débloqué, Verrouillé (niveau), Nouveau, ou Limite atteinte.
     */
    private JComponent createCard(Entry e) {
        int builtCount = countBuiltInstances(e);
        int currentLevel = world.getStats().getLevel();
        boolean locked = e.levelRequirement > currentLevel; // Trop bas niveau
        boolean isNew  = e.levelRequirement == currentLevel && builtCount == 0; // Vient d'être débloqué

        // Texte indicateur de quantité (ex: 1/1 pour la grange)
        String countText = (e.maxCount != -1) ? " (" + builtCount + "/" + e.maxCount + ")" : " (" + builtCount + ")";

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setMaximumSize(new Dimension(350, 80));
        card.setBackground(locked ? new Color(170, 160, 150) : new Color(235, 185, 120));
        card.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2));

        // --- Section Image (Gauche) ---
        int iconSz = 50;
        ImageIcon rawIcon;
        if (locked) {
            // Affichage du cadenas
            if (LOCK_ICON == null) LOCK_ICON = new ImageIcon("src/assets/lock.png");
            rawIcon = new ImageIcon(LOCK_ICON.getImage().getScaledInstance(iconSz, iconSz, Image.SCALE_SMOOTH));
        } else if (isNew) {
            // Composition : Image du bâtiment + badge "New" superposé
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
            // Icône standard
            Image img = new ImageIcon(e.iconPath).getImage().getScaledInstance(iconSz, iconSz, Image.SCALE_SMOOTH);
            rawIcon = new ImageIcon(img);
        }

        JLabel imgLabel = new JLabel(rawIcon);
        imgLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        card.add(imgLabel, BorderLayout.WEST);

        // --- Section Infos (Centre) ---
        JPanel info = new JPanel(new GridLayout(3, 1));
        info.setOpaque(false);
        JLabel title = new JLabel(locked ? "???" : e.title + countText);
        title.setForeground(PopupPanel.SDV_TEXT);
        title.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 14f) : new Font("Arial", Font.BOLD, 14));

        JLabel sub = new JLabel(locked ? "Niveau " + e.levelRequirement : e.price + " PO");
        sub.setForeground(locked ? new Color(100, 80, 150) : new Color(110, 60, 20));
        sub.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(12f) : new Font("Arial", Font.PLAIN, 12));

        info.add(title);

        // Petit bouton "i" pour les détails
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

        // --- Section Bouton "Poser" (Droite) ---
        JButton place = new JButton("Poser");
        place.setFocusable(false);
        place.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 12f) : new Font("Arial", Font.BOLD, 12));
        place.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 1));

        if (locked) {
            // État désactivé (Niveau)
            place.setEnabled(false);
            place.setBackground(new Color(150, 145, 140));
            place.setForeground(new Color(100, 95, 90));
        } else if (e.maxCount != -1 && builtCount >= e.maxCount) {
            // État désactivé (Limite atteinte)
            place.setEnabled(false);
            place.setText("Max atteint");
            place.setBackground(new Color(150, 145, 140));
            place.setForeground(new Color(100, 95, 90));
        } else {
            // État actif
            Color selLight = new Color(160, 100, 60);
            Color selDark  = new Color(80, 40, 10);
            place.setBackground(selLight);
            place.setForeground(Color.WHITE);
            place.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { place.setBackground(selDark); }
                public void mouseExited (java.awt.event.MouseEvent evt) { place.setBackground(selLight); }
            });
            place.addActionListener(a -> {
                // Déclenche le mode placement du bâtiment sélectionné
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

    /**
     * Crée le petit bouton d'information (?).
     */
    private JButton getJButton(Entry e) {
        JButton btnInfo = new JButton();
        btnInfo.setIcon(new ImageIcon(new ImageIcon("src/assets/btninfo.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        btnInfo.setFocusable(false);
        btnInfo.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 8f) : new Font("Arial", Font.BOLD, 10));
        btnInfo.setPreferredSize(new Dimension(20,20));
        btnInfo.setOpaque(false);
        btnInfo.setContentAreaFilled(false);
        btnInfo.setBorder(BorderFactory.createEmptyBorder());
        btnInfo.addActionListener(a -> {createDescriptionPanel(e);});
        return btnInfo;
    }

    /**
     * Affiche une boîte de dialogue avec la description longue du bâtiment.
     */
    private void createDescriptionPanel(Entry e) {
        GameDialog.showMessage(display.getGlobalView(), e.title, e.description);
    }

    /**
     * Rendu graphique du fond du panneau (style Stardew Valley : triple bordure).
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        int b = 4; // épaisseur de bordure

        g2.setColor(PopupPanel.SDV_BORDER_DARK);
        g2.fillRect(0, 0, w, h);
        g2.setColor(PopupPanel.SDV_BORDER_LIGHT);
        g2.fillRect(b, b, w - b*2, h - b*2);
        g2.setColor(PopupPanel.SDV_BG);
        g2.fillRect(b*2, b*2, w - b*4, h - b*4);
        g2.dispose();
    }

    /**
     * Définit le contenu du catalogue (base de données des articles).
     */
    private List<Entry> getEntriesFor(String category) {
        List<Entry> out = new ArrayList<>();
        switch (category) {
            case "Batiment":
                out.add(new Entry("Grange", "src/assets/Buildings/barn.png", "Le batiment principal de votre ferme qui permet de \nstocker/vendre/acheter les objets/plantes", () -> new BarnBuilding(), 0, 1, 1));
                out.add(new Entry("Arroseur auto",    "src/assets/arroseur.png", "Le meilleur ami du fermier paresseux. \nIl arrose automatiquement toutes les plantations dans un rayon de 2 cases. (zone de 5x5)", () -> new Sprinkler(), 120, 3));
                out.add(new Entry("Epouvantail",      "src/assets/Buildings/scarecrow.png", "Le protecteur de vos champs. Il effraie et fait \nfuir instantanement les corbeaux dans un rayon de 3 cases (zone de 7x7).",() -> new Scarecrow(), 100, 5));
                break;
            case "Decoration":
                out.add(new Entry("Chemin pierre", "src/assets/Buildings/path.png", "Un petit chemin en pierre pour décorer votre ferme.", () -> new Path(), 5, 1));
                out.add(new Entry("Tonneau 1", "src/assets/Buildings/barrel1.png", "De simples tonneaux en bois concu pour decorer votre terrain.", () -> new Barrel1(), 10, 1,30));
                out.add(new Entry("Tonneau 2", "src/assets/Buildings/barrel2.png", "De simples tonneaux en bois concu pour decorer votre terrain.", () -> new Barrel2(), 10, 1,30));
                out.add(new Entry("Poteau",    "src/assets/Buildings/poto.png",  "Un simple poteau decoratif",  () -> new Poto(),    20, 2,30));
                out.add(new Entry("Puits",            "src/assets/Buildings/well.png",  "Un puits traditionnel  qui ajoute une touche \nde charme indispensable a toute ferme.",    () -> new Well(),      40, 3, 2));
                out.add(new Entry("Boite aux lettres","src/assets/Buildings/mailbox1.png", "Indispensable pour recevoir les nouvelles \ndu voisinage (ou juste pour le style).", () -> new Mailbox1(),  30, 4, 1));
                out.add(new Entry("Linge",            "src/assets/Buildings/linge.png",  "Un element decoratif qui donne un air de \ncampagne habitee a votre ferme.",  () -> new Linge(),    50, 5,2));
                out.add(new Entry("Statue",            "src/assets/Buildings/statue.png",  "Une statue dorée.",  () -> new Statue(),    300, 6));
                break;
            case "Nature":
                out.add(new Entry("Rocher",  "src/assets/Buildings/rock1.png", "Un element naturel et robuste pour decorer votre \nterrain avec un aspect sauvage.",() -> new Rock1(), 10, 1,30));
                out.add(new Entry("Arbre 1", "src/assets/Buildings/tree1.png", "Un petit arbre decoratif qui apporte un peu de verdure \net d'ombre a votre terrain.",() -> new Tree1(), 10, 2,30));
                out.add(new Entry("Arbre 2", "src/assets/Buildings/tree2.png", "Plusieurs petits arbres decoratifs qui apportent un peu \nde verdure et d'ombre a votre terrain.",() -> new Tree2(), 25, 3,15));
                break;
            case "Divers":
                out.add(new Entry("Barriere", "src/assets/Obstacles/fence.png", "Des clotures modulables qui s'adaptent et forment des angles \nautomatiquement pour bloquer les entites terrestres.",() -> new    Fence(), 10, 1));
                out.add(new Entry("Enseigne carrote",  "src/assets/Buildings/carrotsign.png", "Ideal pour organiser votre potager et ne plus \nconfondre vos plantations de carottes avec le reste.",  () -> new carrotsign(),   10, 2,3));
                out.add(new Entry("Enseigne choux",  "src/assets/Buildings/chouxsign.png", "Une petite pancarte artisanale pour indiquer \nfierement ou poussent vos plus beaux specimens de choux.",  () -> new chouxsign(),   10, 2,3));
                out.add(new Entry("Enseigne citrouille",  "src/assets/Buildings/pumpkinsign.png", "Une petite pancarte pour baliser \nvos plantations de citrouilles et preparer sereinement Halloween.",  () -> new pumpkinsign(),   10, 3,3));
                out.add(new Entry("Grande enseigne",  "src/assets/Buildings/bigsign.png", "Un grand panneau imposant \npour marquer votre territoire.",  () -> new Bigsign(),   20, 3,3));
                out.add(new Entry("Enseigne fraise",  "src/assets/Buildings/strawberrysign.png", "Pour ne jamais perdre de vue \nvos precieux plants de fraises.",  () -> new strawberrysign(),   10, 4,3));
                out.add(new Entry("Porte (face)",    "src/assets/Obstacles/fence.png", "Une version amelioree de la barriere qui permet a \nvos jardiniers de circuler librement dans vos enclos.", () -> new GateFace(),  15, 4));
                out.add(new Entry("Porte (cote)",    "src/assets/Obstacles/fence.png", "Une version amelioree de la barriere qui permet a \nvos jardiniers de circuler librement dans vos enclos.", () -> new GateSide(),  15, 4));
                break;
        }
        return out;
    }

    /**
     * Classe interne représentant une entrée du catalogue.
     */
    private static class Entry {
        final String title, iconPath, description;
        final Supplier<Building> creator; // Fournit une nouvelle instance du bâtiment
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
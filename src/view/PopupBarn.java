package src.view;

import src.control.popups.BarnCategoriesController;
import src.control.popups.BarnController;
import src.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class PopupBarn extends PopupPanel {

    private Display display;
    private World world;
    private Barn barn;

    private JPanel itemGrid;
    private JPanel categories;
    private JPanel descriptionPanel;

    private String selectedCategory = "Toutes";
    private String[] categoryNames = {"Toutes", "Graines", "Plantes", "Fertilisants"};

    // NOUVEAU : On mémorise l'objet cliqué pour l'afficher à droite
    private Item selectedItem = null;

    private static final int WIDTH_SLOTS = 3;
    private static final int HEIGHT_SLOTS = 5;
    private static final int DESCRIPTION_SIZE = 350; // Plus large pour le preview

    public PopupBarn(Display display, World world) {
        super(display, Camera.WIDTH * Display.RATIO_X - 2 * Display.RATIO_X, Camera.HEIGHT * Display.RATIO_Y - 2 * Display.RATIO_Y, "Grange");
        this.display = display;
        this.world = world;
        this.barn = world.getBarn();
        initializeUI();
    }

    private void initializeUI() {
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        categories = new JPanel(new GridLayout(1, 4, 4, 0));
        categories.setOpaque(false);
        categories.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        itemGrid = new JPanel(new GridLayout(HEIGHT_SLOTS, WIDTH_SLOTS, 10, 10));
        itemGrid.setOpaque(false);
        itemGrid.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 10));

        // Panneau de droite (Preview) séparé par une ligne verticale
        descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setOpaque(false);
        descriptionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 2, 0, 0, SDV_BORDER_DARK),
                BorderFactory.createEmptyBorder(10, 10, 20, 20)
        ));

        buildItemGrid();
        buildDescription();

        ArrayList<JButton> categoryButtons = buildCategories();
        for (int i = 0; i < categoryButtons.size() && i < categoryNames.length; i++) {
            JButton catButton = categoryButtons.get(i);
            catButton.addActionListener(new BarnCategoriesController(catButton, this, categoryNames[i]));
        }

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(categories, BorderLayout.NORTH);
        left.add(itemGrid, BorderLayout.CENTER);

        panel.add(left, BorderLayout.CENTER);
        panel.add(descriptionPanel, BorderLayout.EAST);

        center.add(panel, BorderLayout.CENTER);
        this.add(center, BorderLayout.CENTER);
    }

    private boolean isItemUnlocked(Item item) {
        if (item == null || item.getPlantType() == null) return false;

        // On récupère le nom de la plante (en minuscules pour éviter les soucis de majuscules)
        String plantName = item.getPlantType().getName().toLowerCase();

        // On autorise uniquement les carottes et les choux pour le début du jeu
        if (plantName.contains("carotte") || plantName.contains("chou")) {
            return true;
        }

        // Tout le reste sera grisé et bloqué
        return false;
    }

    private void buildItemGrid() {
        itemGrid.setPreferredSize(new Dimension(this.width - DESCRIPTION_SIZE, this.height - 20));
        itemGrid.removeAll();

        ArrayList<Item> filtered = new ArrayList<>();
        for (Item it : barn.getItems()) {
            switch (selectedCategory) {
                case "Graines": if (it instanceof ItemSeed) filtered.add(it); break;
                case "Plantes": if (it instanceof ItemPlant) filtered.add(it); break;
                case "Fertilisants": if (!(it instanceof ItemSeed) && !(it instanceof ItemPlant)) filtered.add(it); break;
                default: filtered.add(it); break;
            }
        }

        for (int i = 0; i < filtered.size(); i++) {
            itemGrid.add(createPanelItem(filtered.get(i)));
        }

        for (int i = filtered.size(); i < WIDTH_SLOTS * HEIGHT_SLOTS; i++) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(new Color(230, 180, 110, 150));
            emptyPanel.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 2));
            itemGrid.add(emptyPanel);
        }

        itemGrid.revalidate();
        itemGrid.repaint();
    }

    public void setCategory(String category) {
        if (category != null) {
            this.selectedCategory = category;
            this.selectedItem = null; // On désélectionne quand on change d'onglet
        }
    }

    private ArrayList<JButton> buildCategories() {
        ArrayList<JButton> categorieButtons = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i < categoryNames.length) {
                JButton catButton = createCategoryButton(categoryNames[i]);
                categories.add(catButton);
                categorieButtons.add(catButton);
            } else {
                JButton emptyButton = createCategoryButton("");
                emptyButton.setVisible(false);
                categories.add(emptyButton);
            }
        }
        return categorieButtons;
    }

    // --- LE PANNEAU DE PREVIEW ET D'ACHAT/VENTE (À DROITE) ---
    private void buildDescription() {
        descriptionPanel.removeAll();
        descriptionPanel.setPreferredSize(new Dimension(DESCRIPTION_SIZE, this.height - 20));

        if (selectedItem == null) {
            // Écran par défaut
            JTextArea descr = new JTextArea("\n\nBienvenue à la Grange !\n\nCliquez sur un objet à gauche pour voir ses détails et l'acheter ou le vendre.");
            descr.setEditable(false);
            descr.setOpaque(false);
            descr.setForeground(SDV_TEXT);
            descr.setFont(getCustomFont(16f));
            descr.setLineWrap(true);
            descr.setWrapStyleWord(true);
            descriptionPanel.add(descr, BorderLayout.NORTH);
        } else {
            boolean unlocked = isItemUnlocked(selectedItem);

            JPanel detailPanel = new JPanel();
            detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
            detailPanel.setOpaque(false);

            // 1. Titre
            String typeName = unlocked ? ((selectedItem instanceof ItemSeed ? "Graine - " : "Plante - ") + selectedItem.getPlantType().getName()) : "???";
            JLabel title = new JLabel(typeName);
            title.setFont(getCustomFont(22f));
            title.setForeground(SDV_TEXT);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            // 2. Image en grand (Noire/Grisée si bloquée)
            int imgSize = 80;
            JLabel imgLabel = new JLabel(new ImageIcon(selectedItem.getImage().getImage().getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH)));
            imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imgLabel.setBorder(new EmptyBorder(15, 0, 15, 0));
            // Si bloqué, on pourrait assombrir l'image ici (facultatif)

            // 3. Stats ou Message de blocage
            JPanel statsPanel = new JPanel(new GridLayout(5, 1, 0, 8));
            statsPanel.setOpaque(false);

            if (unlocked) {
                statsPanel.add(createStatLabel("En stock : " + selectedItem.getQuantity()));
                statsPanel.add(createStatLabel("Prix d'Achat : " + barn.buyItem(selectedItem, 0) + " PO"));
                statsPanel.add(createStatLabel("Prix de Vente : " + barn.sellItem(selectedItem, 0) + " PO"));
            } else {
                JLabel lockedMsg = new JLabel("<html><center>Cet objet est verrouille.<br>Continuez à progresser pour le débloquer !</center></html>");
                lockedMsg.setFont(getCustomFont(14f));
                lockedMsg.setForeground(new Color(150, 50, 50)); // Rouge sombre
                statsPanel.add(lockedMsg);
            }

            // 4. Contrôles Achat/Vente (Uniquement si débloqué)
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            controlPanel.setOpaque(false);

            if (unlocked) {
                JTextField qtyInput = new JTextField("1", 3);
                qtyInput.setBackground(new Color(255, 240, 210));
                qtyInput.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 1));

                JButton buyBtn = createActionBtn("Acheter");
                JButton sellBtn = createActionBtn("Vendre");

                // Les contrôleurs gèrent la transaction et appellent refresh() automatiquement
                buyBtn.addActionListener(new BarnController(barn, this, selectedItem, true, qtyInput));
                sellBtn.addActionListener(new BarnController(barn, this, selectedItem, false, qtyInput));
                sellBtn.setEnabled(selectedItem.getQuantity() > 0);

                controlPanel.add(new JLabel("Qté:"));
                controlPanel.add(qtyInput);
                controlPanel.add(buyBtn);
                controlPanel.add(sellBtn);
            }

            // 5. Argent du joueur
            JLabel moneyLabel = new JLabel("Votre Portefeuille : " + barn.getMoney() + " PO");
            moneyLabel.setFont(getCustomFont(16f));
            moneyLabel.setForeground(new Color(40, 100, 40));
            moneyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            detailPanel.add(title);
            detailPanel.add(imgLabel);
            detailPanel.add(statsPanel);
            detailPanel.add(Box.createVerticalGlue());
            detailPanel.add(controlPanel);
            detailPanel.add(Box.createVerticalStrut(15));
            detailPanel.add(moneyLabel);

            descriptionPanel.add(detailPanel, BorderLayout.CENTER);
        }

        descriptionPanel.revalidate();
        descriptionPanel.repaint();
    }

    private JLabel createStatLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(SDV_TEXT);
        lbl.setFont(getCustomFont(14f));
        return lbl;
    }

    private JButton createCategoryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setBackground(SDV_BORDER_LIGHT);
        btn.setForeground(SDV_TEXT);
        btn.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 2));
        btn.setFont(getCustomFont(14f));
        return btn;
    }

    // --- CASE DE LA GRILLE (Plus de boutons ici, juste la sélection) ---
    private JPanel createPanelItem(Item item) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(true);

        boolean unlocked = isItemUnlocked(item);
        boolean isSelected = (selectedItem == item);

        // Couleurs selon l'état (Débloqué / Bloqué / Sélectionné)
        Color baseColor = unlocked ? new Color(235, 185, 120) : new Color(170, 160, 150); // Gris si bloqué
        Color hoverColor = unlocked ? new Color(245, 195, 130) : new Color(180, 170, 160);
        Color selectedColor = new Color(255, 210, 150);

        panel.setBackground(isSelected ? selectedColor : baseColor);
        panel.setBorder(BorderFactory.createLineBorder(isSelected ? Color.WHITE : SDV_BORDER_DARK, 2));
        panel.setPreferredSize(new Dimension(0, 60)); // Hauteur fixe

        if (unlocked) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        // Clic sur la case -> Met à jour le panneau de droite
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedItem = item;
                refresh(); // Reconstruit la page pour afficher le preview
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isSelected) panel.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!isSelected) panel.setBackground(baseColor);
            }
        });

        // Icône à gauche
        int iconSize = 35;
        JLabel iconLabel = new JLabel(new ImageIcon(item.getImage().getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH)));
        iconLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(iconLabel, BorderLayout.WEST);

        // Nom et Quantité
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(10, 0, 5, 5));

        String name = unlocked ? item.getPlantType().getName() : "???";
        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(SDV_TEXT);
        nameLabel.setFont(getCustomFont(14f));

        String qty = unlocked ? "Stock: " + item.getQuantity() : "Verrouille";
        JLabel qtyLabel = new JLabel(qty);
        qtyLabel.setForeground(new Color(110, 60, 20));
        qtyLabel.setFont(getCustomFont(12f));

        infoPanel.add(nameLabel);
        infoPanel.add(qtyLabel);
        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JButton createActionBtn(String text) {
        JButton btn = new JButton(text);
        Color selectLight = new Color(160, 100, 60);
        Color selectDark = new Color(80, 40, 10);

        btn.setFocusable(false);
        btn.setBackground(selectLight);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 1));
        btn.setFont(getCustomFont(12f));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { if (btn.isEnabled()) btn.setBackground(selectDark); }
            public void mouseExited(MouseEvent evt) { if (btn.isEnabled()) btn.setBackground(selectLight); }
        });

        return btn;
    }

    private Font getCustomFont(float size) {
        return GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, size) : new Font("Arial", Font.BOLD, (int)size);
    }

    public void refresh() {
        buildItemGrid();
        buildDescription();
    }
}
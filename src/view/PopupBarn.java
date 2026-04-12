package src.view;

import src.control.popups.BarnCategoriesController;
import src.control.popups.BarnController;
import src.model.*;

import javax.swing.*;
import java.awt.*;
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

    private static final int WIDTH_SLOTS = 3;
    private static final int HEIGHT_SLOTS = 5;
    private static final int DESCRIPTION_SIZE = 400;

    public PopupBarn(Display display, World world) {
        super(display, Camera.WIDTH * Display.RATIO_X - 2 * Display.RATIO_X, Camera.HEIGHT * Display.RATIO_Y - 2 * Display.RATIO_Y, "Grange");
        this.display = display;
        this.world = world;
        this.barn = world.getBarn();
        initializeUI();
    }

    private void initializeUI() {
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false); // Transparent pour voir le fond Stardew !

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        categories = new JPanel(new GridLayout(1, 4, 4, 0));
        categories.setOpaque(false);
        categories.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        itemGrid = new JPanel(new GridLayout(HEIGHT_SLOTS, WIDTH_SLOTS, 10, 10));
        itemGrid.setOpaque(false);
        itemGrid.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 10));

        descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setOpaque(false);
        descriptionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 20));

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
            // Case vide style Stardew (sable transparent)
            emptyPanel.setBackground(new Color(230, 180, 110, 150));
            emptyPanel.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 2));
            itemGrid.add(emptyPanel);
        }

        itemGrid.revalidate();
        itemGrid.repaint();
    }

    public void setCategory(String category) {
        if (category != null) this.selectedCategory = category;
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

    private void buildDescription() {
        descriptionPanel.setPreferredSize(new Dimension(DESCRIPTION_SIZE, this.height - 20));
        JTextArea descr = new JTextArea("Bienvenue à la Grange !\n\nCliquez sur un objet pour interagir.");
        descr.setEditable(false);
        descr.setOpaque(false);
        descr.setForeground(SDV_TEXT);
        descr.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(16f) : new Font("Arial", Font.PLAIN, 16));
        descr.setLineWrap(true);
        descr.setWrapStyleWord(true);
        descriptionPanel.add(descr, BorderLayout.NORTH);
    }

    private JButton createCategoryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setBackground(SDV_BORDER_LIGHT);
        btn.setForeground(SDV_TEXT);
        btn.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 2));
        btn.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 14f) : new Font("Arial", Font.BOLD, 14));
        return btn;
    }

    private JPanel createPanelItem(Item item) {
        int slotWidth = (this.width - DESCRIPTION_SIZE) / WIDTH_SLOTS;
        int slotHeight = (this.height - 20) / HEIGHT_SLOTS;
        int iconSize = Math.max(40, Math.min(slotHeight - 20, slotWidth / 4));

        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(true);

        // Couleur de la case (Orange sable style Stardew)
        panel.setBackground(new Color(235, 185, 120));
        panel.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 2));
        panel.setPreferredSize(new Dimension(slotWidth, slotHeight));

        // Bloc Gauche: Icône de l'objet
        JPanel iconSquare = new JPanel(new BorderLayout());
        iconSquare.setOpaque(false);
        iconSquare.setPreferredSize(new Dimension(iconSize + 10, iconSize + 10));
        iconSquare.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel iconLabel = new JLabel(new ImageIcon(item.getImage().getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH)));
        iconSquare.add(iconLabel, BorderLayout.CENTER);

        // Quantité
        JLabel qtyLabel = new JLabel(String.valueOf(item.getQuantity()), SwingConstants.RIGHT);
        qtyLabel.setForeground(SDV_TEXT);
        qtyLabel.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 14f) : new Font("Arial", Font.BOLD, 14));
        iconSquare.add(qtyLabel, BorderLayout.SOUTH);

        // Bloc Droit: Textes + Boutons
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel nameLabel = new JLabel((item instanceof ItemSeed ? "Graine - " : "Plante - ") + item.getPlantType().getName());
        nameLabel.setForeground(SDV_TEXT);
        nameLabel.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 14f) : new Font("Arial", Font.BOLD, 14));

        JLabel descLabel = new JLabel("Achat: " + barn.buyItem(item, 0) + " | Vente: " + barn.sellItem(item, 0));
        descLabel.setForeground(new Color(110, 60, 20));
        descLabel.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(12f) : new Font("Arial", Font.PLAIN, 12));

        // Champ texte + boutons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        actionPanel.setOpaque(false);

        JTextField qtyInput = new JTextField("1", 3);
        qtyInput.setBackground(new Color(255, 240, 210));
        qtyInput.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 1));

        JButton buyBtn = createActionBtn("Acheter");
        JButton sellBtn = createActionBtn("Vendre");

        buyBtn.addActionListener(new BarnController(barn, this, item, true, qtyInput));
        sellBtn.addActionListener(new BarnController(barn, this, item, false, qtyInput));
        sellBtn.setEnabled(item.getQuantity() > 0);

        actionPanel.add(qtyInput);
        actionPanel.add(buyBtn);
        actionPanel.add(sellBtn);

        rightPanel.add(nameLabel);
        rightPanel.add(Box.createVerticalStrut(2));
        rightPanel.add(descLabel);
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(actionPanel);

        panel.add(iconSquare, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);

        return panel;
    }


    private JButton createActionBtn(String text) {
        JButton btn = new JButton(text);

        Color selectLight = new Color(160, 100, 60);
        Color selectDark = new Color(80, 40, 10);

        btn.setFocusable(false);
        btn.setBackground(selectLight);
        btn.setForeground(Color.WHITE); // Texte blanc
        btn.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 1));

        if (GameFonts.MINECRAFT_FONT != null) {
            btn.setFont(GameFonts.MINECRAFT_FONT.deriveFont(12f));
        }

        // Effet de survol pour les boutons d'achat/vente
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(selectDark); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(selectLight); }
        });

        return btn;
    }

    public void refresh() { buildItemGrid(); }
}
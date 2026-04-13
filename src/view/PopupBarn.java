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

    private Item selectedItem = null;

    private static final int WIDTH_SLOTS = 3;
    private static final int HEIGHT_SLOTS = 5;
    private static final int DESCRIPTION_SIZE = 350;

    /** Image cadenas chargée une seule fois */
    private static ImageIcon LOCK_ICON = null;

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
        if (item == null || item.getPlantType() == null) return true;
        return world.getStats().getLevel() >= item.getPlantType().getLevelRequirement();
    }

    private ImageIcon getLockIcon(int size) {
        if (LOCK_ICON == null) {
            LOCK_ICON = new ImageIcon("src/assets/lock.png");
        }
        return new ImageIcon(LOCK_ICON.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    private void buildItemGrid() {
        itemGrid.setPreferredSize(new Dimension(this.width - DESCRIPTION_SIZE, this.height - 20));
        itemGrid.removeAll();

        ArrayList<Item> filtered = new ArrayList<>();
        for (Item it : barn.getItems()) {
            switch (selectedCategory) {
                case "Graines":      if (it instanceof ItemSeed)  filtered.add(it); break;
                case "Plantes":      if (it instanceof ItemPlant) filtered.add(it); break;
                case "Fertilisants": if (!(it instanceof ItemSeed) && !(it instanceof ItemPlant)) filtered.add(it); break;
                default:             filtered.add(it); break;
            }
        }

        for (int i = 0; i < filtered.size(); i++) {
            itemGrid.add(createPanelItem(filtered.get(i)));
        }

        // Slots vides : panneau transparent sans bordure ni fond
        for (int i = filtered.size(); i < WIDTH_SLOTS * HEIGHT_SLOTS; i++) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setOpaque(false);
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
            JTextArea descr = new JTextArea("\n\nBienvenue a la Grange !\n\nCliquez sur un objet a gauche pour voir ses details et l'acheter ou le vendre.");
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
            String typeName = unlocked
                ? (selectedItem instanceof ItemSeed ? "Graine - " : "Plante - ") + selectedItem.getPlantType().getName()
                : "???";
            JLabel title = new JLabel(typeName);
            title.setFont(getCustomFont(22f));
            title.setForeground(SDV_TEXT);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            // 2. Image (cadenas si bloqué)
            int imgSize = 80;
            ImageIcon icon = unlocked
                ? new ImageIcon(selectedItem.getImage().getImage().getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH))
                : getLockIcon(imgSize);
            JLabel imgLabel = new JLabel(icon);
            imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imgLabel.setBorder(new EmptyBorder(15, 0, 15, 0));

            // 3. Stats ou message de blocage
            JPanel statsPanel = new JPanel(new GridLayout(5, 1, 0, 8));
            statsPanel.setOpaque(false);

            if (unlocked) {
                statsPanel.add(createStatLabel("En stock : " + selectedItem.getQuantity()));
                statsPanel.add(createStatLabel("Prix d'Achat : " + barn.buyItem(selectedItem, 0) + " PO"));
                statsPanel.add(createStatLabel("Prix de Vente : " + barn.sellItem(selectedItem, 0) + " PO"));
            } else {
                int req = selectedItem.getPlantType().getLevelRequirement();
                JLabel lockedMsg = new JLabel("<html><center>Cet objet se débloque<br>au <b>niveau " + req + "</b>.<br><br>Vendez des récoltes pour gagner de l'XP !</center></html>");
                lockedMsg.setFont(getCustomFont(14f));
                lockedMsg.setForeground(new Color(150, 50, 50));
                lockedMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
                statsPanel.add(lockedMsg);
            }

            // 4. Contrôles Achat/Vente (uniquement si débloqué)
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            controlPanel.setOpaque(false);

            if (unlocked) {
                JTextField qtyInput = new JTextField("1", 3);
                qtyInput.setBackground(new Color(255, 240, 210));
                qtyInput.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 1));

                JButton buyBtn  = createActionBtn("Acheter");
                JButton sellBtn = createActionBtn("Vendre");

                buyBtn.addActionListener(new BarnController(barn, this, selectedItem, true, qtyInput));
                sellBtn.addActionListener(new BarnController(barn, this, selectedItem, false, qtyInput));
                sellBtn.setEnabled(selectedItem.getQuantity() > 0);

                controlPanel.add(new JLabel("Qte:"));
                controlPanel.add(qtyInput);
                controlPanel.add(buyBtn);
                controlPanel.add(sellBtn);
            }

            // 5. Argent du joueur
            JLabel moneyLabel = new JLabel("Portefeuille : " + barn.getMoney() + " PO");
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

    // --- CASE DE LA GRILLE ---
    private JPanel createPanelItem(Item item) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(true);

        boolean unlocked = isItemUnlocked(item);
        boolean isSelected = (selectedItem == item);

        Color baseColor    = unlocked ? new Color(235, 185, 120) : new Color(170, 160, 150);
        Color hoverColor   = unlocked ? new Color(245, 195, 130) : new Color(180, 170, 160);
        Color selectedColor = new Color(255, 210, 150);

        panel.setBackground(isSelected ? selectedColor : baseColor);
        panel.setBorder(BorderFactory.createLineBorder(isSelected ? Color.WHITE : SDV_BORDER_DARK, 2));
        panel.setPreferredSize(new Dimension(0, 60));

        if (unlocked) panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                selectedItem = item;
                refresh();
            }
            @Override public void mouseEntered(MouseEvent e) {
                if (!isSelected) panel.setBackground(hoverColor);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!isSelected) panel.setBackground(baseColor);
            }
        });

        // Icône : cadenas si bloqué, sprite sinon
        int iconSize = 35;
        ImageIcon icon = unlocked
            ? new ImageIcon(item.getImage().getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH))
            : getLockIcon(iconSize);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(iconLabel, BorderLayout.WEST);

        // Nom et info droite
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(10, 0, 5, 5));

        String name = unlocked ? item.getPlantType().getName() : "???";
        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(SDV_TEXT);
        nameLabel.setFont(getCustomFont(14f));

        // Ligne du bas : quantité si débloqué, "Niveau X" si bloqué
        String subText;
        if (unlocked) {
            subText = "Stock: " + item.getQuantity();
        } else {
            subText = "Niv. " + item.getPlantType().getLevelRequirement();
        }
        JLabel subLabel = new JLabel(subText);
        subLabel.setForeground(unlocked ? new Color(110, 60, 20) : new Color(100, 80, 150));
        subLabel.setFont(getCustomFont(12f));

        infoPanel.add(nameLabel);
        infoPanel.add(subLabel);
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
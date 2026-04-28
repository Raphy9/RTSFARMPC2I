package src.view;

import src.control.popups.BarnController;
import src.control.popups.BarnCategoriesController;
import src.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Popup d'inventaire de la grange: affiche les items stockés dans une grille 4x5.
 * Interface 3 zones: catégories (haut), grille d'items (centre), description (droite).
 * Permet le transfert d'items via clic/shift-clic.
 */
public class PopupBarn extends PopupPanel {

    // Références du jeu
    private Display display;      // Pour rafraîchir l'écran après les mises à jour
    private World world;          // Pour accéder à la grange: world.getBarn()
    private Barn barn;            // Référence directe à la grange (extends Inventory)

    // Composants visuels
    private JPanel itemGrid;         // Grille 4x5 des items (20 cases)
    private JPanel categories;       // Barre de 8 boutons: Toutes, Graines, Plantes, + vides
    private JPanel descriptionPanel; // Zone droite: détails de l'item sélectionné

    private String selectedCategory = "Toutes";
    private Item selectedItem = null;

    // Configuration
    private static final int WIDTH_SLOTS = 3;       // 3 colonnes
    private static final int HEIGHT_SLOTS = 5;      // 5 lignes = 20 cases
    private static final int DESCRIPTION_SIZE = 400; // 400px de largeur pour la description

    private static final Color SDV_TEXT = new Color(60, 30, 10);
    private static final Color SDV_BORDER_LIGHT = new Color(235, 185, 120);
    private static final Color SDV_BORDER_DARK = new Color(110, 45, 15);

    /**
     * Initialise le popup de la grange.
     * @param display affichage principal (pour rafraîchir après transferts)
     * @param world le monde (accès à la grange via getBarn())
     */
    public PopupBarn(Display display, World world) {
        // Créer le popup avec dimensions maximales (presque tout l'écran)
        super(display, Camera.WIDTH*Display.RATIO_X - 2*Display.RATIO_X, Camera.HEIGHT*Display.RATIO_Y-2*Display.RATIO_Y, "Grange");
        
        // Stocker les références
        this.display = display;
        this.world = world;
        // Sécurité: assure un catalogue minimum visible même après une sauvegarde partielle/corrompue.
        this.world.ensureBarnCatalog();
        this.barn = world.getBarn();
        
        // Construire l'interface: catégories + grille + description
        initializeUI();
    }

    /**
     * Construit l'interface: 3 zones disposées verticalement et horizontalement.
     * NORD: boutons de catégories (Toutes, Graines, Plantes)
     * CENTRE: grille 4x5 d'items
     * EST: panneau de description (détails de l'item)
     */
    private void initializeUI() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(new Color(0, 0, 0, 0));
        center.setOpaque(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 0, 0, 0));
        panel.setOpaque(false);
        
        // === Créer les 3 zones vides ===
        categories = new JPanel(new GridLayout(1, 4, 4, 0));       // 4 colonnes pour 4 boutons
        categories.setOpaque(false);
        categories.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        itemGrid = new JPanel(new GridLayout(HEIGHT_SLOTS, WIDTH_SLOTS, 10, 10)); // 5x4, espacement 10px
        itemGrid.setOpaque(false);
        itemGrid.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 10));

        // Panneau de droite (Preview) séparé par une ligne verticale
        descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setOpaque(false);
        descriptionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 2, 0, 0, SDV_BORDER_DARK),
                BorderFactory.createEmptyBorder(10, 10, 20, 20)
        ));

        // === Remplir les zones ===
        buildItemGrid();      // Parcourir inventaire et créer les cases d'items
        buildDescription();   // Ajouter le texte "Description des items ici"
        ArrayList<JButton> categoryButtons = buildCategories();    // Créer les boutons de catégories
        
        for (int i = 0; i < categoryButtons.size() && i < 4; i++) {
            JButton catButton = categoryButtons.get(i);
            catButton.addActionListener(new BarnCategoriesController(catButton, this, new String[]{"Toutes", "Graines", "Plantes", "Fertilisants"}[i]));
        }

        // === Assembler: NORD (catégories) | CENTRE (grille) | EST (description) ===
        panel.add(categories, BorderLayout.NORTH);
        panel.add(itemGrid, BorderLayout.CENTER);
        panel.add(descriptionPanel, BorderLayout.EAST);
        
        center.add(panel, BorderLayout.CENTER);
        this.add(center, BorderLayout.CENTER);
    }

    /**
     * Remplit la grille avec les items de l'inventaire.
     * Cycle: VIDER → REMPLIR → REDESSINER (utile pour les rafraîchissements après transferts).
     */
    private void buildItemGrid() {
        itemGrid.setPreferredSize(new Dimension(this.width-DESCRIPTION_SIZE, this.height-20));
        
        // ÉTAPE 1: Vider la grille (enlever les anciens items)
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
            if (filtered.get(i).getRequiredLevel() <= world.getStats().getLevel()) {
                itemGrid.add(createPanelItem(filtered.get(i)));
            }
        }
        for (int i = 0; i < filtered.size(); i++) {
            if (filtered.get(i).getRequiredLevel() > world.getStats().getLevel()) {
                itemGrid.add(createPanelItem(filtered.get(i)));
            }
        }

        // Slots vides : panneau transparent sans bordure ni fond
        for (int i = filtered.size(); i < WIDTH_SLOTS * HEIGHT_SLOTS; i++) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setOpaque(false);
            itemGrid.add(emptyPanel);
        }
        
        // ÉTAPE 3: Forcer Swing à recalculer et redessiner
        itemGrid.revalidate();
        itemGrid.repaint();
    }

    public void setCategory(String category) {
        if (category != null) {
            this.selectedCategory = category;
            this.selectedItem = null; // On désélectionne quand on change d'onglet
        }
    }

    /**
     * Crée la barre de boutons de catégories: 3 nommés (Toutes, Graines, Plantes) + 1 vide.
     */
    private ArrayList<JButton> buildCategories() {
        String[] categoryNames = {"Toutes", "Graines", "Plantes", "Fertilisants"};
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

    /**
     * Panneau de description (droite): affiche les détails de l'item sélectionné.
     */
    private void buildDescription() {
        descriptionPanel.removeAll();
        descriptionPanel.setPreferredSize(new Dimension(DESCRIPTION_SIZE, this.height - 20));

        if (selectedItem == null) {
            // Écran par défaut
            JTextArea descr = new JTextArea("\n\nBienvenue a la Grange !\n\nCliquez sur un objet a gauche pour voir ses details.");
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

            // 2. Image
            int imgSize = 80;
            ImageIcon icon = unlocked
                ? new ImageIcon(selectedItem.getImage().getImage().getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH))
                : getLockIcon(imgSize);
            JLabel imgLabel = new JLabel(icon);
            imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imgLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

            // 3. Détails complets
            JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 0, 6));
            detailsPanel.setOpaque(false);

            if (unlocked) {
                PlantType plantType = selectedItem.getPlantType();
                
                // Type d'item
                String typeStr = selectedItem instanceof ItemSeed ? "Type : Graine" : "Type : Plante/Produit";
                detailsPanel.add(createDetailLabel(typeStr));
                
                // Stock
                detailsPanel.add(createDetailLabel("En stock : " + selectedItem.getQuantity()));
                
                // Prix de vente (toujours affiché)
                detailsPanel.add(createDetailLabel("Prix de Vente : " + barn.sellItem(selectedItem, 0) + " PO"));
                // N'afficher le prix d'achat QUE pour les graines ou autres items non-plante
                if (!(selectedItem instanceof ItemPlant)) {
                    detailsPanel.add(createDetailLabel("Prix d'Achat : " + barn.buyItem(selectedItem, 0) + " PO"));
                }

                // Croissance (si c'est une graine)
                if (selectedItem instanceof ItemSeed) {
                    detailsPanel.add(createDetailLabel("Durée de croissance : " + plantType.getGrowthDuration() + " unités"));
                    detailsPanel.add(createDetailLabel("Consommation d'eau : " + String.format("%.1f", plantType.getWaterConsumption()) + "/unité"));
                }
                
            } else {
                int req = selectedItem.getPlantType().getLevelRequirement();
                JLabel lockedMsg = new JLabel("<html><center>Cet objet se débloque<br>au niveau " + req + ".</center></html>");
                lockedMsg.setFont(getCustomFont(14f));
                lockedMsg.setForeground(SDV_TEXT);
                lockedMsg.setHorizontalAlignment(SwingConstants.CENTER);

                JPanel lockedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                lockedPanel.setOpaque(false);
                lockedPanel.add(lockedMsg);

                detailsPanel.add(lockedPanel);
                detailsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            }


            detailPanel.add(title);
            detailPanel.add(imgLabel);
            detailPanel.add(detailsPanel);
            detailPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            descriptionPanel.add(detailPanel, BorderLayout.CENTER);
        }

        descriptionPanel.revalidate();
        descriptionPanel.repaint();
    }

    private boolean isItemUnlocked(Item item) {
        if (item == null || item.getPlantType() == null) return true;
        return world.getStats().getLevel() >= item.getPlantType().getLevelRequirement();
    }

    private ImageIcon getLockIcon(int size) {
        // Placeholder, assume lock.png exists
        return new ImageIcon("src/assets/lock.png");
    }

    private JLabel createStatLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(SDV_TEXT);
        lbl.setFont(getCustomFont(14f));
        return lbl;
    }

    /**
     * Crée un bouton pour la barre de catégories.
     */
    private JButton createCategoryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setBackground(SDV_BORDER_LIGHT);
        btn.setForeground(SDV_TEXT);
        btn.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 2));
        btn.setFont(getCustomFont(14f));
        return btn;
    }

    /**
     * Crée une case d'item: carré icône à gauche + nom/description à droite + boutons acheter/vendre.
     */
    private JPanel createPanelItem(Item item) {
        // Calculer les dimensions de la case
        int slotWidth = (this.width - DESCRIPTION_SIZE) / WIDTH_SLOTS;
        int slotHeight = (this.height - 20) / HEIGHT_SLOTS;
        int iconSize = Math.max(45, Math.min(slotHeight - 16, slotWidth / 3));

        // === CONTENEUR PRINCIPAL ===
        JPanel panel = new JPanel(new BorderLayout(10, 0)); // 10px espace horizontal entre gauche/droite
        boolean unlocked = isItemUnlocked(item);
        boolean outOfStock = item.getQuantity() <= 0;
        // si débloqué -> couleur claire, sinon gris
        panel.setBackground(unlocked ? SDV_BORDER_LIGHT : Color.GRAY);
        panel.setPreferredSize(new Dimension(slotWidth, slotHeight));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); // Marges

        boolean isSelected = (selectedItem == item);

        if (isSelected) {
            panel.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 3));
        }

        if (unlocked) panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                selectedItem = item;
                refresh();
            }
        });

        // === BLOC GAUCHE: Carré avec icône + badge quantité ===
        JPanel iconSquare = new JPanel(new BorderLayout());
        iconSquare.setPreferredSize(new Dimension(iconSize, iconSize));
        iconSquare.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK));
        iconSquare.setBackground(unlocked ? SDV_BORDER_LIGHT : Color.GRAY);

        // Superposer l'icône et le badge
        JPanel overlay = new JPanel();
        overlay.setLayout(new OverlayLayout(overlay));
        overlay.setOpaque(false);

        // Icône centrée
        ImageIcon iconImg = unlocked
            ? new ImageIcon(item.getImage().getImage().getScaledInstance(iconSize - 40, iconSize - 40, Image.SCALE_SMOOTH))
            : getLockIcon(iconSize - 40);
        JLabel iconLabel = new JLabel(iconImg);
        iconLabel.setAlignmentX(0.5f);
        iconLabel.setAlignmentY(0.5f);

        // Si out of stock, ajouter un calque semi-transparent pour "dim" l'icône
        if (outOfStock) {
            JPanel dimmer = new JPanel();
            dimmer.setOpaque(true);
            // blanc semi-transparent pour éclaircir et donner l'effet grisé; on peut aussi utiliser noir transparent
            dimmer.setBackground(new Color(150, 150, 150, 160));
            dimmer.setAlignmentX(0.5f);
            dimmer.setAlignmentY(0.5f);
            overlay.add(dimmer);
        }

        // Texte quantité en bas à droite
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 1));
        quantityPanel.setOpaque(true);
        quantityPanel.setBackground(new Color(0, 0, 0, 170));
        quantityPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 120)));
        quantityPanel.setAlignmentX(1.0f);
        quantityPanel.setAlignmentY(1.0f);

        JLabel quantityLabel = new JLabel("x" + item.getQuantity());
        quantityLabel.setForeground(outOfStock ? Color.LIGHT_GRAY : Color.WHITE);
        quantityLabel.setFont(quantityLabel.getFont().deriveFont(Font.BOLD, 11f));
        quantityPanel.add(quantityLabel);

        overlay.add(iconLabel);
        iconSquare.add(overlay, BorderLayout.CENTER);
        iconSquare.add(quantityPanel, BorderLayout.SOUTH);

        // === BLOC DROIT: infos en haut + actions en bas ===
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        String name = unlocked ? item.getPlantType().getName() : "???";
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(getCustomFont(14f));
        // Si débloqué mais outOfStock -> gris, sinon si débloqué normal couleur SDV_TEXT, sinon dark gray
        nameLabel.setForeground(!unlocked ? Color.DARK_GRAY : SDV_TEXT);

        infoPanel.add(nameLabel);

        JPanel economyPanel = new JPanel();
        economyPanel.setLayout(new BoxLayout(economyPanel, BoxLayout.Y_AXIS));
        economyPanel.setOpaque(false);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        actionsPanel.setOpaque(false);

        JTextField quantityInput = new JTextField("1", 3);
        quantityInput.setPreferredSize(new Dimension(40, 20));
        quantityInput.setMaximumSize(new Dimension(40, 20));
        quantityInput.setFont(getCustomFont(10f));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        inputPanel.setOpaque(false);
        inputPanel.add(new JLabel("Qte:"));
        inputPanel.add(quantityInput);

        JButton buyButton = createActionBtn("Acheter");
        JButton sellButton = createActionBtn("Vendre");  

        if (unlocked) {
            // Do not allow buying of ItemPlant (légumes) from the barn — only selling
            if (!(item instanceof ItemPlant)) {
                buyButton.addActionListener(new BarnController(world, barn, PopupBarn.this, item, true, quantityInput));
            } else {
                // hide the buy button for plants to avoid leaving empty space
                buyButton.setVisible(false);
            }
            sellButton.addActionListener(new BarnController(world, barn, PopupBarn.this, item, false, quantityInput));
            sellButton.setEnabled(item.getQuantity() > 0);
         } else {
             buyButton.setEnabled(false);
             sellButton.setEnabled(false);
             buyButton.setBackground(Color.LIGHT_GRAY);
             sellButton.setBackground(Color.LIGHT_GRAY);
         }

         actionsPanel.add(buyButton);
         actionsPanel.add(sellButton);

        economyPanel.add(inputPanel);
        economyPanel.add(Box.createVerticalStrut(2));
        economyPanel.add(actionsPanel);

        textPanel.add(infoPanel, BorderLayout.NORTH);
        textPanel.add(economyPanel, BorderLayout.SOUTH);

        // === ASSEMBLER ===
        panel.add(iconSquare, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Génère la mini-description.
     */
    private String buildItemDescription(Item item) {
        PlantType plantType = item.getPlantType();
        String usage = item instanceof ItemSeed ? "A planter" : "Pret a vendre";
        return String.format("%s | Croissance: %dt | Eau: %.1f/t | Valeur: %d", 
                usage, plantType.getGrowthDuration(), plantType.getWaterConsumption(), plantType.getValue());
    }

    private Font getCustomFont(float size) {
        return GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, size) : new Font("Arial", Font.BOLD, (int)size);
    }

    /** Rafraîchit la grille et la description après une action. */
    public void refresh() {
        buildItemGrid();
        buildDescription();
    }

    private JButton createActionBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(60, 20));
        btn.setFont(getCustomFont(10f));
        btn.setBackground(SDV_BORDER_LIGHT);
        btn.setForeground(SDV_TEXT);
        btn.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 1));
        return btn;
    }

    private JLabel createDetailLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(SDV_TEXT);
        lbl.setFont(getCustomFont(14f));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
}

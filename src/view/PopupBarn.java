package src.view;

import src.control.CameraController;
import src.control.popups.BarnCategoriesController;
import src.control.popups.BarnController;
import src.model.*;

import javax.swing.*;
import java.awt.*;
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
    private JPanel categories;       // Barre de 4 boutons: Toutes, Graines, Plantes, Fertilisants
    private JPanel descriptionPanel; // Zone droite: détails de l'item sélectionné

    // Filtre courant (nom de la catégorie sélectionnée)
    private String selectedCategory = "Toutes";
    private String[] categoryNames = {"Toutes", "Graines", "Plantes", "Fertilisants"};

    // Configuration
    private static final int WIDTH_SLOTS = 3;       // 4 colonnes
    private static final int HEIGHT_SLOTS = 5;      // 5 lignes = 20 cases
    private static final int DESCRIPTION_SIZE = 400; // 400px de largeur pour la description


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
        JPanel panel = new JPanel(new BorderLayout());
        
        // === Créer les 3 zones vides ===
        categories = new JPanel(new GridLayout(1, 4));       // 8 colonnes pour 8 boutons
        itemGrid = new JPanel(new GridLayout(HEIGHT_SLOTS, WIDTH_SLOTS, 10, 10)); // 5x4, espacement 10px
        descriptionPanel = new JPanel(new FlowLayout());     // Texte fluide à droite
        
        // === Remplir les zones ===
        buildItemGrid();      // Parcourir inventaire et créer les cases d'items
        buildDescription();   // Ajouter le texte "Description des items ici"

        ArrayList<JButton> categoryButtons = buildCategories();    // Créer les 4 boutons de catégories
        // Attacher un controller qui transmet la catégorie (nom) au popup
        for (int i = 0; i < categoryButtons.size() && i < categoryNames.length; i++) {
            JButton catButton = categoryButtons.get(i);
            catButton.addActionListener(new BarnCategoriesController(catButton, this, categoryNames[i]));
        }

        // Panel contenant catégories et grille à gauche et description à droite
        JPanel left = new JPanel(new BorderLayout());
        left.add(categories, BorderLayout.NORTH);
        left.add(itemGrid, BorderLayout.CENTER);
        panel.add(left, BorderLayout.CENTER);
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

        itemGrid.removeAll();

        // Préparer la liste filtrée selon la catégorie sélectionnée
        ArrayList<Item> filtered = new ArrayList<>();
        for (Item it : barn.getItems()) {
            switch (selectedCategory) {
                case "Graines":
                    if (it instanceof ItemSeed) filtered.add(it);
                    break;
                case "Plantes":
                    if (it instanceof ItemPlant) filtered.add(it);
                    break;
                case "Fertilisants":
                    if (!(it instanceof ItemSeed) && !(it instanceof ItemPlant)) filtered.add(it);
                    break;
                default: // "Toutes"
                    filtered.add(it);
                    break;
            }
        }

        // Remplir la grille avec les items filtrés
        for (int i = 0; i < WIDTH_SLOTS * HEIGHT_SLOTS; i++) {
            if (i < filtered.size()) {
                Item item = filtered.get(i);
                JPanel p = createPanelItem(item); // Crée la case visuelle
                itemGrid.add(p);
            } else {
                // Pas d'item: ajouter une case vide pour garder la structure
                itemGrid.add(new JLabel());
            }
        }
        
        // ÉTAPE 3: Forcer Swing à recalculer et redessiner
        itemGrid.revalidate();
        itemGrid.repaint();
    }

    /**
     * Définit la catégorie filtrée et force le rafraîchissement de l'affichage.
     * @param category nom de la catégorie ("Toutes", "Graines", "Plantes", "Fertilisants")
     */
    public void setCategory(String category) {
        if (category == null) return;
        this.selectedCategory = category;
    }

    /**
     * Crée la barre de boutons de catégories: 3 nommés (Toutes, Graines, Plantes) + 5 vides.
     * Les vides sont désactivés pour maintenir l'alignement de la grille.
     */
    private ArrayList<JButton> buildCategories() {
        ArrayList<JButton> categorieButtons = new ArrayList<>();
        // Remplir les 4 colonnes de la grille des catégories
        for (int i = 0; i < 4; i++) {
            if (i < categoryNames.length) {
                // Bouton avec nom
                JButton catButton = createCategoryButton(categoryNames[i]);
                categories.add(catButton);
                categorieButtons.add(catButton);
            } else {
                // Bouton vide et désactivé
                JButton emptyButton = createCategoryButton("");
                emptyButton.setEnabled(false);
                emptyButton.setBorder(BorderFactory.createEmptyBorder()); // Pas de bordure
                categories.add(emptyButton);
            }
        }
        return categorieButtons;
    }

    /**
     * Panneau de description (droite): affiche les détails de l'item sélectionné.
     * Pour l'instant juste un placeholder, à améliorer pour détails réels.
     */
    private void buildDescription() {
        descriptionPanel.setPreferredSize(new Dimension(DESCRIPTION_SIZE, this.height-20));
        JTextArea descr = new JTextArea("Description des items ici");
        descr.setEditable(false);
        descriptionPanel.add(descr);
    }

    /**
     * Crée un bouton pour la barre de catégories.
     * Propriétés: pas de focus border, texte centré sous l'icône.
     */
    private JButton createCategoryButton(String text) {
        JButton CButton = new JButton(text);
        CButton.setFocusable(false); // Pas de bordure au clic
        CButton.setHorizontalTextPosition(SwingConstants.CENTER); // Texte centré
        CButton.setVerticalTextPosition(SwingConstants.BOTTOM);   // Texte sous l'icône

        return CButton;
    }

    /**
     * Crée une case d'item: carré icône à gauche + nom/description à droite.
     * Le carré a un badge de quantité en bas à droite (superposé avec OverlayLayout).
     */
    private JPanel createPanelItem(Item item) {
        // Calculer les dimensions de la case
        int slotWidth = (this.width - DESCRIPTION_SIZE) / WIDTH_SLOTS;
        int slotHeight = (this.height - 20) / HEIGHT_SLOTS;
        int iconSize = Math.max(45, Math.min(slotHeight - 16, slotWidth / 3));

        // === CONTENEUR PRINCIPAL ===
        JPanel panel = new JPanel(new BorderLayout(10, 0)); // 10px espace horizontal entre gauche/droite

        if (item.getQuantity() == 0) {
            // Item en rupture de stock: fond noir clair
            panel.setBackground(Color.getHSBColor(77, 52, 50));
        } else {
            // Item disponible: fond normal
            panel.setBackground(Color.getHSBColor(77, 52, 34));
        }
        panel.setPreferredSize(new Dimension(slotWidth, slotHeight));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); // Marges

        // === BLOC GAUCHE: Carré avec icône + badge quantité ===
        JPanel iconSquare = new JPanel(new BorderLayout());
        iconSquare.setPreferredSize(new Dimension(iconSize, iconSize));
        iconSquare.setBorder(BorderFactory.createLineBorder(new Color(120, 95, 70)));
        iconSquare.setBackground(new Color(242, 231, 213));

        // Superposer l'icône et le badge (OverlayLayout: alignement détermine la position)
        JPanel overlay = new JPanel();
        overlay.setLayout(new OverlayLayout(overlay));
        overlay.setOpaque(false);

        // Icône centrée (0.5f = centre)
        JLabel iconLabel = new JLabel(new ImageIcon(item.getImage().getImage().getScaledInstance(iconSize - 40, iconSize - 40, Image.SCALE_SMOOTH)));
        iconLabel.setAlignmentX(0.5f);
        iconLabel.setAlignmentY(0.5f);

        // Texte quantité en bas à droite (1.0f = droite/bas)
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 1));
        quantityPanel.setOpaque(true);
        quantityPanel.setBackground(new Color(0, 0, 0, 170)); // Noir semi-transparent
        quantityPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 120))); // Bordure blanche
        quantityPanel.setAlignmentX(1.0f);
        quantityPanel.setAlignmentY(1.0f);

        JLabel quantityLabel = new JLabel("x" + item.getQuantity());
        quantityLabel.setForeground(Color.WHITE);
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

        JLabel nameLabel = new JLabel(buildItemTitle(item));
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));

        JLabel descriptionLabel = new JLabel(buildItemDescription(item));
        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.PLAIN, 11f));

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(4)); // Petit espace
        infoPanel.add(descriptionLabel);

        JPanel economyPanel = new JPanel();
        economyPanel.setLayout(new BoxLayout(economyPanel, BoxLayout.Y_AXIS));
        economyPanel.setOpaque(false);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionsPanel.setOpaque(false);

        JTextField quantityInput = new JTextField(7);
        quantityInput.setPreferredSize(new Dimension(100, 22));
        quantityInput.setMaximumSize(new Dimension(100, 22));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        inputPanel.setOpaque(false);
        inputPanel.add(quantityInput);

        JButton buyButton = new JButton("Acheter");
        JButton sellButton = new JButton("Vendre");  
        buyButton.setFocusable(false);
        sellButton.setFocusable(false);

        // Acheter/Vendre agit sur l'item de la ligne courante.
        buyButton.addActionListener(new BarnController(barn, this, item, true, quantityInput));
        sellButton.addActionListener(new BarnController(barn, this, item, false, quantityInput));
        sellButton.setEnabled(item.getQuantity() > 0);

        actionsPanel.add(buyButton);
        actionsPanel.add(sellButton);

        economyPanel.add(inputPanel);
        economyPanel.add(Box.createVerticalStrut(5));
        economyPanel.add(Box.createHorizontalStrut(10));
        economyPanel.add(actionsPanel);

        textPanel.add(infoPanel, BorderLayout.NORTH);
        textPanel.add(economyPanel, BorderLayout.SOUTH);

        // === ASSEMBLER ===
        panel.add(iconSquare, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);

        return panel;
    }


    /**
     * Génère le titre de l'item: "Graine - X" ou "Produit - X"
     * @return Ex: "Graine - Carotte" ou "Produit - Fraise"
     */
    private String buildItemTitle(Item item) {
        // Vérifier si c'est une graine (ItemSeed) ou un produit (ItemPlant)
        return (item instanceof ItemSeed ? "Graine - " : "Produit - ") + item.getPlantType().getName();
    }


    /**
     * Génère la mini-description: usage | croissance | eau | valeur
     * @return Ex: "A planter | Croissance: 75t | Eau: 0.5/t | Valeur: 10"
     */
    private String buildItemDescription(Item item) {
        return "Prix achat: " + barn.buyItem(item, 0) + " | Prix vente: " + barn.sellItem(item,0);
    }

    /** Rafraîchit la grille après une action d'achat/vente. */
    public void refresh() {
        buildItemGrid();
    }
}

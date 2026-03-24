package src.view;

import src.control.popups.BarnInventorySelector;
import src.model.Barn;
import src.model.Gardener;
import src.model.Inventory;
import src.model.Item;
import src.model.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Popup pour gérer les transferts d'items entre le jardinier et la grange.
 * Affiche deux grilles côte à côte : à gauche l'inventaire du jardinier, à droite l'inventaire de la grange.
 * Permet de transférer des items d'une grille à l'autre en cliquant sur les items. Un clic simple transfère une unité,
 * un shift-clic transfère toute la pile.
 * Affiche un message temporaire pour confirmer le transfert (ex: "Transféré 1 unité : Carotte" ou
 * "Transféré toute la pile : Chou") pendant 1 seconde après chaque transfert.
 * Le popup se rafraîchit automatiquement après chaque transfert pour afficher les quantités mises à jour.
 */
public class PopupBarn extends PopupPanel {

    // Références nécessaires pour accéder aux inventaires du jardinier et de la grange, et pour rafraîchir l'affichage après les transferts
    private Display display;
    private World world;
    private Gardener gardener;
    private Barn barn;

    // Inventaires à afficher dans les grilles
    private Inventory gardenerInventory;
    private Inventory barnInventory;

    private JPanel rightGrid;

    private JLabel feedbackLabel; // message temporaire

    // Dimensions de la grille d'affichage des items (5x3)
    private static final int WIDTH_SLOTS = 5;
    private static final int HEIGHT_SLOTS = 3;

    /**
     * Constructeur du PopupBarn.
     * @param display L'affichage global de la vue, nécessaire pour fermer le popup et rafraîchir l'affichage après les transferts.
     * @param world Le monde dans lequel évolue le jardinier, nécessaire pour accéder à la grange et ses inventaires.
     * @param gardener Le jardinier qui interagit avec la grange, nécessaire pour accéder à son inventaire et effectuer les transferts.
     *
     * Le constructeur initialise les références nécessaires, récupère les inventaires du jardinier et de la grange,
     * puis construit l'interface utilisateur du popup en appelant initializeUI().
     */
    public PopupBarn(Display display, World world, Gardener gardener) {
        super(display, 600, 300, "Grange");
        this.display = display;
        this.world = world;
        this.gardener = gardener;
        this.barn = world.getBarn();
        this.gardenerInventory = gardener.getInventory();
        this.barnInventory = this.barn;

        initializeUI();
    }

    /** Méthode pour initialiser l'interface utilisateur du popup.
     * Elle construit une interface avec deux grilles côte à côte : à gauche l'inventaire du jardinier, à droite l'inventaire de la grange.
     * Chaque grille affiche les items disponibles avec des boutons cliquables pour effectuer les transferts.
     * Un JLabel en bas est utilisé pour afficher des messages temporaires de confirmation après chaque transfert.
     */
    private void initializeUI() {
        JPanel center = new JPanel(new BorderLayout());


        // Grilles d'inventaire
        JPanel grids = new JPanel(new GridLayout(1,2));
        rightGrid = new JPanel(new GridLayout(HEIGHT_SLOTS, WIDTH_SLOTS));

        // Construire les grilles avec les items initiaux
        buildRightGrid();

        // Ajouter les grilles au panel central
        grids.add(rightGrid);

        // Label de feedback en bas
        feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
        feedbackLabel.setForeground(Color.BLUE);

        // Assembler le panel central
        center.add(grids, BorderLayout.CENTER);
        center.add(feedbackLabel, BorderLayout.SOUTH);

        this.add(center, BorderLayout.CENTER);
    }

    /** Méthode pour construire la grille de droite qui affiche l'inventaire de la grange.
     * Elle fonctionne de la même manière que buildLeftGrid(), mais elle affiche les items de l'inventaire de la grange et les boutons permettent de transférer les items vers le jardinier.
     */
    private void buildRightGrid() {
        rightGrid.removeAll(); // vider la grille avant de la reconstruire
        // Parcourir les cases de la grille (5x3) et ajouter les items de l'inventaire de la grange
        for (int i = 0; i < WIDTH_SLOTS * HEIGHT_SLOTS; i++) {
            if (i < barnInventory.getItems().size()) { // Si il y a un item à afficher pour cette case de la grille
                Item item = barnInventory.getItems().get(i);
                JButton b = createItemButton(item, barnInventory, gardenerInventory);
                rightGrid.add(b);
            } else { // Sinon ajouter une case vide pour garder la structure de la grille
                rightGrid.add(new JLabel());
            }
        }
        rightGrid.revalidate();
        rightGrid.repaint();
    }

    /** Méthode pour créer un bouton d'item dans les grilles d'inventaire.
     * @param item L'item à afficher sur le bouton, qui contient la quantité et l'icône à afficher.
     * @param source L'inventaire source du transfert (soit le jardinier soit la grange).
     * @param target L'inventaire cible du transfert (soit la grange soit le jardinier).
     * Le bouton affiche la quantité de l'item (ex: "x3" pour 3 unités) et une icône de l'item.
     * Un clic simple sur le bouton transfère une unité de l'item vers l'inventaire cible,
     * tandis qu'un shift-clic transfère toute la pile de l'item. Après le transfert,
     * le popup se rafraîchit pour afficher les quantités mises à jour, et un message
     * temporaire de confirmation est affiché en bas du popup (ex: "Transféré 1 unité :
     * Carotte" ou "Transféré toute la pile : Chou").
     */
    private JButton createItemButton(Item item, Inventory source, Inventory target) {
        // Créer un bouton pour afficher l'item, avec la quantité de l'item affichée sur le bouton (ex: "x3" pour 3 unités)
        JButton itemButton = new JButton("x" + item.getQuantity());
        itemButton.setIcon(new ImageIcon(item.getImage().getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH)));
        itemButton.setFocusable(false);
        itemButton.setHorizontalTextPosition(SwingConstants.CENTER);
        itemButton.setVerticalTextPosition(SwingConstants.BOTTOM);


        return itemButton;
    }

    /** Méthode pour afficher un message temporaire de confirmation après un transfert d'item.
     * @param msg Le message à afficher, qui indique ce qui a été transféré (ex: "Transféré
     * 1 unité : Carotte" ou "Transféré toute la pile : Chou").
     * Le message est affiché dans le feedbackLabel en bas du popup, et il est automatiquement
     * réinitialisé à une chaîne vide après 1 seconde grâce à un Timer Swing.
     */
//    private void showTemporaryMessage(String msg) {
//        feedbackLabel.setText(msg);
//        // Timer Swing pour réinitialiser le message au bout d'une seconde
//        javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
//            feedbackLabel.setText(" ");
//        });
//        t.setRepeats(false);
//        t.start();
//    }

    /** Méthode pour effectuer le transfert d'items entre les inventaires du jardinier et de la grange.
     * @param source L'inventaire source du transfert (soit le jardinier soit la grange).
     * @param target L'inventaire cible du transfert (soit la grange soit le jardinier).
     * @param item L'item à transférer, qui contient la quantité disponible dans l'inventaire source.
     * @param qty La quantité à transférer, qui peut être 1 pour un transfert simple ou -1 pour transférer toute la pile de l'item.
     * La méthode utilise la méthode transferTo() de l'inventaire source pour effectuer le transfert vers l'inventaire cible, en passant la quantité à transférer.
     */
    public void transfer(Inventory source, Inventory target, Item item, int qty) {
        int toTransfer = qty;
        if (qty == -1) toTransfer = item.getQuantity();
        source.transferTo(target, item, toTransfer);
        refresh();
        display.repaint();
    }

    /** Méthode pour rafraîchir les grilles d'inventaire après un transfert d'item.
     * Elle reconstruit les deux grilles (gauche et droite) en appelant buildLeftGrid() et buildRightGrid(),
     * ce qui met à jour l'affichage des items et de leurs quantités dans les deux grilles.
     */
    public void refresh() {
        buildRightGrid();
    }
}

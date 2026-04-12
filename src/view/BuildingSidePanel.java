package src.view;

import src.control.BuildingManager;
import src.model.World;
import src.model.buildings.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Panneau latéral droit plus grand et esthétique pour choisir les bâtiments à poser.
 * - Catégories en haut
 * - Liste de cartes uniformes (image à gauche, titre + prix à droite)
 * - Bouton "Poser" qui déclenche le placement via BuildingManager
 * - Transparent et sans scroll (les éléments doivent tenir dans la hauteur disponible)
 */
public class BuildingSidePanel extends JPanel {

    private final BuildingManager manager;
    private final Display display;
    private final World world;
    private final JPanel itemsPanel;
    private Runnable onClose;

    public BuildingSidePanel(BuildingManager manager, Display display, World world, Runnable onClose) {
        this.manager = manager;
        this.display = display;
        this.world = world;
        this.onClose = onClose;

        this.setLayout(new BorderLayout(8, 8));
        // Fond semi-transparent noir (plus lisible que 100% transparent)
        this.setOpaque(true);
        this.setBackground(new Color(20, 24, 30, 200));
        // garder un léger contour pour la lisibilité
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 70, 90, 200)),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));

        // Top bar: categories à gauche + close à droite
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel categories = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        categories.setOpaque(false);
        String[] cats = new String[]{"Bâtiments", "Décoration", "Nature", "Chemin"};
        for (String c : cats) {
            JButton b = new JButton(c);
            b.setFocusable(false);
            b.setBackground(new Color(60, 70, 85));
            b.setForeground(Color.WHITE);
            b.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            b.addActionListener(e -> showCategory(c));
            categories.add(b);
        }
        topBar.add(categories, BorderLayout.WEST);

        // Bouton fermer (icône) à droite
        JButton btnClose = ImageButtonFactory.createImageButton(
                "src/assets/UI/close_idle.png",
                "src/assets/UI/close_hover.png",
                "src/assets/UI/close_pressed.png"
        );
        btnClose.setPreferredSize(new Dimension(28, 28));
        btnClose.setMaximumSize(new Dimension(28, 28));
        btnClose.setToolTipText("Fermer");
        btnClose.addActionListener(e -> {
            this.setVisible(false);
            // Notify display directly to ensure the control button is restored
            try {
                display.onBuildingPanelClose();
            } catch (Exception ex) {
                // ignore if not implemented
            }
            if (onClose != null) onClose.run();
        });
        JPanel closeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closeWrap.setOpaque(false);
        // Réduire le padding droit pour revenir à l'affichage précédent
        closeWrap.setBorder(new EmptyBorder(0,0,0,12));
        closeWrap.add(btnClose);
        topBar.add(closeWrap, BorderLayout.EAST);

        this.add(topBar, BorderLayout.NORTH);

        // Zone des items (directement, sans scroll)
        itemsPanel = new JPanel();
        itemsPanel.setOpaque(false);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        this.add(itemsPanel, BorderLayout.CENTER);

        // Footer: Open Barn (optionnel)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        JButton btnBarn = new JButton("Ouvrir la Grange");
        btnBarn.setFocusable(false);
        btnBarn.addActionListener(e -> {
            // Fermer le panneau et s'assurer que le Display restaure le bouton Construire
            this.setVisible(false);
            try {
                display.onBuildingPanelClose();
            } catch (Exception ex) {
            }
            if (onClose != null) onClose.run();
            // Ouvrir le popup Barn
            PopupBarn popup = new PopupBarn(display, world);
            display.switchToPopup(popup);
        });
        footer.add(btnBarn);
        this.add(footer, BorderLayout.SOUTH);

        // Afficher la catégorie par défaut
        showCategory("Bâtiments");
    }

    // Permet d'assigner le callback onClose après création (utile pour éviter référence circulaire)
    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    // Affiche les items pour une catégorie
    private void showCategory(String category) {
        itemsPanel.removeAll();

        List<Entry> list = getEntriesFor(category);
        for (Entry e : list) {
            itemsPanel.add(createCard(e));
            itemsPanel.add(Box.createVerticalStrut(8));
        }

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    // Crée une carte uniforme: image gauche, titre + prix à droite + bouton Poser
     private JComponent createCard(Entry e) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setMaximumSize(new Dimension(360, 84));
        card.setPreferredSize(new Dimension(360, 84));
        // Fond transparent pour éviter les boîtes blanches sur les sprites
        card.setOpaque(true);
        card.setBackground(new Color(46, 52, 59, 220));
        card.setBorder(BorderFactory.createLineBorder(new Color(80, 90, 105, 140)));

         // Image
         ImageIcon rawIcon = new ImageIcon(e.iconPath);
         Image img = rawIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
         JLabel imgLabel = new JLabel(new ImageIcon(img));
        imgLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
        imgLabel.setOpaque(true);
        imgLabel.setBackground(new Color(46, 52, 59, 220));
         card.add(imgLabel, BorderLayout.WEST);

         // Info (titre + prix)
         JPanel info = new JPanel();
         info.setOpaque(false);
         info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
         JLabel title = new JLabel(e.title);
         title.setForeground(Color.WHITE);
         title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
         JLabel price = new JLabel(e.price + " coins");
         price.setForeground(new Color(180, 190, 200));
         price.setFont(price.getFont().deriveFont(Font.PLAIN, 12f));
         info.add(Box.createVerticalGlue());
         info.add(title);
         info.add(Box.createVerticalStrut(6));
         info.add(price);
         info.add(Box.createVerticalGlue());

         card.add(info, BorderLayout.CENTER);

         // Action: bouton Poser
         JButton place = new JButton("Poser");
         place.setFocusable(false);
         place.addActionListener(a -> {
             manager.startPlacement(e.creator.get());
             display.getGlobalView().requestFocusInWindow();
         });
         JPanel right = new JPanel(new BorderLayout());
         right.setOpaque(false);
         right.add(place, BorderLayout.NORTH);
         right.setBorder(new EmptyBorder(8,8,8,8));
         card.add(right, BorderLayout.EAST);

         return card;
     }

    // Retourne la liste d'entries pour une catégorie (simple et extensible)
    private List<Entry> getEntriesFor(String category) {
        List<Entry> out = new ArrayList<>();
        switch (category) {
            case "Bâtiments":
                out.add(new Entry("Mailbox1", "src/assets/Buildings/mailbox1.png", () -> new Mailbox1(), 50));
                out.add(new Entry("Well", "src/assets/Buildings/well.png", () -> new Well(), 50));
                out.add(new Entry("Barrière (Face)", "src/assets/Obstacles/fence_face.png", () -> new FenceFace(), 15));
                out.add(new Entry("Barrière (Côté)", "src/assets/Obstacles/fence_side.png", () -> new FenceSide(), 15));
                out.add(new Entry("Porte (Face)", "src/assets/Obstacles/fence_face.png", () -> new GateFace(), 15));
                out.add(new Entry("Porte (Côté)", "src/assets/Obstacles/fence_side.png", () -> new GateSide(), 15));
                break;
            case "Décoration":
                out.add(new Entry("Poto", "src/assets/Buildings/poto.png", () -> new Poto(), 30));
                out.add(new Entry("Linge", "src/assets/Buildings/linge.png", () -> new Linge(), 30));
                out.add(new Entry("Bigsign", "src/assets/Buildings/bigsign.png", () -> new Bigsign(), 20));
                out.add(new Entry("Barrel2", "src/assets/Buildings/barrel2.png", () -> new Barrel2(), 20));
                out.add(new Entry("Barrel1", "src/assets/Buildings/barrel1.png", () -> new Barrel1(), 20));
                break;
            case "Nature":
                out.add(new Entry("Tree1", "src/assets/Buildings/tree1.png", () -> new Tree1(), 10));
                out.add(new Entry("Tree2", "src/assets/Buildings/tree2.png", () -> new Tree2(), 20));
                out.add(new Entry("Rock1", "src/assets/Buildings/rock1.png", () -> new Rock1(), 20));
                break;
            case "Chemin":
                // Placeholder
                break;
        }
        return out;
    }

    // Entrée décrivant un bâtiment affichable
    private static class Entry {
        final String title;
        final String iconPath;
        final Supplier<Building> creator;
        final int price;

        Entry(String title, String iconPath, Supplier<Building> creator, int price) {
            this.title = title;
            this.iconPath = iconPath;
            this.creator = creator;
            this.price = price;
        }
    }
}

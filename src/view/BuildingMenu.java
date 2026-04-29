package src.view;

import src.control.popups.BuildingManager;
import src.model.buildings.*;

import javax.swing.*;
import java.awt.*;

public class BuildingMenu extends JPanel {

    private JButton selectedButton = null; // pour garder la selection visuelle

    // Le constructeur prend un BuildingManager pour declencher les placements et deux Runnables: onClose et onOpenBarn
    public BuildingMenu(BuildingManager manager, Runnable onClose) {
        this(manager, onClose, null);
    }

    public BuildingMenu(BuildingManager manager, Runnable onClose, Runnable onOpenBarn) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //La transparence et le style du menu
        this.setBackground(new Color(0, 0, 0, 150));

        // Bordure avec titre
        javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE), "Batiments");
        border.setTitleColor(Color.WHITE);
        this.setBorder(border);

        // Croix de fermeture
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        topPanel.setOpaque(false);
        topPanel.setMaximumSize(new Dimension(150, 40));

        // Creation du bouton de fermeture avec des images pour les differents etats
        JButton btnClose = ImageButtonFactory.createImageButton(
                "src/assets/UI/close_idle.png",   // Image normale
                "src/assets/UI/close_hover.png",  // Image au survol (plus claire)
                "src/assets/UI/close_pressed.png" // Image au clic (enfoncee)
        );
        btnClose.setPreferredSize(new Dimension(30, 30));
        btnClose.setMaximumSize(new Dimension(30, 30));

        // Action de fermeture du menu
        btnClose.addActionListener(e -> {
            this.setVisible(false);
            if (onClose != null) onClose.run();
        });

        topPanel.add(btnClose);
        this.add(topPanel);
        this.add(Box.createRigidArea(new Dimension(0, 12))); // Plus d'espace
        // ------------------------------------------

        // Taille unifiee pour la plupart des boutons
        Dimension btnSize = new Dimension(120, 60);

        JButton btnLinge = ImageButtonFactory.createImageButton(
                "src/assets/UI/btn_linge_idle.png",
                "src/assets/UI/btn_linge_hover.png",
                null // Pas d'image speciale pour le clic (optionnel)
        );
        btnLinge.setPreferredSize(btnSize);
        btnLinge.setMaximumSize(btnSize);
        btnLinge.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLinge.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnLinge.setHorizontalTextPosition(SwingConstants.CENTER);
        btnLinge.setText("Linge");
        btnLinge.setToolTipText("Linge: augmente le confort des habitants");
        btnLinge.addActionListener(e -> {
            manager.startPlacement(new Linge());
            setSelectedButton(btnLinge);
        });

        this.add(btnLinge);

        // ------------------------------------------

        JButton btnPoto = ImageButtonFactory.createImageButton(
                "src/assets/UI/btn_poto_idle.png",
                "src/assets/UI/btn_poto_hover.png",
                null // Pas d'image speciale pour le clic (optionnel)
        );
        btnPoto.setPreferredSize(btnSize);
        btnPoto.setMaximumSize(btnSize);
        btnPoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPoto.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnPoto.setHorizontalTextPosition(SwingConstants.CENTER);
        btnPoto.setText("Poto");
        btnPoto.setToolTipText("Poto: decoration");
        btnPoto.addActionListener(e -> {
            manager.startPlacement(new Poto());
            setSelectedButton(btnPoto);
        });

        this.add(btnPoto);

        // ------------------------------------------

        JButton btnMailbox1 = ImageButtonFactory.createImageButton(
                "src/assets/Buildings/mailbox1.png",
                "src/assets/Buildings/mailbox1.png",
                null // Pas d'image spéciale pour le clic (optionnel)
        );
        btnMailbox1.setPreferredSize(btnSize);
        btnMailbox1.setMaximumSize(btnSize);
        btnMailbox1.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMailbox1.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnMailbox1.setHorizontalTextPosition(SwingConstants.CENTER);
        btnMailbox1.setText("Boite aux lettres");
        btnMailbox1.setToolTipText("Boite aux lettres: Un bel ajout pour stocker le courier");
        btnMailbox1.addActionListener(e -> {
            manager.startPlacement(new Mailbox1());
            setSelectedButton(btnMailbox1);
        });

        this.add(btnMailbox1);

        // ------------------------------------------
        // Bouton Grange (ouvre la popup de la grange si un callback est fourni)
        if (onOpenBarn != null) {
            JButton btnBarn = ImageButtonFactory.createImageButton(
                    "src/assets/Buildings/poto.png", // réutilise une icone simple (a adapter)
                    "src/assets/Buildings/poto.png",
                    null
            );
            btnBarn.setPreferredSize(btnSize);
            btnBarn.setMaximumSize(btnSize);
            btnBarn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnBarn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btnBarn.setHorizontalTextPosition(SwingConstants.CENTER);
            btnBarn.setText("Grange");
            btnBarn.setToolTipText("Ouvrir la grange: gerer l'inventaire de la ferme");
            btnBarn.addActionListener(e -> {
                onOpenBarn.run();
            });
            this.add(Box.createRigidArea(new Dimension(0, 8)));
            this.add(btnBarn);
        }

    }

    // Applique une bordure colorée au bouton sélectionné et retire celle de l'ancien
    public void setSelectedButton(JButton b) {
        if (selectedButton != null) {
            selectedButton.setBorderPainted(false);
            selectedButton.setBorder(BorderFactory.createEmptyBorder());
        }
        selectedButton = b;
        if (selectedButton != null) {
            selectedButton.setBorderPainted(true);
            selectedButton.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
        }
    }

    // Enleve la sélection visuelle
    public void clearSelection() {
        if (selectedButton != null) {
            selectedButton.setBorderPainted(false);
            selectedButton.setBorder(BorderFactory.createEmptyBorder());
            selectedButton = null;
        }
    }
}
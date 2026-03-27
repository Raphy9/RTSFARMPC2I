package src.view;

import src.control.BuildingManager;
import src.model.buildings.*;

import javax.swing.*;
import java.awt.*;

public class BuildingMenu extends JPanel {

    // Le constructeur prend un BuildingManager pour déclencher les placements et un Runnable pour gérer la fermeture
    public BuildingMenu(BuildingManager manager, Runnable onClose) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //La transparence et le style du menu
        this.setBackground(new Color(0, 0, 0, 150));

        // Bordure avec titre
        javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE), "Bâtiments");
        border.setTitleColor(Color.WHITE);
        this.setBorder(border);

        // Croix de fermeture
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        topPanel.setOpaque(false);
        topPanel.setMaximumSize(new Dimension(150, 40));

        // Création du bouton de fermeture avec des images pour les différents états
        JButton btnClose = ImageButtonFactory.createImageButton(
                "src/assets/UI/close_idle.png",   // Image normale
                "src/assets/UI/close_hover.png",  // Image au survol (plus claire)
                "src/assets/UI/close_pressed.png" // Image au clic (enfoncée)
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
        this.add(Box.createRigidArea(new Dimension(0, 20))); // Plus d'espace
        // ------------------------------------------

        JButton btnLinge = ImageButtonFactory.createImageButton(
                "src/assets/UI/btn_linge_idle.png",
                "src/assets/UI/btn_linge_hover.png",
                null // Pas d'image spéciale pour le clic (optionnel)
        );
        btnLinge.setPreferredSize(new Dimension(120, 60));
        btnLinge.setMaximumSize(new Dimension(120, 60));
        btnLinge.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLinge.addActionListener(e -> manager.startPlacement(new Linge()));

        this.add(btnLinge);

        // ------------------------------------------

        JButton btnPoto = ImageButtonFactory.createImageButton(
                "src/assets/UI/btn_poto_idle.png",
                "src/assets/UI/btn_poto_hover.png",
                null // Pas d'image spéciale pour le clic (optionnel)
        );
        btnPoto.setPreferredSize(new Dimension(60, 60));
        btnPoto.setMaximumSize(new Dimension(60, 60));
        btnPoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPoto.addActionListener(e -> manager.startPlacement(new Poto()));

        this.add(btnPoto);

        // ------------------------------------------

        JButton btnMailbox1 = ImageButtonFactory.createImageButton(
                "src/assets/Buildings/mailbox1.png",
                "src/assets/Buildings/mailbox1.png",
                null // Pas d'image spéciale pour le clic (optionnel)
        );
        btnMailbox1.setPreferredSize(new Dimension(60, 60));
        btnMailbox1.setMaximumSize(new Dimension(60, 60));
        btnMailbox1.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMailbox1.addActionListener(e -> manager.startPlacement(new Mailbox1()));

        this.add(btnMailbox1);

    }
}
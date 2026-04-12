package src.view;

import src.control.popups.HarvestActionSelector;
import src.control.popups.PlantActionSelector;
import src.control.popups.PlowActionSelector;
import src.control.popups.WaterActionSelector;
import src.model.Gardener;
import src.model.World;

import javax.swing.*;
import java.awt.*;

public class ActionsPopup extends PopupPanel {

    public ActionsPopup(Display display, World world, Gardener gardener) {
        super(display, 320, 200, "Actions");

        JPanel actionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        actionsPanel.setOpaque(false);
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));

        actionsPanel.add(createStyledButton("Labourer", new PlowActionSelector(display, world, gardener)));
        actionsPanel.add(createStyledButton("Planter", new PlantActionSelector(display, world, gardener)));
        actionsPanel.add(createStyledButton("Arroser", new WaterActionSelector(display, world, gardener)));
        actionsPanel.add(createStyledButton("Recolter", new HarvestActionSelector(display, gardener, world)));

        this.add(actionsPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);

        // Palette de sélection
        Color selectLight = new Color(160, 100, 60); // Brun clair boisé
        Color selectDark = new Color(80, 40, 10);    // Brun foncé terreux

        btn.setFocusable(false);
        btn.setBackground(selectLight);
        btn.setForeground(Color.WHITE); // Texte blanc pour le contraste
        btn.setBorder(BorderFactory.createLineBorder(SDV_BORDER_DARK, 2));

        if (GameFonts.MINECRAFT_FONT != null) {
            btn.setFont(GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 12f));
        }

        btn.addActionListener(listener);

        // Gestion du survol
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(selectDark);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(selectLight);
            }
        });

        return btn;
    }
}
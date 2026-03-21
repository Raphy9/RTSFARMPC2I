package src.view;

import src.model.Camera;
import src.model.World;

import javax.swing.*;
import java.awt.*;

/**
 * Mode Selection de la vue
 * Comme la vue globale, scroling active, mais avec pour but de selectionner un type de case
 * Generalement lancee par un popup, finit des qu'une case du bon type est selectionnee
 */
public class Selection extends Global {

    // Message à afficher pour indiquer à l'utilisateur ce qu'il doit selectionner
    // par exemple "Selectionnez une case plantable"
    private String message = "Selectionnez une case";

    // UI components for the bottom message panel
    private JLabel messageLabel;
    private JPanel bottomPanel;

    public Selection(World world, Camera camera) {
        super(world, camera);

        // Use a BorderLayout so we can place a small panel at the bottom
        setLayout(new BorderLayout());

        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        // translucent background so the game view is still visible underneath
        bottomPanel.setOpaque(true);
        bottomPanel.setBackground(new Color(0, 0, 0, 160));
        bottomPanel.setPreferredSize(new Dimension(0, 80)); // height 80px

        messageLabel = new JLabel(message);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 32));

        bottomPanel.add(messageLabel);
        // add the panel to the bottom so it is painted above the game rendering
        add(bottomPanel, BorderLayout.NORTH);
    }

    public void setMessage(String message) {
        this.message = message;
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.revalidate();
            messageLabel.repaint();
        }
    }
}

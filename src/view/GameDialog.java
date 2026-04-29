package src.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Utilitaire de dialogues modaux stylises Stardew Valley.
 * Remplace JOptionPane pour rester coherent avec le reste de l'UI.
 */
public class GameDialog {

    /**
     * Affiche une boîte de confirmation "Oui / Non" bloquante.
     * @return true si l'utilisateur a clique "Oui", false sinon.
     */
    public static boolean showConfirm(Component parent, String title, String message) {
        boolean[] result = {false};
        JDialog dialog = buildDialog(parent, title);

        dialog.add(buildMessage(message), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttons.setOpaque(false);

        JButton oui = buildButton("Oui", new Color(80, 140, 70), new Color(50, 100, 45));
        oui.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        JButton non = buildButton("Non", new Color(180, 60, 50), new Color(130, 40, 30));
        non.addActionListener(e -> dialog.dispose());

        buttons.add(oui);
        buttons.add(non);
        dialog.add(buttons, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(380, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true); // bloquant jusqu'a fermeture
        return result[0];
    }

    /**
     * Affiche un message d'information stylise avec un bouton "OK".
     */
    public static void showMessage(Component parent, String title, String message) {
        JDialog dialog = buildDialog(parent, title);
        dialog.add(buildMessage(message), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        buttons.setOpaque(false);
        JButton ok = buildButton("OK", new Color(80, 120, 170), new Color(50, 80, 130));
        ok.addActionListener(e -> dialog.dispose());
        buttons.add(ok);
        dialog.add(buttons, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(360, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * Affiche une boîte de saisie stylisee et retourne le texte saisi, ou null si annule.
     */
    public static String showInput(Component parent, String title, String message, String defaultValue) {
        final String[] result = {null};
        JDialog dialog = buildDialog(parent, title);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(buildMessage(message), BorderLayout.NORTH);

        JTextField input = new JTextField(defaultValue != null ? defaultValue : "");
        input.setFont(GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(13f)
                : new Font("Arial", Font.PLAIN, 13));
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JPanel inputWrap = new JPanel(new BorderLayout());
        inputWrap.setOpaque(false);
        inputWrap.setBorder(new EmptyBorder(0, 28, 0, 28));
        inputWrap.add(input, BorderLayout.CENTER);
        center.add(inputWrap, BorderLayout.CENTER);

        dialog.add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        buttons.setOpaque(false);

        JButton ok = buildButton("Creer", new Color(80, 140, 70), new Color(50, 100, 45));
        ok.addActionListener(e -> {
            String value = input.getText();
            if (value != null) {
                value = value.trim();
            }
            if (value == null || value.isEmpty()) {
                return;
            }
            result[0] = value;
            dialog.dispose();
        });

        JButton cancel = buildButton("Annuler", new Color(180, 60, 50), new Color(130, 40, 30));
        cancel.addActionListener(e -> dialog.dispose());

        buttons.add(ok);
        buttons.add(cancel);
        dialog.add(buttons, BorderLayout.SOUTH);

        // Valider rapidement avec Entree.
        dialog.getRootPane().setDefaultButton(ok);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(430, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(parent);
        SwingUtilities.invokeLater(input::requestFocusInWindow);
        dialog.setVisible(true);
        return result[0];
    }

    // ── Helpers prives ────────────────────────────────────────────────────────

    /** Cree le JDialog modal sans decoration avec le fond style SDV. */
    private static JDialog buildDialog(Component parent, String title) {
        Window w = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(w instanceof Frame ? (Frame) w : null, true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        // Fond peint style Stardew Valley (3 couches)
        JPanel bg = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int b = 4;
                g2.setColor(PopupPanel.SDV_BORDER_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(PopupPanel.SDV_BORDER_LIGHT);
                g2.fillRect(b, b, getWidth() - b * 2, getHeight() - b * 2);
                g2.setColor(PopupPanel.SDV_BG);
                g2.fillRect(b * 2, b * 2, getWidth() - b * 4, getHeight() - b * 4);
                g2.dispose();
            }
        };
        bg.setOpaque(false);

        // Titre
        Font titleFont = GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 18f)
                : new Font("Arial", Font.BOLD, 18);
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(PopupPanel.SDV_TEXT);
        titleLabel.setBorder(new EmptyBorder(14, 20, 6, 20));
        bg.add(titleLabel, BorderLayout.NORTH);

        dialog.setContentPane(bg);
        return dialog;
    }

    /** Cree le label de message HTML centre. */
    private static JPanel buildMessage(String message) {
        Font textFont = GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(13f)
                : new Font("Arial", Font.PLAIN, 13);
        JLabel label = new JLabel(
                "<html><center>" + message.replace("\n", "<br>") + "</center></html>",
                SwingConstants.CENTER);
        label.setFont(textFont);
        label.setForeground(PopupPanel.SDV_TEXT);
        label.setBorder(new EmptyBorder(8, 30, 8, 30));

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(label, BorderLayout.CENTER);
        return p;
    }

    /** Cree un bouton stylise SDV avec changement de couleur au survol. */
    private static JButton buildButton(String text, Color normal, Color hover) {
        Font btnFont = GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 13f)
                : new Font("Arial", Font.BOLD, 13);
        JButton b = new JButton(text);
        b.setFocusable(false);
        b.setBackground(normal);
        b.setForeground(Color.WHITE);
        b.setFont(btnFont);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2),
                BorderFactory.createEmptyBorder(6, 22, 6, 22)
        ));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(normal); }
        });
        return b;
    }
}


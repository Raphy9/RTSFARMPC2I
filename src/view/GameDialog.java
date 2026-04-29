package src.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Utilitaire de dialogues modaux stylisés "Stardew Valley".
 * Cette classe fournit des méthodes statiques pour afficher des confirmations,
 * des messages et des champs de saisie en utilisant le thème graphique du jeu.
 * Elle remplace JOptionPane pour garantir la cohérence visuelle.
 */
public class GameDialog {

    /**
     * Affiche une boîte de confirmation "Oui / Non".
     * @param parent Le composant parent pour le positionnement.
     * @param title Le titre affiché en haut du dialogue.
     * @param message Le texte de la question (supporte \n pour les retours à la ligne).
     * @return true si l'utilisateur a cliqué sur "Oui", false s'il a cliqué sur "Non" ou fermé.
     */
    public static boolean showConfirm(Component parent, String title, String message) {
        // Utilisation d'un tableau pour modifier la valeur depuis une classe anonyme (ActionListeners)
        boolean[] result = {false};
        JDialog dialog = buildDialog(parent, title);

        // Ajout du texte au centre
        dialog.add(buildMessage(message), BorderLayout.CENTER);

        // Configuration du panneau de boutons (Sud)
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttons.setOpaque(false);

        // Bouton OUI (Vert)
        JButton oui = buildButton("Oui", new Color(80, 140, 70), new Color(50, 100, 45));
        oui.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        // Bouton NON (Rouge)
        JButton non = buildButton("Non", new Color(180, 60, 50), new Color(130, 40, 30));
        non.addActionListener(e -> dialog.dispose());

        buttons.add(oui);
        buttons.add(non);
        dialog.add(buttons, BorderLayout.SOUTH);

        // Finalisation de l'affichage
        dialog.pack();
        dialog.setMinimumSize(new Dimension(380, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true); // Bloque l'exécution du thread jusqu'à la fermeture du dialogue
        return result[0];
    }

    /**
     * Affiche un message d'information avec un bouton "OK".
     * @param onOk Action de type Runnable à exécuter après avoir cliqué sur OK.
     */
    public static void showMessage(Component parent, String title, String message, Runnable onOk) {
        JDialog dialog = buildDialog(parent, title);
        dialog.add(buildMessage(message), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        buttons.setOpaque(false);

        // Bouton OK (Bleu)
        JButton ok = buildButton("OK", new Color(80, 120, 170), new Color(50, 80, 130));
        ok.addActionListener(e -> {
            dialog.dispose();
            onOk.run();
        });

        buttons.add(ok);
        dialog.add(buttons, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(360, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /** Surcharge simplifiée de showMessage sans callback d'action. */
    public static void showMessage(Component parent, String title, String message) {
        showMessage(parent, title, message, () -> {});
    }

    /**
     * Affiche une boîte de saisie de texte.
     * @param defaultValue Valeur affichée par défaut dans le champ.
     * @return Le texte saisi si validé, null si annulé ou vide.
     */
    public static String showInput(Component parent, String title, String message, String defaultValue) {
        final String[] result = {null};
        JDialog dialog = buildDialog(parent, title);

        // Zone centrale contenant le message et le champ de saisie
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(buildMessage(message), BorderLayout.NORTH);

        // Configuration du champ de texte
        JTextField input = new JTextField(defaultValue != null ? defaultValue : "");
        input.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(13f) : new Font("Arial", Font.PLAIN, 13));
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        // Wrapper pour donner des marges au champ de texte
        JPanel inputWrap = new JPanel(new BorderLayout());
        inputWrap.setOpaque(false);
        inputWrap.setBorder(new EmptyBorder(0, 28, 0, 28));
        inputWrap.add(input, BorderLayout.CENTER);
        center.add(inputWrap, BorderLayout.CENTER);

        dialog.add(center, BorderLayout.CENTER);

        // Boutons Créer / Annuler
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        buttons.setOpaque(false);

        JButton ok = buildButton("Creer", new Color(80, 140, 70), new Color(50, 100, 45));
        ok.addActionListener(e -> {
            String value = input.getText();
            if (value != null) value = value.trim();
            if (value == null || value.isEmpty()) return; // Empêche la création sans nom
            result[0] = value;
            dialog.dispose();
        });

        JButton cancel = buildButton("Annuler", new Color(180, 60, 50), new Color(130, 40, 30));
        cancel.addActionListener(e -> dialog.dispose());

        buttons.add(ok);
        buttons.add(cancel);
        dialog.add(buttons, BorderLayout.SOUTH);

        // Raccourci clavier : La touche Entrée valide automatiquement le dialogue
        dialog.getRootPane().setDefaultButton(ok);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(430, dialog.getPreferredSize().height));
        dialog.setLocationRelativeTo(parent);

        // Focus automatique sur le champ texte dès l'ouverture
        SwingUtilities.invokeLater(input::requestFocusInWindow);

        dialog.setVisible(true);
        return result[0];
    }

    // ── Helpers privés ────────────────────────────────────────────────────────

    /**
     * Initialise la structure de base du JDialog.
     * Retire les bordures système (setUndecorated) pour dessiner le fond personnalisé.
     */
    private static JDialog buildDialog(Component parent, String title) {
        Window w = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(w instanceof Frame ? (Frame) w : null, true);
        dialog.setUndecorated(true); // Supprime la barre de titre Windows/Mac standard
        dialog.setLayout(new BorderLayout());

        // Fond personnalisé avec un rendu à 3 couches (Bordure sombre, Bordure claire, Fond beige)
        JPanel bg = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int b = 4; // Épaisseur de la bordure
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

        // Label du titre du dialogue
        Font titleFont = GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 18f) : new Font("Arial", Font.BOLD, 18);
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(PopupPanel.SDV_TEXT);
        titleLabel.setBorder(new EmptyBorder(14, 20, 6, 20));
        bg.add(titleLabel, BorderLayout.NORTH);

        dialog.setContentPane(bg);
        return dialog;
    }

    /**
     * Construit le label contenant le message.
     * Utilise le HTML pour permettre l'alignement centré et le retour à la ligne automatique.
     */
    private static JPanel buildMessage(String message) {
        Font textFont = GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(16f) : new Font("Arial", Font.PLAIN, 16);
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

    /**
     * Crée un bouton stylisé avec une double bordure et un effet de changement de couleur au survol.
     */
    private static JButton buildButton(String text, Color normal, Color hover) {
        Font btnFont = GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 13f) : new Font("Arial", Font.BOLD, 13);
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
        // Effet Hover (Survol)
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(normal); }
        });
        return b;
    }
}
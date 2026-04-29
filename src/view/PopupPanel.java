package src.view;

import src.control.popups.CloseController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Classe de base pour tous les panneaux surgissants (popups).
 * Gère le rendu visuel style "Stardew Valley" et permet de déplacer la fenêtre à la souris.
 */
public class PopupPanel extends JPanel {

    // --- Palette de couleurs inspirée de Stardew Valley ---
    public static final Color SDV_BG = new Color(255, 218, 153);        // Beige (fond bois clair)
    public static final Color SDV_BORDER_LIGHT = new Color(245, 190, 105); // Marron clair (relief interne)
    public static final Color SDV_BORDER_DARK = new Color(110, 45, 15);    // Marron foncé (contour pixel)
    public static final Color SDV_TEXT = new Color(75, 35, 10);            // Texte marron très sombre

    protected final int width, height;
    private Point initialClick; // Stocke la position du clic pour le calcul du drag-and-drop

    /**
     * Constructeur du panneau popup.
     * @param display Référence à la vue principale.
     * @param width Largeur souhaitée.
     * @param height Hauteur souhaitée.
     * @param title Texte affiché dans la barre de titre.
     */
    public PopupPanel(Display display, int width, int height, String title) {
        super();
        this.width = width;
        this.height = height;

        this.setLayout(new BorderLayout());
        setPreferredSize(new Dimension(width, height));
        setFocusable(true);
        // Désactive l'opacité Swing standard pour laisser notre paintComponent dessiner les bordures
        setOpaque(false);

        // --- EN-TÊTE : Barre de titre déplaçable (draggable) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); // Curseur "main" pour indiquer le déplacement
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        // Enregistre le point de départ du clic
        topPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { initialClick = e.getPoint(); }
        });

        // Calcule et applique le déplacement de la fenêtre sur le LayeredPane
        topPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick != null) {
                    int newX = Math.max(0, getX() + e.getX() - initialClick.x);
                    int newY = Math.max(0, getY() + e.getY() - initialClick.y);

                    // Empêche la fenêtre de sortir totalement de l'écran de jeu
                    Container parent = getParent();
                    if (parent != null) {
                        newX = Math.min(newX, parent.getWidth() - getWidth() / 2);
                        newY = Math.min(newY, parent.getHeight() - 30);
                    }
                    setLocation(newX, newY);
                }
            }
        });
        this.add(topPanel, BorderLayout.NORTH);

        // --- BOUTON FERMER (X) ---
        JButton exit = new JButton("X");
        exit.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 16f) : new Font("Arial", Font.BOLD, 16));
        exit.setBackground(new Color(210, 60, 50)); // Rouge brique
        exit.setForeground(Color.WHITE);
        exit.setFocusPainted(false);
        // Bordure pixelisée épaisse
        exit.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SDV_BORDER_DARK, 2),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));

        // Contrôleur de fermeture partagé
        CloseController closeController = new CloseController(display);
        exit.addActionListener(closeController);
        this.addKeyListener(closeController); // Permet de fermer aussi via une touche clavier
        topPanel.add(exit, BorderLayout.EAST);

        // --- TITRE DU POPUP ---
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 24f) : new Font("Arial", Font.BOLD, 24));
        t.setForeground(SDV_TEXT);
        topPanel.add(t, BorderLayout.CENTER);
    }

    /**
     * Rendu graphique personnalisé.
     * Dessine trois couches de rectangles superposés pour créer un effet de bordure pixelisée.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        int borderSize = 4; // Épaisseur simulant un gros pixel

        // 1. Couche externe : Bordure sombre (contour)
        g2.setColor(SDV_BORDER_DARK);
        g2.fillRect(0, 0, w, h);

        // 2. Couche intermédiaire : Bordure claire (effet de biseau)
        g2.setColor(SDV_BORDER_LIGHT);
        g2.fillRect(borderSize, borderSize, w - borderSize * 2, h - borderSize * 2);

        // 3. Couche centrale : Fond beige principal
        g2.setColor(SDV_BG);
        g2.fillRect(borderSize * 2, borderSize * 2, w - borderSize * 4, h - borderSize * 4);

        g2.dispose();
    }
}
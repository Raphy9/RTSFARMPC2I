package src.view;

import src.control.popups.CloseController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PopupPanel extends JPanel {

    // Couleurs palettes Stardew Valley
    public static final Color SDV_BG = new Color(255, 218, 153);        // Beige bois clair
    public static final Color SDV_BORDER_LIGHT = new Color(245, 190, 105); // Marron clair
    public static final Color SDV_BORDER_DARK = new Color(110, 45, 15);    // Marron foncé / Contour
    public static final Color SDV_TEXT = new Color(75, 35, 10);            // Texte marron très sombre

    protected final int width, height;
    private Point initialClick;

    public PopupPanel(Display display, int width, int height, String title) {
        super();
        this.width = width;
        this.height = height;

        this.setLayout(new BorderLayout());
        setPreferredSize(new Dimension(width, height));
        setFocusable(true);
        setOpaque(false); // TRÈS IMPORTANT : permet au paintComponent personnalisé d'apparaître

        // --- Panel du haut (Barre de titre draggable) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false); // Transparent pour voir le fond Stardew
        topPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15)); // Marges

        topPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { initialClick = e.getPoint(); }
        });

        topPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick != null) {
                    int newX = Math.max(0, getX() + e.getX() - initialClick.x);
                    int newY = Math.max(0, getY() + e.getY() - initialClick.y);
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

        // Bouton annuler style Stardew (Rouge brique)
        JButton exit = new JButton("X");
        exit.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 16f) : new Font("Arial", Font.BOLD, 16));
        exit.setBackground(new Color(210, 60, 50));
        exit.setForeground(Color.WHITE);
        exit.setFocusPainted(false);
        exit.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SDV_BORDER_DARK, 2),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        CloseController closeController = new CloseController(display);
        exit.addActionListener(closeController);
        this.addKeyListener(closeController);
        topPanel.add(exit, BorderLayout.EAST);

        // Titre
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 24f) : new Font("Arial", Font.BOLD, 24));
        t.setForeground(SDV_TEXT);
        topPanel.add(t, BorderLayout.CENTER);
    }

    /** Dessin de la texture style Stardew Valley (3 épaisseurs) */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        int borderSize = 4; // Épaisseur des pixels

        // 1. Bordure externe sombre
        g2.setColor(SDV_BORDER_DARK);
        g2.fillRect(0, 0, w, h);

        // 2. Bordure interne claire
        g2.setColor(SDV_BORDER_LIGHT);
        g2.fillRect(borderSize, borderSize, w - borderSize * 2, h - borderSize * 2);

        // 3. Fond principal beige
        g2.setColor(SDV_BG);
        g2.fillRect(borderSize * 2, borderSize * 2, w - borderSize * 4, h - borderSize * 4);

        g2.dispose();
    }
}
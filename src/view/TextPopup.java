package src.view;

import javax.swing.*;
import java.awt.*;

/** Classe des popups informatifs avec titre, texte et bouton annuler */
public class TextPopup extends PopupPanel {

    public static final int TEXT_PADDING = 12;
    public static final Font TEXT_FONT = new Font("Arial", Font.BOLD, 14);
    private String text;

    public TextPopup(Display display, int width, int height, String title, String text) {
        super(display, width, height, title);
        this.text = text;
        JLabel t  = new JLabel("<html><body style='width:100%; padding:0px;'>" + text + "</body></html>", SwingConstants.CENTER);
        t.setFont(TEXT_FONT);
        // ajouter du padding autour du texte
        t.setBorder(BorderFactory.createEmptyBorder(TEXT_PADDING, TEXT_PADDING, TEXT_PADDING, TEXT_PADDING));
        this.add(t, BorderLayout.CENTER);
    }
}

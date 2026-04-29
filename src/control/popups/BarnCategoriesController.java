package src.control.popups;

import src.view.PopupBarn;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class BarnCategoriesController implements ActionListener {
    JButton button;
    PopupBarn popupBarn;
    String category;

        public BarnCategoriesController(JButton button, PopupBarn popupBarn, String category) {
            this.popupBarn = popupBarn;
            this.button = button;
            this.category = category;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Transmettre la categorie au popup et rafraîchir la grille
            popupBarn.setCategory(category);
            popupBarn.refresh();
        }
}

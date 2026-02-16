package src;

import src.model.World;
import src.view.Display;

import javax.swing.*;

public class Main {

    public static void main(String [] args) {

        JFrame f = new JFrame("Saclay Valley");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);

        // Initialisation de la vue
        Display display = new Display(f);

        // show frame
        f.pack();
        f.setVisible(true);

    }
}

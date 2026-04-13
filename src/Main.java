package src;
import src.model.World;
import src.view.Display;
import src.view.Rendering;

import javax.swing.*;
import java.util.Timer;

public class Main {

    public static void main(String [] args) {

        JFrame f = new JFrame("Saclay Valley");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);

        Timer timer = new Timer();

        // Initialisation de la vue
        Display display = new Display(f);
        Rendering renderer = new Rendering(display);
        timer.schedule(renderer, 0, 1000 / Rendering.FPS);
        // show frame
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);

    }
}




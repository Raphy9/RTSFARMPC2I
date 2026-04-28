package src.model.buildings;

import src.model.Crow;
import src.model.World;
import javax.swing.ImageIcon;

public class Scarecrow extends Building {

    // Un rayon de 3 cases autour de l'épouvantail (crée une zone de protection de 7x7)
    public static final int RADIUS = 3;

    public Scarecrow() {
        // PlacementRule.ANYWHERE permet de le poser sur l'herbe OU sur la terre labourée !
        super(1, 1, false, PlacementRule.ANYWHERE, new ImageIcon("src/assets/Buildings/scarecrow.png"));
        this.buyPrice = 100;
        this.levelRequirement = 2; // Débloqué au niveau 2
    }

    @Override
    public void applyEffect(World world) {
        // À chaque "tick" de la journée, ou en temps réel, l'épouvantail effraie les corbeaux
        if (world.getCrows() != null) {
            for (Crow crow : world.getCrows()) {
                // Si le corbeau entre dans le rayon de l'épouvantail
                if (Math.abs(crow.getX() - this.getX()) <= RADIUS &&
                        Math.abs(crow.getY() - this.getY()) <= RADIUS) {

                    // On le force à s'enfuir immédiatement !
                    crow.flee();
                }
            }
        }
    }
}
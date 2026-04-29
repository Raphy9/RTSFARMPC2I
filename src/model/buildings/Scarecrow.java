package src.model.buildings;

import src.model.Crow;
import src.model.World;
import javax.swing.ImageIcon;

/**
 * Classe représentant l'épouvantail (Scarecrow).
 * Ce bâtiment possède une utilité défensive : il protège les cultures
 * en faisant fuir les corbeaux qui s'approchent trop près.
 */
public class Scarecrow extends Building {

    /**
     * Rayon d'action de l'épouvantail.
     * Un rayon de 3 définit une zone de protection de 7x7 tuiles (le centre + 3 cases dans chaque direction).
     */
    public static final int RADIUS = 3;

    /**
     * Constructeur de l'épouvantail.
     */
    public Scarecrow() {
        // - 1, 1 : Occupe une seule case.
        // - false : Obstacle physique (non-passable).
        // - PlacementRule.ANYWHERE : Très important ! Permet au joueur de le placer
        //   soit au bord des champs (herbe), soit au milieu des cultures (PlantTile).
        super(1, 1, false, PlacementRule.ANYWHERE, new ImageIcon("src/assets/Buildings/scarecrow.png"));

        // Investissement important (100 PO) justifié par son utilité pratique.
        this.buyPrice = 100;

        // Progression : disponible uniquement à partir du niveau 5.
        this.levelRequirement = 5;
    }

    /**
     * Logique active exécutée à chaque cycle de mise à jour du monde.
     * Scanne la présence de corbeaux dans sa zone d'influence et les repousse.
     *
     * @param world Référence au monde pour accéder à la liste des corbeaux.
     */
    @Override
    public void applyEffect(World world) {
        // Vérification de sécurité : s'assurer qu'il existe des corbeaux dans le monde
        if (world.getCrows() != null) {
            for (Crow crow : world.getCrows()) {

                // Calcul de la distance de Manhattan entre l'épouvantail et le corbeau.
                // On utilise Math.abs pour obtenir une distance positive en X et en Y.
                if (Math.abs(crow.getX() - this.getX()) <= RADIUS &&
                        Math.abs(crow.getY() - this.getY()) <= RADIUS) {

                    /*
                     * Le corbeau est entré dans la zone protégée !
                     * crow.flee(true) : Déclenche l'animation de fuite du corbeau.
                     * Le paramètre 'true' permet probablement de valider un objectif de quête
                     * ou de comptabiliser un "corbeau effrayé" dans les stats.
                     */
                    crow.flee(true);
                }
            }
        }
    }
}
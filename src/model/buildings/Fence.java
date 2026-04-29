package src.model.buildings;

import src.model.World;
import src.view.SpriteUtils;
import javax.swing.ImageIcon;

/**
 * Classe représentant une barrière (Fence).
 * Cette classe est particulière car elle possède une logique d'analyse de voisinage
 * permettant de savoir si elle doit se connecter visuellement à d'autres barrières adjacentes.
 */
public class Fence extends Building {

    // Utilisation de variables 'static' pour ne charger les images en mémoire qu'une seule fois
    // peu importe le nombre de barrières posées (Optimisation mémoire).
    private static ImageIcon baseSprite, face, side;
    private static boolean loaded = false;

    /**
     * Constructeur de la barrière.
     */
    public Fence() {
        // 1, 1 : Occupe une case.
        // false : Bloque le passage (non passable).
        // PlacementRule.NORMAL_ONLY : Se pose uniquement sur l'herbe.
        super(1, 1, false, PlacementRule.NORMAL_ONLY, null);

        this.buyPrice = 10;
        this.levelRequirement = 1; // Débloqué dès le niveau 1

        // Chargement unique des ressources graphiques
        if (!loaded) loadSprites();
        this.sprite = baseSprite;
    }

    /**
     * Charge les différentes variantes de sprites pour la barrière.
     * Utilise SpriteUtils pour traiter les images (suppression du fond, redimensionnement, etc.).
     */
    private static void loadSprites() {
        if (loaded) return;
        baseSprite = SpriteUtils.processFenceImage("src/assets/Obstacles/fence.png");
        face = SpriteUtils.processFenceImage("src/assets/Obstacles/fence_face.png");
        side = SpriteUtils.processFenceImage("src/assets/Obstacles/fence_side.png");
        loaded = true;
    }

    /**
     * Méthode utilitaire interne pour vérifier si un bâtiment voisin est aussi une barrière.
     * On exclut les Portails (Gate) pour éviter des problèmes de connexion visuelle hybride.
     */
    private boolean isFence(Building b) {
        return b instanceof Fence && !(b instanceof Gate);
    }

    // --- Méthodes d'analyse de voisinage (Adjacence) ---
    // Ces méthodes permettent au moteur de rendu de savoir s'il faut dessiner
    // un poteau de jonction ou une barre horizontale/verticale.

    /** Vérifie la présence d'une barrière sur la case à droite. */
    public boolean hasFenceRight(World world, int x, int y) {
        if (x < 0 || x >= World.WIDTH || y < 0 || y >= World.HEIGHT) return false;
        return isFence(world.getBuildingAt(x + 1, y));
    }

    /** Vérifie la présence d'une barrière sur la case à gauche. */
    public boolean hasFenceLeft(World world, int x, int y) {
        if (x < 0 || x >= World.WIDTH || y < 0 || y >= World.HEIGHT) return false;
        return isFence(world.getBuildingAt(x - 1, y));
    }

    /** Vérifie la présence d'une barrière sur la case en dessous. */
    public boolean hasFenceBelow(World world, int x, int y) {
        if (x < 0 || x >= World.WIDTH || y < 0 || y >= World.HEIGHT) return false;
        return isFence(world.getBuildingAt(x, y + 1));
    }

    /** Vérifie la présence d'une barrière sur la case au-dessus. */
    public boolean hasFenceAbove(World world, int x, int y) {
        if (x < 0 || x >= World.WIDTH || y < 0 || y >= World.HEIGHT) return false;
        return isFence(world.getBuildingAt(x, y - 1));
    }

    // --- Accesseurs pour les variantes de sprites ---

    public ImageIcon getFaceSprite() {
        return face;
    }

    public ImageIcon getSideSprite() {
        return side;
    }

    @Override
    public ImageIcon getSprite() {
        return baseSprite;
    }

    /**
     * Pas d'effet logique particulier sur le monde pour une barrière physique.
     */
    @Override
    public void applyEffect(World world) {}

}
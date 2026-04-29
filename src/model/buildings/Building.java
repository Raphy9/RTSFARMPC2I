package src.model.buildings;

import src.model.World;
import javax.swing.ImageIcon;

/**
 * Classe de base abstraite définissant ce qu'est un bâtiment dans le jeu.
 * Elle regroupe les propriétés physiques (taille, collision), économiques (prix)
 * et graphiques (sprite) communes à tous les objets posables sur la carte.
 */
public abstract class Building {

    /**
     * Définit les contraintes de pose sur le terrain.
     */
    public enum PlacementRule {
        NORMAL_ONLY,     // Uniquement sur l'herbe (pas sur les parcelles labourées)
        PLANTABLE_ONLY,  // Uniquement sur les parcelles labourées (ex: épouvantail)
        ANYWHERE         // Pas de contrainte particulière
    }

    // Coordonnées de l'ancrage (le coin supérieur gauche du bâtiment sur la grille)
    protected int anchorX;
    protected int anchorY;

    // Dimensions en nombre de tuiles (ex: 1x1, 2x2, etc.)
    protected int width;
    protected int height;

    // Si vrai, les entités peuvent marcher à travers (ex: fleurs).
    // Si faux, le bâtiment agit comme un mur (ex: tonneau).
    protected boolean isPassable;

    // La règle de placement associée à ce type de bâtiment
    protected PlacementRule placementRule;

    // L'image utilisée pour représenter le bâtiment dans la vue
    protected ImageIcon sprite;

    // Prix d'achat par défaut (0 = gratuit)
    protected int buyPrice = 0;

    /** Niveau requis pour débloquer ce batiment dans le shop (0 = disponible des le départ) */
    protected int levelRequirement = 0;

    // État interne : permet de savoir si l'objet est dans la main du joueur ou déjà posé
    protected boolean placed = false;

    /**
     * Constructeur parent. Utilisé par les classes filles via super().
     */
    public Building(int width, int height, boolean isPassable, PlacementRule rule, ImageIcon sprite) {
        this.width = width;
        this.height = height;
        this.isPassable = isPassable;
        this.placementRule = rule;
        this.sprite = sprite;
    }

    /** Définit l'emplacement du bâtiment sur la grille du monde. */
    public void setPosition(int x, int y) { this.anchorX = x; this.anchorY = y; }

    /** Marque le bâtiment comme étant officiellement ancré dans le monde. */
    public void placed() { this.placed = true; }

    /**
     * Méthode abstraite : chaque bâtiment doit définir son propre comportement cyclique
     * (ex: une forge produit du fer, une grange gère le stockage, un puits remplit l'eau).
     */
    public abstract void applyEffect(World world);

    // --- Getters standards ---
    public int getX()                       { return anchorX; }
    public int getY()                       { return anchorY; }
    public int getWidth()                   { return width; }
    public int getHeight()                  { return height; }
    public boolean wasIsPlaced()            { return placed; }
    public boolean isPassable()             { return isPassable; }
    public PlacementRule getPlacementRule() { return placementRule; }
    public ImageIcon getSprite()            { return sprite; }

    /**
     * Variante contextuelle : permet à un bâtiment de changer d'apparence selon son contexte.
     * Très utile pour les barrières qui doivent se connecter visuellement à leurs voisines.
     */
    public ImageIcon getSprite(World world, int x, int y) {
        return getSprite();
    }

    public int getBuyPrice() { return buyPrice; }
    public int getLevelRequirement() { return levelRequirement; }

    /**
     * Calcul dynamique du prix de revente.
     * Ici, le joueur récupère 40% de son investissement initial, arrondi à l'entier supérieur.
     */
    public int getSellPrice() { return (int) Math.ceil(buyPrice * 0.4); }

    /**
     * Permet d'identifier des comportements spécifiques sans faire de 'instanceof' coûteux.
     * Par défaut à false, à surcharger pour les objets de type barrière/portillon.
     */
    public boolean isGate() { return false; }
}
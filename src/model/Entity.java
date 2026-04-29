package src.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe de base pour les entités du jeu (jardinier, ennemis, etc.).
 * Elle définit la position, l'orientation et surtout la logique de déplacement
 * intelligente via l'algorithme de pathfinding A*.
 */
public class Entity {

    // --- Constantes de direction pour la gestion des sprites ---
    public static final int DOWN = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 3;

    protected World world;
    protected int x, y;
    protected int facingDirection = DOWN; // Direction vers laquelle l'entité regarde

    // --- Getters et Setters ---
    public int getX() { return x; }
    public int getY() { return y; }
    public int getFacingDirection() { return facingDirection; }
    public void setFacingDirection(int direction) { this.facingDirection = direction; }

    /**
     * Constructeur : Initialise la position et enregistre l'entité dans le monde.
     */
    public Entity(World world, int x, int y) {
        this.x = x;
        this.y = y;
        this.world = world;
        // Inscription de l'entité sur la tuile spécifique pour la gestion des collisions/rendu
        world.getTiles()[y][x].addEntity(this);
    }

    /**
     * Implémentation de l'algorithme A* pour trouver le chemin le plus court.
     * @return Une liste de tuiles à parcourir, ou null si la cible est inaccessible.
     */
    public List<Tile> pathFinding(World world, int targetX, int targetY) {

        // 1. VÉRIFICATIONS PRÉLIMINAIRES
        if (targetX < 0 || targetX >= World.WIDTH || targetY < 0 || targetY >= World.HEIGHT) {
            return null;
        }

        boolean targetWalkable = world.getTile(targetX, targetY).isWalkable();
        src.model.buildings.Building targetB = world.getBuildingAt(targetX, targetY);

        // RÈGLE SPÉCIALE : Le jardinier peut traverser les barrières avec portillon (Gate),
        // contrairement aux ennemis (poules, etc.)
        if (targetB != null && targetB.isGate() && this instanceof src.model.Gardener) {
            targetWalkable = true;
        }

        if (!targetWalkable) return null;

        // 2. INITIALISATION
        // Liste ouverte : cases à explorer.
        // Liste fermée (closedList) : cases déjà traitées pour éviter les boucles infinies.
        List<Node> openList = new ArrayList<>();
        boolean[][] closedList = new boolean[World.WIDTH][World.HEIGHT];

        // Ajout du point de départ (coût G=0, coût H calculé vers la cible)
        Node startNode = new Node(this.x, this.y, null, 0, getManhattanDistance(this.x, this.y, targetX, targetY));
        openList.add(startNode);

        // 3. BOUCLE DE RECHERCHE
        while (!openList.isEmpty()) {

            // Sélection du "meilleur" nœud (celui avec le plus petit fCost)
            Node current = openList.get(0);
            for (int i = 1; i < openList.size(); i++) {
                if (openList.get(i).fCost < current.fCost) {
                    current = openList.get(i);
                }
                // Si égalité de coût total, on privilégie le plus proche de l'arrivée (hCost)
                else if (openList.get(i).fCost == current.fCost && openList.get(i).hCost < current.hCost) {
                    current = openList.get(i);
                }
            }

            openList.remove(current);
            closedList[current.x][current.y] = true;

            // VICTOIRE : On a atteint la cible
            if (current.x == targetX && current.y == targetY) {
                return reconstructPath(world, current);
            }

            // 4. EXPLORATION DES VOISINS (4 directions)
            int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] dir : directions) {
                int neighborX = current.x + dir[0];
                int neighborY = current.y + dir[1];

                // Vérification des limites et de la liste fermée
                if (neighborX < 0 || neighborX >= World.WIDTH || neighborY < 0 || neighborY >= World.HEIGHT) continue;
                if (closedList[neighborX][neighborY]) continue;

                // Logique de collision (Gate/Jardinier) identique à la vérification de base
                boolean isWalkable = world.getTile(neighborX, neighborY).isWalkable();
                src.model.buildings.Building b = world.getBuildingAt(neighborX, neighborY);
                if (b != null && b.isGate() && this instanceof src.model.Gardener) {
                    isWalkable = true;
                }

                if (!isWalkable) continue;

                // Calcul des coûts G (distance parcourue) et H (estimation restante)
                int newG = current.gCost + 1;
                int newH = getManhattanDistance(neighborX, neighborY, targetX, targetY);

                // Gestion de la liste ouverte
                Node neighborNode = null;
                for (Node n : openList) {
                    if (n.x == neighborX && n.y == neighborY) {
                        neighborNode = n;
                        break;
                    }
                }

                if (neighborNode == null) {
                    // Nouveau chemin découvert
                    openList.add(new Node(neighborX, neighborY, current, newG, newH));
                } else if (newG < neighborNode.gCost) {
                    // Chemin existant amélioré (on met à jour le parent et le coût)
                    neighborNode.gCost = newG;
                    neighborNode.fCost = newG + neighborNode.hCost;
                    neighborNode.parent = current;
                }
            }
        }

        return null; // Aucun chemin trouvé après exploration
    }

    /**
     * Remonte la chaîne des parents depuis le nœud final pour créer la liste de Tiles.
     */
    private List<Tile> reconstructPath(World world, Node endNode) {
        List<Tile> path = new ArrayList<>();
        Node current = endNode;
        while (current.parent != null) {
            path.add(world.getTile(current.x, current.y));
            current = current.parent;
        }
        Collections.reverse(path); // On inverse pour partir du début vers la fin
        return path;
    }

    /**
     * Heuristique de Manhattan : calcul simple et rapide pour une grille carrée.
     * Somme des valeurs absolues des différences de coordonnées.
     */
    private int getManhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    /**
     * Classe interne Node : Représente une "étape" dans la recherche A*.
     */
    private class Node {
        int x, y;
        Node parent; // Nœud précédent pour reconstruire le chemin
        int gCost;   // Distance réelle depuis le point de départ
        int hCost;   // Distance estimée (heuristique) jusqu'à l'arrivée
        int fCost;   // Coût total (G + H) utilisé pour prioriser l'exploration

        public Node(int x, int y, Node parent, int gCost, int hCost) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }
    }
}
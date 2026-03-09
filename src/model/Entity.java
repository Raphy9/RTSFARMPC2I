package src.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe de base pour les entités du jeu (jardinier, ennemis, etc.)
 * Contient la logique de pathfinding A* pour se déplacer vers une cible
 */
public class Entity {

        // Constantes de direction
        public static final int DOWN = 0;
        public static final int LEFT = 1;
        public static final int RIGHT = 2;
        public static final int UP = 3;

        protected int x, y;
        protected int facingDirection = DOWN; // Par défaut, regarde en bas


        public int getX() { return x; }
        public int getY() { return y; }

        // Getter de direction
        public int getFacingDirection() { return facingDirection; }


    // Constructeur pour initialiser la position de l'entité
    public Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }

/**     * Implémentation de l'algorithme A* pour trouver un chemin vers une cible
     * @param world Le monde dans lequel se trouve l'entité
     * @param targetX La coordonnée X de la cible
     * @param targetY La coordonnée Y de la cible
     * @return Une liste de Tiles représentant le chemin à suivre, ou null si aucun chemin n'est trouvé
     */
    public List<Tile> pathFinding(World world, int targetX, int targetY) {
        // Vérifications de base
        if (targetX < 0 || targetX >= World.WIDTH || targetY < 0 || targetY >= World.HEIGHT) {
            return null;
        }
        if (!world.getTile(targetX, targetY).isWalkable()) {
            return null;
        }

        // Initialisation des Listes
        // liste ouverte, Les cases qu'on a repérées mais pas encore traitées
        List<Node> openList = new ArrayList<>();

        // liste fermée, Les cases déjà traitées (on utilise un tableau booléen pour faire simple)
        boolean[][] closedList = new boolean[World.WIDTH][World.HEIGHT];

        // On ajoute le point de départ
        Node startNode = new Node(this.x, this.y, null, 0, getManhattanDistance(this.x, this.y, targetX, targetY));
        openList.add(startNode);

        // Boucle principale de l'algorithme
        while (!openList.isEmpty()) {
            // On cherche le noeud avec le plus petit coût F dans la liste ouverte
            Node current = openList.get(0);
            for (int i = 1; i < openList.size(); i++) {
                // Si on trouve un noeud avec un coût F plus petit, il devient le courant
                if (openList.get(i).fCost < current.fCost) {
                    current = openList.get(i);
                }
                // Si les coûts sont égaux, on peut prendre celui le plus proche de l'arrivée (H) pour optimiser un peu
                else if (openList.get(i).fCost == current.fCost && openList.get(i).hCost < current.hCost) {
                    current = openList.get(i);
                }
            }

            // On retire le noeud choisi de la liste ouverte et on le met dans la fermée
            openList.remove(current);
            closedList[current.x][current.y] = true;

            // Si on est arrivé
            if (current.x == targetX && current.y == targetY) {
                return reconstructPath(world, current);
            }

            // Explorer les voisins (Haut, Bas, Gauche, Droite)
            int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

            for (int[] dir : directions) {
                int neighborX = current.x + dir[0];
                int neighborY = current.y + dir[1];

                // Vérifier limites du monde
                if (neighborX < 0 || neighborX >= World.WIDTH || neighborY < 0 || neighborY >= World.HEIGHT) {
                    continue;
                }

                // Si c'est un mur ou si c'est déjà dans la liste fermée, on ignore
                if (!world.getTile(neighborX, neighborY).isWalkable() || closedList[neighborX][neighborY]) {
                    continue;
                }

                // Calculs des coûts
                int newG = current.gCost + 1;
                int newH = getManhattanDistance(neighborX, neighborY, targetX, targetY);

                // Vérifier si ce voisin est déjà dans la liste ouverte
                Node neighborNode = null;
                for (Node n : openList) {
                    if (n.x == neighborX && n.y == neighborY) {
                        neighborNode = n;
                        break;
                    }
                }

                if (neighborNode == null) {
                    // Si pas dans la liste, on l'ajoute
                    Node newNode = new Node(neighborX, neighborY, current, newG, newH);
                    openList.add(newNode);
                } else {
                    // Si déjà dans la liste, on regarde si le nouveau chemin est meilleur
                    if (newG < neighborNode.gCost) {
                        neighborNode.gCost = newG;
                        neighborNode.fCost = newG + neighborNode.hCost;
                        neighborNode.parent = current;
                    }
                }
            }
        }

        return null; // Pas de chemin trouvé
    }

    private List<Tile> reconstructPath(World world, Node endNode) {
        List<Tile> path = new ArrayList<>();
        Node current = endNode;
        while (current.parent != null) {
            path.add(world.getTile(current.x, current.y));
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private int getManhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    /** Classe interne simple pour stocker les infos d'une case */
    private class Node {
        int x, y;
        Node parent;
        int gCost; // Coût depuis le départ
        int hCost; // Distance estimée vers la fin
        int fCost; // Total

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
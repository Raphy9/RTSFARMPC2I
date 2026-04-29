package src.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe de base pour les entites du jeu (jardinier, ennemis, etc.)
 * Contient la logique de pathfinding A* pour se deplacer vers une cible
 */
public class Entity {

        // Constantes de direction
        public static final int DOWN = 0;
        public static final int LEFT = 1;
        public static final int RIGHT = 2;
        public static final int UP = 3;

        protected World world;
        protected int x, y;
        protected int facingDirection = DOWN; // Par defaut, regarde en bas

        public int getX() { return x; }
        public int getY() { return y; }

        // Getter de direction
        public int getFacingDirection() { return facingDirection; }

        //Setter de direction
        public void setFacingDirection(int direction) {
            this.facingDirection = direction;
        }

    // Constructeur pour initialiser la position de l'entite
    public Entity(World world, int x, int y) {
        this.x = x;
        this.y = y;
        this.world = world;
        System.out.println("Creation de l'entite a la position (" + x + ", " + y + ")");
        world.getTiles()[y][x].addEntity(this); // Ajoute l'entite a la tuile correspondante
    }

/**     * Implementation de l'algorithme A* pour trouver un chemin vers une cible
     * @param world Le monde dans lequel se trouve l'entite
     * @param targetX La coordonnee X de la cible
     * @param targetY La coordonnee Y de la cible
     * @return Une liste de Tiles representant le chemin a suivre, ou null si aucun chemin n'est trouvé
     */
    public List<Tile> pathFinding(World world, int targetX, int targetY) {
        // Vérifications de base
        if (targetX < 0 || targetX >= World.WIDTH || targetY < 0 || targetY >= World.HEIGHT) {
            return null;
        }
        boolean targetWalkable = world.getTile(targetX, targetY).isWalkable();
        src.model.buildings.Building targetB = world.getBuildingAt(targetX, targetY);
        // Si c'est une porte et que l'entité est un jardinier, on autorise le passage !
        if (targetB != null && targetB.isGate() && this instanceof src.model.Gardener) {
            targetWalkable = true;
        }
        if (!targetWalkable) {
            return null;
        }
        // Initialisation des Listes
        // liste ouverte, Les cases qu'on a repérées mais pas encore traitées
        List<Node> openList = new ArrayList<>();

        // liste fermée, Les cases déja traitées (on utilise un tableau booléen pour faire simple)
        boolean[][] closedList = new boolean[World.WIDTH][World.HEIGHT];

        // On ajoute le point de départ
        Node startNode = new Node(this.x, this.y, null, 0, getManhattanDistance(this.x, this.y, targetX, targetY));
        openList.add(startNode);

        // Boucle principale de l'algorithme
        while (!openList.isEmpty()) {
            // On cherche le noeud avec le plus petit cout F dans la liste ouverte
            Node current = openList.get(0);
            for (int i = 1; i < openList.size(); i++) {
                // Si on trouve un noeud avec un cout F plus petit, il devient le courant
                if (openList.get(i).fCost < current.fCost) {
                    current = openList.get(i);
                }
                // Si les couts sont égaux, on peut prendre celui le plus proche de l'arrivée (H) pour optimiser un peu
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

                boolean isWalkable = world.getTile(neighborX, neighborY).isWalkable();
                src.model.buildings.Building b = world.getBuildingAt(neighborX, neighborY);
                // Si c'est une porte et que l'entité est un jardinier, on autorise le passage !
                if (b != null && b.isGate() && this instanceof src.model.Gardener) {
                    isWalkable = true;
                }

                if (!isWalkable || closedList[neighborX][neighborY]) {
                    continue;
                }

                // Calculs des couts
                int newG = current.gCost + 1;
                int newH = getManhattanDistance(neighborX, neighborY, targetX, targetY);

                // Vérifier si ce voisin est déja dans la liste ouverte
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
                    // Si déja dans la liste, on regarde si le nouveau chemin est meilleur
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
        int gCost; // Cout depuis le départ
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
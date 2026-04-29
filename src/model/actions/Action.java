package src.model.actions;

import src.model.Gardener;
import src.model.World;

/**
 * Classe mère de l'architecture des tâches (fortement inspirée du patron de conception Command).
 * Elle représente une action unitaire, autonome et différée dans le temps, assignée à un agent (Jardinier).
 * Ce socle abstrait permet de découpler l'intention du joueur (le clic sur l'interface)
 * de l'exécution physique (ce qu'il se passe quand l'agent arrive sur la case).
 */
public abstract class Action {

    // Coordonnées spatiales définissant le point de destination de la tâche.
    // L'encapsulation est 'protected' pour permettre aux classes filles (PlantAction, HarvestAction...)
    // de lire et manipuler ces valeurs directement lors de l'exécution de leur logique métier.
    protected int targetX;
    protected int targetY;

    /**
     * Constructeur de base forçant toute action à posséder un point d'ancrage sur la grille.
     */
    public Action(int targetX, int targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

    // Accesseurs en lecture seule. Ils sont principalement consommés par le module de navigation
    // de l'entité (l'algorithme A*) pour calculer l'itinéraire avant même que l'action ne commence.
    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }

    /**
     * Contrat d'exécution strict de l'action.
     * Cette méthode est appelée exclusivement par le Thread du Jardinier UNE FOIS qu'il a
     * physiquement atteint les coordonnées (targetX, targetY).
     *
     * Note architecturale : C'est le point névralgique de la sécurité multithread (Late Binding).
     * Puisque le monde a pu évoluer pendant le trajet de l'agent (ex: une plante est morte,
     * un autre jardinier a labouré la case), chaque implémentation de 'perform' devra
     * obligatoirement re-vérifier la validité de la case avant de la modifier.
     *
     * @param gardener L'agent autonome qui exécute la tâche (utile pour interagir avec son inventaire personnel).
     * @param world Le moteur de simulation pour interroger ou modifier l'état de la matrice (World).
     */
    public abstract void perform(Gardener gardener, World world);
}
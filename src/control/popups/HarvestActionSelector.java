package src.control.popups;

import src.model.*;
import src.model.actions.HarvestActionBuilder;
import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Predicate;

/**
 * Contrôleur lié au bouton "Récolter" de l'interface utilisateur (Hotbar ou menu).
 * Son rôle est d'agir comme un intermédiaire entre l'intention du joueur (le clic sur le bouton UI)
 * et le passage du jeu en mode "sélection de case" sur le terrain physique.
 */
public class HarvestActionSelector implements ActionListener {

    // Référence au chef d'orchestre visuel. Nécessaire pour demander le changement
    // d'état de l'interface (fermer le menu actuel, changer l'affichage de la souris, etc.)
    private Display display;

    // Le monteur (pattern Builder) qui va assembler les éléments de la commande finale.
    // Il conserve en mémoire "Qui" va agir (le jardinier) en attendant de savoir "Où".
    private HarvestActionBuilder builder;

    /**
     * Initialise le contrôleur en injectant les dépendances du Modèle et de la Vue.
     * Cette instanciation a généralement lieu lorsqu'un joueur sélectionne un jardinier spécifique.
     *
     * @param display L'interface globale pour piloter le changement de vue.
     * @param gardener Le jardinier (agent) qui exécutera physiquement la récolte.
     * @param world La référence au moteur de simulation.
     */
    public HarvestActionSelector(Display display, Gardener gardener, World world) {
        this.display = display;

        // Initialisation de la construction de l'action. On sécurise immédiatement
        // l'identité du jardinier qui va travailler, pour éviter tout conflit si le joueur
        // sélectionne un autre jardinier pendant qu'il choisit sa case.
        this.builder = new HarvestActionBuilder(gardener, world);

        // On attache la vue au builder. C'est souvent utilisé pour que l'action finale
        // puisse envoyer un callback visuel (ex: retirer une surbrillance de case à l'arrivée).
        this.builder.setDisplay(display);
    }

    /**
     * Méthode déclenchée instantanément lors du clic sur le bouton.
     * Elle prépare les contraintes métiers et bascule l'écoute de la souris sur la grille du jeu.
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        // Définition d'un filtre conditionnel dynamique (Predicate).
        // Le contrôleur de sélection de la grille utilisera ce code pour évaluer, à chaque
        // survol ou clic, si la case visée est une cible légale pour l'action en cours.
        Predicate<Tile> criteria = tile -> {

            // Pré-requis 1 : La tuile ciblée doit obligatoirement être un bloc de terre cultivé
            if (tile instanceof PlantTile) {
                Plant p = ((PlantTile) tile).getPlant();

                // Pré-requis 2 : La case ne doit pas être vide (p != null).
                // Pré-requis 3 : La plante doit se trouver dans un état terminal justifiant une récolte :
                //   - isHarvestable() (souvent MATURE) -> action productive (génère un item).
                //   - MORT (desséchée) -> action de nettoyage pour libérer la case.
                //   - EATEN (attaquée par un nuisible) -> action de nettoyage pour libérer la case.
                return p != null && (p.isHarvestable() ||
                        p.getState() == src.model.PlantState.MORT ||
                        p.getState() == src.model.PlantState.EATEN);
            }

            // Si la case est de l'eau, un chemin ou un bâtiment, on rejette le clic silencieusement
            return false;
        };

        // Délégation du contrôle à l'interface graphique.
        // On lui transmet notre filtre métier, un message indicatif pour le joueur,
        // et l'instance de notre Builder. Lorsque le joueur cliquera sur une case valide,
        // la vue invoquera automatiquement 'builder.buildAction(x, y)'.
        display.switchToSelection(criteria, "Sélectionnez une plante à récolter/nettoyer", builder);
    }

}
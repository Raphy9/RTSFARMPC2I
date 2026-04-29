package src.control.popups;

// Importation des éléments du Modèle (les données métier)
import src.model.Item;
import src.model.ItemSeed;
import src.model.PlantTile;
import src.model.Tile;

// Importation de l'architecture d'actions (Patron de conception Builder)
import src.model.actions.ActionBuilder;

// Importation de la Vue principale pour modifier l'état de l'interface
import src.view.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Contrôleur événementiel associé aux boutons représentant des objets (Items) dans l'inventaire.
 * Agit comme un filtre intelligent : il s'assure que l'objet cliqué est bien du type requis
 * pour l'action en cours (ex: on ne peut pas sélectionner une pelle si on est en train de planter),
 * puis transmet cet objet au constructeur d'action (Builder) avant de lancer la sélection de la cible.
 */
public class InventorySelector implements ActionListener {

    // Utilisation de la réflexion Java (Reflection API).
    // Définit la "classe" attendue pour valider l'action (ex: ItemSeed.class ou ItemPlant.class).
    private Class itemType;

    // La référence physique à l'objet que le joueur tente de manipuler.
    private Item item;

    // Le gestionnaire de la vue, utilisé ici pour fermer l'inventaire et repasser la caméra en mode "Sélection sur la grille".
    private Display display;

    // Le monteur (Builder) qui orchestre la création de l'action. Il a déjà mémorisé le Jardinier actif,
    // il attend maintenant qu'on lui fournisse l'Item à utiliser via ce contrôleur.
    private ActionBuilder builder;

    /**
     * Constructeur injectant le contexte d'exécution du bouton.
     * Chaque bouton de l'inventaire possède sa propre instance de ce contrôleur, configurée spécifiquement pour l'objet qu'il représente.
     *
     * @param display Le chef d'orchestre de l'interface graphique.
     * @param itemType Le type de classe (Class) exigé pour que le clic soit valide.
     * @param item L'instance de l'objet lié à ce bouton.
     * @param builder Le conteneur temporaire qui accumule les paramètres de l'action (Jardinier -> Item -> Coordonnées).
     */
    public InventorySelector(Display display, Class itemType, Item item, ActionBuilder builder) {
        this.itemType = itemType;
        this.item = item;
        this.display = display;
        this.builder = builder;
    }

    /**
     * Cœur logique du contrôleur, invoqué lorsque le joueur clique sur le bouton de l'objet dans son inventaire.
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        // 1. Validation de type (Type Safety dynamique)
        // Au lieu d'utiliser un "instanceof" codé en dur, on utilise la méthode isInstance() de la classe Class.
        // Cela permet au contrôleur d'être générique et réutilisable pour n'importe quel type d'Item.
        if (itemType.isInstance(item)) {

            // 2. Injection de la dépendance dans le Builder
            // L'objet sélectionné est valide, on le transmet au système de construction d'action.
            builder.setItem(item);

            // 3. Routage conditionnel selon la nature de l'action
            // Si le joueur est en train de manipuler spécifiquement une graine, on doit enchaîner
            // sur une phase de ciblage spatial (où va-t-on planter cette graine ?).
            if (itemType == ItemSeed.class) {

                // Définition du filtre (Predicate) pour restreindre les cases cliquables sur le terrain.
                display.switchToSelection(
                        t ->
                                // Règle A : La case visée doit avoir été labourée au préalable.
                                t instanceof PlantTile

                                        // Règle B : La terre doit être libre (pas de plante déjà existante ou morte).
                                        && t.isFarmable()

                                        // Règle C : On interdit formellement de planter sous une infrastructure (ex: sous un épouvantail).
                                        && !display.getWorld().hasBuildingAt(t.getX(), t.getY()),

                        // Texte indicatif affiché à l'écran pour guider le joueur
                        "Selectionner une parcelle",

                        // On passe le Builder à la vue. Dès que le joueur cliquera sur une case validant
                        // les 3 règles ci-dessus, la vue appellera 'builder.buildAction()'.
                        builder
                );
            }

            // Trace de débogage pour suivre les manipulations dans la console système
            System.out.println("Item selectionne: " + item);
        }
    }
}
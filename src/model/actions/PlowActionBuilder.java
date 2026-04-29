package src.model.actions;

import src.model.Gardener;
import src.model.World;
import java.awt.Point;

/**
 * Monteur (Builder) dédié à l'action de labourer (Plow).
 * Il convertit la sélection de cases du joueur en une série d'ordres de déplacement
 * et de labourage pour le jardinier, tout en gérant intelligemment la limite globale de parcelles.
 */
public class PlowActionBuilder extends ActionBuilder {
    private World world;

    /**
     * Constructeur initialisant le contexte de l'action.
     * @param gardener Le jardinier (l'agent) qui va exécuter physiquement l'action.
     * @param world Le monde dans lequel l'action s'inscrit (nécessaire pour les limites).
     */
    public PlowActionBuilder(Gardener gardener, World world) {
        super(gardener);
        this.world = world;
    }

    // Accesseur pour permettre à la vue (ex: SelectionController) d'interroger les règles du monde
    public World getWorld() {
        return world;
    }

    // Récupère la limite maximale absolue de parcelles labourables dictée par le niveau actuel du joueur
    public int getPlowLimit() {
        return world.getPlowLimit();
    }

    // Récupère le nombre de parcelles actuellement labourées (ou en cours de création) sur la carte entière
    public int getCurrentPlowedTilesCount() {
        return world.getEffectivePlowedTilesCount();
    }

    /** Message affiché en mode sélection labour.
     *  Méthode utilitaire qui génère le texte de feedback dynamique de l'interface utilisateur.
     */
    public String getSelectionMessage() {
        int current = getCurrentPlowedTilesCount();
        int limit = getPlowLimit();
        int queued = getSelectedPoints().size();
        return "Ajoutez une case a labourer " + current + "/"
                + limit + " (+5 par niveau) | En attente : " + queued;
    }

    /**
     * Construit la file d'attente d'actions et l'injecte dans le jardinier.
     * Appelé lorsque le joueur valide sa sélection de cases à labourer avec "Entrée".
     */
    @Override
    public void buildAction() {
        int reserved = getSelectedPoints().size();

        // --- Étape 1 : Réservation asynchrone ---
        // C'est une excellente pratique : on réserve virtuellement les cases auprès du monde
        // AVANT même que le jardinier ne commence à marcher.
        // Cela empêche le joueur de spammer de nouvelles sélections de labourage pour contourner
        // sa limite de niveau pendant que le jardinier est en train de se déplacer.
        if (reserved > 0) {
            world.reservePlowTiles(reserved);
        }

        // --- Étape 2 : Traduction de la sélection en ordres ---
        for (Point p : getSelectedPoints()) {

            // Recherche d'une case libre adjacente. On ne veut pas que le jardinier se place
            // directement SUR la case qu'il doit frapper avec sa houe, mais à côté.
            Point adjacent = world.findClosestWalkableAdjacent(p.x, p.y, getGardener());
            int execX = (adjacent != null) ? adjacent.x : p.x;
            int execY = (adjacent != null) ? adjacent.y : p.y;

            // Feedback visuel : on colorie la case ciblée (souvent en jaune) dans la vue globale
            if (getDisplay() != null) {
                getDisplay().getGlobalView().setHighlight(p.x, p.y);
            }

            // Fonction de rappel (Callback) passée à l'action de mouvement.
            // Ce code ne s'exécutera pas maintenant, mais exactement au moment où le jardinier
            // atteindra sa destination, effaçant ainsi la surbrillance au moment parfait.
            Runnable clearHighlight = () -> {
                if (getDisplay() != null) {
                    getDisplay().getGlobalView().clearHighlight(p.x, p.y);
                }
            };

            // Ajout du binôme "Marche jusqu'à la destination -> Laboure la cible" dans la file d'attente
            getGardener().addAction(new MoveAction(execX, execY, clearHighlight));
            getGardener().addAction(new PlowAction(execX, execY, p.x, p.y));
        }
    }
}
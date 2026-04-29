package src.model.actions;

import src.model.Gardener;
import src.model.World;
import java.awt.Point;

/**
 * Monteur (Builder) spécifique à la séquence de récolte.
 * Son rôle est de traduire la sélection de multiples cases par le joueur en une
 * suite logique d'instructions (Se déplacer -> Récolter -> Revenir à la grange -> Stocker)
 * qui sera injectée dans la file d'attente du jardinier.
 */
public class HarvestActionBuilder extends ActionBuilder {

    // Référence au monde pour interroger l'environnement (ex: position de la grange, calcul des chemins)
    private World world;

    /**
     * Constructeur injectant l'agent cible et le contexte de simulation.
     */
    public HarvestActionBuilder(Gardener gardener, World world) {
        super(gardener);
        this.world = world;
    }

    /**
     * Cœur du monteur : assemble la séquence d'actions finale et la transmet au jardinier.
     * Cette méthode est appelée par le SelectionController lorsque le joueur valide avec la touche Entrée.
     */
    @Override
    public void buildAction() {

        // Règle métier stricte (Guard Clause) : Le joueur ne peut pas récolter s'il n'a nulle part où stocker.
        // Si la grange a été détruite ou n'a pas encore été placée, on annule toute la séquence.
        if (world.getBarnY() == -1 || world.getBarnX() == -1) {
            if (getDisplay() != null) {
                // Retour utilisateur via une fenêtre contextuelle pour expliquer le blocage
                getDisplay().switchToPopup(new src.view.TextPopup(getDisplay(), 350, 150, "Grange manquante",
                        "<div style='text-align: center;'>Vous devez construire une grange pour ramasser / récolter !</div>"));
            }
            return;
        }

        // Itération sur l'ensemble des cases ciblées par le joueur lors de sa sélection (Shift+clic ou Drag)
        for (Point p : getSelectedPoints()) {

            // Feedback visuel : On maintient une surbrillance (souvent jaune) sur les cases
            // qui sont en attente d'être traitées par le jardinier.
            if (getDisplay() != null) {
                getDisplay().getGlobalView().setHighlight(p.x, p.y);
            }

            // Création d'un Callback (fonction de rappel).
            // Cette instruction sera passée au MoveAction et exécutée uniquement lorsque
            // le jardinier arrivera physiquement sur la case, permettant d'effacer la surbrillance au bon moment.
            Runnable clearHighlight = () -> {
                if (getDisplay() != null) {
                    getDisplay().getGlobalView().clearHighlight(p.x, p.y);
                }
            };

            // Logique de navigation intelligente :
            // La case contenant la plante (p.x, p.y) est potentiellement infranchissable (obstacle).
            // On demande au monde de trouver la case libre adjacente la plus proche pour que
            // le jardinier se place *à côté* de la plante pour la récolter, et non *dessus*.
            Point adjacent = world.findClosestWalkableAdjacent(p.x, p.y, getGardener());

            // Si une case adjacente valide est trouvée, on l'utilise comme destination de marche.
            // Sinon (ex: plante totalement encerclée), on tente quand même de marcher sur la plante en dernier recours.
            int execX = (adjacent != null) ? adjacent.x : p.x;
            int execY = (adjacent != null) ? adjacent.y : p.y;

            // Enchaînement des micro-actions pour cette cible précise
            getGardener().addAction(new MoveAction(execX, execY, clearHighlight));
            getGardener().addAction(new HarvestAction(p.x, p.y, world.getStats(), getDisplay()));
        }

        // Mécanique de Quality of Life (QoL) : Automatisation du stockage.
        // Une fois que toutes les récoltes de la sélection sont terminées, on ordonne
        // automatiquement au jardinier de retourner à la grange pour vider ses poches.
        Point barnPos = world.findClosestWalkableAdjacent(world.getBarnX(), world.getBarnY(), getGardener());
        if (barnPos != null) {
            getGardener().addAction(new MoveAction(barnPos.x, barnPos.y));
            getGardener().addAction(new StoreAction(barnPos.x, barnPos.y));
        }
    }
}
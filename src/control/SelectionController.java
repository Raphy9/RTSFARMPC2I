package src.control;

import src.model.PlantTile;
import src.model.Tile;
import src.model.World;
import src.model.actions.ActionBuilder;
import src.model.actions.PlowActionBuilder;
import src.view.Display;
import src.view.GameDialog;

import java.awt.*;
import java.awt.event.*;
import java.util.function.Predicate;
import java.util.HashSet;
import javax.swing.SwingUtilities;

/**
 * Contrôleur responsable de la phase de ciblage sur le terrain.
 * Il intercepte les interactions utilisateur (souris et clavier) lorsque le jeu
 * bascule en "Mode Sélection", permettant au joueur de choisir une ou plusieurs
 * cases cibles avant de valider l'exécution d'une action (labourer, planter, récolter).
 */
public class SelectionController implements MouseListener, MouseMotionListener, KeyListener {

    private Display display;
    private World world;

    // Le conteneur (pattern Builder) qui va stocker les coordonnées des cases sélectionnées
    // en attendant la validation finale par l'utilisateur.
    private ActionBuilder builder;

    // Règle métier dynamique : filtre les tuiles sur lesquelles l'utilisateur a le droit de cliquer.
    // Par défaut, la condition est toujours vraie.
    private Predicate<Tile> selectionCriteria = tile -> true;

    // --- Variables d'état pour la gestion du "Click & Drag" (glisser-sélectionner) ---
    // Indique si le bouton gauche de la souris est actuellement maintenu enfoncé.
    private boolean dragging = false;

    // Permet de différencier un clic simple d'un mouvement de drag, afin de ne pas
    // déclencher deux fois la sélection au moment du relâchement de la souris.
    private boolean movedDuringDrag = false;

    // Conserve les coordonnées de la dernière case traitée pour éviter d'ajouter
    // la même case des dizaines de fois par seconde si le curseur bouge légèrement au sein de celle-ci.
    private Point lastDragPoint = null;

    // Drapeau pour éviter de spammer le joueur avec la popup de limite de labour
    // s'il continue de "draguer" sa souris après avoir atteint le quota.
    private boolean plowLimitPopupShown = false;

    /**
     * Constructeur injectant le contexte de l'interface et de la simulation.
     */
    public SelectionController(Display display, World world) {
        this.display = display;
        this.world = world;
    }

    /**
     * Définit le prédicat (filtre) utilisé pour valider les clics.
     * Cette méthode est appelée par les différents contrôleurs de la Hotbar (ex: PlantActionSelector)
     * juste avant de basculer la vue en mode sélection.
     */
    public void setSelectionCriteria(Predicate<Tile> selectionCriteria) {
        this.selectionCriteria = selectionCriteria;
    }

    /**
     * Attache le constructeur d'action qui recevra les coordonnées validées par ce contrôleur.
     */
    public void setActionBuilder(ActionBuilder builder) {
        this.builder = builder;
        // Met à jour l'interface avec les textes contextuels liés à ce builder spécifique
        updateSelectionMessage();
    }

    // =======================================================
    // Méthodes de l'interface MouseListener (Clics simples)
    // =======================================================

    @Override
    public void mouseClicked(MouseEvent e) {
        // Sécurité : si l'utilisateur vient de faire un glisser-sélectionner,
        // l'événement "mouseClicked" est tout de même déclenché par Java.
        // On l'ignore pour ne pas fausser le traitement.
        if (movedDuringDrag) {
            movedDuringDrag = false;
            return;
        }

        // On ne gère que les clics gauches pour la sélection
        if (!SwingUtilities.isLeftMouseButton(e)) return;

        // Conversion des coordonnées écran en coordonnées de la grille
        Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());
        Tile tile = world.getTile(coords.x, coords.y);
        Point targetPoint = new Point(coords.x, coords.y);

        // On vérifie d'abord si la tuile respecte le critère métier en cours
        if (selectionCriteria.test(tile)) {

            // Interaction avancée : Shift + Clic sur un champ cultivé
            // Permet de sélectionner instantanément l'intégralité de la parcelle (groupe de tuiles adjacentes).
            if (e.isShiftDown() && tile instanceof PlantTile) {
                // On récupère le conteneur logique de la parcelle et on itère sur ses tuiles
                for (PlantTile pt : ((PlantTile) tile).getParcel().getTiles()) {
                    // Même au sein d'une parcelle, on s'assure que chaque tuile respecte le critère
                    if (selectionCriteria.test(pt)) {
                        Point p = new Point(pt.getX(), pt.getY());
                        tryAddTarget(p);
                    }
                }
            }
            // Interaction standard : Clic simple (Toggle)
            else if (builder.getSelectedPoints().contains(targetPoint)) {
                // Si la case était déjà sélectionnée, on la retire de la liste (désélection)
                builder.removeTarget(targetPoint);
            } else {
                // Sinon, on tente de l'ajouter
                tryAddTarget(targetPoint);
            }
        }

        // Rafraîchissement textuel (compteur) et visuel (overlay bleu)
        updateSelectionMessage();
        display.getSelectionView().setSelectedTilesBlueHighlight(
                new HashSet<>(builder.getSelectedPoints())
        );
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            // Initialisation de l'état de glissement
            dragging = true;
            movedDuringDrag = false;
            lastDragPoint = null;
            plowLimitPopupShown = false;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Fin de l'état de glissement
        dragging = false;
        lastDragPoint = null;
        plowLimitPopupShown = false;
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // =======================================================
    // Méthodes de l'interface MouseMotionListener (Drag)
    // =======================================================

    @Override
    public void mouseDragged(MouseEvent e) {
        // On ne traite le glissement que s'il a été initié par un clic gauche validé
        if (!dragging) return;

        Point coords;
        try {
            coords = display.getCamera().screenToWorld(e.getX(), e.getY());
        } catch (Exception ex) {
            return; // Exception silencieuse si le curseur sort violemment de l'écran
        }

        Point targetPoint = new Point(coords.x, coords.y);

        // Optimisation : on ignore le traitement si la souris bouge au sein de la même tuile
        if (targetPoint.equals(lastDragPoint)) return;

        lastDragPoint = targetPoint;
        movedDuringDrag = true;

        try {
            Tile tile = world.getTile(coords.x, coords.y);

            // Lors d'un drag, on n'ajoute que de nouvelles cases (pas de toggle/désélection)
            if (selectionCriteria.test(tile) && !builder.getSelectedPoints().contains(targetPoint)) {
                if (tryAddTarget(targetPoint)) {
                    updateSelectionMessage();
                    display.getSelectionView().setSelectedTilesBlueHighlight(
                            new HashSet<>(builder.getSelectedPoints())
                    );
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            // Sécurité : ignorer le drag si la caméra est en bordure de la map et vise le vide
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    // =======================================================
    // Méthodes de l'interface KeyListener (Validation / Annulation)
    // =======================================================

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        // L'utilisateur valide sa sélection en appuyant sur la touche Entrée
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {

            // On s'assure qu'au moins une case a été ciblée avant de procéder
            if (!builder.getSelectedPoints().isEmpty()) {

                // Nettoyage de l'interface : suppression des surbrillances bleues et jaunes
                display.getSelectionView().setSelectedTilesBlueHighlight(new HashSet<>());
                display.switchToGlobal();

                // Injection de l'action dans la file d'attente du Jardinier
                builder.buildAction();

                // Purge du conteneur pour la prochaine utilisation
                builder.clearTargets();
            }
        }
        // L'utilisateur annule sa sélection en appuyant sur la touche Échap
        else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            cancelSelection();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    // =======================================================
    // Méthodes internes de validation et de mise à jour
    // =======================================================

    /**
     * Tente d'ajouter une case à la sélection en vérifiant les règles métier spécifiques.
     * @return true si l'ajout a été accepté, false sinon.
     */
    private boolean tryAddTarget(Point targetPoint) {
        // Vérification de redondance
        if (builder.getSelectedPoints().contains(targetPoint)) {
            return false;
        }

        // --- Logique métier spécifique au Labourage ---
        if (builder instanceof PlowActionBuilder) {
            PlowActionBuilder plowBuilder = (PlowActionBuilder) builder;
            int selectedCount = builder.getSelectedPoints().size();

            // On vérifie auprès du World s'il est autorisé de créer de nouvelles parcelles
            // (La limite est gérée par le système d'expérience/niveau du joueur).
            if (!plowBuilder.getWorld().canAddPlowedTiles(selectedCount + 1)) {

                // On affiche la popup d'avertissement une seule fois par séquence de drag
                if (!plowLimitPopupShown) {
                    int current = plowBuilder.getCurrentPlowedTilesCount();
                    int limit = plowBuilder.getPlowLimit();
                    GameDialog.showMessage(display.getSelectionView(),
                            "Limite de labour atteinte",
                            "Impossible de labourer plus de cases.\n"
                                    + "Actuel : " + current + " / Limite : " + limit);
                    plowLimitPopupShown = true;
                }
                updateSelectionMessage();
                return false;
            }
        }

        // Si aucune contrainte métier ne bloque, on valide l'ajout
        builder.addTarget(targetPoint);
        return true;
    }

    /**
     * Rafraîchit les informations textuelles affichées à l'écran (ex: "X cases sélectionnées").
     */
    private void updateSelectionMessage() {
        if (builder instanceof PlowActionBuilder) {
            // Le Builder sait comment formater son propre message d'état
            display.getSelectionView().setMessage(((PlowActionBuilder) builder).getSelectionMessage());
        }
    }

    /**
     * Interrompt le processus de sélection, nettoie les variables d'état et
     * restitue le contrôle complet au mode de vue global.
     * Cette méthode peut être appelée soit par la touche Echap, soit lorsqu'un joueur
     * change d'outil dans la Hotbar en pleine sélection.
     */
    public void cancelSelection() {
        display.getSelectionView().setSelectedTilesBlueHighlight(new HashSet<>());
        if (builder != null) {
            builder.clearTargets();
        }
        dragging = false;
        movedDuringDrag = false;
        lastDragPoint = null;
        plowLimitPopupShown = false;

        display.switchToGlobal();
    }
}
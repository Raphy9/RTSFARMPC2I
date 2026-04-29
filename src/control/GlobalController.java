package src.control;

import src.model.*;
import src.view.*;

import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import java.awt.event.MouseMotionListener;

/**
 * Contrôleur principal gérant les interactions à la souris sur l'écran de jeu (la ferme).
 * Il capte les clics sur l'interface superposée (hotbar) ainsi que sur les éléments
 * physiques du monde (bâtiments, entités, tuiles).
 */
public class GlobalController implements MouseListener, MouseMotionListener {

    private final Display display;
    private final World world;

    // Drapeau prévu pour ignorer un clic de souris si un autre événement prioritaire
    // vient de se produire (ex: fermeture d'un menu qui ne doit pas déclencher d'action sur le terrain).
    private boolean ignoreNextClick = false;

    public GlobalController(Display display, Global globalView, World world) {
        // On s'abonne directement à la vue globale pour écouter ses événements matériels
        globalView.addMouseListener(this);
        globalView.addMouseMotionListener(this);

        this.display = display;
        this.world = world;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        // Bloc de sécurité : on évite que le joueur clique "à travers" le mode construction.
        if (display.getBuildingManager() != null) {

            // Si le joueur tient un bâtiment dans sa main ou utilise l'outil de suppression,
            // on bloque les interactions classiques (comme cliquer sur un corbeau ou la grange).
            if (display.getBuildingManager().isPlacing() || display.getBuildingManager().isDeletionMode()) {
                return;
            }

            // Sécurité anti-spam : le BuildingManager vient de consommer un clic pour poser un objet.
            // On bloque la propagation de l'événement pendant une courte fenêtre (200ms)
            // pour ne pas déclencher une autre action par accident sur la même frame.
            if (display.getBuildingManager().hasJustActed()) {
                return;
            }
        }

        // Si l'interface est masquée (ex: cinématique ou menu ouvert au-dessus), on ignore les clics
        if (!display.getGlobalView().isHotbarVisible()) {
            return;
        }

        // Gestion des clics sur l'interface graphique (Hotbar)
        if (display.getGlobalView().isHotbarVisible() &&
                world.getGardeners() != null && !world.getGardeners().isEmpty()) {

            // Constantes géométriques de la Hotbar pour calculer la zone de collision du clic
            int nbSlots = 4;
            int slotSize = 52; // Taille en pixels d'une case
            int spacing = 8;   // Espace entre chaque case

            // Calcul de la largeur totale pour pouvoir centrer dynamiquement la barre
            int totalWidth = (slotSize * nbSlots) + (spacing * (nbSlots - 1));
            int panelWidth = display.getGlobalView().getWidth();
            int panelHeight = display.getGlobalView().getHeight();

            // Origines X et Y de la Hotbar sur l'écran
            int startX = (panelWidth - totalWidth) / 2;
            int startY = panelHeight - slotSize - 50;

            // Vérification sur l'axe Y : le clic est-il à la hauteur de la Hotbar ?
            if (e.getY() >= startY && e.getY() <= startY + slotSize) {

                // Si oui, on vérifie sur quelle case spécifique le joueur a cliqué
                for (int i = 0; i < nbSlots; i++) {
                    int slotX = startX + i * (slotSize + spacing);

                    if (e.getX() >= slotX && e.getX() <= slotX + slotSize) {

                        // Sécurité liée au tutoriel : empêche le joueur d'utiliser un outil
                        // qu'il n'est pas censé connaître encore.
                        if (!Tutorial.isHotbarSlotActive(i)) {
                            return;
                        }

                        // Récupération d'un agent libre pour exécuter l'action
                        Gardener gardener = world.getAvailableGardener();
                        if (gardener == null) return;

                        // Mise à jour visuelle : on indique au modèle quel outil est tenu en main,
                        // puis on force un rafraîchissement pour afficher la bordure blanche de sélection.
                        world.getGardeners().get(0).setSelectedHotbarIndex(i);
                        display.getGlobalView().repaint();

                        // On délègue l'exécution de l'action à la vue principale
                        display.triggerHotbarAction(i, gardener);
                        return;
                    }
                }
            }
        }

        // Si le clic n'a pas été intercepté par l'interface, on traduit les coordonnées en pixels
        // de l'écran vers des coordonnées logiques de la matrice du jeu via la caméra.
        Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());

        // Protection pour éviter une exception d'IndexOutOfBounds si le joueur clique dans
        // les bordures noires au-delà de la carte générée.
        if (coords.x < 0 || coords.x >= World.WIDTH || coords.y < 0 || coords.y >= World.HEIGHT) {
            return;
        }

        Tile tile = world.getTile(coords.x, coords.y);

        // Détection d'interaction avec le magasin principal
        if (world.isBarnInside(coords.x, coords.y)) {
            System.out.println("Clic proche de la grange -> Ouverture PopupBarn");
            display.switchToPopup(new src.view.PopupBarn(display, world));
            return;
        }

        // Détection d'interaction avec la faune (animaux mobiles)
        src.model.Chicken chickenToClick = null;
        src.model.Crow crowToClick = null;

        // On parcourt toutes les entités physiquement présentes sur la tuile cliquée
        for (src.model.Entity entity : tile.getEntities()) {
            if (chickenToClick == null && entity instanceof src.model.Chicken) {
                chickenToClick = (src.model.Chicken) entity;
            } else if (crowToClick == null && entity instanceof src.model.Crow) {
                crowToClick = (src.model.Crow) entity;
            }
        }

        // Si une poule est trouvée sous le curseur, on la chasse et on notifie le système de quêtes
        if (chickenToClick != null) {
            chickenToClick.flee();
            world.registerQuestAction(Quests.ACTION_CHASE_CHICKEN);
            return;
        }

        // Pareil pour le corbeau
        if (crowToClick != null) {
            crowToClick.flee();
            world.registerQuestAction(Quests.ACTION_CLICK_CROW);
            return;
        }

        // Note architecturale : On ne gère plus les clics directs sur la terre ici.
        // C'est désormais le rôle exclusif de la Hotbar et des ActionSelectors d'interagir avec le sol.
    }

    // Méthodes de l'interface MouseListener non utilisées pour le moment, mais obligatoires
    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    /**
     * Gère les mouvements passifs de la souris (sans clic).
     * Sert uniquement à fournir un retour visuel (highlight/surbrillance) de la case survolée.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        // Traduction des coordonnées de la souris en temps réel
        Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());

        if (coords.x >= 0 && coords.x < World.WIDTH && coords.y >= 0 && coords.y < World.HEIGHT) {
            // Indique à la vue de dessiner un marqueur sur cette case précise
            display.getGlobalView().setHoveredTile(coords.x, coords.y);
        } else {
            // Si la souris sort de la grille, on passe des coordonnées négatives pour masquer le marqueur
            display.getGlobalView().setHoveredTile(-1, -1);
        }
    }
}
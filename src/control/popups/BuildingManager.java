package src.control.popups;

import src.model.SoundManager;
import src.model.World;
import src.model.Quests;
import src.model.buildings.Building;
import src.model.buildings.Obstacle;
import src.view.Display;
import src.view.GameDialog;
import src.model.Tile;
import src.model.PlantTile;

import javax.swing.SwingUtilities;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

/**
 * Contrôleur principal pour le système de construction et de destruction du jeu.
 * Hérite de MouseAdapter pour intercepter les interactions de la souris sur la grille globale.
 * Gère deux états principaux : le "Mode Placement" (instanciation de nouveaux bâtiments)
 * et le "Mode Suppression" (vente et destruction d'infrastructures existantes).
 */
public class BuildingManager extends MouseAdapter {

    // Fenêtre temporelle de tolérance pour éviter les doubles clics accidentels
    // entre l'interface utilisateur et le clic sur la grille.
    private static final long JUST_ACTED_WINDOW_MS = 200L;

    private World world;
    private Display display;

    // Variables d'état pour le Mode Placement
    // Représente le modèle virtuel du bâtiment qui suit le curseur avant confirmation
    private Building ghostBuilding = null;
    private int ghostX = -1;
    private int ghostY = -1;

    // Permet d'éviter de reposer plusieurs fois le bâtiment sur la même case en maintenant le clic enfoncé
    private int lastPlacedX = Integer.MIN_VALUE;
    private int lastPlacedY = Integer.MIN_VALUE;

    // Fonction de rappel exécutée après un placement réussi (utile pour mettre à jour les inventaires ou UI)
    private Runnable onPlacementComplete = null;

    // Contrainte optionnelle limitant le nombre de bâtiments d'un même type (ex: un seul puit par ferme)
    private int placementMaxCount = -1;

    // --- Variables d'état pour le Mode Suppression ---
    private boolean deletionMode = false;

    // Listener permettant de notifier d'autres composants UI (comme un bouton qui changerait de couleur) de l'état du mode
    private Consumer<Boolean> deletionModeListener = null;

    // Indique si le joueur est en train de maintenir le clic pour sélectionner plusieurs éléments (Click-and-Drag)
    private boolean deletionDragActive = false;

    // Collections conservant l'ordre de sélection (LinkedHashSet) pour garantir l'unicité des éléments ciblés
    private final Set<Building> pendingDeletionBuildings = new LinkedHashSet<>();
    private final Set<Point> pendingDeletionPlantTiles = new LinkedHashSet<>();

    // Variables de contrôle global
    private long lastActionTimestampMs = 0L;
    private boolean leftMousePressed = false;

    public BuildingManager(World world, Display display) {
        this.world = world;
        this.display = display;
    }

    public void setDeletionModeListener(Consumer<Boolean> listener) {
        this.deletionModeListener = listener;
    }

    public boolean isDeletionMode() { return deletionMode; }
    public boolean isPlacing() { return ghostBuilding != null; }
    public boolean hasJustActed() {
        return (System.currentTimeMillis() - lastActionTimestampMs) < JUST_ACTED_WINDOW_MS;
    }

    // Accesseurs en lecture seule pour protéger l'intégrité des listes de sélection
    public Set<Building> getPendingDeletionBuildings() {
        return Collections.unmodifiableSet(pendingDeletionBuildings);
    }
    public Set<Point> getPendingDeletionPlantTiles() {
        return Collections.unmodifiableSet(pendingDeletionPlantTiles);
    }

    /**
     * Initialise le mode de construction.
     * Cette méthode surcharge la version complète avec des paramètres par défaut.
     */
    public void startPlacement(Building buildingTemplate) {
        startPlacement(buildingTemplate, -1, null);
    }

    public void startPlacement(Building buildingTemplate, Runnable onComplete) {
        startPlacement(buildingTemplate, -1, onComplete);
    }

    /**
     * Initialise le mode de construction avec toutes les contraintes.
     */
    public void startPlacement(Building buildingTemplate, int maxCount, Runnable onComplete) {
        this.placementMaxCount = maxCount;
        this.onPlacementComplete = onComplete;
        this.ghostBuilding = buildingTemplate;
        this.lastPlacedX = Integer.MIN_VALUE;
        this.lastPlacedY = Integer.MIN_VALUE;

        // Notification à la Vue pour qu'elle commence à dessiner l'hologramme du bâtiment sous le curseur
        display.getGlobalView().setGhostBuilding(this);
    }

    public void setOnPlacementComplete(Runnable onPlacementComplete) {
        this.onPlacementComplete = onPlacementComplete;
    }

    /**
     * Quitte proprement le mode de construction et réinitialise les états associés.
     */
    public void cancelPlacement() {
        this.ghostBuilding = null;
        this.leftMousePressed = false;
        this.lastPlacedX = Integer.MIN_VALUE;
        this.lastPlacedY = Integer.MIN_VALUE;
        display.getGlobalView().setGhostBuilding(null);
        display.getGlobalView().repaint();
        display.getGlobalView().requestFocusInWindow();
    }

    /**
     * Active le mode de suppression, permettant de revendre des bâtiments.
     */
    public void startDeletionMode() {
        this.deletionMode = true;
        this.ghostBuilding = null;
        this.deletionDragActive = false;
        this.pendingDeletionBuildings.clear();
        this.pendingDeletionPlantTiles.clear();

        display.getGlobalView().setGhostBuilding(this);
        display.getGlobalView().repaint();

        if (this.deletionModeListener != null) this.deletionModeListener.accept(true);
    }

    public void cancelDeletionMode() {
        if (!this.deletionMode) return;
        this.deletionMode = false;
        this.deletionDragActive = false;
        this.pendingDeletionBuildings.clear();
        this.pendingDeletionPlantTiles.clear();
        display.getGlobalView().repaint();
        if (this.deletionModeListener != null) this.deletionModeListener.accept(false);
        display.getGlobalView().setGhostBuilding(null);
    }

    /**
     * Événement déclenché lorsque la souris se déplace sans clic enfoncé.
     * Utilisé pour mettre à jour les coordonnées de l'hologramme (Ghost) ou du curseur rouge.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        if (ghostBuilding != null || deletionMode) {
            // Conversion des coordonnées de l'écran (pixels) en coordonnées de la matrice du monde
            Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());
            ghostX = coords.x;
            ghostY = coords.y;
            display.getGlobalView().repaint();
        }
    }

    /**
     * Événement déclenché lorsque l'utilisateur maintient le clic gauche et déplace la souris.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        // La logique de suivi du curseur reste la même
        mouseMoved(e);

        // Si on est en suppression, glisser la souris ajoute continuellement les bâtiments survolés à la sélection
        if (deletionMode && leftMousePressed) {
            addBuildingToDeletionSelection(ghostX, ghostY);
            return;
        }

        // Si on est en placement, glisser la souris tente de poser un bâtiment en continu (ex: peindre des barrières)
        if (!deletionMode && ghostBuilding != null && leftMousePressed) {
            tryPlaceCurrentGhost();
        }
    }

    /**
     * Événement déclenché lorsque l'utilisateur relâche le bouton de la souris.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            leftMousePressed = false;
            // On réinitialise la dernière position pour permettre de reposer un élément au même endroit plus tard
            lastPlacedX = Integer.MIN_VALUE;
            lastPlacedY = Integer.MIN_VALUE;

            // Fin du glisser-sélectionner : on valide la suppression de tout ce qui a été ciblé
            if (deletionMode && deletionDragActive) {
                confirmAndApplyDeletionSelection();
            }
        }
    }

    /**
     * Événement déclenché à l'instant où l'utilisateur appuie sur un bouton de la souris.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            leftMousePressed = true;
        }

        if (deletionMode) {
            // Le clic droit annule le mode en cours
            if (SwingUtilities.isRightMouseButton(e)) {
                cancelDeletionMode();
                return;
            }
            if (SwingUtilities.isLeftMouseButton(e)) {
                // Initiation d'une séquence de sélection multiple
                deletionDragActive = true;
                pendingDeletionBuildings.clear();
                pendingDeletionPlantTiles.clear();

                Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());
                ghostX = coords.x;
                ghostY = coords.y;
                addBuildingToDeletionSelection(coords.x, coords.y);
            }
            return;
        }

        if (ghostBuilding != null) {
            if (SwingUtilities.isRightMouseButton(e)) {
                cancelPlacement();
                return;
            }

            if (SwingUtilities.isLeftMouseButton(e)) {
                tryPlaceCurrentGhost();
            }
        }
    }

    /**
     * Tente d'instancier et de positionner le bâtiment sur la grille selon les règles métier.
     */
    private void tryPlaceCurrentGhost() {
        if (ghostBuilding == null) return;

        // Protection contre le spam : on empêche de poser deux fois sur la même case en un seul clic prolongé
        if (ghostX == lastPlacedX && ghostY == lastPlacedY) return;

        // Vérification de la contrainte d'unicité (ex: un seul joueur a le droit de poser 10 barrières maximum)
        if (placementMaxCount != -1 && countPlacedInstances(ghostBuilding.getClass()) >= placementMaxCount) {
            leftMousePressed = false;
            GameDialog.showMessage(display.getGlobalView(),
                    "Limite atteinte",
                    "Vous avez déjà construit le nombre maximum de ce bâtiment.");
            cancelPlacement();
            return;
        }

        // Appel au moteur de validation pour s'assurer que la case est valide
        if (!canPlace(ghostX, ghostY, ghostBuilding)) return;

        // Vérification financière
        int cost = ghostBuilding.getBuyPrice();
        if (cost > 0 && world.getStats().getMoney() < cost) {
            leftMousePressed = false; // Stoppe le drag pour ne pas afficher la popup 50 fois
            GameDialog.showMessage(display.getGlobalView(),
                    "Fonds insuffisants",
                    "Pas assez d'argent !\nCoût : " + cost + " PO\nSolde : " + world.getStats().getMoney() + " PO");
            return;
        }

        // Clonage dynamique de l'instance "Ghost" pour créer l'objet final
        Building placedBuilding = createBuildingLikeGhost();
        if (placedBuilding == null) return;

        // Finalisation de l'objet et insertion dans le monde
        placedBuilding.setPosition(ghostX, ghostY);
        world.addBuilding(placedBuilding);

        // Notification au système de quêtes qu'une construction a été réalisée
        world.registerBuildEvent(placedBuilding);

        SoundManager.playSound(SoundManager.PLACE);

        if (cost > 0) {
            world.getStats().removeMoney(cost);
        }

        // Mise à jour de la topologie de la grille
        // On modifie l'état des tuiles situées sous l'emprise du bâtiment (largeur x hauteur)
        for (int dx = 0; dx < placedBuilding.getWidth(); dx++) {
            for (int dy = 0; dy < placedBuilding.getHeight(); dy++) {
                Tile tileUnder = world.getTile(ghostX + dx, ghostY + dy);

                // Le sol sous un bâtiment ne peut plus être labouré
                tileUnder.setPlowable(false);

                // Si le bâtiment est un mur, la case devient infranchissable pour l'algorithme A*
                if (!placedBuilding.isPassable()) tileUnder.setWalkable(false);

                // Si on a posé un objet par-dessus un champ (ex: épouvantail), on bloque la pousse
                if (tileUnder instanceof PlantTile) {
                    ((PlantTile) tileUnder).setPlantingBlocked(true);
                }
            }
        }

        // Mise à jour de l'état pour le drag and drop
        lastPlacedX = ghostX;
        lastPlacedY = ghostY;
        display.getGlobalView().repaint();
        notifyPlacementComplete();
    }

    /**
     * Calcule le nombre d'instances existantes d'une classe spécifique de bâtiment sur la carte.
     */
    private int countPlacedInstances(Class<?> buildingClass) {
        int count = 0;
        for (Building b : world.getBuildings()) {
            if (b != null && b.getClass().equals(buildingClass)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Utilise la réflexion (Reflection) Java pour instancier un nouvel objet du même type que le ghost.
     */
    private Building createBuildingLikeGhost() {
        try {
            return ghostBuilding.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            System.err.println("Impossible d'instancier le bâtiment: " + ghostBuilding.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Ajoute l'entité présente aux coordonnées fournies dans la liste des éléments à supprimer.
     */
    private void addBuildingToDeletionSelection(int wx, int wy) {
        // Test 1: Est-ce un bâtiment ?
        Building b = world.getBuildingAt(wx, wy);
        if (b != null && pendingDeletionBuildings.add(b)) {
            display.getGlobalView().repaint();
            return;
        }

        // Test 2: Est-ce une parcelle de terre labourée ?
        Tile t = world.getTile(wx, wy);
        if (t instanceof PlantTile) {
            PlantTile pt = (PlantTile) t;
            // Sécurité : On interdit la suppression d'un champ s'il contient une plante vivante
            if (pt.getPlant() == null && pendingDeletionPlantTiles.add(new Point(wx, wy))) {
                display.getGlobalView().repaint();
            }
        }
    }

    /**
     * Calcule le centre géométrique de la sélection pour afficher l'animation de gain d'argent
     * au milieu du groupe d'objets détruits.
     */
    private Point computeDeletionFeedbackWorldPoint() {
        int count = 0;
        int sumX = 0;
        int sumY = 0;

        for (Building b : pendingDeletionBuildings) {
            int cx = b.getX() + (b.getWidth() / 2);
            int cy = b.getY() + (b.getHeight() / 2);
            sumX += cx;
            sumY += cy;
            count++;
        }
        for (Point p : pendingDeletionPlantTiles) {
            sumX += p.x;
            sumY += p.y;
            count++;
        }

        if (count == 0) return new Point(ghostX, ghostY);

        return new Point(Math.round(sumX / (float) count), Math.round(sumY / (float) count));
    }

    /**
     * Exécute la destruction de la sélection après confirmation de l'utilisateur.
     */
    private void confirmAndApplyDeletionSelection() {
        deletionDragActive = false;
        if (pendingDeletionBuildings.isEmpty() && pendingDeletionPlantTiles.isEmpty()) return;

        // Calcul de la valeur de revente totale de la sélection
        int totalSell = 0;
        for (Building b : pendingDeletionBuildings) {
            totalSell += Math.max(0, b.getSellPrice());
        }

        // Avertissement de sécurité (Popup modale)
        String msg = "Supprimer " + pendingDeletionBuildings.size() + " bâtiment(s)"
                + " et " + pendingDeletionPlantTiles.size() + " parcelle(s) ?\n"
                + "Revente totale : " + totalSell + " PO";

        boolean confirmed = GameDialog.showConfirm(display.getGlobalView(), "Confirmer suppression", msg);
        if (!confirmed) {
            pendingDeletionBuildings.clear();
            pendingDeletionPlantTiles.clear();
            display.getGlobalView().repaint();
            return;
        }

        // Procédure de destruction
        for (Building b : pendingDeletionBuildings) {
            world.removeBuilding(b);

            // Notification au gestionnaire de quêtes (différence entre déblayer un rocher ou vendre un enclos)
            if (b instanceof Obstacle) {
                world.registerQuestAction(Quests.ACTION_DESTROY_OBSTACLE);
            } else {
                world.registerQuestAction(Quests.ACTION_DESTROY_BUILDING);
            }
        }

        for (Point p : pendingDeletionPlantTiles) {
            // Remise à l'état naturel de la case
            world.toNormalTile(p.x, p.y);
        }

        // Application financière et retour visuel (Floating text)
        if (totalSell > 0) {
            world.getStats().addMoney(totalSell);
            Point feedbackPoint = computeDeletionFeedbackWorldPoint();
            display.showMoneyTextWorld(totalSell, feedbackPoint.x, feedbackPoint.y);
        }

        // Nettoyage final
        pendingDeletionBuildings.clear();
        pendingDeletionPlantTiles.clear();
        display.getGlobalView().repaint();
        notifyPlacementComplete();
    }

    private void notifyPlacementComplete() {
        lastActionTimestampMs = System.currentTimeMillis();
        if (this.onPlacementComplete != null) {
            this.onPlacementComplete.run();
        }
    }

    // === MOTEUR DE VALIDATION DES RÈGLES DE PLACEMENT ===
    /**
     * Vérifie si un bâtiment peut être physiquement et logiquement placé aux coordonnées données.
     * Prend en compte les dimensions du bâtiment (largeur x hauteur) pour vérifier toutes les tuiles concernées.
     */
    public boolean canPlace(int startX, int startY, Building b) {
        for (int dx = 0; dx < b.getWidth(); dx++) {
            for (int dy = 0; dy < b.getHeight(); dy++) {
                int checkX = startX + dx;
                int checkY = startY + dy;

                // 1. Règle absolue : Interdiction de sortir des limites de la matrice du monde
                if (checkX < 0 || checkY < 0 || checkX >= World.WIDTH || checkY >= World.HEIGHT)
                    return false;

                // 2. Règle anticollision : Un bâtiment existe-t-il déjà sur cette tuile spécifique ?
                if (world.hasBuildingAt(checkX, checkY)) return false;

                Tile t = world.getTile(checkX, checkY);

                // 3. Règle environnementale : La case est-elle de base infranchissable (eau, bordure naturelle) ?
                if (!t.isWalkable()) return false;

                // 4. Règle métier : Vérification des spécifications de placement du bâtiment lui-même
                boolean isPlantTile = (t instanceof src.model.PlantTile);

                // Un puits ou une clôture refuse d'être posé sur un champ cultivable
                if (b.getPlacementRule() == Building.PlacementRule.NORMAL_ONLY && isPlantTile) {
                    return false;
                }

                // Un épouvantail ou un asperseur EXIGE d'être posé sur une zone cultivable
                if (b.getPlacementRule() == Building.PlacementRule.PLANTABLE_ONLY && !isPlantTile) {
                    return false;
                }

                // De plus, si on pose sur un champ, celui-ci doit être vide
                if (b.getPlacementRule() == Building.PlacementRule.PLANTABLE_ONLY && isPlantTile) {
                    PlantTile pt = (PlantTile) t;
                    if (pt.getPlant() != null) {
                        return false;
                    }
                }
            }
        }
        // Si la boucle complète se termine sans lever de flag, le placement est valide
        return true;
    }

    // Accesseurs utilisés par le moteur de rendu graphique (Global.java) pour dessiner l'hologramme
    public Building getGhostBuilding() { return ghostBuilding; }
    public int getGhostX() { return ghostX; }
    public int getGhostY() { return ghostY; }
}
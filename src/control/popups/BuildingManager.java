package src.control.popups;

import src.model.World;
import src.model.buildings.Building;
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

public class BuildingManager extends MouseAdapter {
    private static final long JUST_ACTED_WINDOW_MS = 200L;

    private World world;
    private Display display;

    private Building ghostBuilding = null; // Le bâtiment en cours de placement
    private int ghostX = -1;
    private int ghostY = -1;
    // Mode suppression : si true, un clic gauche supprime le bâtiment sous le curseur
    private boolean deletionMode = false;
    private Consumer<Boolean> deletionModeListener = null;
    private Runnable onPlacementComplete = null;
    private long lastActionTimestampMs = 0L;
    private boolean leftMousePressed = false;
    private int lastPlacedX = Integer.MIN_VALUE;
    private int lastPlacedY = Integer.MIN_VALUE;
    private boolean deletionDragActive = false;
    private final Set<Building> pendingDeletionBuildings = new LinkedHashSet<>();
    private final Set<Point> pendingDeletionPlantTiles = new LinkedHashSet<>();
    private int placementMaxCount = -1;

    public void setDeletionModeListener(Consumer<Boolean> listener) {
        this.deletionModeListener = listener;
    }

    public boolean isDeletionMode() { return deletionMode; }
    public boolean isPlacing() { return ghostBuilding != null; }
    public boolean hasJustActed() {
        return (System.currentTimeMillis() - lastActionTimestampMs) < JUST_ACTED_WINDOW_MS;
    }
    public Set<Building> getPendingDeletionBuildings() {
        return Collections.unmodifiableSet(pendingDeletionBuildings);
    }

    public Set<Point> getPendingDeletionPlantTiles() {
        return Collections.unmodifiableSet(pendingDeletionPlantTiles);
    }

    public BuildingManager(World world, Display display) {
        this.world = world;
        this.display = display;
    }

    // Active le mode construction
    public void startPlacement(Building buildingTemplate) {
        startPlacement(buildingTemplate, -1, null);
    }

    // Active le mode construction et mémorise un callback de refresh UI
    public void startPlacement(Building buildingTemplate, Runnable onComplete) {
        startPlacement(buildingTemplate, -1, onComplete);
    }

    // Active le mode construction avec une limite de quantité maximale pour ce type de bâtiment
    public void startPlacement(Building buildingTemplate, int maxCount, Runnable onComplete) {
        this.placementMaxCount = maxCount;
        this.onPlacementComplete = onComplete;
        this.ghostBuilding = buildingTemplate;
        this.lastPlacedX = Integer.MIN_VALUE;
        this.lastPlacedY = Integer.MIN_VALUE;
        display.getGlobalView().setGhostBuilding(this); // On informe la vue
    }

    public void setOnPlacementComplete(Runnable onPlacementComplete) {
        this.onPlacementComplete = onPlacementComplete;
    }

    // Annule le mode construction
    public void cancelPlacement() {
        this.ghostBuilding = null;
        this.leftMousePressed = false;
        this.lastPlacedX = Integer.MIN_VALUE;
        this.lastPlacedY = Integer.MIN_VALUE;
        display.getGlobalView().setGhostBuilding(null);
        display.getGlobalView().repaint();
        display.getGlobalView().requestFocusInWindow();
    }

    // Mode suppression simple
    public void startDeletionMode() {
        this.deletionMode = true;
        this.ghostBuilding = null;
        this.deletionDragActive = false;
        this.pendingDeletionBuildings.clear();
        this.pendingDeletionPlantTiles.clear();
        // Informer la vue globale pour qu'elle affiche le surlignage rouge
        display.getGlobalView().setGhostBuilding(this);
        display.getGlobalView().repaint();
        if (this.deletionModeListener != null) this.deletionModeListener.accept(true);
    }

    public void cancelDeletionMode() {
        if (!this.deletionMode) return; // rien à faire si le mode n'était pas actif
        this.deletionMode = false;
        this.deletionDragActive = false;
        this.pendingDeletionBuildings.clear();
        this.pendingDeletionPlantTiles.clear();
        display.getGlobalView().repaint();
        if (this.deletionModeListener != null) this.deletionModeListener.accept(false);
        // Retirer la référence pour arrêter le rendu du ghost/highlight
        display.getGlobalView().setGhostBuilding(null);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Mettre à jour la position du ghost quand on est en mode placement ou suppression
        if (ghostBuilding != null || deletionMode) {
            Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());
            ghostX = coords.x;
            ghostY = coords.y;
            display.getGlobalView().repaint(); // Force le dessin du ghost / highlight
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
        if (deletionMode && leftMousePressed) {
            addBuildingToDeletionSelection(ghostX, ghostY);
            return;
        }
        if (!deletionMode && ghostBuilding != null && leftMousePressed) {
            tryPlaceCurrentGhost();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            leftMousePressed = false;
            lastPlacedX = Integer.MIN_VALUE;
            lastPlacedY = Integer.MIN_VALUE;
            if (deletionMode && deletionDragActive) {
                confirmAndApplyDeletionSelection();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            leftMousePressed = true;
        }

        // Si on est en mode suppression
        if (deletionMode) {
            if (SwingUtilities.isRightMouseButton(e)) {
                cancelDeletionMode();
                return;
            }
            if (SwingUtilities.isLeftMouseButton(e)) {
                deletionDragActive = true;
                pendingDeletionBuildings.clear();
                pendingDeletionPlantTiles.clear();
                Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());
                ghostX = coords.x;
                ghostY = coords.y;
                addBuildingToDeletionSelection(coords.x, coords.y);
            }
            return; // multi-suppression : on reste en mode suppression
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

    private void tryPlaceCurrentGhost() {
        if (ghostBuilding == null) return;
        if (ghostX == lastPlacedX && ghostY == lastPlacedY) return;

        if (placementMaxCount != -1 && countPlacedInstances(ghostBuilding.getClass()) >= placementMaxCount) {
            leftMousePressed = false;
            GameDialog.showMessage(display.getGlobalView(),
                    "Limite atteinte",
                    "Vous avez déjà construit le nombre maximum de ce bâtiment.");
            cancelPlacement();
            return;
        }

        if (!canPlace(ghostX, ghostY, ghostBuilding)) return;

        int cost = ghostBuilding.getBuyPrice();
        if (cost > 0 && world.getStats().getMoney() < cost) {
            // On coupe le drag pour eviter de spammer la popup pendant le maintien du clic.
            leftMousePressed = false;
            GameDialog.showMessage(display.getGlobalView(),
                    "Fonds insuffisants",
                    "Pas assez d'argent !\nCoût : " + cost + " PO\nSolde : " + world.getStats().getMoney() + " PO");
            return;
        }

        Building placedBuilding = createBuildingLikeGhost();
        if (placedBuilding == null) {
            return;
        }

        placedBuilding.setPosition(ghostX, ghostY);
        world.addBuilding(placedBuilding);

        if (cost > 0) {
            world.getStats().removeMoney(cost);
            System.out.println("Bâtiment acheté : " + cost + " PO | Solde : " + world.getStats().getMoney());
        }

        for (int dx = 0; dx < placedBuilding.getWidth(); dx++) {
            for (int dy = 0; dy < placedBuilding.getHeight(); dy++) {
                Tile tileUnder = world.getTile(ghostX + dx, ghostY + dy);
                tileUnder.setPlowable(false);
                if (!placedBuilding.isPassable()) tileUnder.setWalkable(false);
                if (tileUnder instanceof PlantTile) {
                    ((PlantTile) tileUnder).setPlantingBlocked(true);
                }
            }
        }

        lastPlacedX = ghostX;
        lastPlacedY = ghostY;
        display.getGlobalView().repaint();
        notifyPlacementComplete();
    }

    private int countPlacedInstances(Class<?> buildingClass) {
        int count = 0;
        for (Building b : world.getBuildings()) {
            if (b != null && b.getClass().equals(buildingClass)) {
                count++;
            }
        }
        return count;
    }

    private Building createBuildingLikeGhost() {
        try {
            return ghostBuilding.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            System.err.println("Impossible d'instancier le batiment: " + ghostBuilding.getClass().getSimpleName());
            return null;
        }
    }

    private void addBuildingToDeletionSelection(int wx, int wy) {
        Building b = world.getBuildingAt(wx, wy);
        if (b != null && pendingDeletionBuildings.add(b)) {
            display.getGlobalView().repaint();
            return;
        }

        Tile t = world.getTile(wx, wy);
        if (t instanceof PlantTile) {
            PlantTile pt = (PlantTile) t;
            // On autorise la suppression de parcelle seulement si elle est vide.
            if (pt.getPlant() == null && pendingDeletionPlantTiles.add(new Point(wx, wy))) {
                display.getGlobalView().repaint();
            }
        }
    }

    private void confirmAndApplyDeletionSelection() {
        deletionDragActive = false;
        if (pendingDeletionBuildings.isEmpty() && pendingDeletionPlantTiles.isEmpty()) {
            return;
        }

        int totalSell = 0;
        for (Building b : pendingDeletionBuildings) {
            totalSell += Math.max(0, b.getSellPrice());
        }

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

        for (Building b : pendingDeletionBuildings) {
            world.removeBuilding(b);
        }
        for (Point p : pendingDeletionPlantTiles) {
            world.toNormalTile(p.x, p.y);
        }
        if (totalSell > 0) {
            world.getStats().addMoney(totalSell);
        }

        pendingDeletionBuildings.clear();
        pendingDeletionPlantTiles.clear();
        display.getGlobalView().repaint();
        notifyPlacementComplete();
        System.out.println("Bâtiments supprimés -> +" + totalSell + " PO | Solde : " + world.getStats().getMoney());
    }

    private void notifyPlacementComplete() {
        lastActionTimestampMs = System.currentTimeMillis();
        if (this.onPlacementComplete != null) {
            this.onPlacementComplete.run();
        }
    }

    // === MOTEUR DE VALIDATION MIS À JOUR ===
    public boolean canPlace(int startX, int startY, Building b) {
        for (int dx = 0; dx < b.getWidth(); dx++) {
            for (int dy = 0; dy < b.getHeight(); dy++) {
                int checkX = startX + dx;
                int checkY = startY + dy;

                // 1. Hors limites du monde
                if (checkX < 0 || checkY < 0 || checkX >= World.WIDTH || checkY >= World.HEIGHT)
                    return false;

                // 2. CORRECTION DU BUG DE SUPERPOSITION : Un bâtiment est déjà là ?
                if (world.hasBuildingAt(checkX, checkY)) return false;

                Tile t = world.getTile(checkX, checkY);

                // 3. Case non franchissable de base (eau, rocher, buisson)
                if (!t.isWalkable()) return false;

                // 4. VÉRIFICATION DES RÈGLES DE PLACEMENT (Normal vs Plantable)
                boolean isPlantTile = (t instanceof src.model.PlantTile);

                if (b.getPlacementRule() == Building.PlacementRule.NORMAL_ONLY && isPlantTile) {
                    return false; // On refuse de poser sur une culture
                }
                if (b.getPlacementRule() == Building.PlacementRule.PLANTABLE_ONLY && !isPlantTile) {
                    return false; // On refuse de poser sur de l'herbe normale
                }
                if (b.getPlacementRule() == Building.PlacementRule.PLANTABLE_ONLY && isPlantTile) {
                    PlantTile pt = (PlantTile) t;
                    if (pt.getPlant() != null) {
                        return false; // Evite de recouvrir une plante existante
                    }
                }

                // (Optionnel) Refuser si le jardinier est exactement sur cette case
                // if (world.getGardener().getX() == checkX && world.getGardener().getY() == checkY) return false;
            }
        }
        return true;
    }

    // Getters pour le rendu
    public Building getGhostBuilding() { return ghostBuilding; }
    public int getGhostX() { return ghostX; }
    public int getGhostY() { return ghostY; }
}
package src.control.popups;

import src.model.World;
import src.model.buildings.Building;
import src.view.Display;
import src.view.GameDialog;
import src.model.Tile;
import src.model.PlantTile;

import javax.swing.SwingUtilities;
import java.util.function.Consumer;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import src.model.buildings.BarnBuilding;

public class BuildingManager extends MouseAdapter {
    private World world;
    private Display display;

    private Building ghostBuilding = null; // Le bâtiment en cours de placement
    private int ghostX = -1;
    private int ghostY = -1;
    // Mode suppression : si true, un clic gauche supprime le bâtiment sous le curseur
    private boolean deletionMode = false;
    private Consumer<Boolean> deletionModeListener = null;

    public void setDeletionModeListener(Consumer<Boolean> listener) {
        this.deletionModeListener = listener;
    }

    public boolean isDeletionMode() { return deletionMode; }

    public BuildingManager(World world, Display display) {
        this.world = world;
        this.display = display;
    }

    // Dans BuildingManager.java
    private long lastActionTime = 0; // Chronomètre
    private Runnable onPlacementComplete = null;

    // Nouvelle méthode pour le GlobalController
    public boolean hasJustActed() {
        // Renvoie true si le manager a agi il y a moins de 200 millisecondes
        return (System.currentTimeMillis() - lastActionTime) < 200;
    }

    public void startPlacement(Building buildingTemplate) {
        // Appelle la nouvelle méthode en lui disant qu'il n'y a pas d'action de fin (null)
        startPlacement(buildingTemplate, null);
    }

    // Active le mode construction
    public void startPlacement(Building buildingTemplate, Runnable callback) {
        this.ghostBuilding = buildingTemplate;
        this.onPlacementComplete = callback; // On sauvegarde l'action
        display.getGlobalView().setGhostBuilding(this);
    }
    // Dans BuildingManager.java
    public boolean isPlacing() {
        return ghostBuilding != null;
    }

    public void setOnPlacementComplete(Runnable callback) {
        this.onPlacementComplete = callback;
    }

    // Annule le mode construction
    public void cancelPlacement() {
        this.ghostBuilding = null;
        display.getGlobalView().setGhostBuilding(null);
        display.getGlobalView().repaint();
        display.getGlobalView().requestFocusInWindow();
    }

    // Mode suppression simple
    public void startDeletionMode() {
        this.deletionMode = true;
        this.ghostBuilding = null;
        // Informer la vue globale pour qu'elle affiche le surlignage rouge
        display.getGlobalView().setGhostBuilding(this);
        display.getGlobalView().repaint();
        if (this.deletionModeListener != null) this.deletionModeListener.accept(true);
    }

    public void cancelDeletionMode() {
        if (!this.deletionMode) return; // rien à faire si le mode n'était pas actif
        this.deletionMode = false;
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
    public void mousePressed(MouseEvent e) {
        // Si on est en mode suppression
        if (deletionMode) {
            if (SwingUtilities.isRightMouseButton(e)) {
                cancelDeletionMode();
                return;
            }
            Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());
            Building toRemove = world.getBuildingAt(coords.x, coords.y);
            if (toRemove != null) {
                int sell = toRemove.getSellPrice();
                String msg = "Supprimer ce bâtiment ?\nRevente : " + sell + " PO";
                if (GameDialog.showConfirm(display.getGlobalView(), "Confirmer suppression", msg)) {
                    if (sell > 0) world.getStats().addMoney(sell);
                    world.removeBuilding(toRemove);
                    this.lastActionTime = System.currentTimeMillis();
                    if (onPlacementComplete != null) {
                        onPlacementComplete.run();
                    }
                    display.getGlobalView().repaint();
                    System.out.println("Bâtiment supprimé -> +" + sell + " PO | Solde : " + world.getStats().getMoney());
                }
            } else {
                // Pas de bâtiment : vérifier si c'est une terre labourée vide
                Tile tile = world.getTile(coords.x, coords.y);
                if (tile instanceof PlantTile && ((PlantTile) tile).getPlant() == null) {
                    if (GameDialog.showConfirm(display.getGlobalView(), "Confirmer", "Retransformer cette terre en herbe ?")) {
                        world.toNormalTile(coords.x, coords.y);
                        display.getGlobalView().repaint();
                    }
                }
            }
            return; // multi-suppression : on reste en mode suppression
        }

        if (ghostBuilding != null) {
            if (SwingUtilities.isRightMouseButton(e)) {
                cancelPlacement();
                return;
            }

            if (SwingUtilities.isLeftMouseButton(e) && canPlace(ghostX, ghostY, ghostBuilding)) {
                int cost = ghostBuilding.getBuyPrice();
                if (cost > 0 && world.getStats().getMoney() < cost) {
                    GameDialog.showMessage(display.getGlobalView(),
                            "Fonds insuffisants",
                            "Pas assez d'argent !\nCoût : " + cost + " PO\nSolde : " + world.getStats().getMoney() + " PO");
                    return;
                }

                ghostBuilding.setPosition(ghostX, ghostY);
                world.addBuilding(ghostBuilding);
                this.lastActionTime = System.currentTimeMillis();

                if (onPlacementComplete != null) {
                    onPlacementComplete.run();
                }

                if (cost > 0) {
                    world.getStats().removeMoney(cost);
                    System.out.println("Bâtiment acheté : " + cost + " PO | Solde : " + world.getStats().getMoney());
                }

                for (int dx = 0; dx < ghostBuilding.getWidth(); dx++) {
                    for (int dy = 0; dy < ghostBuilding.getHeight(); dy++) {
                        Tile tileUnder = world.getTile(ghostX + dx, ghostY + dy);
                        tileUnder.setPlowable(false);
                        if (!ghostBuilding.isPassable()) tileUnder.setWalkable(false);
                    }
                }
                cancelPlacement();
            }
        }
    }

    // === MOTEUR DE VALIDATION MIS À JOUR ===
    public boolean canPlace(int startX, int startY, Building b) {

        if (b instanceof BarnBuilding && world.hasBarn()) {
            return false;
        }

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
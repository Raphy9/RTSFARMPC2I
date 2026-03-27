package src.control;

import src.model.World;
import src.model.buildings.Building;
import src.view.Display;
import src.model.Tile;
import src.model.PlantTile; // Ou tout autre nom donné à tes cultures

import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

public class BuildingManager extends MouseAdapter {
    private World world;
    private Display display;

    private Building ghostBuilding = null; // Le bâtiment en cours de placement
    private int ghostX = -1;
    private int ghostY = -1;

    public BuildingManager(World world, Display display) {
        this.world = world;
        this.display = display;
    }

    // Active le mode construction
    public void startPlacement(Building buildingTemplate) {
        this.ghostBuilding = buildingTemplate;
        display.getGlobalView().setGhostBuilding(this); // On informe la vue
    }

    // Annule le mode construction
    public void cancelPlacement() {
        this.ghostBuilding = null;
        display.getGlobalView().setGhostBuilding(null);
        display.getGlobalView().repaint();
        display.getGlobalView().requestFocusInWindow();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (ghostBuilding != null) {
            Point coords = display.getCamera().screenToWorld(e.getX(), e.getY());
            ghostX = coords.x;
            ghostY = coords.y;
            display.getGlobalView().repaint(); // Force le dessin du ghost
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (ghostBuilding != null) {
            if (SwingUtilities.isRightMouseButton(e)) {
                cancelPlacement();
                return;
            }

            if (SwingUtilities.isLeftMouseButton(e) && canPlace(ghostX, ghostY, ghostBuilding)) {
                ghostBuilding.setPosition(ghostX, ghostY);
                world.addBuilding(ghostBuilding);

                // === MODIFICATION ICI : On bloque la case ===
                for(int dx = 0; dx < ghostBuilding.getWidth(); dx++){
                    for(int dy = 0; dy < ghostBuilding.getHeight(); dy++){
                        Tile tileUnder = world.getTile(ghostX + dx, ghostY + dy);

                        // Empêcher le jardinier de labourer sous le bâtiment
                        tileUnder.setPlowable(false);

                        // Empêcher de marcher si le bâtiment bloque le passage
                        if (!ghostBuilding.isPassable()) {
                            tileUnder.setWalkable(false);
                        }
                    }
                }
                System.out.println("Bâtiment construit !");
                cancelPlacement();
            }
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
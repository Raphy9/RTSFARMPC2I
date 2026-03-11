package src.control;

import src.model.Camera;
import src.model.Gardener;
import src.model.MoveAction;
import src.model.World;
import src.view.*;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

/** Classe qui gère les interactions de l'utilisateur avec la vue globale,
 * comme les clics de souris pour sélectionner des cases ou des entités */
public class GlobalController implements MouseListener{

    private Display display;
    private World world;
    private Camera camera;

    public GlobalController(Display display, Global globalView, World world, Camera camera) {
        globalView.addMouseListener(this);
        this.display = display;
        this.world = world;
        this.camera = camera;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
//        int clickX = e.getX();
//        int clickY = e.getY();
//
//        // 1. Calcul du décalage en pixels par rapport à la première case visible
//        int fstTileX = (int) camera.getX();
//        int fstTileY = (int) camera.getY();
//        int pixelDiffX = (int) ((camera.getX() - fstTileX) * Display.RATIO_X);
//        int pixelDiffY = (int) ((camera.getY() - fstTileY) * Display.RATIO_Y);
//
//        // et on ajoute la première case visible
//        int worldX = fstTileX + ((clickX + pixelDiffX) / Display.RATIO_X);
//        int worldY = fstTileY + ((clickY + pixelDiffY) / Display.RATIO_Y);
//
//        System.out.println("Clic Case du monde : (" + worldX + ", " + worldY + ")");
//
//        if (worldX >= 0 && worldX < World.WIDTH && worldY >= 0 && worldY < World.HEIGHT) {
//            Gardener gardener = world.getGardenerTest();
//            if (gardener != null) {
//                gardener.interruptGardener();
//                gardener.addAction(new MoveAction(worldX, worldY));
//            }
//        }
//
//        System.out.println("Clic!");

        // TEMPORAIRE : affiche un popup de test (enlever quand on implementera la vraie fonction clic)
        display.switchToPopup(new PopupInventory(display, 400, 250, "Popup de test inventaire", world.getBarn()));
//        display.switchToPopup(new TextPopup(display,400, 250,
//                "Popup de test","Voici un popup de test pour vérifier que les clics fonctionnent correctement. Il devrait s'afficher lorsque vous cliquez n'importe où sur la vue globale."));
//        // TEMPORAIRE : passer en vue selection
        display.switchToSelection();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
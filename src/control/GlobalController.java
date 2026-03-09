package src.control;

import src.model.Camera;
import src.model.Gardener;
import src.model.MoveAction;
import src.model.World;
import src.view.Display;
import src.view.Global;
import src.view.PopupPanel;
import src.view.TextPopup;

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
        // 1. Récupération des coordonnées du clic à l'écran (en pixels)
        int clickX = e.getX();
        int clickY = e.getY();

        // 2. Conversion en coordonnées de la grille (World) en tenant compte de la caméra et du zoom
        int worldX = (int) (clickX / Display.RATIO_X + camera.getX());
        int worldY = (int) (clickY / Display.RATIO_Y + camera.getY());

        System.out.println("Clic sur l'écran : (" + clickX + ", " + clickY + ") -> Case du monde : (" + worldX + ", " + worldY + ")");

        // 3. Vérification que l'on ne clique pas hors de la carte
        if (worldX >= 0 && worldX < World.WIDTH && worldY >= 0 && worldY < World.HEIGHT) {

            // 4. Récupérer le jardinier et lui donner l'ordre de se déplacer
            Gardener gardener = world.getGardenerTest();
            if (gardener != null) {
                // On vide sa file d'attente et on l'interrompt pour qu'il réagisse immédiatement
                gardener.interruptGardener();

                // On ajoute la nouvelle action de déplacement
                gardener.addAction(new MoveAction(worldX, worldY));
            }
        }

        // J'ai commenté le popup de test selon votre commentaire "TEMPORAIRE".
        // Vous pourrez le réutiliser plus tard pour faire un clic droit d'information par exemple !
        /*
        display.switchToPopup(new TextPopup(display,400, 250,
                "Popup de test","Voici un popup de test pour vérifier que les clics fonctionnent correctement. Il devrait s'afficher lorsque vous cliquez n'importe où sur la vue globale."));
        */
        System.out.println("Clic!");
        // TEMPORAIRE : affiche un popup de test (enlever quand on implementera la vraie fonction clic)
//        display.switchToPopup(new TextPopup(display,400, 250,
//                "Popup de test","Voici un popup de test pour vérifier que les clics fonctionnent correctement. Il devrait s'afficher lorsque vous cliquez n'importe où sur la vue globale."));
        // TEMPORAIRE : passer en vue selection
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
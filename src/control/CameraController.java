package src.control;

import src.model.Camera;
import src.view.Global;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Contrôleur dédié à la gestion des entrées clavier pour le déplacement du point de vue (Viewport).
 * Il hérite de KeyAdapter (plutôt que d'implémenter KeyListener) ce qui permet de redéfinir
 * uniquement la méthode keyPressed sans avoir à surcharger inutilement keyReleased et keyTyped.
 */
public class CameraController extends KeyAdapter {

    // Référence au modèle logique de la caméra qui stocke les coordonnées (x, y) du point de vue actuel.
    private Camera camera;

    // Référence au composant graphique principal. Nécessaire pour forcer le moteur de rendu
    // à redessiner la scène une fois que les coordonnées de la caméra ont été modifiées.
    private Global global;

    // Facteur de translation appliqué à chaque pression de touche.
    // L'utilisation d'un float (0.2f) indique que le déplacement se fait probablement en fractions
    // d'unités logiques (ex: 20% d'une tuile) et non en pixels absolus, assurant une fluidité indépendante de la résolution.
    private final float speed = 0.2f;

    /**
     * Constructeur injectant les dépendances nécessaires au fonctionnement du contrôleur.
     *
     * @param camera L'instance de la caméra à manipuler.
     * @param global Le conteneur de rendu (Vue) à rafraîchir.
     */
    public CameraController(Camera camera, Global global) {
        this.camera = camera;
        this.global = global;
    }

    /**
     * Intercepte les événements matériels du clavier dès qu'une touche est enfoncée.
     * Convertit les entrées directionnelles en vecteurs de déplacement pour la caméra.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        // Extraction du code de la touche pressée pour l'identifier indépendamment de la disposition du clavier
        int key = e.getKeyCode();

        // Routage de l'action selon la touche directionnelle utilisée.
        // La méthode move(dx, dy) de la caméra met à jour ses coordonnées internes.
        switch (key) {
            case KeyEvent.VK_UP:
                camera.move(0, -speed);
                break;
            case KeyEvent.VK_DOWN:
                camera.move(0, speed);
                break;
            case KeyEvent.VK_LEFT:
                camera.move(-speed, 0);
                break;
            case KeyEvent.VK_RIGHT:
                camera.move(speed, 0);
                break;
        }

        // Une fois l'état logique de la caméra mis à jour, on place une requête de rafraîchissement
        // dans la file d'attente de l'Event Dispatch Thread (EDT) de Swing.
        // Cela va déclencher paintComponent() dans la classe Global avec le nouveau décalage visuel.
        global.repaint();
    }
}
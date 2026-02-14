package src.view;

import src.model.World;

import java.awt.*;
import javax.swing.*;


/**
 * Classe qui represente la vue de base du jeu : le terrain, les entites, etc
 * Utilise World et Camera pour permettre le scrolling
 * Permet de selectionner les cases et les entites pour afficher des popups d'information ou des menus d'action
 * Permet l'activation / desactivation du Panel de controle
 */
public class Global extends JPanel {

    private World world;

    public Global(World world) {
        super();
        this.world = world;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }
}

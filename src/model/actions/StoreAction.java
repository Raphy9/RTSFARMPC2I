package src.model.actions;

import src.model.Barn;
import src.model.Gardener;
import src.model.Item;
import src.model.World;
import java.util.ArrayList; // N'oublie pas l'import !

/**
 * Action de stockage : le jardinier se déplace sur la tuile d'exécution (targetX,targetY) et dépose tous les items de son inventaire dans la grange.
 * Cette action est généralement utilisée pour vider l'inventaire du jardinier après une récolte.
 */
public class StoreAction extends Action {

    /** Constructeur de StoreAction.
     * @param targetX Les coordonnées x de la tuile d'exécution (où le jardinier doit se déplacer pour effectuer l'action).
     * @param targetY Les coordonnées y de la tuile d'exécution.
     */
    public StoreAction(int targetX, int targetY) {
        super(targetX, targetY);
    }

    /** Le jardinier arrive sur la tuile d'exécution (targetX,targetY). Cette action dépose tous les items de l'inventaire du jardinier dans la grange.
     * Si le jardinier n'a aucun item, un message d'information est affiché dans la console.
     */
    @Override
    public void perform(Gardener gardener, World world) {
        Barn barn = world.getBarn();
        System.out.println("Déposer les items dans la grange...");

        // FIX : On crée une COPIE de la liste des items
        // On itère sur la copie pendant que transferTo() modifie l'original
        ArrayList<Item> itemsToStore = new ArrayList<>(gardener.getInventory().getItems());

        for (Item item : itemsToStore) {
            if (item != null) {
                // On transfère toute la quantité de cet item
                gardener.getInventory().transferTo(barn, item, item.getQuantity());
            }
        }

        System.out.println("Inventaire du jardinier vidé avec succès.");
    }
}
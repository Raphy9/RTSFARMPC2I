package src.control.popups;

import src.view.PopupBarn;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Contrôleur responsable de la gestion des boutons de catégories dans l'interface de la grange (PopupBarn).
 * Il écoute les clics de l'utilisateur sur les onglets (ex: "Graines", "Récoltes", "Outils")
 * et met à jour l'affichage en conséquence.
 */
public class BarnCategoriesController implements ActionListener {

    // Le bouton de l'interface graphique auquel ce contrôleur est attaché.
    // Il permet de lier l'action de l'utilisateur au composant visuel.
    JButton button;

    // Référence vers la fenêtre popup de la grange.
    // Cela permet au contrôleur d'ordonner à la vue de se mettre à jour.
    PopupBarn popupBarn;

    // Identifiant de la catégorie associée à ce bouton spécifique (ex: "SEEDS", "PLANTS").
    String category;

    /**
     * Constructeur du contrôleur.
     * Initialise les liaisons entre le bouton de l'interface, la vue principale de la grange,
     * et la catégorie de données à afficher lors du clic.
     */
    public BarnCategoriesController(JButton button, PopupBarn popupBarn, String category) {
        this.popupBarn = popupBarn;
        this.button = button;
        this.category = category;
    }

    /**
     * Méthode déclenchée automatiquement par Java Swing lorsque l'utilisateur clique sur le bouton.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Informe la vue de la grange de la nouvelle catégorie sélectionnée par le joueur.
        popupBarn.setCategory(category);

        // Ordonne à la vue de redessiner complètement sa grille d'inventaire
        // pour n'afficher que les objets appartenant à la catégorie fraîchement sélectionnée.
        popupBarn.refresh();
    }
}
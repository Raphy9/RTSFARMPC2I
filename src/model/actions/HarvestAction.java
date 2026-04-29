package src.model.actions;

import src.model.*;

/**
 * Action concrète représentant la récolte d'une plante ou le nettoyage d'une parcelle gâtée.
 * Hérite de la classe abstraite Action et définit le comportement exact que l'agent
 * (le jardinier) doit exécuter une fois arrivé sur sa case de destination.
 */
public class HarvestAction extends Action {

    // Référence aux statistiques globales pour pouvoir attribuer de l'expérience lors d'une récolte réussie.
    private Stats stats;

    // Référence optionnelle à l'interface graphique, utilisée ici spécifiquement pour
    // générer des retours visuels (comme les textes flottants "+10 EXP") directement sur la grille.
    private src.view.Display display;

    /**
     * Constructeur basique, souvent utilisé si l'action est déclenchée sans contexte d'interface
     * (par exemple lors de tests unitaires ou d'actions automatisées en arrière-plan sans rendu).
     */
    public HarvestAction(int targetX, int targetY, Stats stats) {
        this(targetX, targetY, stats, null);
    }

    /**
     * Constructeur complet avec injection de la dépendance Display.
     * C'est la surcharge privilégiée pour le gameplay afin d'avoir le retour visuel à l'écran.
     */
    public HarvestAction(int targetX, int targetY, Stats stats, src.view.Display display) {
        super(targetX, targetY);
        this.stats = stats;
        this.display = display;
    }

    /**
     * Cœur de la logique métier de la récolte.
     * Cette méthode n'est appelée par le Thread du jardinier que lorsqu'il se trouve physiquement
     * sur la case (targetX, targetY).
     */
    @Override
    public void perform(Gardener gardener, World world) {
        // Récupération de la case cible dans la matrice du monde
        Tile tile = world.getTile(targetX, targetY);

        // Vérification de sécurité : on s'assure que la case est bien une parcelle cultivable
        if (tile instanceof PlantTile) {
            PlantTile parcel = (PlantTile) tile;
            Plant plant = parcel.getPlant();

            // Validation tardive (Late Binding) : on vérifie que la plante existe toujours.
            // Elle aurait pu être mangée par un corbeau ou une poule pendant le trajet du jardinier.
            if (plant != null) {

                // Scénario A : La plante est arrivée à maturité (Action productive)
                if (plant.isHarvestable()) {
                    PlantType type = plant.getType();

                    // Modification du Modèle : on réinitialise l'état de la tuile
                    parcel.harvest();

                    // Notification au système de quêtes pour la progression du joueur
                    world.registerHarvestEvent(type);

                    // Récompense matérielle : on génère l'objet (Item) correspondant au légume
                    // et on le place directement dans l'inventaire personnel du jardinier
                    gardener.getInventory().addItem(new ItemPlant(type, 1));
                    System.out.println("Succès : Le jardinier a récolté " + type.getName() + " !");

                    // Feedbacks audiovisuels et progression RPG
                    SoundManager.playSound(SoundManager.HARVEST);
                    stats.addExp(type.getExpGain());

                    // Si la vue est connectée, on lance l'animation du texte flottant d'expérience
                    if (display != null && type.getExpGain() > 0) {
                        display.showExpTextWorld(type.getExpGain(), targetX, targetY);
                    }
                }

                // Scénario B : La plante est morte ou détruite (Action de maintenance)
                // Ce scénario ne rapporte ni argent, ni objet, ni expérience, mais il est vital
                // pour nettoyer la case et permettre au joueur d'y replanter une nouvelle graine.
                else if (plant.getState() == src.model.PlantState.MORT || plant.getState() == src.model.PlantState.EATEN) {
                    parcel.clean();
                    System.out.println("Le jardinier a nettoyé les restes de la plante. La case est prête pour une nouvelle graine !");
                }

            } else {
                // La plante a disparu avant l'arrivée du jardinier, l'action est avortée proprement.
                System.out.println("Échec : Il n'y a rien sur cette case.");
            }
        }
    }
}
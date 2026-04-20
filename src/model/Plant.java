package src.model;

import javax.swing.ImageIcon;

/**
 * La classe Plant gère la logique de croissance, d'eau, de pourriture et de mort d'une plante.
 * Elle est indépendante de la vue et ne connaît que son type et son état.
 * C'est la "source de vérité" pour tout ce qui concerne une plante.
 */
public class Plant {

    // --- Constantes ---
    private static final float MAX_WATER_LEVEL = 100.0f;
    private static final int TIME_BEFORE_DEATH = 300; // Temps (ticks) sans eau avant de mourir

    // --- Attributs ---
    private final PlantType type; // Le type de plante (Salade, Carotte, Tomate, etc.)
    private PlantState state; // L'état actuel de la plante (GRAINE, POUSSE, CROISSANCE, MATURE, MORT, EATEN)

    private float currentWaterLevel; // Niveau d'eau actuel (0 à MAX_WATER_LEVEL)
    private int age;             // Progression de la croissance
    private int ticksWithoutWater; // Compteur pour la mort de soif
    private boolean hasFertilizer; // Si de l'engrais a été mis

    private ImageIcon sprite; // Image actuelle de la plante

    // --- Constructeur ---
    public Plant(PlantType type) {
        this.type = type;
        this.state = PlantState.GRAINE;
        this.currentWaterLevel = 0.0f; // Humidité initiale
        this.age = 0;
        this.ticksWithoutWater = 0;
        this.hasFertilizer = false;
        updateSprite();
    }

    /**
     * Méthode appelée à chaque cycle de jeu (Tick).
     * Gère l'eau, la croissance et la mort.
     */
    public void tick() {
        // Si la plante est morte ou mangée, elle ne fait rien (mais l'eau s'évapore quand même)
        if (state == PlantState.MORT || state == PlantState.EATEN) {
            if (currentWaterLevel > 0) currentWaterLevel -= 0.5f; // Évaporation naturelle
            return;
        }

        // Gestion de l'Eau
        if (currentWaterLevel > 0) {
            currentWaterLevel -= type.getWaterConsumption();
            ticksWithoutWater = 0; // La plante boit, tout va bien
        } else {
            currentWaterLevel = 0;
            ticksWithoutWater++; // Le danger augmente (Sol sec)
        }

        //  Vérification des conditions de Mort (Soif)
        if (ticksWithoutWater >= TIME_BEFORE_DEATH) {
            state = PlantState.MORT;
            updateSprite();
            return; // Fin de la mise à jour
        }

        //  Gestion de la Croissance et des États
        // La plante ne grandit que si elle a de l'eau
        if (currentWaterLevel > 0) {
            int croissance = 1;
            if (hasFertilizer) croissance = 2; // Bonus Engrais

            age += croissance;
            updateState(); // La mise à jour de l'état (et du sprite si besoin) est gérée ici
        }
    }

    /**
     * Mise à jour de l'état de la plante en fonction de son âge et de son type.
     * GRAINE -> POUSSE -> CROISSANCE -> MATURE
     */
    private void updateState() {
        PlantState oldState = this.state;
        int duration = type.getGrowthDuration();

        // On ne met à jour que si la plante n'est pas déjà mature, morte ou mangée
        if (state != PlantState.MATURE && state != PlantState.MORT && state != PlantState.EATEN) {
            if (age >= duration) {
                state = PlantState.MATURE;
            } else if (age >= duration * 0.66f) { // 2/3 de la croissance
                state = PlantState.CROISSANCE;
            } else if (age >= duration * 0.33f) { // 1/3 de la croissance
                state = PlantState.POUSSE;
            }
        }

        // Si l'état a changé, on met à jour l'image pour correspondre
        if (oldState != state) {
            updateSprite();
        }
    }

    // Actions

    /**
     * Arrose la plante d'une certaine quantité d'eau.
     *
     * @param amount Quantité d'eau à ajouter (en pourcentage, ex: 20.0f)
     */
    public void water(float amount) {
        this.currentWaterLevel += amount;
        if (this.currentWaterLevel > MAX_WATER_LEVEL) {
            this.currentWaterLevel = MAX_WATER_LEVEL;
        }
        // Si on arrose, on reset le compteur de mort de soif (sauvetage in extremis)
        if (state != PlantState.MORT && state != PlantState.EATEN) {
            ticksWithoutWater = 0;
        }
    }

    /**
     * Applique de l'engrais à la plante, ce qui accélère sa croissance.
     *
     * @return true si l'engrais a été appliqué, false sinon (ex: déjà mature, morte ou mangée)
     */
    public boolean applyFertilizer() {
        // On ne peut mettre de l'engrais que sur une plante en croissance
        if (this.hasFertilizer || this.state == PlantState.MATURE || this.state == PlantState.MORT || this.state == PlantState.EATEN) {
            return false;
        }
        this.hasFertilizer = true;
        return true;
    }

    // méthodes pour les sprites et l'affichage

    /**
     * INDISPENSABLE POUR L'AFFICHAGE
     * Met à jour l'attribut 'sprite' en fonction de l'état actuel de la plante.
     */
    private void updateSprite() {
        // Cas spécial : si la plante est mangée, on charge l'image générique
        if (state == PlantState.EATEN) {
            this.sprite = new ImageIcon("src/assets/CropSprites/eatenCrop.png");
            return;
        }

        String plantName = type.name().toLowerCase();
        String stepFile = "";

        switch (state) {
            case GRAINE:
                stepFile = "step1.png";
                break;
            case POUSSE:
                stepFile = "step2.png";
                break;
            case CROISSANCE:
                stepFile = "step3.png";
                break;
            case MATURE:
                stepFile = "step4.png";
                break;
            case MORT:
                // Affiche l'image "morte" correspondant au stade où elle est décédée
                int duration = type.getGrowthDuration();
                if (age >= duration * 0.66f) {
                    stepFile = "fstep3.png";
                } else if (age >= duration * 0.33f) {
                    stepFile = "fstep2.png";
                } else {
                    stepFile = "fstep1.png";
                }
                break;
            default:
                return; // Ne fait rien si l'état est inconnu
        }

        String path = "src/assets/CropSprites/" + plantName + "/" + stepFile;
        this.sprite = new ImageIcon(path);
    }

    /**
     * INDISPENSABLE POUR L'AFFICHAGE (utilisé par view.Global)
     *
     * @return L'image (ImageIcon) actuelle de la plante.
     */
    public ImageIcon getSprite() {
        return this.sprite;
    }


    // Getters & Helpers pour la Vue

    /**
     * @return true si le sol est humide (Terre foncée), false si sec (Terre claire)
     */
    public boolean isIrrigated() {
        return currentWaterLevel > 0;
    }

    /**
     * INDISPENSABLE POUR LA RÉCOLTE
     *
     * @return true si la plante est mûre et peut être récoltée, false sinon
     */
    public boolean isHarvestable() {
        return state == PlantState.MATURE;
    }

    // Getters pour la Vue (type, état, niveau d'eau)
    public PlantType getType() {
        return type;
    }

    public PlantState getState() {
        return state;
    }

    public float getWaterLevel() {
        return currentWaterLevel;
    }

    public int getAge() {
        return age;
    }

    public int getTicksWithoutWater() {
        return ticksWithoutWater;
    }

    public boolean hasFertilizer() {
        return hasFertilizer;
    }

    // Pour debug ou barre de vie
    public float getGrowthPercentage() {
        return Math.min((float) age / type.getGrowthDuration(), 1.0f);
    }


    /**
     * Détruit la plante sur le coup (appelé lorsqu'un ennemi l'attaque).
     * Passe l'état de la plante à EATEN et met à jour son image.
     */
    public void destroyByEnemy() {
        if (this.state != PlantState.EATEN) {
            this.state = PlantState.EATEN;
            updateSprite(); // Change l'image pour afficher la souche grignotée
        }
    }

    /**
     * Restaure un état complet de plante depuis une sauvegarde.
     */
    public void restoreState(int age, float waterLevel, int ticksWithoutWater, boolean hasFertilizer, PlantState state) {
        this.age = Math.max(0, age);
        this.currentWaterLevel = Math.max(0.0f, Math.min(MAX_WATER_LEVEL, waterLevel));
        this.ticksWithoutWater = Math.max(0, ticksWithoutWater);
        this.hasFertilizer = hasFertilizer;
        this.state = (state != null) ? state : PlantState.GRAINE;
        updateSprite();
    }
}
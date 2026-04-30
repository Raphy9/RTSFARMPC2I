package src.model;

import src.model.buildings.Sprinkler;
import src.view.GameDialog;

/**
 * La classe Quests est le gestionnaire de progression (le "Maître du Jeu").
 * Elle maintient l'état d'avancement du joueur à travers différents "Chapitres" (QuestLine).
 * Elle fonctionne sur un modèle réactif (Event-Driven) : elle écoute ce qu'il se passe
 * dans le monde (World) et met à jour les objectifs en conséquence.
 */
public class Quests {

    // Ces chaînes de caractères servent de "clés uniques" (identifiants) pour relier
    // une action effectuée dans le jeu à une condition de quête.
    // L'utilisation de constantes évite les fautes de frappe ("PLOW_TILE" vs "plow_tile")
    // qui pourraient casser discrètement le système de progression.

    public static final String ACTION_PLOW_TILE = "PLOW_TILE";
    public static final String ACTION_WATER_TILE = "WATER_TILE";
    public static final String ACTION_SELL_CAROTTE = "SELL_CAROTTE";
    public static final String ACTION_SELL_CHOUX = "SELL_CHOUX";
    public static final String ACTION_SELL_CITROUILLE = "SELL_CITROUILLE";
    public static final String ACTION_SELL_FRAISE = "SELL_FRAISE";
    public static final String ACTION_BUY_SEED_CITROUILLE = "BUY_SEED_CITROUILLE";
    public static final String ACTION_CHASE_CHICKEN = "CHASE_CHICKEN";
    public static final String ACTION_DESTROY_OBSTACLE = "DESTROY_OBSTACLE";
    public static final String ACTION_DESTROY_BUILDING = "DESTROY_BUILDING";
    public static final String ACTION_PLACE_FENCE = "PLACE_FENCE";
    public static final String ACTION_REACH_LEVEL_3 = "REACH_LEVEL_3";
    public static final String ACTION_REACH_LEVEL_4 = "REACH_LEVEL_4";
    public static final String ACTION_REACH_LEVEL_5 = "REACH_LEVEL_5";
    public static final String ACTION_CLICK_CROW = "CLICK_CROW";


    /**
     * Une "QuestLine" représente un chapitre de l'histoire ou un palier de progression.
     * C'est un conteneur logique qui regroupe plusieurs quêtes indépendantes.
     * Un chapitre n'est validé que lorsque TOUTES ses quêtes internes sont terminées.
     */
    public static class QuestLine {
        /** Le titre affiché en gros dans l'interface utilisateur. */
        private final String title;

        /** Le texte d'ambiance ou les instructions générales du chapitre. */
        private final String description;

        /** La liste fermée des objectifs à accomplir pour ce chapitre. */
        private final java.util.List<Quest> quests;

        /** * Verrou logique. Si false, le joueur ne voit pas ce chapitre et ses actions
         * ne feront pas avancer les quêtes qu'il contient.
         */
        private boolean unlocked;

        /**
         * Constructeur d'un chapitre.
         */
        public QuestLine(String title, String description, java.util.List<Quest> quests) {
            this.title = title;
            this.description = description;
            // On copie la liste fournie dans une nouvelle ArrayList pour garantir que
            // la liste d'origine ne sera pas modifiée accidentellement de l'extérieur.
            this.quests = new java.util.ArrayList<>(quests);
            this.unlocked = false;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }

        /**
         * Retourne une version en "Lecture Seule" (unmodifiableList) de la liste des quêtes.
         * C'est une sécurité (Encapsulation) : l'interface graphique peut lire les quêtes,
         * mais ne peut pas faire de `getQuests().clear()` ou `add()`.
         */
        public java.util.List<Quest> getQuests() {
            return java.util.Collections.unmodifiableList(quests);
        }

        public boolean isUnlocked() { return unlocked; }

        private void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

        /**
         * Vérifie si le chapitre entier est terminé.
         * Parcourt toutes les quêtes ; à la première quête non terminée trouvée, retourne false.
         */
        public boolean isCompleted() {
            for (Quest quest : quests) {
                if (!quest.isCompleted()) return false;
            }
            return true;
        }

        /** Compte combien de quêtes de ce chapitre sont déjà validées (utile pour les barres de progression UI). */
        public int getCompletedCount() {
            int count = 0;
            for (Quest quest : quests) {
                if (quest.isCompleted()) count++;
            }
            return count;
        }

        public int getQuestCount() { return quests.size(); }
    }

    /** Le "Storyboard" du jeu : la liste ordonnée de tous les chapitres disponibles. */
    private final java.util.List<QuestLine> questLines = new java.util.ArrayList<>();

    /** * Le marque-page (Curseur). Garde en mémoire l'index (0, 1, 2...) du chapitre
     * sur lequel le joueur est actuellement en train de jouer.
     */
    private int activeLineIndex = 0;

    /** * Fonction de rappel (Callback). Lorsqu'une quête avance, on appelle ce "bouton rouge"
     * pour dire à l'interface graphique (Swing) "Hé, l'état a changé, redessine le panneau de quêtes !".
     */
    private Runnable changeListener = null;

    /** * Constante pointant sur le dernier chapitre (Chapitre 6), car il a un comportement
     * spécial : il boucle sur lui-même à l'infini (Endgame).
     */
    private static final int CHAPTER_6_INDEX = 5;  // Index 5 = 6ème élément du tableau

    /**
     * Constructeur du gestionnaire de quêtes.
     * Prépare le scénario et déverrouille automatiquement le premier chapitre pour que le joueur puisse commencer.
     */
    public Quests() {
        buildDefaultQuestLines();
        if (!questLines.isEmpty()) {
            questLines.get(0).setUnlocked(true);
        }
        notifyChange();
    }

    /**
     * Construit l'arbre complet des quêtes du jeu (Le Scénario).
     * Utilise des "Factory Methods" (méthodes de fabrication comme Quest.createBuildQuest)
     * pour alléger l'écriture et garantir que chaque quête est bien paramétrée.
     */
    private void buildDefaultQuestLines() {
        questLines.clear();

        questLines.add(new QuestLine(
                "Chapitre 1 - Premiers pas",
                "Lancer la ferme et maitriser les bases.",
                java.util.List.of(
                        Quest.createBuildQuest("Q1_BUILD_BARN", "Poser une grange", "Poser 1 grange.", null, 1, 20, 10, Tutorial::tuto3),
                        Quest.createActionQuest("Q1_PLOW_10", "Labourer des parcelles", "Labourer 10 plant tiles.", ACTION_PLOW_TILE, 10, 10, 10, Tutorial::tuto4),
                        Quest.createPlantQuest("Q1_PLANT_CAROTTE", "Planter des carottes", "Planter 10 carottes.", PlantType.CAROTTE, 10, 10, 10, Tutorial::tuto5),
                        Quest.createActionQuest("Q1_WATER_5", "Arroser des cases", "Arroser 10 cases.", ACTION_WATER_TILE, 10, 10, 10, Tutorial::tuto6),
                        Quest.createActionQuest("Q1_CHASE_CHICKEN", "Chasser des poules", "Chasser 4 poules.", ACTION_CHASE_CHICKEN, 4, 15, 15, Tutorial::tuto7)
                )
        ));

        questLines.add(new QuestLine(
                "Chapitre 2 - Nettoyage de la ferme",
                "Vendre, chasser et amenager le terrain.",
                java.util.List.of(
                        Quest.createHarvestQuest("Q2_HARVEST_CAROTTE", "Recolter des carottes", "Recolter 3 carottes mures.", PlantType.CAROTTE, 3, 10, 15, Tutorial::tuto8),
                        Quest.createActionQuest("Q2_SELL_CAROTTE", "Vendre des carottes", "Vendre 3 carottes.", ACTION_SELL_CAROTTE, 3, 20, 10, Tutorial::tuto9),
                        Quest.createActionQuest("Q2_DESTROY_OBSTACLE", "Detruire des obstacles", "Detruire 5 obstacles.", ACTION_DESTROY_OBSTACLE, 5, 10, 20, Tutorial::tuto10),
                        Quest.createBuildQuest("Q2_BUILD_2", "Poser des batiments", "Poser 2 batiments.", null, 2, 20, 15, Tutorial::tuto11),
                        Quest.createActionQuest("Q2_DESTROY_BUILDING", "Detruire un batiment", "Detruire 1 batiment.", ACTION_DESTROY_BUILDING, 1, 15, 20)
                )
        ));

        // ... Chapitres 3, 4 et 5 (Code identique, masqué pour la concision des commentaires) ...
        questLines.add(new QuestLine("Chapitre 3 - Filiere choux", "Specialisation choux et progression de niveau.", java.util.List.of(
                Quest.createActionQuest("Q3_PLOW_5", "Labourer des parcelles", "Labourer 5 plant tiles.", ACTION_PLOW_TILE, 5, 10, 20),
                Quest.createPlantQuest("Q3_PLANT_CHOUX", "Planter des choux", "Planter 5 choux.", PlantType.CHOUX, 5, 10, 20),
                Quest.createHarvestQuest("Q3_HARVEST_CHOUX", "Recolter des choux", "Recolter 3 choux.", PlantType.CHOUX, 3, 15, 20),
                Quest.createActionQuest("Q3_SELL_CHOUX", "Vendre des choux", "Vendre 3 choux a la grange.", ACTION_SELL_CHOUX, 3, 10, 20),
                Quest.createActionQuest("Q3_LEVEL_3", "Passer niveau 3", "Atteindre le niveau 3.", ACTION_REACH_LEVEL_3, 1, 30, 30)
        )));

        questLines.add(new QuestLine("Chapitre 4 - Filiere citrouille", "Monter en puissance avec les citrouilles.", java.util.List.of(
                Quest.createActionQuest("Q4_PLOW_5", "Labourer des parcelles", "Labourer 5 plant tiles.", ACTION_PLOW_TILE, 5, 15, 20),
                Quest.createActionQuest("Q4_BUY_SEED_PUMPKIN", "Acheter des graines", "Acheter 10 graines de citrouille a la grange.", ACTION_BUY_SEED_CITROUILLE, 10, 15, 20),
                Quest.createActionQuest("Q4_SELL_PUMPKIN", "Vendre des citrouilles", "Vendre 8 citrouilles a la grange.", ACTION_SELL_CITROUILLE, 8, 15, 25),
                Quest.createActionQuest("Q4_PLACE_FENCE", "Poser des barrieres", "Poser 5 barrieres.", ACTION_PLACE_FENCE, 5, 20, 20),
                Quest.createActionQuest("Q4_LEVEL_4", "Passer niveau 4", "Atteindre le niveau 4.", ACTION_REACH_LEVEL_4, 1, 30, 30)
        )));

        questLines.add(new QuestLine("Chapitre 5 - Maitrise totale", "Finaliser la ferme fraise et automatiser.", java.util.List.of(
                Quest.createHarvestQuest("Q5_HARVEST_FRAISE", "Recolter des fraises", "Recolter 20 fraises.", PlantType.FRAISE, 20, 30, 30),
                Quest.createActionQuest("Q5_CLICK_CROW", "Chasser des corbeaux", "Chasser 5 corbeaux (clic ou epouvantail).", ACTION_CLICK_CROW, 5, 20, 30),
                Quest.createBuildQuest("Q5_BUILD_SPRINKLER", "Poser un arroseur", "Poser un arroseur automatique.", Sprinkler.class, 1, 40, 35),
                Quest.createBuildQuest("Q5_BUILD_10", "Poser des batiments", "Poser 10 batiments.", null, 10, 40, 35),
                Quest.createActionQuest("Q5_LEVEL_5", "Passer niveau 5", "Atteindre le niveau 5.", ACTION_REACH_LEVEL_5, 1, 50, 40)
        )));

        // CHAPITRE 6 : Le "Endgame". Une fois fini, il se réinitialise pour donner des objectifs continus.
        questLines.add(new QuestLine(
                "Chapitre 6 - Les Defis",
                "Releves des defis infinis pour devenir un fermier ultimate !",
                java.util.List.of(
                        Quest.createActionQuest("Q6_CHASE_CHICKEN", "Chasser des poules", "Chasser 20 poules.", ACTION_CHASE_CHICKEN, 20, 30, 50),
                        Quest.createActionQuest("Q6_CHASE_CROW", "Chasser des corbeaux", "Chasser 20 corbeaux.", ACTION_CLICK_CROW, 20, 30, 50),
                        Quest.createHarvestQuest("Q6_HARVEST_FRAISE", "Recolter des fraises", "Recolter 50 fraises.", PlantType.FRAISE, 50, 30, 50),
                        Quest.createBuildQuest("Q6_BUILD_10", "Poser des batiments", "Poser 10 batiments.", null, 10, 30, 50),
                        Quest.createBuildQuest("Q6_BUILD_STATUE", "Poser une statue", "Poser une statue.", src.model.buildings.Statue.class, 1, 30, 50)
                )
        ));
    }

    public java.util.List<QuestLine> getQuestLines() {
        return java.util.Collections.unmodifiableList(questLines);
    }

    /** Permet à l'UI de s'abonner aux changements d'état des quêtes. */
    public void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
    }

    /** Récupère le chapitre actuel. Retourne null si le joueur a fini le jeu et qu'il n'y a plus de chapitre 6. */
    public QuestLine getActiveQuestLine() {
        if (activeLineIndex < 0 || activeLineIndex >= questLines.size()) {
            return null;
        }
        return questLines.get(activeLineIndex);
    }

    public int getActiveQuestLineIndex() { return activeLineIndex; }

    /** Indique si le joueur a dépassé le dernier chapitre disponible. */
    public boolean isFinished() { return activeLineIndex >= questLines.size(); }

    // Ces fonctions agissent comme des entonnoirs. Elles sont appelées depuis World.java.
    // Elles formatent la demande et l'envoient à la méthode centrale "handleEvent".

    public void onPlant(PlantType plantType, Stats stats) {
        handleEvent(Quest.Type.PLANT, plantType, null, stats);
    }

    public void onHarvest(PlantType plantType, Stats stats) {
        handleEvent(Quest.Type.HARVEST, plantType, null, stats);
    }

    public void onBuild(src.model.buildings.Building building, Stats stats) {
        handleEvent(Quest.Type.BUILD, null, building, stats);
    }

    public void onAction(String actionKey, Stats stats) {
        onAction(actionKey, stats, 1);
    }

    public void onAction(String actionKey, Stats stats, int amount) {
        handleEvent(Quest.Type.ACTION, null, null, actionKey, stats, amount);
    }

    /** Surcharge de confort pour simplifier les appels qui ne modifient qu'un élément (amount = 1). */
    private void handleEvent(Quest.Type type, PlantType plantType, src.model.buildings.Building building, Stats stats) {
        handleEvent(type, plantType, building, null, stats, 1);
    }

    /**
     * LE MOTEUR CENTRAL DE VALIDATION DES QUÊTES.
     * Évalue chaque événement survenu dans le jeu pour voir s'il correspond à un objectif en cours.
     * * @param type La catégorie d'action (PLANTER, RÉCOLTER, CONSTRUIRE, ACTION_GÉNÉRIQUE).
     * @param plantType Le type de graine/plante concernée (si applicable, sinon null).
     * @param building Le type de bâtiment concerné (si applicable, sinon null).
     * @param actionKey L'identifiant de l'action générique (si applicable, ex: ACTION_SELL_CAROTTE).
     * @param stats La référence au joueur (pour lui donner l'XP et l'or s'il réussit).
     * @param amount La quantité (ex: j'ai vendu 3 carottes d'un coup -> amount = 3).
     */
    private void handleEvent(Quest.Type type, PlantType plantType, src.model.buildings.Building building, String actionKey, Stats stats, int amount) {
        QuestLine line = getActiveQuestLine();

        // Si le jeu est fini ou le chapitre actuel est verrouillé, on ignore l'événement.
        if (line == null || !line.isUnlocked()) return;

        // On inspecte toutes les quêtes du chapitre actif
        for (Quest quest : line.quests) {

            // Switch Expression (Java 14+) : Aiguillage propre selon le type d'événement.
            // On demande à la quête : "Est-ce que l'événement qui vient de se produire est ce que tu attends ?"
            boolean matches = switch (type) {
                case PLANT -> quest.matchesPlantEvent(plantType);
                case HARVEST -> quest.matchesHarvestEvent(plantType);
                case BUILD -> quest.matchesBuildEvent(building);
                case ACTION -> quest.matchesActionEvent(actionKey);
            };

            // Si ça ne correspond pas, OU si la quête est DÉJÀ finie, on passe à la quête suivante.
            if (!matches || quest.isCompleted()) {
                continue;
            }

            // --- L'ÉVÉNEMENT EST VALIDE ---

            // On ajoute la progression (ex: 3/10 carottes).
            // addProgress() renvoie TRUE seulement au moment exact où la barre atteint 100%.
            if (quest.addProgress(amount)) {
                // Joue un effet sonore de réussite
                SoundManager.playSound(SoundManager.QUEST_COMPLETE);

                // Distribue les récompenses (XP, Argent).
                // grantReward renvoie TRUE s'il y a eu une distribution pour forcer le rafraîchissement UI.
                if (quest.grantReward(stats)) {
                    notifyChange();
                }
            }

            // VÉRIFICATION DU CHAPITRE & SÉCURITÉ RÉCURSIVE
            // Si la quête qu'on vient de faire avancer était la dernière du chapitre, isCompleted() passe à vrai.
            // Le "&& line == getActiveQuestLine()" est vital : il empêche le bug du "Double Saut".
            // Si la récompense (ex: XP) déclenche un Level Up, le Level Up va relancer handleEvent() (Récursion).
            // Sans cette sécurité, la fin du deuxième appel ferait passer au chapitre suivant, puis le retour
            // au premier appel ferait passer au chapitre d'APRÈS, sautant un chapitre entier !
            if (line.isCompleted() && line == getActiveQuestLine()) {
                unlockNextLine();
            }

            notifyChange();

            // Dès qu'on a trouvé et fait avancer la bonne quête, on s'arrête.
            // (Si plusieurs quêtes demandaient la même chose, seule la première de la liste avancera.
            // C'est un choix de conception logique pour obliger à les faire séquentiellement si besoin).
            return;
        }
    }

    /**
     * Gère la transition entre les chapitres.
     */
    private void unlockNextLine() {
        // RÈGLE SPÉCIALE DU CHAPITRE 6 (Boucle Endgame).
        // Au lieu de passer au Chapitre 7 (qui n'existe pas), on vide la progression du Chapitre 6
        // pour que le joueur puisse le refaire et farmer les récompenses.
        if (activeLineIndex == CHAPTER_6_INDEX) {
            resetChapter6();
            return;
        }

        // Si on n'est pas au bout de la liste, on incrémente le curseur de chapitre.
        if (activeLineIndex < questLines.size()) {
            activeLineIndex++;

            // On déverrouille le nouveau chapitre actif pour permettre d'y progresser.
            if (activeLineIndex < questLines.size()) {
                questLines.get(activeLineIndex).setUnlocked(true);
            }
        }
    }

    /**
     * Remet à zéro toutes les jauges de progression des quêtes du Chapitre 6.
     */
    private void resetChapter6() {
        if (CHAPTER_6_INDEX >= 0 && CHAPTER_6_INDEX < questLines.size()) {
            QuestLine chapter6 = questLines.get(CHAPTER_6_INDEX);
            for (Quest quest : chapter6.getQuests()) {
                quest.reset();
            }
        }
    }


    /**
     * SÉRIALISATION LÉGÈRE (Création d'une photo de l'état).
     * Prépare les données pour le fichier de sauvegarde (JSON ou autre).
     * Au lieu de sauvegarder les objets complexes, on extrait juste une matrice de nombres (List<List<Integer>>).
     * Structure :
     * [Chapitre 1] -> [Quest1_Progression, Quest2_Progression...]
     * [Chapitre 2] -> [Quest1_Progression, Quest2_Progression...]
     */
    public java.util.List<java.util.List<Integer>> getProgressSnapshot() {
        java.util.List<java.util.List<Integer>> snapshot = new java.util.ArrayList<>();

        for (QuestLine line : questLines) {
            java.util.List<Integer> lineProgress = new java.util.ArrayList<>();
            for (Quest quest : line.quests) {
                // On stocke simplement le nombre (ex: "8" pour 8/10 carottes)
                lineProgress.add(quest.getProgress());
            }
            snapshot.add(lineProgress); // On ajoute la liste du chapitre à la liste globale
        }
        return snapshot;
    }

    /**
     * DÉSÉRIALISATION (Restauration d'une sauvegarde).
     * * @param progresses La matrice de nombres générée précédemment par getProgressSnapshot().
     * @param restoredActiveLineIndex L'index du chapitre sur lequel le joueur s'était arrêté.
     */
    public void restoreProgress(java.util.List<java.util.List<Integer>> progresses, int restoredActiveLineIndex) {

        // ÉTAPE 1 : On parcourt toute l'arborescence par défaut pour injecter les chiffres sauvegardés.
        for (int i = 0; i < questLines.size(); i++) {
            QuestLine line = questLines.get(i);

            // On verrouille tout par défaut (sauf le premier chapitre). On déverrouillera plus tard.
            line.setUnlocked(i == 0);

            // On récupère les données de sauvegarde pour CE chapitre (si elles existent, sinon liste vide).
            java.util.List<Integer> lineProgress = (progresses != null && i < progresses.size()) ? progresses.get(i) : java.util.Collections.emptyList();

            for (int q = 0; q < line.quests.size(); q++) {
                Quest quest = line.quests.get(q);

                // Si la sauvegarde contient un chiffre pour cette quête, on le prend, sinon c'est 0.
                int restoredProgress = (q < lineProgress.size()) ? lineProgress.get(q) : 0;

                // On calcule mathématiquement si la quête était finie (Progression >= Objectif).
                boolean completed = restoredProgress >= quest.getGoal();

                // On force l'état de la quête sans lui redonner les récompenses (elles sont déjà dans les Stats du joueur !)
                quest.restoreState(restoredProgress, completed, completed);
            }
        }

        // Sécurité : si jamais on n'a pas chargé de quêtes, on remet à zéro et on arrête.
        if (questLines.isEmpty()) {
            activeLineIndex = 0;
            notifyChange();
            return;
        }

        // ÉTAPE 2 : Restauration du Chapitre Actif.
        // On s'assure que l'index sauvegardé n'est pas devenu aberrant (ex: un vieux fichier de sauvegarde sur un jeu mis à jour).
        activeLineIndex = Math.max(0, Math.min(restoredActiveLineIndex, questLines.size()));

        // On déverrouille tous les chapitres AVANT le chapitre actif
        // (pour que l'historique UI soit lisible).
        for (int i = 0; i < activeLineIndex && i < questLines.size(); i++) {
            questLines.get(i).setUnlocked(true);
        }

        // On déverrouille le chapitre en cours pour y jouer.
        if (activeLineIndex < questLines.size()) {
            questLines.get(activeLineIndex).setUnlocked(true);
        }

        // On demande à l'écran de se rafraîchir avec ces nouvelles données.
        notifyChange();
    }

    /**
     * Appelle le changeListener si la vue en a défini un.
     */
    private void notifyChange() {
        if (changeListener != null) {
            changeListener.run();
        }
    }
}
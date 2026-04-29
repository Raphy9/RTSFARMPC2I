package src.control.popups;

import src.view.Display;
import src.view.QuestPanelState;
import src.model.Quests;
import src.model.Quest;

import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Contrôleur responsable de la gestion du menu des quêtes (MVC).
 * Il intercepte les actions du joueur (clics sur les onglets de chapitres, ouverture du menu)
 * et met à jour la vue en lui fournissant un "snapshot" de l'état actuel du modèle.
 */
public class QuestMenuController implements ActionListener {

    // Référence à la vue globale pour pouvoir afficher ou masquer le panneau latéral
    private final Display display;

    // Garde en mémoire le chapitre actuellement consulté par le joueur dans l'interface
    private int selectedChapterIndex = 0;

    // Drapeau crucial pour l'UX : permet de savoir si le joueur a cliqué de lui-même sur un ancien chapitre.
    // Cela évite que l'interface le ramène de force sur le chapitre actif à chaque fois qu'une quête progresse.
    private boolean manualSelection = false;

    public QuestMenuController(Display display) {
        this.display = display;
    }

    /**
     * Méthode déclenchée lorsque le joueur clique sur le bouton d'ouverture du menu des quêtes.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        openMenu();
    }

    /**
     * Initialise les écouteurs d'événements. Cette méthode est appelée une seule fois au démarrage.
     */
    public void bind() {
        // Branchement du bouton principal situé dans l'interface globale
        display.getQuestMenuButton().addActionListener(this);

        // Branchement du bouton "Croix" pour fermer le panneau latéral
        display.getQuestSidePanel().getCloseButton().addActionListener(e -> closeMenu());

        // On s'abonne aux notifications du modèle de quêtes.
        // Dès qu'une quête avance (ex: le joueur récolte une carotte), le modèle appelle ce callback.
        // On utilise SwingUtilities.invokeLater pour garantir que le rafraîchissement visuel se fera
        // de manière sécurisée sur le thread graphique (Event Dispatch Thread).
        display.setQuestChangeCallback(() -> SwingUtilities.invokeLater(this::refreshFromModel));

        // Premier affichage pour initialiser l'état visuel du menu (même s'il est caché)
        refreshFromModel();
    }

    /**
     * Ouvre le menu et gère le ciblage automatique du bon chapitre.
     */
    public void openMenu() {
        Quests quests = display.getQuests();

        // Sécurité au cas où le système de quêtes ne serait pas encore chargé
        if (quests == null || quests.getQuestLines().isEmpty()) {
            display.showQuestPanel();
            refreshFromModel();
            return;
        }

        // À l'ouverture du menu, on annule la sélection manuelle.
        // Le comportement par défaut est de toujours montrer le chapitre actif (celui en cours) au joueur.
        manualSelection = false;
        selectedChapterIndex = quests.getActiveQuestLineIndex();

        display.showQuestPanel();
        refreshFromModel();
    }

    /**
     * Ferme le panneau et réinitialise l'état de navigation.
     */
    public void closeMenu() {
        manualSelection = false;
        display.hideQuestPanel();
    }

    /**
     * Cœur logique du contrôleur. Synchronise l'affichage avec les données réelles du jeu.
     */
    public void refreshFromModel() {
        Quests quests = display.getQuests();

        // Gestion gracieuse du cas où aucune donnée de quête n'existe
        if (quests == null || quests.getQuestLines().isEmpty()) {
            display.getQuestSidePanel().render(QuestPanelState.empty(
                    "Aucune quête disponible",
                    "Le système de quêtes n'a pas été initialisé."
            ));
            return;
        }

        int activeIndex = quests.getActiveQuestLineIndex();

        // Logique de ciblage : on décide quel chapitre afficher.
        // Si le joueur n'a pas cliqué manuellement sur un autre chapitre, on suit sa progression.
        if (!manualSelection) {
            selectedChapterIndex = activeIndex;
        }
        // Si le joueur avait sélectionné un chapitre manuellement, mais que ce chapitre est devenu
        // invalide (par exemple suite à un rechargement de sauvegarde), on le ramène sur le chapitre actif.
        else if (selectedChapterIndex > activeIndex) {
            selectedChapterIndex = activeIndex;
        }

        // On s'assure que l'index de sélection ne sort pas des limites de la liste (Out Of Bounds)
        selectedChapterIndex = Math.max(0, Math.min(selectedChapterIndex, quests.getQuestLines().size() - 1));

        // On construit un objet d'état (DTO) purement visuel et on l'envoie à la vue pour qu'elle se dessine
        display.getQuestSidePanel().render(buildState(quests, selectedChapterIndex, activeIndex));

        // Comme la vue vient d'être recréée (nouveaux boutons générés), on doit rebrancher nos écouteurs
        rewireChapterButtons();
    }

    /**
     * Reconnecte dynamiquement les actions aux boutons des onglets de chapitres générés par la vue.
     */
    private void rewireChapterButtons() {
        List<javax.swing.JButton> buttons = display.getQuestSidePanel().getChapterButtons();

        for (javax.swing.JButton button : buttons) {
            // La vue injecte l'index du chapitre dans la propriété "chapterIndex" du bouton Swing
            Object indexObj = button.getClientProperty("chapterIndex");

            // Si le bouton n'a pas d'index valide, on l'ignore
            if (!(indexObj instanceof Integer index)) {
                continue;
            }

            // Nettoyage des anciens écouteurs pour éviter que les actions ne se déclenchent en double
            // suite aux rafraîchissements successifs
            for (java.awt.event.ActionListener listener : button.getActionListeners()) {
                button.removeActionListener(listener);
            }

            // Ajout du nouvel écouteur qui appellera la méthode selectChapter avec le bon index
            button.addActionListener(evt -> selectChapter(index));
        }
    }

    /**
     * Invoquée quand le joueur clique sur un onglet de chapitre dans le menu.
     */
    public void selectChapter(int chapterIndex) {
        Quests quests = display.getQuests();
        if (quests == null || quests.getQuestLines().isEmpty()) {
            return;
        }

        int activeIndex = quests.getActiveQuestLineIndex();

        // Règle métier : On bloque la navigation vers les chapitres que le joueur n'a pas encore atteints.
        if (chapterIndex > activeIndex) {
            return;
        }

        // Sécurisation de l'index cliqué
        selectedChapterIndex = Math.max(0, Math.min(chapterIndex, quests.getQuestLines().size() - 1));

        // Si le joueur clique sur un ancien chapitre, on passe en sélection manuelle.
        // S'il clique sur le chapitre actuel, on repasse en sélection automatique.
        manualSelection = chapterIndex < activeIndex;

        // Mise à jour immédiate de l'interface graphique
        display.getQuestSidePanel().render(buildState(quests, selectedChapterIndex, activeIndex));
        rewireChapterButtons();
    }

    /**
     * Méthode de traduction : convertit les objets complexes du Modèle (Quests, QuestLine)
     * en objets simples et primitifs (QuestPanelState) pour la Vue.
     * C'est l'essence même de la séparation des responsabilités.
     */
    private QuestPanelState buildState(Quests quests, int selectedIndex, int activeIndex) {
        List<QuestPanelState.ChapterButtonState> buttons = new java.util.ArrayList<>();
        List<Quests.QuestLine> lines = quests.getQuestLines();

        // 1. Préparation de l'état des onglets de chapitres (Boutons latéraux)
        for (int i = 0; i < lines.size(); i++) {
            Quests.QuestLine line = lines.get(i);

            // Détermine si ce bouton doit apparaître visuellement comme "actif" ou "appuyé"
            boolean selected = i == selectedIndex;
            // Détermine si le bouton est cliquable (on bloque les chapitres futurs)
            boolean enabled = i <= activeIndex;

            // Formatage du texte de statut affiché sous le nom du chapitre
            String status = line.isCompleted()
                    ? "Terminé"
                    : (line.isUnlocked() ? "En cours" : "Verrouillé");

            buttons.add(new QuestPanelState.ChapterButtonState(
                    i,
                    "Chap " + (i + 1),
                    line.getTitle() + " - " + status,
                    selected,
                    enabled
            ));
        }

        // 2. Préparation du contenu central (Les quêtes du chapitre sélectionné)
        Quests.QuestLine selectedLine = lines.get(selectedIndex);
        boolean locked = !selectedLine.isUnlocked();

        List<QuestPanelState.QuestCardState> questsCards = new java.util.ArrayList<>();

        // Si le chapitre est déverrouillé, on extrait les informations de chaque quête
        if (!locked) {
            for (Quest quest : selectedLine.getQuests()) {
                questsCards.add(new QuestPanelState.QuestCardState(
                        quest.getTitle(),
                        quest.getDescription(),
                        quest.getProgress(),
                        quest.getGoal(),
                        quest.getRewardMoney(),
                        quest.getRewardExp(),
                        quest.isCompleted(),
                        quest.isRewardClaimed()
                ));
            }
        }

        // 3. Assemblage final
        // On regroupe les données du chapitre et la liste des cartes de quêtes
        QuestPanelState.ChapterState chapterState = new QuestPanelState.ChapterState(
                selectedLine.getTitle(),
                selectedLine.getDescription(),
                locked,
                questsCards
        );

        // Retourne le DTO final prêt à être consommé par la vue
        return QuestPanelState.of(buttons, chapterState);
    }
}
package src.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente l'état immuable du panneau de quêtes.
 * Cette classe sert de pont entre le modèle et la vue (Pattern State/ViewModel).
 * Elle contient toutes les informations textuelles et de progression prêtes à être affichées.
 */
public class QuestPanelState {
    private final List<ChapterButtonState> chapterButtons; // États des onglets (Chapitres)
    private final ChapterState chapter;                  // Détails du chapitre actuellement sélectionné
    private final String infoTitle;                       // Titre informatif (si le panneau est vide)
    private final String infoMessage;                     // Message informatif (si le panneau est vide)
    private final boolean isEmpty;                       // Indique s'il y a des données à afficher

    /** Constructeur privé : on utilise les méthodes statiques 'of' ou 'empty' pour instancier */
    private QuestPanelState(List<ChapterButtonState> chapterButtons, ChapterState chapter, String infoTitle, String infoMessage, boolean isEmpty) {
        this.chapterButtons = chapterButtons;
        this.chapter = chapter;
        this.infoTitle = infoTitle;
        this.infoMessage = infoMessage;
        this.isEmpty = isEmpty;
    }

    /** Crée un état valide avec des boutons et un chapitre sélectionné */
    public static QuestPanelState of(List<ChapterButtonState> chapterButtons, ChapterState chapter) {
        return new QuestPanelState(chapterButtons, chapter, null, null, false);
    }

    /** Crée un état "vide" (ex: aucune quête disponible) avec un message d'information */
    public static QuestPanelState empty(String title, String message) {
        return new QuestPanelState(new ArrayList<>(), null, title, message, true);
    }

    // --- Getters standard ---

    public List<ChapterButtonState> getChapterButtons() { return chapterButtons; }
    public ChapterState getChapter() { return chapter; }
    public String getInfoTitle() { return infoTitle; }
    public String getInfoMessage() { return infoMessage; }
    public boolean isEmpty() { return isEmpty; }

    /**
     * Représente l'état visuel d'un bouton d'onglet (Chapitre).
     */
    public static class ChapterButtonState {
        public final int index;      // Index du bouton (0, 1, 2...)
        public final String text;    // Texte affiché (ex: "Chapitre 1")
        public final String tooltip; // Texte d'aide au survol
        public final boolean selected; // Est-ce l'onglet actif ?
        public final boolean enabled;  // Est-ce que l'onglet est cliquable ?

        public ChapterButtonState(int index, String text, String tooltip, boolean selected, boolean enabled) {
            this.index = index;
            this.text = text;
            this.tooltip = tooltip;
            this.selected = selected;
            this.enabled = enabled;
        }
    }

    /**
     * Contenu complet d'un chapitre (Titre, description et liste des quêtes).
     */
    public static class ChapterState {
        public final String title;
        public final String description;
        public final boolean locked;      // Le chapitre est-il encore verrouillé ?
        public final List<QuestCardState> quests; // Liste des quêtes appartenant à ce chapitre

        public ChapterState(String title, String description, boolean locked, List<QuestCardState> quests) {
            this.title = title;
            this.description = description;
            this.locked = locked;
            this.quests = quests;
        }
    }

    /**
     * État individuel d'une carte de quête (progression, récompenses, statut).
     */
    public static class QuestCardState {
        public final String title;
        public final String description;
        public final int progress;       // Valeur actuelle (ex: 2 citrouilles)
        public final int goal;           // Objectif à atteindre (ex: 5 citrouilles)
        public final int rewardMoney;    // Gain en pièces d'or
        public final int rewardExp;      // Gain en expérience
        public final boolean completed;  // L'objectif est-il atteint ?
        public final boolean rewardClaimed; // La récompense a-t-elle déjà été récupérée ?

        public QuestCardState(String title, String description, int progress, int goal, int rewardMoney, int rewardExp, boolean completed) {
            this(title, description, progress, goal, rewardMoney, rewardExp, completed, false);
        }

        public QuestCardState(String title, String description, int progress, int goal, int rewardMoney, int rewardExp, boolean completed, boolean rewardClaimed) {
            this.title = title;
            this.description = description;
            this.progress = progress;
            this.goal = goal;
            this.rewardMoney = rewardMoney;
            this.rewardExp = rewardExp;
            this.completed = completed;
            this.rewardClaimed = rewardClaimed;
        }
    }
}
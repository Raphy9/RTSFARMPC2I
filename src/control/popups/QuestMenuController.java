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
 * Controleur du menu quetes.
 * Il relie les boutons de la vue aux operations de navigation de la Display,
 * sans laisser la vue embarquer la logique de controle.
 */
public class QuestMenuController implements ActionListener {
    private final Display display;
    private int selectedChapterIndex = 0;
    private boolean manualSelection = false;

    public QuestMenuController(Display display) {
        this.display = display;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        openMenu();
    }

    /**
     * Branche tous les listeners du menu quetes : ouverture, fermeture, navigation, refresh modele.
     */
    public void bind() {
        display.getQuestMenuButton().addActionListener(this);
        display.getQuestSidePanel().getCloseButton().addActionListener(e -> closeMenu());

        // Le modele notifie le controleur, qui rafraîchit ensuite la vue et rebranche les boutons.
        display.setQuestChangeCallback(() -> SwingUtilities.invokeLater(this::refreshFromModel));
        refreshFromModel();
    }

    public void openMenu() {
        Quests quests = display.getQuests();
        if (quests == null || quests.getQuestLines().isEmpty()) {
            display.showQuestPanel();
            refreshFromModel();
            return;
        }

        manualSelection = false;
        selectedChapterIndex = quests.getActiveQuestLineIndex();
        display.showQuestPanel();
        refreshFromModel();
    }

    public void closeMenu() {
        manualSelection = false;
        display.hideQuestPanel();
    }

    public void refreshFromModel() {
        Quests quests = display.getQuests();
        if (quests == null || quests.getQuestLines().isEmpty()) {
            display.getQuestSidePanel().render(QuestPanelState.empty(
                    "Aucune quete disponible",
                    "Le systeme de quetes n'a pas ete initialise."
            ));
            return;
        }

        int activeIndex = quests.getActiveQuestLineIndex();
        if (!manualSelection) {
            selectedChapterIndex = activeIndex;
        } else if (selectedChapterIndex > activeIndex) {
            selectedChapterIndex = activeIndex;
        }

        selectedChapterIndex = Math.max(0, Math.min(selectedChapterIndex, quests.getQuestLines().size() - 1));
        display.getQuestSidePanel().render(buildState(quests, selectedChapterIndex, activeIndex));
        rewireChapterButtons();
    }

    private void rewireChapterButtons() {
        List<javax.swing.JButton> buttons = display.getQuestSidePanel().getChapterButtons();
        for (javax.swing.JButton button : buttons) {
            Object indexObj = button.getClientProperty("chapterIndex");
            if (!(indexObj instanceof Integer index)) {
                continue;
            }

            for (java.awt.event.ActionListener listener : button.getActionListeners()) {
                button.removeActionListener(listener);
            }
            button.addActionListener(evt -> selectChapter(index));
        }
    }

    public void selectChapter(int chapterIndex) {
        Quests quests = display.getQuests();
        if (quests == null || quests.getQuestLines().isEmpty()) {
            return;
        }

        int activeIndex = quests.getActiveQuestLineIndex();
        if (chapterIndex > activeIndex) {
            return; // chapitres futurs verrouilles
        }

        selectedChapterIndex = Math.max(0, Math.min(chapterIndex, quests.getQuestLines().size() - 1));
        manualSelection = chapterIndex < activeIndex;
        display.getQuestSidePanel().render(buildState(quests, selectedChapterIndex, activeIndex));
        rewireChapterButtons();
    }

    private QuestPanelState buildState(Quests quests, int selectedIndex, int activeIndex) {
        List<QuestPanelState.ChapterButtonState> buttons = new java.util.ArrayList<>();
        List<Quests.QuestLine> lines = quests.getQuestLines();

        for (int i = 0; i < lines.size(); i++) {
            Quests.QuestLine line = lines.get(i);
            boolean selected = i == selectedIndex;
            boolean enabled = i <= activeIndex;
            String status = line.isCompleted()
                    ? "Termine"
                    : (line.isUnlocked() ? "En cours" : "Verrouille");
            buttons.add(new QuestPanelState.ChapterButtonState(
                    i,
                    "Chap " + (i + 1),
                    line.getTitle() + " - " + status,
                    selected,
                    enabled
            ));
        }

        Quests.QuestLine selectedLine = lines.get(selectedIndex);
        boolean locked = !selectedLine.isUnlocked();
        List<QuestPanelState.QuestCardState> questsCards = new java.util.ArrayList<>();
        if (!locked) {
            for (Quest quest : selectedLine.getQuests()) {
                questsCards.add(new QuestPanelState.QuestCardState(
                        quest.getTitle(),
                        quest.getDescription(),
                        quest.getProgress(),
                        quest.getGoal(),
                        quest.getRewardMoney(),
                        quest.getRewardExp(),
                        quest.isCompleted()
                ));
            }
        }

        QuestPanelState.ChapterState chapterState = new QuestPanelState.ChapterState(
                selectedLine.getTitle(),
                selectedLine.getDescription(),
                locked,
                questsCards
        );
        return QuestPanelState.of(buttons, chapterState);
    }
}


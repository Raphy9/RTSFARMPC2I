package src.view;

import java.util.ArrayList;
import java.util.List;

public class QuestPanelState {
    private final List<ChapterButtonState> chapterButtons;
    private final ChapterState chapter;
    private final String infoTitle;
    private final String infoMessage;
    private final boolean isEmpty;

    private QuestPanelState(List<ChapterButtonState> chapterButtons, ChapterState chapter, String infoTitle, String infoMessage, boolean isEmpty) {
        this.chapterButtons = chapterButtons;
        this.chapter = chapter;
        this.infoTitle = infoTitle;
        this.infoMessage = infoMessage;
        this.isEmpty = isEmpty;
    }

    public static QuestPanelState of(List<ChapterButtonState> chapterButtons, ChapterState chapter) {
        return new QuestPanelState(chapterButtons, chapter, null, null, false);
    }

    public static QuestPanelState empty(String title, String message) {
        return new QuestPanelState(new ArrayList<>(), null, title, message, true);
    }

    public List<ChapterButtonState> getChapterButtons() {
        return chapterButtons;
    }

    public ChapterState getChapter() {
        return chapter;
    }

    public String getInfoTitle() {
        return infoTitle;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public static class ChapterButtonState {
        public final int index;
        public final String text;
        public final String tooltip;
        public final boolean selected;
        public final boolean enabled;

        public ChapterButtonState(int index, String text, String tooltip, boolean selected, boolean enabled) {
            this.index = index;
            this.text = text;
            this.tooltip = tooltip;
            this.selected = selected;
            this.enabled = enabled;
        }
    }

    public static class ChapterState {
        public final String title;
        public final String description;
        public final boolean locked;
        public final List<QuestCardState> quests;

        public ChapterState(String title, String description, boolean locked, List<QuestCardState> quests) {
            this.title = title;
            this.description = description;
            this.locked = locked;
            this.quests = quests;
        }
    }

    public static class QuestCardState {
        public final String title;
        public final String description;
        public final int progress;
        public final int goal;
        public final int rewardMoney;
        public final int rewardExp;
        public final boolean completed;
        public final boolean rewardClaimed;

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


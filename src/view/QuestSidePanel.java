package src.view;

import src.model.Quest;
import src.model.Quests;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Panneau latéral des quêtes.
 * - en haut : chapitres miniatures centrés avec marges propres
 * - au centre : le chapitre sélectionné affiché plus grand
 * - les chapitres futurs restent verrouillés
 */
public class QuestSidePanel extends JPanel {
    private static final int CHAPTER_SLOTS = 4;
    private static final int QUEST_SLOTS = 5;
    private static final int SECTION_GAP = 14;
    private static final int CHAPTER_STRIP_H_GAP = 8;
    private static final int CHAPTER_STRIP_V_GAP = 10;
    private static final int CHAPTER_BUTTON_BASE_HEIGHT = 44;

    private final Quests quests;
    private final JPanel contentPanel;
    private final JButton closeButton;
    private final List<JButton> chapterButtons = new ArrayList<>();
    private int selectedChapterIndex = 0;

    public QuestSidePanel(Quests quests) {
        this.quests = quests;

        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(15, 15, 5, 15));

        JLabel title = new JLabel("Quêtes", SwingConstants.CENTER);
        title.setFont(GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 22f)
                : new Font("Arial", Font.BOLD, 22));
        title.setForeground(PopupPanel.SDV_TEXT);
        topBar.add(title, BorderLayout.CENTER);

        closeButton = new JButton("X");
        closeButton.setFont(GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 16f)
                : new Font("Arial", Font.BOLD, 16));
        closeButton.setBackground(new Color(210, 60, 50));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));

        JPanel closeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closeWrap.setOpaque(false);
        closeWrap.add(closeButton);
        topBar.add(closeWrap, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        add(contentPanel, BorderLayout.CENTER);

        refresh();
    }

    public JButton getCloseButton() {
        return closeButton;
    }

    public void setSelectedChapterIndex(int selectedChapterIndex) {
        this.selectedChapterIndex = clampIndex(selectedChapterIndex);
    }

    public int getSelectedChapterIndex() {
        return selectedChapterIndex;
    }

    public List<JButton> getChapterButtons() {
        return Collections.unmodifiableList(chapterButtons);
    }

    /**
     * Reconstruit toute la liste à partir de l'état courant du moteur de quêtes.
     */
    public void refresh() {
        contentPanel.removeAll();
        chapterButtons.clear();

        if (quests == null || quests.getQuestLines().isEmpty()) {
            contentPanel.add(createInfoCard("Aucune quête disponible", "Le système de quêtes n'a pas été initialisé."), BorderLayout.CENTER);
        } else {
            int activeIndex = quests.getActiveQuestLineIndex();
            selectedChapterIndex = clampIndex(selectedChapterIndex);
            if (selectedChapterIndex > activeIndex) {
                selectedChapterIndex = activeIndex;
            }

            // Revalidation/peinture uniquement du contentPanel
            contentPanel.add(createChapterStrip(activeIndex), BorderLayout.NORTH);
            JPanel centerContent = new JPanel(new BorderLayout());
            centerContent.setOpaque(false);
            centerContent.setBorder(new EmptyBorder(SECTION_GAP, 0, 0, 0));
            centerContent.add(createSelectedChapterCard(selectedChapterIndex, activeIndex), BorderLayout.CENTER);
            contentPanel.add(centerContent, BorderLayout.CENTER);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void render(QuestPanelState state) {
        contentPanel.removeAll();
        chapterButtons.clear();

        if (state.isEmpty()) {
            contentPanel.add(createInfoCard(state.getInfoTitle(), state.getInfoMessage()), BorderLayout.CENTER);
        } else {
            int columns = 2;
            int totalSlots = Math.max(CHAPTER_SLOTS, state.getChapterButtons().size());
            int rows = Math.max(1, (int) Math.ceil(totalSlots / (double) columns));
            JPanel chapterStrip = new JPanel(new GridLayout(rows, columns, CHAPTER_STRIP_H_GAP, CHAPTER_STRIP_V_GAP));
            chapterStrip.setOpaque(false);
            chapterStrip.setBorder(new EmptyBorder(0, 0, 0, 0));

            int panelWidth = getQuestPanelInnerWidth();
            int buttonWidth = Math.max(80, (panelWidth - CHAPTER_STRIP_H_GAP * (columns - 1)) / columns);
            int buttonHeight = Math.max(18, CHAPTER_BUTTON_BASE_HEIGHT / 2);

            for (int i = 0; i < rows * columns; i++) {
                if (i < state.getChapterButtons().size()) {
                    QuestPanelState.ChapterButtonState btnState = state.getChapterButtons().get(i);
                    JButton btn = createButtonFromState(btnState, buttonWidth, buttonHeight);
                    chapterButtons.add(btn);
                    chapterStrip.add(btn);
                } else {
                    chapterStrip.add(createInvisibleSlot(new Dimension(buttonWidth, buttonHeight)));
                }
            }

            contentPanel.add(chapterStrip, BorderLayout.NORTH);

            JPanel centerContent = new JPanel(new BorderLayout());
            centerContent.setOpaque(false);
            centerContent.setBorder(new EmptyBorder(SECTION_GAP, 0, 0, 0));

            QuestPanelState.ChapterState chapter = state.getChapter();
            if (chapter.locked) {
                JPanel card = new JPanel(new GridLayout(QUEST_SLOTS, 1, 0, 8));
                card.setOpaque(false);
                card.add(createInfoCard("Chapitre verrouillé", "Ce chapitre n'est pas encore accessible."));
                for (int i = 1; i < QUEST_SLOTS; i++) {
                    card.add(createInvisibleSlot());
                }
                centerContent.add(card, BorderLayout.CENTER);
            } else {
                JPanel card = new JPanel(new GridLayout(QUEST_SLOTS, 1, 0, 8));
                card.setOpaque(false);

                for (QuestPanelState.QuestCardState questState : chapter.quests) {
                    card.add(createQuestCardFromState(questState));
                }
                for (int i = chapter.quests.size(); i < QUEST_SLOTS; i++) {
                    card.add(createInvisibleSlot());
                }
                centerContent.add(card, BorderLayout.CENTER);
            }

            contentPanel.add(centerContent, BorderLayout.CENTER);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JComponent createChapterStrip(int activeIndex) {
        List<Quests.QuestLine> lines = quests.getQuestLines();
        int columns = 3;
        int totalSlots = Math.max(CHAPTER_SLOTS, lines.size());
        int rows = Math.max(1, (int) Math.ceil(totalSlots / (double) columns));
        int panelWidth = getQuestPanelInnerWidth();
        int buttonWidth = Math.max(80, (panelWidth - CHAPTER_STRIP_H_GAP * (columns - 1)) / columns);
        int buttonHeight = Math.max(18, CHAPTER_BUTTON_BASE_HEIGHT / 2);
        JPanel strip = new JPanel(new GridLayout(rows, columns, CHAPTER_STRIP_H_GAP, CHAPTER_STRIP_V_GAP));
        strip.setOpaque(false);
        strip.setBorder(new EmptyBorder(0, 0, 0, 0));

        for (int i = 0; i < rows * columns; i++) {
            if (i < lines.size()) {
                Quests.QuestLine line = lines.get(i);
                JButton btn = createChapterButton(line, i, activeIndex, buttonWidth, buttonHeight);
                chapterButtons.add(btn);
                strip.add(btn);
            } else {
                strip.add(createInvisibleSlot(new Dimension(buttonWidth, buttonHeight)));
            }
        }
        return strip;
    }

    private JButton createChapterButton(Quests.QuestLine line, int index, int activeIndex, int width, int height) {
        boolean selected = index == selectedChapterIndex;
        boolean futureLocked = index > activeIndex;

        JButton button = new JButton();
        button.putClientProperty("chapterIndex", index);
        button.setText("Chap " + (index + 1));
        button.setToolTipText(line.getTitle());
        button.setFocusable(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setFont(GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(selected ? Font.BOLD : Font.PLAIN, selected ? 13f : 12f)
                : new Font("Arial", selected ? Font.BOLD : Font.PLAIN, selected ? 13 : 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selected ? new Color(160, 100, 40) : PopupPanel.SDV_BORDER_DARK, selected ? 3 : 2),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        if (futureLocked) {
            button.setEnabled(false);
            button.setBackground(new Color(175, 170, 165));
            button.setForeground(new Color(120, 110, 100));
        } else if (selected) {
            button.setEnabled(true);
            button.setBackground(new Color(250, 220, 150));
            button.setForeground(PopupPanel.SDV_TEXT);
        } else {
            button.setEnabled(true);
            button.setBackground(new Color(235, 185, 120));
            button.setForeground(PopupPanel.SDV_TEXT);
        }

        Dimension size = new Dimension(width, height);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        return button;
    }

    private String buildChapterButtonHtml(Quests.QuestLine line, boolean selected) {
        String status = line.isCompleted()
                ? "Terminé"
                : (line.isUnlocked() ? "En cours" : "Verrouillé");

        if (selected) {
            return "<html><center><b>" + line.getTitle() + "</b><br><span style='font-size:10px;'>" + status + "</span></center></html>";
        }
        return "<html><center>" + line.getTitle() + "<br><span style='font-size:10px;'>" + status + "</span></center></html>";
    }

    private JComponent createSelectedChapterCard(int selectedIndex, int activeIndex) {
        List<Quests.QuestLine> lines = quests.getQuestLines();
        if (lines.isEmpty()) {
            return createInfoCard("Aucune quête disponible", "Le système de quêtes n'a pas été initialisé.");
        }

        int safeIndex = clampIndex(selectedIndex);
        Quests.QuestLine line = lines.get(Math.min(safeIndex, lines.size() - 1));

        if (!line.isUnlocked()) {
            JPanel card = new JPanel(new GridLayout(QUEST_SLOTS, 1, 0, 8));
            card.setOpaque(false);
            card.add(createInfoCard("Chapitre verrouillé", "Ce chapitre n'est pas encore accessible."));
            for (int i = 1; i < QUEST_SLOTS; i++) {
                card.add(createInvisibleSlot());
            }
            return card;
        }

        JPanel card = new JPanel();
        card.setLayout(new GridLayout(QUEST_SLOTS, 1, 0, 8));
        card.setOpaque(false);

        int shown = 0;
        for (Quest quest : line.getQuests()) {
            if (shown >= QUEST_SLOTS) {
                break;
            }
            card.add(createQuestCard(quest));
            shown++;
        }
        for (int i = shown; i < QUEST_SLOTS; i++) {
            card.add(createInvisibleSlot());
        }

        return card;
    }

    private JComponent createInvisibleSlot() {
        return createInvisibleSlot(null);
    }

    private JComponent createInvisibleSlot(Dimension size) {
        JPanel slot = new JPanel();
        slot.setOpaque(false);
        if (size != null) {
            slot.setPreferredSize(size);
            slot.setMinimumSize(size);
            slot.setMaximumSize(size);
        }
        return slot;
    }

    private String buildLineState(Quests.QuestLine line, int selectedIndex, int activeIndex) {
        if (!line.isUnlocked()) {
            return "Chapitre bloqué";
        }
        if (selectedIndex == activeIndex) {
            return line.isCompleted()
                    ? "Chapitre actuel terminé"
                    : "Chapitre en cours";
        }
        return line.isCompleted()
                ? "Chapitre précédent terminé"
                : "Chapitre précédent accessible";
    }

    private JComponent createQuestCard(Quest quest) {
        JPanel questCard = new JPanel(new BorderLayout(4, 2));
        questCard.setOpaque(true);
        questCard.setBackground(quest.isCompleted() ? new Color(204, 236, 186) : new Color(250, 236, 210));
        questCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 80, 40), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        questCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(quest.getTitle());
        title.setForeground(PopupPanel.SDV_TEXT);
        title.setFont(GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 12f)
                : new Font("Arial", Font.BOLD, 12));
        questCard.add(title, BorderLayout.NORTH);

        int wrapWidth = Math.max(120, getQuestPanelInnerWidth() - 36);
        String details = "<html><body style='width:" + wrapWidth + "px;'>"
                + quest.getDescription()
                + " <span style='color:rgb(80,40,10);'>[" + quest.getProgress() + "/" + quest.getGoal() + "]</span>"
                + "<br><b>Gain:</b> " + quest.getRewardMoney() + " PO, " + quest.getRewardExp() + " XP"
                + (quest.isCompleted() ? " <i>(Terminée)</i>" : "")
                + "</body></html>";
        JLabel desc = new JLabel(details);
        desc.setForeground(new Color(75, 35, 10));
        desc.setFont(GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(10f)
                : new Font("Arial", Font.PLAIN, 10));
        questCard.add(desc, BorderLayout.CENTER);

        return questCard;
    }

    private JComponent createInfoCard(String title, String message) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(new Color(235, 225, 210));
        card.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 1));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel t = new JLabel(title);
        t.setBorder(new EmptyBorder(8, 10, 2, 10));
        t.setForeground(PopupPanel.SDV_TEXT);
        t.setFont(GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 13f)
                : new Font("Arial", Font.BOLD, 13));
        card.add(t, BorderLayout.NORTH);

        JLabel m = new JLabel(wrapHtml(message));
        m.setBorder(new EmptyBorder(0, 10, 8, 10));
        m.setForeground(new Color(75, 35, 10));
        m.setFont(GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(11f)
                : new Font("Arial", Font.PLAIN, 11));
        card.add(m, BorderLayout.CENTER);

        return card;
    }

    private String wrapHtml(String text) {
        int wrapWidth = Math.max(120, getQuestPanelInnerWidth() - 28);
        return "<html><body style='width:" + wrapWidth + "px;'>" + text + "</body></html>";
    }

    private int getQuestPanelInnerWidth() {
        int panelWidth = contentPanel != null ? contentPanel.getWidth() : 0;
        int sourceWidth = panelWidth > 0 ? panelWidth : getWidth();
        Insets insets = contentPanel != null ? contentPanel.getInsets() : new Insets(0, 0, 0, 0);
        return Math.max(120, sourceWidth - insets.left - insets.right);
    }

    private int clampIndex(int index) {
        if (quests == null || quests.getQuestLines().isEmpty()) {
            return 0;
        }
        return Math.max(0, Math.min(index, quests.getQuestLines().size() - 1));
    }

    private JButton createButtonFromState(QuestPanelState.ChapterButtonState state, int width, int height) {
        JButton button = new JButton(state.text);
        button.putClientProperty("chapterIndex", state.index);
        button.setToolTipText(state.tooltip);
        button.setFocusable(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setFont(GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(state.selected ? Font.BOLD : Font.PLAIN, state.selected ? 13f : 12f)
                : new Font("Arial", state.selected ? Font.BOLD : Font.PLAIN, state.selected ? 13 : 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(state.selected ? new Color(160, 100, 40) : PopupPanel.SDV_BORDER_DARK, state.selected ? 3 : 2),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        button.setEnabled(state.enabled);
        button.setBackground(state.enabled ? (state.selected ? new Color(250, 220, 150) : new Color(235, 185, 120)) : new Color(175, 170, 165));
        button.setForeground(state.enabled ? PopupPanel.SDV_TEXT : new Color(120, 110, 100));

        Dimension size = new Dimension(width, height);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        return button;
    }

    private JComponent createQuestCardFromState(QuestPanelState.QuestCardState state) {
        JPanel questCard = new JPanel(new BorderLayout(4, 2));
        questCard.setOpaque(true);
        questCard.setBackground(state.completed ? new Color(204, 236, 186) : new Color(250, 236, 210));
        questCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 80, 40), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        questCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(state.title);
        title.setForeground(PopupPanel.SDV_TEXT);
        title.setFont(GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 12f)
                : new Font("Arial", Font.BOLD, 12));
        questCard.add(title, BorderLayout.NORTH);

        int wrapWidth = Math.max(120, getQuestPanelInnerWidth() - 36);
        String details = "<html><body style='width:" + wrapWidth + "px;'>"
                + state.description
                + " <span style='color:rgb(80,40,10);'>[" + state.progress + "/" + state.goal + "]</span>"
                + "<br><b>Gain:</b> " + state.rewardMoney + " PO, " + state.rewardExp + " XP"
                + (state.completed ? " <i>(Terminée)</i>" : "")
                + "</body></html>";
        JLabel desc = new JLabel(details);
        desc.setForeground(new Color(75, 35, 10));
        desc.setFont(GameFonts.MINECRAFT_FONT != null
                ? GameFonts.MINECRAFT_FONT.deriveFont(10f)
                : new Font("Arial", Font.PLAIN, 10));
        questCard.add(desc, BorderLayout.CENTER);

        return questCard;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        int b = 4;

        g2.setColor(PopupPanel.SDV_BORDER_DARK);
        g2.fillRect(0, 0, w, h);
        g2.setColor(PopupPanel.SDV_BORDER_LIGHT);
        g2.fillRect(b, b, w - b * 2, h - b * 2);
        g2.setColor(PopupPanel.SDV_BG);
        g2.fillRect(b * 2, b * 2, w - b * 4, h - b * 4);
        g2.dispose();
    }
}

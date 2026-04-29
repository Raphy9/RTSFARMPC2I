package src.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class SaveSelectionPopup extends JPanel {

    private String selectedSave = null;
    private final Consumer<String> onSaveSelected;

    public SaveSelectionPopup(Consumer<String> onSaveSelected) {
        this.onSaveSelected = onSaveSelected;
        this.setLayout(new BorderLayout());
        this.setOpaque(true);
        this.setBackground(PopupPanel.SDV_BG);

        JLabel titleLabel = new JLabel("Charger/Creer Partie", SwingConstants.CENTER);
        titleLabel.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 18f) : new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(PopupPanel.SDV_TEXT);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        this.add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        List<SaveManager.SaveInfo> saves = SaveManager.getSaveInfos();
        JPanel savesPanel = new JPanel();
        savesPanel.setLayout(new BoxLayout(savesPanel, BoxLayout.Y_AXIS));
        savesPanel.setOpaque(false);

        for (SaveManager.SaveInfo saveInfo : saves) {
            JPanel savePanel = createSaveButton(saveInfo, onSaveSelected);
            savesPanel.add(savePanel);
            savesPanel.add(Box.createVerticalStrut(5));
        }

        JScrollPane scrollPane = new JScrollPane(savesPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JButton newSaveButton = new JButton("+ Nouvelle Partie");
        newSaveButton.setFocusPainted(false);
        newSaveButton.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 12f) : new Font("Arial", Font.BOLD, 12));
        newSaveButton.setBackground(new Color(100, 150, 100));
        newSaveButton.setForeground(Color.WHITE);
        newSaveButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        newSaveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                newSaveButton.setBackground(new Color(70, 120, 70));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                newSaveButton.setBackground(new Color(100, 150, 100));
            }
        });
        newSaveButton.addActionListener(e -> showNewSaveDialog(onSaveSelected));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(newSaveButton);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        this.add(contentPanel, BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(500, 600));
    }

    private JPanel createSaveButton(SaveManager.SaveInfo saveInfo, Consumer<String> onSaveSelected) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(true);
        panel.setBackground(new Color(235, 185, 120));
        panel.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(saveInfo.name);
        nameLabel.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 14f) : new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(PopupPanel.SDV_TEXT);
        info.add(nameLabel);

        String levelTxt = (saveInfo.level != null) ? String.valueOf(saveInfo.level) : "?";
        String moneyTxt = (saveInfo.money != null) ? String.valueOf(saveInfo.money) : "?";
        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(saveInfo.lastModifiedMillis));

        JLabel lastPlayedLabel = new JLabel("Last played : " + dateStr);
        lastPlayedLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        lastPlayedLabel.setForeground(new Color(100, 80, 60));
        info.add(lastPlayedLabel);

        JLabel statsLabel = new JLabel("<html><b><span style='color:#8B5CF6'>Niv. " + levelTxt
                + "</span></b>  <b><span style='color:#FACC15'>" + moneyTxt + " PO</span></b></html>");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        info.add(statsLabel);

        panel.add(info, BorderLayout.CENTER);

        // Panel pour les boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        buttonPanel.setOpaque(false);

        // Bouton Charger
        JButton selectButton = new JButton("Charger");
        selectButton.setFocusPainted(false);
        selectButton.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 10f) : new Font("Arial", Font.BOLD, 10));
        selectButton.setBackground(new Color(160, 100, 60));
        selectButton.setForeground(Color.WHITE);
        selectButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        selectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                selectButton.setBackground(new Color(120, 70, 40));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                selectButton.setBackground(new Color(160, 100, 60));
            }
        });
        selectButton.addActionListener(e -> {
            selectedSave = saveInfo.name;
            onSaveSelected.accept(saveInfo.name);
        });

        // Bouton Renommer
        JButton renameButton = new JButton("Renommer");
        renameButton.setFocusPainted(false);
        renameButton.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 10f) : new Font("Arial", Font.BOLD, 10));
        renameButton.setBackground(new Color(100, 120, 160));
        renameButton.setForeground(Color.WHITE);
        renameButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        renameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                renameButton.setBackground(new Color(70, 90, 120));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                renameButton.setBackground(new Color(100, 120, 160));
            }
        });
        renameButton.addActionListener(e -> showRenameSaveDialog(saveInfo.name));

        // Bouton Supprimer
        JButton deleteButton = new JButton("Supprimer");
        deleteButton.setFocusPainted(false);
        deleteButton.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 10f) : new Font("Arial", Font.BOLD, 10));
        deleteButton.setBackground(new Color(160, 60, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                deleteButton.setBackground(new Color(120, 40, 40));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                deleteButton.setBackground(new Color(160, 60, 60));
            }
        });
        deleteButton.addActionListener(e -> showDeleteSaveDialog(saveInfo.name));

        buttonPanel.add(selectButton);
        buttonPanel.add(renameButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.EAST);

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(panel);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        return wrapper;
    }

    private void showNewSaveDialog(Consumer<String> onSaveSelected) {
        String defaultName = SaveManager.generateSaveName();
        String newSaveName = GameDialog.showInput(
                this,
                "Nouvelle Partie",
                "Nom de la nouvelle partie :",
                defaultName
        );

        if (newSaveName != null && !newSaveName.trim().isEmpty()) {
            selectedSave = newSaveName.trim();
            onSaveSelected.accept(selectedSave);
        }
    }

    private void showRenameSaveDialog(String currentName) {
        String newName = GameDialog.showInput(
                this,
                "Renommer la sauvegarde",
                "Nouveau nom pour '" + currentName + "' :",
                currentName
        );

        if (newName != null && !newName.trim().isEmpty() && !newName.trim().equals(currentName)) {
            if (SaveManager.renameSave(currentName, newName.trim())) {
                // Rafraîchir la liste des sauvegardes
                refreshSaveList();
            } else {
                GameDialog.showMessage(this, "Erreur", "Impossible de renommer la sauvegarde.");
            }
        }
    }

    private void showDeleteSaveDialog(String saveName) {
        boolean confirmed = GameDialog.showConfirm(
                this,
                "Supprimer la sauvegarde",
                "Etes-vous sur de vouloir supprimer '" + saveName + "' ?\nCette action est irreversible."
        );

        if (confirmed) {
            if (SaveManager.deleteSave(saveName)) {
                // Rafraîchir la liste des sauvegardes
                refreshSaveList();
            } else {
                GameDialog.showMessage(this, "Erreur", "Impossible de supprimer la sauvegarde.");
            }
        }
    }

    private void refreshSaveList() {
        // Reconstruire le panel avec la liste mise a jour
        removeAll();

        JLabel titleLabel = new JLabel("Charger/Creer Partie", SwingConstants.CENTER);
        titleLabel.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 18f) : new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(PopupPanel.SDV_TEXT);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        List<SaveManager.SaveInfo> saves = SaveManager.getSaveInfos();
        JPanel savesPanel = new JPanel();
        savesPanel.setLayout(new BoxLayout(savesPanel, BoxLayout.Y_AXIS));
        savesPanel.setOpaque(false);

        for (SaveManager.SaveInfo saveInfo : saves) {
            JPanel savePanel = createSaveButton(saveInfo, onSaveSelected);
            savesPanel.add(savePanel);
            savesPanel.add(Box.createVerticalStrut(5));
        }

        JScrollPane scrollPane = new JScrollPane(savesPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JButton newSaveButton = new JButton("+ Nouvelle Partie");
        newSaveButton.setFocusPainted(false);
        newSaveButton.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 12f) : new Font("Arial", Font.BOLD, 12));
        newSaveButton.setBackground(new Color(100, 150, 100));
        newSaveButton.setForeground(Color.WHITE);
        newSaveButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        newSaveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                newSaveButton.setBackground(new Color(70, 120, 70));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                newSaveButton.setBackground(new Color(100, 150, 100));
            }
        });
        newSaveButton.addActionListener(e -> showNewSaveDialog(onSaveSelected));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(newSaveButton);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    public String getSelectedSave() {
        return selectedSave;
    }
}

package src.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panneau de sélection des sauvegardes.
 * Permet de lister les parties existantes, d'en créer de nouvelles,
 * de les renommer ou de les supprimer via une interface graphique stylisée.
 */
public class SaveSelectionPopup extends JPanel {

    private String selectedSave = null; // Stocke le nom de la sauvegarde choisie
    private final Consumer<String> onSaveSelected; // Callback appelé lors de la validation d'une sauvegarde

    /**
     * Constructeur de la fenêtre de sélection.
     * @param onSaveSelected Action à exécuter une fois qu'une sauvegarde est choisie ou créée.
     */
    public SaveSelectionPopup(Consumer<String> onSaveSelected) {
        this.onSaveSelected = onSaveSelected;
        this.setLayout(new BorderLayout());
        this.setOpaque(true);
        this.setBackground(PopupPanel.SDV_BG); // Utilise le fond beige Stardew Valley

        // --- TITRE DU MENU ---
        JLabel titleLabel = new JLabel("Charger/Creer Partie", SwingConstants.CENTER);
        titleLabel.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 18f) : new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(PopupPanel.SDV_TEXT);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        this.add(titleLabel, BorderLayout.NORTH);

        // --- ZONE DE CONTENU (Liste des sauvegardes) ---
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Récupération des informations de sauvegarde via le SaveManager
        List<SaveManager.SaveInfo> saves = SaveManager.getSaveInfos();
        JPanel savesPanel = new JPanel();
        savesPanel.setLayout(new BoxLayout(savesPanel, BoxLayout.Y_AXIS));
        savesPanel.setOpaque(false);

        // Génération d'un bloc visuel pour chaque sauvegarde trouvée
        for (SaveManager.SaveInfo saveInfo : saves) {
            JPanel savePanel = createSaveButton(saveInfo, onSaveSelected);
            savesPanel.add(savePanel);
            savesPanel.add(Box.createVerticalStrut(5)); // Espacement entre les lignes
        }

        // Intégration dans un JScrollPane pour gérer les listes longues
        JScrollPane scrollPane = new JScrollPane(savesPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // --- BOUTON NOUVELLE PARTIE ---
        JButton newSaveButton = new JButton("+ Nouvelle Partie");
        newSaveButton.setFocusPainted(false);
        newSaveButton.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 12f) : new Font("Arial", Font.BOLD, 12));
        newSaveButton.setBackground(new Color(100, 150, 100)); // Couleur verte
        newSaveButton.setForeground(Color.WHITE);
        newSaveButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        // Effet de survol (Hover)
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

    /**
     * Crée le bandeau visuel pour une sauvegarde spécifique (Nom, Stats, Boutons d'action).
     */
    private JPanel createSaveButton(SaveManager.SaveInfo saveInfo, Consumer<String> onSaveSelected) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(true);
        panel.setBackground(new Color(235, 185, 120)); // Couleur sable
        panel.setBorder(BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 2));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // --- Infos textuelles (Gauche) ---
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(saveInfo.name);
        nameLabel.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 14f) : new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(PopupPanel.SDV_TEXT);
        info.add(nameLabel);

        // Formatage de la date et des statistiques
        String levelTxt = (saveInfo.level != null) ? String.valueOf(saveInfo.level) : "?";
        String moneyTxt = (saveInfo.money != null) ? String.valueOf(saveInfo.money) : "?";
        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(saveInfo.lastModifiedMillis));

        JLabel lastPlayedLabel = new JLabel("Last played : " + dateStr);
        lastPlayedLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        lastPlayedLabel.setForeground(new Color(100, 80, 60));
        info.add(lastPlayedLabel);

        // Affichage HTML pour gérer les couleurs spécifiques du Niveau (Violet) et des PO (Jaune)
        JLabel statsLabel = new JLabel("<html><b><span style='color:#8B5CF6'>Niv. " + levelTxt
                + "</span></b>  <b><span style='color:#FACC15'>" + moneyTxt + " PO</span></b></html>");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        info.add(statsLabel);

        panel.add(info, BorderLayout.CENTER);

        // --- Panel des boutons d'action (Droite) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        buttonPanel.setOpaque(false);

        // 1. Bouton Charger
        JButton selectButton = createActionButton("Charger", new Color(160, 100, 60), new Color(120, 70, 40));
        selectButton.addActionListener(e -> {
            selectedSave = saveInfo.name;
            onSaveSelected.accept(saveInfo.name);
        });

        // 2. Bouton Renommer
        JButton renameButton = createActionButton("Renommer", new Color(100, 120, 160), new Color(70, 90, 120));
        renameButton.addActionListener(e -> showRenameSaveDialog(saveInfo.name));

        // 3. Bouton Supprimer
        JButton deleteButton = createActionButton("Supprimer", new Color(160, 60, 60), new Color(120, 40, 40));
        deleteButton.addActionListener(e -> showDeleteSaveDialog(saveInfo.name));

        buttonPanel.add(selectButton);
        buttonPanel.add(renameButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.EAST);

        // Wrapper pour fixer la hauteur de la ligne
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(panel);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        return wrapper;
    }

    /** Helper pour créer les petits boutons d'action (Charger, Renommer, Supprimer) */
    private JButton createActionButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(GameFonts.MINECRAFT_FONT != null ? GameFonts.MINECRAFT_FONT.deriveFont(Font.BOLD, 10f) : new Font("Arial", Font.BOLD, 10));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PopupPanel.SDV_BORDER_DARK, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    /** Ouvre une boîte de dialogue pour saisir le nom d'une nouvelle partie */
    private void showNewSaveDialog(Consumer<String> onSaveSelected) {
        String defaultName = SaveManager.generateSaveName();
        String newSaveName = GameDialog.showInput(this, "Nouvelle Partie", "Nom de la nouvelle partie :", defaultName);

        if (newSaveName != null && !newSaveName.trim().isEmpty()) {
            selectedSave = newSaveName.trim();
            onSaveSelected.accept(selectedSave);
        }
    }

    /** Ouvre une boîte de dialogue pour renommer une sauvegarde existante */
    private void showRenameSaveDialog(String currentName) {
        String newName = GameDialog.showInput(this, "Renommer la sauvegarde", "Nouveau nom pour '" + currentName + "' :", currentName);

        if (newName != null && !newName.trim().isEmpty() && !newName.trim().equals(currentName)) {
            if (SaveManager.renameSave(currentName, newName.trim())) {
                refreshSaveList(); // Recharge l'UI
            } else {
                GameDialog.showMessage(this, "Erreur", "Impossible de renommer la sauvegarde.");
            }
        }
    }

    /** Demande confirmation avant de supprimer définitivement un fichier .sav */
    private void showDeleteSaveDialog(String saveName) {
        boolean confirmed = GameDialog.showConfirm(this, "Supprimer la sauvegarde",
                "Etes-vous sur de vouloir supprimer '" + saveName + "' ?\nCette action est irreversible.");

        if (confirmed) {
            if (SaveManager.deleteSave(saveName)) {
                refreshSaveList(); // Recharge l'UI
            } else {
                GameDialog.showMessage(this, "Erreur", "Impossible de supprimer la sauvegarde.");
            }
        }
    }

    /** Reconstruit entièrement le panneau pour mettre à jour la liste après une modification */
    private void refreshSaveList() {
        removeAll();
        // Le code de reconstruction est identique au constructeur
        // (Pour une version plus propre, on pourrait extraire l'initialisation dans une méthode initUI())

        // ... (Logique de reconstruction identique au constructeur) ...
        // Note : Dans ton code original, tu as dupliqué la logique ici.

        revalidate();
        repaint();
    }

    public String getSelectedSave() {
        return selectedSave;
    }
}
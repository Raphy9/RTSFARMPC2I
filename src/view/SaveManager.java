package src.view;

import src.control.persistence.WorldSaveData;
import src.model.World;
import src.control.persistence.SaveController;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Gestionnaire de fichiers de sauvegarde.
 * S'occupe des opérations de bas niveau sur le système de fichiers (lister, supprimer, renommer)
 * et fait le lien avec le SaveController pour la sérialisation des données.
 */
public class SaveManager {
    // Dossier racine où sont stockées les sauvegardes (.sav)
    private static final String SAVES_DIR = "saves";

    /**
     * Classe interne regroupant les informations essentielles d'une sauvegarde
     * pour l'affichage dans les menus de l'interface utilisateur.
     */
    public static class SaveInfo {
        public final String name;              // Nom du fichier
        public final long lastModifiedMillis;  // Date de dernière modification
        public final Integer level;            // Niveau atteint dans cette partie
        public final Integer money;            // Argent possédé dans cette partie

        public SaveInfo(String name, long lastModifiedMillis, Integer level, Integer money) {
            this.name = name;
            this.lastModifiedMillis = lastModifiedMillis;
            this.level = level;
            this.money = money;
        }
    }

    // Bloc statique pour s'assurer que le dossier "saves" existe dès le lancement
    static {
        try {
            Files.createDirectories(Paths.get(SAVES_DIR));
        } catch (IOException ignored) {}
    }

    /**
     * Obtient la liste des noms de toutes les sauvegardes disponibles.
     * @return Une liste de String contenant les noms sans l'extension .sav.
     */
    public static List<String> getSaveList() {
        List<String> saves = new ArrayList<>();
        try {
            // Filtre uniquement les fichiers se terminant par ".sav"
            DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(SAVES_DIR), "*.sav");
            for (Path path : stream) {
                String filename = path.getFileName().toString();
                saves.add(filename.replace(".sav", ""));
            }
            stream.close();
            Collections.sort(saves); // Tri alphabétique
        } catch (IOException ex) {
            System.err.println("Erreur lors de la lecture des sauvegardes : " + ex.getMessage());
        }
        return saves;
    }

    /**
     * Parcourt les fichiers de sauvegarde pour extraire les métadonnées (niveau, argent).
     * Utile pour afficher des détails sur chaque partie dans le menu de chargement.
     */
    public static List<SaveInfo> getSaveInfos() {
        List<SaveInfo> infos = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(SAVES_DIR), "*.sav")) {
            for (Path path : stream) {
                String filename = path.getFileName().toString();
                String name = filename.replace(".sav", "");
                long lastModified = Files.getLastModifiedTime(path).toMillis();

                Integer level = null;
                Integer money = null;

                // Lecture partielle du fichier pour récupérer l'objet WorldSaveData
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
                    Object obj = ois.readObject();
                    if (obj instanceof WorldSaveData) {
                        WorldSaveData data = (WorldSaveData) obj;
                        level = data.getLevel();
                        money = data.getMoney();
                    }
                } catch (Exception ignored) {
                    // Si le fichier est corrompu, on l'affiche quand même mais sans stats.
                }

                infos.add(new SaveInfo(name, lastModified, level, money));
            }
            // Tri par nom, insensible à la casse
            infos.sort(Comparator.comparing(a -> a.name.toLowerCase()));
        } catch (IOException ex) {
            System.err.println("Erreur lors de la lecture des sauvegardes : " + ex.getMessage());
        }
        return infos;
    }

    /**
     * Appelle le SaveController pour sérialiser l'état actuel du monde.
     */
    public static boolean saveGame(String saveName, World world) {
        return src.control.persistence.SaveController.saveGame(saveName, world);
    }

    /**
     * Appelle le SaveController pour désérialiser un fichier vers l'objet World.
     */
    public static void loadGame(String saveName, World world) {
        SaveController.loadGame(saveName, world);
    }

    /**
     * Supprime définitivement le fichier de sauvegarde du disque.
     */
    public static boolean deleteSave(String saveName) {
        try {
            Path savePath = Paths.get(SAVES_DIR, saveName + ".sav");
            Files.deleteIfExists(savePath);
            System.out.println("Sauvegarde supprimee : " + saveName);
            return true;
        } catch (IOException ex) {
            System.err.println("Erreur lors de la suppression : " + ex.getMessage());
            return false;
        }
    }

    /**
     * Renomme un fichier de sauvegarde physique.
     * Vérifie si l'ancien existe et si le nouveau nom n'est pas déjà pris.
     */
    public static boolean renameSave(String oldName, String newName) {
        try {
            Path oldPath = Paths.get(SAVES_DIR, oldName + ".sav");
            Path newPath = Paths.get(SAVES_DIR, newName + ".sav");

            if (!Files.exists(oldPath)) {
                System.err.println("La sauvegarde a renommer n'existe pas : " + oldName);
                return false;
            }

            if (Files.exists(newPath)) {
                System.err.println("Une sauvegarde avec ce nom existe deja : " + newName);
                return false;
            }

            Files.move(oldPath, newPath);
            System.out.println("Sauvegarde renommee : " + oldName + " -> " + newName);
            return true;
        } catch (IOException ex) {
            System.err.println("Erreur lors du renommage : " + ex.getMessage());
            return false;
        }
    }

    /**
     * Vérifie l'existence d'un fichier .sav spécifique.
     */
    public static boolean savExists(String saveName) {
        return Files.exists(Paths.get(SAVES_DIR, saveName + ".sav"));
    }

    /**
     * Logique de suggestion de nom : "Partie 1", "Partie 2", etc.
     * Trouve le premier index disponible pour éviter d'écraser une partie existante.
     */
    public static String generateSaveName() {
        List<String> existingSaves = getSaveList();
        int index = 1;
        while (existingSaves.contains("Partie " + index)) {
            index++;
        }
        return "Partie " + index;
    }
}
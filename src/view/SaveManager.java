package src.view;

import src.model.World;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SaveManager {
    private static final String SAVES_DIR = "saves";

    public static class SaveInfo {
        public final String name;
        public final long lastModifiedMillis;
        public final Integer level;
        public final Integer money;

        public SaveInfo(String name, long lastModifiedMillis, Integer level, Integer money) {
            this.name = name;
            this.lastModifiedMillis = lastModifiedMillis;
            this.level = level;
            this.money = money;
        }
    }

    static {
        try {
            Files.createDirectories(Paths.get(SAVES_DIR));
        } catch (IOException ignored) {}
    }

    /**
     * Obtient la liste de toutes les sauvegardes disponibles
     */
    public static List<String> getSaveList() {
        List<String> saves = new ArrayList<>();
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(SAVES_DIR), "*.sav");
            for (Path path : stream) {
                String filename = path.getFileName().toString();
                saves.add(filename.replace(".sav", ""));
            }
            stream.close();
            Collections.sort(saves);
        } catch (IOException ex) {
            System.err.println("Erreur lors de la lecture des sauvegardes : " + ex.getMessage());
        }
        return saves;
    }

    /**
     * Retourne les sauvegardes avec métadonnées utiles pour l'UI (date, niveau, PO).
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

                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
                    Object obj = ois.readObject();
                    if (obj instanceof WorldSaveData data) {
                        level = data.getLevel();
                        money = data.getMoney();
                    }
                } catch (Exception ignored) {
                    // On garde la sauvegarde listée même si on ne peut pas lire les métadonnées.
                }

                infos.add(new SaveInfo(name, lastModified, level, money));
            }
            infos.sort(Comparator.comparing(a -> a.name.toLowerCase()));
        } catch (IOException ex) {
            System.err.println("Erreur lors de la lecture des sauvegardes : " + ex.getMessage());
        }
        return infos;
    }

    /**
     * Sauvegarde l'état du monde dans un fichier
     */
    public static boolean saveGame(String saveName, World world) {
        try {
            Path savePath = Paths.get(SAVES_DIR, saveName + ".sav");
            FileOutputStream fos = new FileOutputStream(savePath.toFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            WorldSaveData data = new WorldSaveData(world);
            oos.writeObject(data);
            oos.close();
            fos.close();

            System.out.println("Jeu sauvegardé : " + saveName);
            return true;
        } catch (Exception ex) {
            System.err.println("Erreur lors de la sauvegarde : " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Charge l'état du monde depuis un fichier
     */
    public static void loadGame(String saveName, World world) {
        try {
            Path savePath = Paths.get(SAVES_DIR, saveName + ".sav");
            FileInputStream fis = new FileInputStream(savePath.toFile());
            ObjectInputStream ois = new ObjectInputStream(fis);

            WorldSaveData data = (WorldSaveData) ois.readObject();
            ois.close();
            fis.close();

            world.prepareForLoad();
            data.applyToWorld(world);
            System.out.println("Jeu chargé : " + saveName);
            world.computeParcels();
        } catch (Exception ex) {
            System.err.println("Erreur lors du chargement : " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            // Toujours garantir un inventaire minimum pour que l'UI grange reste utilisable.
            world.ensureBarnCatalog();
        }
    }

    /**
     * Supprime une sauvegarde
     */
    public static boolean deleteSave(String saveName) {
        try {
            Path savePath = Paths.get(SAVES_DIR, saveName + ".sav");
            Files.deleteIfExists(savePath);
            System.out.println("Sauvegarde supprimée : " + saveName);
            return true;
        } catch (IOException ex) {
            System.err.println("Erreur lors de la suppression : " + ex.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si une sauvegarde existe déjà
     */
    public static boolean savExists(String saveName) {
        return Files.exists(Paths.get(SAVES_DIR, saveName + ".sav"));
    }

    /**
     * Génère un nom de sauvegarde par défaut
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





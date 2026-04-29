package src.control.persistence;

import src.model.World;
import src.view.WorldSaveData;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.Point;

/**
 * Controleur responsable de la persistance (sauvegarde / chargement).
 */
public class SaveController {
	private static final String SAVES_DIR = "saves";

	static {
		try {
			Files.createDirectories(Paths.get(SAVES_DIR));
		} catch (IOException ignored) {}
	}

	public static boolean saveGame(String saveName, World world) {
		try {
			Path savePath = Paths.get(SAVES_DIR, saveName + ".sav");
			FileOutputStream fos = new FileOutputStream(savePath.toFile());
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			WorldSaveData data = new WorldSaveData(world);
			oos.writeObject(data);
			oos.close();
			fos.close();
			System.out.println("Partie sauvegardee avec succes : " + savePath.toString());
			return true;
		} catch (Exception ex) {
			System.err.println("Erreur lors de la sauvegarde : " + ex.getMessage());
			ex.printStackTrace();
			return false;
		}
	}

	public static void loadGame(String saveName, World world) {
		try {
			Path savePath = Paths.get(SAVES_DIR, saveName + ".sav");
			FileInputStream fis = new FileInputStream(savePath.toFile());
			ObjectInputStream ois = new ObjectInputStream(fis);

			WorldSaveData data = (WorldSaveData) ois.readObject();
			ois.close();
			fis.close();

			world.prepareForLoad();
			applySaveToWorld(data, world);
			world.computeParcels();
		} catch (Exception ex) {
			System.err.println("Erreur lors du chargement : " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			try { world.ensureBarnCatalog(); } catch (Exception ignored) {}
		}
	}

	private static void applySaveToWorld(WorldSaveData data, World world) {
		if (data == null || world == null) return;
		try {
			try { world.getStats().setLevel(data.getLevel()); } catch (Exception ignored) {}
			try { world.getStats().setMoney(data.getMoney()); } catch (Exception ignored) {}
			try { world.getStats().setExp(data.getExp()); } catch (Exception ignored) {}

			if (data.getBarnItems() != null) {
				world.getBarn().getItems().clear();
				for (Object itemObj : data.getBarnItems()) {
					try {
						src.view.WorldSaveData.ItemSaveData item = (src.view.WorldSaveData.ItemSaveData) itemObj;
						item.restoreToBarn(world);
					} catch (Exception ignored) {}
				}
			}

			if (data.getPlantTiles() != null) {
				for (java.util.Map.Entry<String, src.view.WorldSaveData.PlantTileSaveData> entry : data.getPlantTiles().entrySet()) {
					String[] coords = entry.getKey().split(",");
					int x = Integer.parseInt(coords[0]);
					int y = Integer.parseInt(coords[1]);
					entry.getValue().restoreToWorld(world, x, y);
				}
			}

			if (data.getBuildings() != null) {
				for (src.view.WorldSaveData.BuildingSaveData bsd : data.getBuildings()) {
					bsd.restoreToWorld(world);
				}
			}

			// Restaurer la position des jardiniers (apres les batiments)
			try {
				if (data.getGardeners() != null && world.getGardeners() != null) {
					int count = Math.min(data.getGardeners().size(), world.getGardeners().size());
					for (int i = 0; i < count; i++) {
						src.view.WorldSaveData.GardenerSaveData gsd = data.getGardeners().get(i);
						src.model.Gardener gardener = world.getGardeners().get(i);
						int targetX = gsd.x;
						int targetY = gsd.y;
						if (targetX >= 0 && targetX < World.WIDTH && targetY >= 0 && targetY < World.HEIGHT) {
							if (!world.getTile(targetX, targetY).isWalkable()) {
								Point fallback = world.findClosestWalkableAdjacent(targetX, targetY, gardener);
								if (fallback != null) {
									targetX = fallback.x;
									targetY = fallback.y;
								} else {
									continue;
								}
							}
							gardener.teleportTo(targetX, targetY);
							gardener.setFacingDirection(gsd.facingDirection);
						}
					}
				}
			} catch (Exception ignored) {}

			// Restaurer la progression des quetes si disponible
			try {
				if (data.getQuestProgresses() != null && world.getQuests() != null) {
					world.getQuests().restoreProgress(data.getQuestProgresses(), data.getActiveQuestLineIndex());
				}
			} catch (Exception ignored) {}
		} catch (Exception ex) {
			System.err.println("Erreur lors de l'application de la sauvegarde : " + ex.getMessage());
			ex.printStackTrace();
		}
	}
}



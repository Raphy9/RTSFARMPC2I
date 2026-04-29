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
 * Contrôleur responsable de la persistance (sauvegarde / chargement).
 * Gère la sérialisation et la désérialisation du monde en utilisant le patron DTO (WorldSaveData)
 * pour éviter de sauvegarder les processus actifs (Threads) et les éléments graphiques.
 */
public class SaveController {

	// Définition du répertoire cible où tous les fichiers de sauvegarde seront stockés
	private static final String SAVES_DIR = "saves";

	// Ce bloc statique est exécuté une seule fois lors du chargement de la classe en mémoire.
	// Il s'assure que le dossier de sauvegarde existe sur le disque avant toute opération d'écriture ou de lecture.
	static {
		try {
			Files.createDirectories(Paths.get(SAVES_DIR));
		} catch (IOException ignored) {
			// Si le dossier existe déjà, une exception est levée mais on l'ignore silencieusement
		}
	}

	/**
	 * Exporte l'état actuel du monde dans un fichier binaire.
	 */
	public static boolean saveGame(String saveName, World world) {
		try {
			// Construction du chemin relatif vers le fichier cible (ex: saves/ma_partie.sav)
			Path savePath = Paths.get(SAVES_DIR, saveName + ".sav");

			// Ouverture du flux d'écriture vers le fichier physique
			FileOutputStream fos = new FileOutputStream(savePath.toFile());
			// Création d'un flux d'objets pour traduire les instances Java en flux d'octets (binaire)
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			// On encapsule les données du monde actif dans un objet de transfert (DTO) pur et sérialisable
			WorldSaveData data = new WorldSaveData(world);

			// On écrit l'intégralité de cet objet dans le fichier
			oos.writeObject(data);

			// Fermeture propre des flux pour libérer les ressources du système d'exploitation
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

	/**
	 * Restaure l'état d'un monde existant à partir d'un fichier de sauvegarde.
	 */
	public static void loadGame(String saveName, World world) {
		try {
			// On localise le fichier demandé dans le répertoire des sauvegardes
			Path savePath = Paths.get(SAVES_DIR, saveName + ".sav");

			// Ouverture des flux de lecture en sens inverse de la sauvegarde
			FileInputStream fis = new FileInputStream(savePath.toFile());
			ObjectInputStream ois = new ObjectInputStream(fis);

			// On lit le fichier binaire et on le cast explicitement dans notre type DTO
			WorldSaveData data = (WorldSaveData) ois.readObject();

			ois.close();
			fis.close();

			// Avant d'appliquer les données, on nettoie le monde actuel (arrêt des entités, nettoyage des listes)
			world.prepareForLoad();

			// On repeuple le monde actif avec les données extraites du fichier
			applySaveToWorld(data, world);

			// Recalcul des propriétés adjacentes des parcelles (utile pour les bordures graphiques des champs)
			world.computeParcels();
		} catch (Exception ex) {
			System.err.println("Erreur lors du chargement : " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			// Le bloc finally garantit que, même si le chargement plante partiellement,
			// la grange sera initialisée avec au moins ses éléments par défaut pour éviter un crash complet du jeu.
			try { world.ensureBarnCatalog(); } catch (Exception ignored) {}
		}
	}

	/**
	 * Méthode interne qui effectue le "Reverse Engineering".
	 * Elle prend le conteneur de données mortes (WorldSaveData) et réinstancie les objets vivants dans le World.
	 */
	private static void applySaveToWorld(WorldSaveData data, World world) {
		// Sécurité basique pour éviter un NullPointerException si la désérialisation a échoué
		if (data == null || world == null) return;

		try {
			// Restauration des statistiques globales.
			// Les try/catch individuels permettent de ne pas interrompre tout le chargement si une seule stat est corrompue.
			try { world.getStats().setLevel(data.getLevel()); } catch (Exception ignored) {}
			try { world.getStats().setMoney(data.getMoney()); } catch (Exception ignored) {}
			try { world.getStats().setExp(data.getExp()); } catch (Exception ignored) {}

			// On s'assure d'avoir le bon nombre de jardiniers en fonction du niveau récupéré
			try { world.syncGardenersForLevel(data.getLevel()); } catch (Exception ignored) {}

			// Restauration de l'inventaire de la grange
			if (data.getBarnItems() != null) {
				world.getBarn().getItems().clear();
				for (Object itemObj : data.getBarnItems()) {
					try {
						// Chaque élément est casté puis demande à être réinstancié dans le monde physique
						src.view.WorldSaveData.ItemSaveData item = (src.view.WorldSaveData.ItemSaveData) itemObj;
						item.restoreToBarn(world);
					} catch (Exception ignored) {}
				}
			}

			// Restauration des tuiles de plantation
			if (data.getPlantTiles() != null) {
				// La map des tuiles utilise les coordonnées sous forme de String ("x,y") comme clé de hachage
				for (java.util.Map.Entry<String, src.view.WorldSaveData.PlantTileSaveData> entry : data.getPlantTiles().entrySet()) {
					// On extrait les coordonnées depuis la clé
					String[] coords = entry.getKey().split(",");
					int x = Integer.parseInt(coords[0]);
					int y = Integer.parseInt(coords[1]);

					// On demande au DTO de la tuile de recréer l'objet PlantTile et son éventuelle Plante au bon endroit
					entry.getValue().restoreToWorld(world, x, y);
				}
			}

			// Restauration des infrastructures (barrières, puits, épouvantails...)
			if (data.getBuildings() != null) {
				for (src.view.WorldSaveData.BuildingSaveData bsd : data.getBuildings()) {
					bsd.restoreToWorld(world);
				}
			}

			// Restauration des agents (Jardiniers) - Doit impérativement se faire APRES les bâtiments
			// pour éviter de faire apparaitre un jardinier à l'intérieur d'un mur fraichement chargé.
			try {
				if (data.getGardeners() != null && world.getGardeners() != null) {
					// On s'assure de ne pas dépasser la taille des listes pour éviter les OutOfBounds
					int count = Math.min(data.getGardeners().size(), world.getGardeners().size());

					for (int i = 0; i < count; i++) {
						src.view.WorldSaveData.GardenerSaveData gsd = data.getGardeners().get(i);
						src.model.Gardener gardener = world.getGardeners().get(i);

						int targetX = gsd.x;
						int targetY = gsd.y;

						// On vérifie que les coordonnées de sauvegarde sont bien à l'intérieur de la carte
						if (targetX >= 0 && targetX < World.WIDTH && targetY >= 0 && targetY < World.HEIGHT) {

							// Système anticollision : si l'ancienne case du jardinier est devenue impraticable (ex: une plante a poussé)
							if (!world.getTile(targetX, targetY).isWalkable()) {
								// On cherche la case libre adjacente la plus proche pour l'y placer en sécurité
								Point fallback = world.findClosestWalkableAdjacent(targetX, targetY, gardener);
								if (fallback != null) {
									targetX = fallback.x;
									targetY = fallback.y;
								} else {
									// Si le jardinier est totalement encerclé et qu'aucun repli n'est possible, on ignore le placement
									continue;
								}
							}
							// On téléporte physiquement l'agent sur la grille et on restaure son orientation
							gardener.teleportTo(targetX, targetY);
							gardener.setFacingDirection(gsd.facingDirection);
						}

						// Gestion de l'inventaire personnel du jardinier
						// Pour éviter la perte d'items, tous les objets qu'il portait sont envoyés dans la grange globale
						if (gsd.inventoryItems != null && !gsd.inventoryItems.isEmpty()) {
							for (src.view.WorldSaveData.ItemSaveData itemData : gsd.inventoryItems) {
								itemData.restoreToBarn(world);
							}
						}
					}
				}
			} catch (Exception ignored) {}

			// Restauration de l'avancée du joueur dans l'arbre des quêtes
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
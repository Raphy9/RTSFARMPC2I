package src.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

/** Classe qui constitue l'unite de base pour les actions des jardiniers
 * Comprend plusieurs cases plantables, selectionnables toutes ensemble
 * La liste de cases ne doit pas changer. Une fois creee, on ne peut pas modifier une parcelle,
 * il faut la detruire et en creer une nouvelle si on veut changer les cases comprises dans la parcelle
 */
public class Parcel {

    private final ArrayList<PlantTile> tiles;

    public Parcel(ArrayList<PlantTile> tiles) {
        // On cree une copie pour garantir l'immuabilite
        this.tiles = new ArrayList<>(tiles);
        // Tri pour garantir un ordre coherent pour les jardiniers
        this.tiles.sort((t1, t2) -> {
            if (t1.getY() != t2.getY()) return Integer.compare(t1.getY(), t2.getY());
            return Integer.compare(t1.getX(), t2.getX());
        });
        // Lier les cases a cette parcelle
        for (PlantTile tile : this.tiles) {
            tile.setParcel(this);
        }
    }

    /** Renvoie un iterable des cases de cette parcelle, triee par coordonnees (d'abord par Y, puis par X) */
    public Iterable<PlantTile> getTiles() {
        return tiles;
    }

    /** Renvoie le nombre de cases dans cette parcelle */
    public int getSize() {
        return tiles.size();
    }

    /** Renvoie le nombre de cases plantables non occupees dans cette parcelle */
    public int getAvailableSpotsNb() {
        int count = 0;
        for (PlantTile tile : tiles) {
            if (tile.isFarmable()) {
                count++;
            }
        }
        return count;
    }


}


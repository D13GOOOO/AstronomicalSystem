package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Keeps the maps that the discs are wrapped in, by their names.
 */
public interface TexturesView {

    /**
     * @param name  the name by which the discs ask for the map; a map already there with this name
     *              is replaced
     * @param image the map as a JPEG or PNG file, twice as wide as tall: the longitudes from 180
     *              degrees west to 180 degrees east across it, the north at the top
     * @throws IllegalArgumentException if the image cannot be read
     */
    void load(@NotNull String name, byte @NotNull [] image);

    /**
     * A map that comes in tiles: images of the same size that side by side, row after row from the
     * north, make the whole map.
     *
     * @param name    as in {@link #load(String, byte[])}
     * @param columns how many tiles make a row; precondition: positive
     * @param tiles   the JPEG or PNG files of the tiles, row after row, each row from the west;
     *                precondition: whole rows, at least one
     * @throws IllegalArgumentException if a tile cannot be read
     */
    void loadMosaic(@NotNull String name, int columns, @NotNull List<byte[]> tiles);
}

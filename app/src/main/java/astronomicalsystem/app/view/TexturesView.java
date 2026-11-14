package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

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
}

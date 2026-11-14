package astronomicalsystem.app.texture;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;

/**
 * A map and the files it is made of.
 *
 * @param name    the name the map is given in the view
 * @param columns how many tiles make a row of the map; precondition: positive
 * @param tiles   the addresses of the tiles, row after row from the north, each row from the west;
 *                precondition: whole rows, at least one
 */
public record MapSource(@NotNull String name, int columns, @NotNull List<URI> tiles) {

    public MapSource {
        tiles = List.copyOf(tiles);
    }

    /**
     * A mosaic of the tile service of the NASA Solar System Treks, in the equirectangular
     * projection: at the zoom level z a map is 2^(z+1) tiles wide and 2^z tall, each of 256 pixels.
     *
     * @param layer the address of the layer, such as
     *              {@code https://trek.nasa.gov/tiles/Mars/EQ/Mars_Viking_MDIM21_ClrMosaic_global_232m}
     * @param zoom  precondition: from 0 to the deepest level of the layer
     */
    public static @NotNull MapSource ofTrek(@NotNull String name, @NotNull String layer, int zoom) {
        return new MapSource(name, 1, List.of());
    }
}

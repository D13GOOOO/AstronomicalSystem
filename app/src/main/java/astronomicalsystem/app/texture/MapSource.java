package astronomicalsystem.app.texture;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A map and the files it is made of.
 *
 * @param name    the name the map is given in the view
 * @param columns how many tiles make a row of the map; precondition: positive
 * @param tiles   the addresses of the tiles, row after row from the north, each row from the west;
 *                precondition: whole rows, at least one
 */
public record MapSource(@NotNull String name, int columns, @NotNull List<URI> tiles) {

    // The version of the service, the default style and the set of tile matrices of every layer
    private static final String TREK_TILES = "/1.0.0/default/default028mm/";

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
        int rows = 1 << zoom;
        int columns = 2 * rows;
        List<URI> tiles = IntStream.range(0, rows * columns)
                .mapToObj(index -> URI.create(layer + TREK_TILES + zoom + "/" + index / columns + "/" + index % columns
                        + ".jpg"))
                .toList();
        return new MapSource(name, columns, tiles);
    }
}

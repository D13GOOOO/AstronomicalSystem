package astronomicalsystem.app.texture;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MapSourceTest {

    private static final String LAYER = "https://trek.example/tiles/Mars/EQ/Viking";
    private static final String TILES = LAYER + "/1.0.0/default/default028mm";

    // At the first level two rows of four tiles; the address ends with the level, the row and the column
    @Test
    void aTrekMosaicListsItsTilesRowAfterRowFromTheNorthWest() {
        MapSource SUT = MapSource.ofTrek("mars", LAYER, 1);

        assertThat(SUT).isEqualTo(new MapSource("mars", 4, List.of(
                URI.create(TILES + "/1/0/0.jpg"), URI.create(TILES + "/1/0/1.jpg"),
                URI.create(TILES + "/1/0/2.jpg"), URI.create(TILES + "/1/0/3.jpg"),
                URI.create(TILES + "/1/1/0.jpg"), URI.create(TILES + "/1/1/1.jpg"),
                URI.create(TILES + "/1/1/2.jpg"), URI.create(TILES + "/1/1/3.jpg"))));
    }
}

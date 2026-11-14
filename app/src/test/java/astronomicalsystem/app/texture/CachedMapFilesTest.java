package astronomicalsystem.app.texture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CachedMapFilesTest {

    private static final URI TILE_ADDRESS = URI.create("https://maps.example/Mars/3/1/2.jpg");
    private static final byte[] TILE = {(byte) 0xFF, (byte) 0xD8, 1, 2, 3};

    private final MapFiles origin = mock();
    private CachedMapFiles SUT;

    // Not yet there: the first file kept creates it
    @BeforeEach
    void keepTheFilesInANewDirectory(@TempDir Path temporary) {
        SUT = new CachedMapFiles(origin, temporary.resolve("maps"));
    }

    // The origin is gone the second time
    @Test
    void aFileFetchedOnceIsThereWithoutTheOrigin() {
        when(origin.fetch(TILE_ADDRESS)).thenReturn(TILE)
                .thenThrow(new MapUnavailableException("The map server cannot be reached"));
        SUT.fetch(TILE_ADDRESS);

        byte[] file = SUT.fetch(TILE_ADDRESS);

        assertThat(file).isEqualTo(TILE);
    }
}

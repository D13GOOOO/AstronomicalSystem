package astronomicalsystem.app.texture;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetryingMapFilesTest {

    private static final URI ADDRESS = URI.create("https://maps.example/earth.jpg");
    private static final byte[] MAP = {(byte) 0xFF, (byte) 0xD8, 1, 2, 3};
    private static final int THREE_ATTEMPTS = 3;

    private final MapFiles origin = mock();
    private final RetryingMapFiles SUT = new RetryingMapFiles(origin, THREE_ATTEMPTS);

    @Test
    void aFileThatCannotBeHadAtFirstIsAskedForAgain() {
        when(origin.fetch(ADDRESS)).thenThrow(new MapUnavailableException("The map server answered with HTTP status 503"))
                .thenReturn(MAP);

        byte[] file = SUT.fetch(ADDRESS);

        assertThat(file).isEqualTo(MAP);
    }

    @Test
    void aFileThatCannotBeHadIsGivenUpAfterTheAttempts() {
        when(origin.fetch(ADDRESS)).thenThrow(new MapUnavailableException("The map server cannot be reached"));

        assertThatThrownBy(() -> SUT.fetch(ADDRESS)).isInstanceOf(MapUnavailableException.class)
                .hasMessage("The map server cannot be reached");

        verify(origin, times(THREE_ATTEMPTS)).fetch(ADDRESS);
    }
}

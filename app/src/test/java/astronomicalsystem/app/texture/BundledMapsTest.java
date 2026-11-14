package astronomicalsystem.app.texture;

import astronomicalsystem.app.view.TexturesView;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BundledMapsTest {

    // Every JPEG file starts with the bytes FF D8
    private static final byte[] START_OF_A_JPEG = {(byte) 0xFF, (byte) 0xD8};

    private final TexturesView view = mock();
    private final ArgumentCaptor<byte[]> image = ArgumentCaptor.captor();
    private final BundledMaps SUT = new BundledMaps(view);

    @Test
    void aMapThatComesWithTheApplicationIsGivenToTheViewAsItsFile() {
        SUT.load("jupiter");

        verify(view).load(eq("jupiter"), image.capture());
        assertThat(image.getValue()).startsWith(START_OF_A_JPEG);
    }

    @Test
    void aMapThatDoesNotComeWithTheApplicationIsRefused() {
        assertThatThrownBy(() -> SUT.load("pandora"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No map named pandora comes with the application");
    }
}

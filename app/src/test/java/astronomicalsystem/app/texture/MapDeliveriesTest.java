package astronomicalsystem.app.texture;

import astronomicalsystem.app.view.TexturesView;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class MapDeliveriesTest {

    private static final byte[] EARTH_MAP = {1, 2, 3};
    private static final byte[] MARS_MAP = {4, 5, 6};

    private final TexturesView view = mock();
    private final MapDeliveries SUT = new MapDeliveries(view);

    @Test
    void theMapsPostedReachTheViewWhenTheFrameIsDrawn() {
        SUT.post(textures -> textures.load("earth", EARTH_MAP));
        SUT.post(textures -> textures.load("mars", MARS_MAP));

        SUT.draw();

        verify(view).load("earth", EARTH_MAP);
        verify(view).load("mars", MARS_MAP);
    }

    // The thread that posts must not touch the view
    @Test
    void aMapPostedWaitsForTheFrame() {
        SUT.post(textures -> textures.load("earth", EARTH_MAP));

        verifyNoInteractions(view);
    }

    @Test
    void aMapIsHandedOverOnlyOnce() {
        SUT.post(textures -> textures.load("earth", EARTH_MAP));
        SUT.draw();

        SUT.draw();

        verify(view).load("earth", EARTH_MAP);
    }
}

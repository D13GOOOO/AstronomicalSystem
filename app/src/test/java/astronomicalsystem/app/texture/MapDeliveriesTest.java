package astronomicalsystem.app.texture;

import astronomicalsystem.app.view.TexturesView;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class MapDeliveriesTest {

    private static final byte[] EARTH_MAP = {1, 2, 3};
    private static final byte[] WEST_TILE = {4, 5, 6};
    private static final byte[] EAST_TILE = {7, 8, 9};
    private static final int TWO_COLUMNS = 2;

    private final TexturesView view = mock();
    private final MapDeliveries SUT = new MapDeliveries(view);

    @Test
    void theMapsGivenReachTheViewWhenTheFrameIsDrawn() {
        SUT.load("earth", EARTH_MAP);
        SUT.loadMosaic("mars", TWO_COLUMNS, List.of(WEST_TILE, EAST_TILE));

        SUT.draw();

        verify(view).load("earth", EARTH_MAP);
        verify(view).loadMosaic("mars", TWO_COLUMNS, List.of(WEST_TILE, EAST_TILE));
    }

    // A server may answer with something that is not an image, which only the view finds out: the
    // application goes on without that map
    @Test
    void aMapTheViewCannotReadIsDroppedAndTheOthersStillArrive() {
        doThrow(new IllegalArgumentException("Map mars cannot be read")).when(view)
                .loadMosaic("mars", TWO_COLUMNS, List.of(WEST_TILE, EAST_TILE));
        SUT.loadMosaic("mars", TWO_COLUMNS, List.of(WEST_TILE, EAST_TILE));
        SUT.load("earth", EARTH_MAP);

        SUT.draw();

        verify(view).load("earth", EARTH_MAP);
    }

    // The thread that gives a map must not touch the view
    @Test
    void aMapGivenWaitsForTheFrame() {
        SUT.load("earth", EARTH_MAP);

        verifyNoInteractions(view);
    }

    @Test
    void aMapIsHandedOverOnlyOnce() {
        SUT.load("earth", EARTH_MAP);
        SUT.draw();

        SUT.draw();

        verify(view).load("earth", EARTH_MAP);
    }
}

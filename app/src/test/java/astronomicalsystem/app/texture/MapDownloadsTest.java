package astronomicalsystem.app.texture;

import astronomicalsystem.app.view.TexturesView;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MapDownloadsTest {

    private static final URI WEST = URI.create("https://maps.example/0/0/0.jpg");
    private static final URI EAST = URI.create("https://maps.example/0/0/1.jpg");
    private static final byte[] WEST_TILE = {1, 2, 3};
    private static final byte[] EAST_TILE = {4, 5, 6};
    private static final int TWO_COLUMNS = 2;
    private static final MapSource MARS = new MapSource("mars", TWO_COLUMNS, List.of(WEST, EAST));
    // Runs every download at once, where it is asked for
    private static final Executor AT_ONCE = Runnable::run;

    private final MapFiles files = mock();
    private final TexturesView view = mock();
    private final MapDownloads SUT = new MapDownloads(files, view, AT_ONCE);

    @Test
    void theTilesOfAMapAreFetchedAndGivenToTheViewAsAMosaic() {
        when(files.fetch(WEST)).thenReturn(WEST_TILE);
        when(files.fetch(EAST)).thenReturn(EAST_TILE);

        SUT.request(MARS);

        verify(view).loadMosaic("mars", TWO_COLUMNS, List.of(WEST_TILE, EAST_TILE));
    }

    // An executor that only takes note: nothing is fetched by the one who asks
    @Test
    void aRequestIsLeftToTheExecutor() {
        Executor later = mock();
        MapDownloads background = new MapDownloads(files, view, later);

        background.request(MARS);

        verify(later).execute(any());
        verifyNoInteractions(files, view);
    }
}

package astronomicalsystem.app.presenter;

import astronomicalsystem.app.view.TextView;
import astronomicalsystem.ephemeris.Provenance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ProvenancePresenterTest {

    private final TextView view = mock();

    @ParameterizedTest
    @CsvSource(textBlock = """
            JPL_HORIZONS,     Initial positions: JPL Horizons
            KEPLER_ELEMENTS,  Initial positions: Kepler orbital elements
            """)
    void tellsWhereTheInitialPositionsCameFromAsSoonAsItIsCreated(Provenance provenance, String line) {
        new ProvenancePresenter(view, provenance);

        verify(view).show(List.of(line));
    }
}

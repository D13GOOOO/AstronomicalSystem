package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.StateVector;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.EphemerisEntry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HorizonsClientTest {

    private static final JulianDate EPOCH = new JulianDate(2461041.5);
    private static final HorizonsQuery EARTH_QUERY = new HorizonsQuery(new HorizonsCommand("399"), EPOCH);
    private static final String RESPONSE = "text of the Horizons response";
    private static final EphemerisEntry EARTH_ENTRY = new EphemerisEntry(EPOCH,
            new StateVector(new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.5, 0.0)));

    private final Transport transport = mock();
    private final ResponseParser parser = mock();
    private final HorizonsClient SUT = new HorizonsClient(transport, parser);

    @Test
    void parsesTheResponseFetchedForTheQuery() {
        when(transport.fetch(EARTH_QUERY.uri())).thenReturn(RESPONSE);
        when(parser.parse(RESPONSE)).thenReturn(EARTH_ENTRY);

        EphemerisEntry entry = SUT.entryOf(EARTH_QUERY);

        assertThat(entry).isEqualTo(EARTH_ENTRY);
    }
}

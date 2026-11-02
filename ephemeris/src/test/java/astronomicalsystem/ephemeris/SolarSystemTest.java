package astronomicalsystem.ephemeris;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.StateVector;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.horizons.Horizons;
import astronomicalsystem.ephemeris.horizons.HorizonsCommand;
import astronomicalsystem.ephemeris.horizons.HorizonsQuery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SolarSystemTest {

    private static final JulianDate DATE = new JulianDate(2461041.5);
    private static final EphemerisEntry SOME_ENTRY =
            new EphemerisEntry(DATE, new StateVector(new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.01, 0.0)));
    private static final int SUN_AND_PLANETS = 9;

    private final Horizons horizons = mock();

    @Test
    void horizonsIsAskedForTheSunAndTheBarycentresOfThePlanetarySystems() {
        when(horizons.entryOf(any())).thenReturn(SOME_ENTRY);
        EphemerisSource SUT = SolarSystem.fromHorizons(horizons);

        List<Body> bodies = SUT.bodiesAt(DATE);

        ArgumentCaptor<HorizonsQuery> queries = ArgumentCaptor.forClass(HorizonsQuery.class);
        verify(horizons, times(SUN_AND_PLANETS)).entryOf(queries.capture());
        assertThat(queries.getAllValues()).containsExactly(query("10"), query("1"), query("2"), query("3"),
                query("4"), query("5"), query("6"), query("7"), query("8"));
        assertThat(propertiesOf(bodies)).isEqualTo(propertiesOf(SolarSystem.meanOrbits().bodiesAt(DATE)));
    }

    private static HorizonsQuery query(String command) {
        return new HorizonsQuery(new HorizonsCommand(command), DATE);
    }

    private static List<List<Object>> propertiesOf(List<Body> bodies) {
        return bodies.stream().map(body -> List.<Object>of(body.name(), body.kind(), body.mass(), body.radius()))
                .toList();
    }
}

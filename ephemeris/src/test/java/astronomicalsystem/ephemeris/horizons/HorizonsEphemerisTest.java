package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.StateVector;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.BodyProperties;
import astronomicalsystem.ephemeris.Ephemeris;
import astronomicalsystem.ephemeris.EphemerisEntry;
import astronomicalsystem.ephemeris.Provenance;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HorizonsEphemerisTest {

    private static final JulianDate DATE = new JulianDate(2461041.5);
    private static final HorizonsTarget SUN = new HorizonsTarget(
            new BodyProperties(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(4.65e-3)),
            new HorizonsCommand("10"));
    private static final HorizonsTarget EARTH = new HorizonsTarget(
            new BodyProperties(new BodyName("Earth"), BodyKind.PLANET, new Mass(3.0e-6), new Radius(4.26e-5)),
            new HorizonsCommand("3"));
    private static final StateVector SUN_STATE =
            new StateVector(new Vector3(-0.003, -0.005, 0.0001), new Vector3(7.0e-6, 2.0e-7, -1.0e-7));
    private static final StateVector EARTH_STATE =
            new StateVector(new Vector3(-0.18, 0.96, 0.0001), new Vector3(-0.0172, -0.0031, 0.0));

    private final Horizons horizons = mock();

    @Test
    void everyTargetIsWhereHorizonsPutsItAtTheDate() {
        HorizonsEphemeris SUT = new HorizonsEphemeris(horizons, List.of(SUN, EARTH));
        when(horizons.entryOf(new HorizonsQuery(SUN.command(), DATE))).thenReturn(new EphemerisEntry(DATE, SUN_STATE));
        when(horizons.entryOf(new HorizonsQuery(EARTH.command(), DATE)))
                .thenReturn(new EphemerisEntry(DATE, EARTH_STATE));

        List<Body> bodies = SUT.ephemerisAt(DATE).bodies();

        assertThat(bodies).containsExactly(SUN.body().in(SUN_STATE), EARTH.body().in(EARTH_STATE));
    }

    @Test
    void thePositionsComeFromHorizons() {
        HorizonsEphemeris SUT = new HorizonsEphemeris(horizons, List.of(SUN));
        when(horizons.entryOf(new HorizonsQuery(SUN.command(), DATE))).thenReturn(new EphemerisEntry(DATE, SUN_STATE));

        Ephemeris ephemeris = SUT.ephemerisAt(DATE);

        assertThat(ephemeris).isEqualTo(new Ephemeris(List.of(SUN.body().in(SUN_STATE)), Provenance.JPL_HORIZONS));
    }

    @Test
    void changingTheListOfTargetsAfterwardsDoesNotChangeTheEphemeris() {
        List<HorizonsTarget> targets = new ArrayList<>(List.of(SUN));
        HorizonsEphemeris SUT = new HorizonsEphemeris(horizons, targets);
        targets.clear();
        when(horizons.entryOf(new HorizonsQuery(SUN.command(), DATE))).thenReturn(new EphemerisEntry(DATE, SUN_STATE));

        List<Body> bodies = SUT.ephemerisAt(DATE).bodies();

        assertThat(bodies).containsExactly(SUN.body().in(SUN_STATE));
    }
}

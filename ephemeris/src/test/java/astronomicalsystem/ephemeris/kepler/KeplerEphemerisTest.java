package astronomicalsystem.ephemeris.kepler;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.Eccentricity;
import astronomicalsystem.core.orbit.OrbitalElements;
import astronomicalsystem.core.orbit.StateVector;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.BodyProperties;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.assertj.core.util.DoubleComparator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeplerEphemerisTest {

    private static final BodyProperties SUN =
            new BodyProperties(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(4.65e-3));
    // As heavy as the Sun, so that the centre of mass lies halfway between them
    private static final BodyProperties TWIN =
            new BodyProperties(new BodyName("Twin"), BodyKind.STAR, new Mass(1.0), new Radius(4.65e-3));
    private static final JulianDate J2000 = new JulianDate(2451545.0);
    // k² in AU³ per solar mass per day squared
    private static final double GRAVITATIONAL_CONSTANT = 2.959122082855911e-4;
    // On a circle of radius 2 around a total of 2 solar masses the speed is √(G·2/2) = √G
    private static final OrbitalElements CIRCLE_OF_RADIUS_TWO =
            new OrbitalElements(2.0, new Eccentricity(0.0), 0.0, 0.0, 0.0, 0.0);
    private static final double CIRCULAR_SPEED = Math.sqrt(GRAVITATIONAL_CONSTANT);
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = RecursiveComparisonConfiguration.builder()
            .withComparatorForType(new DoubleComparator(1e-12), Double.class)
            .build();

    @Test
    void theCentralBodyAloneRestsAtTheOrigin() {
        KeplerEphemeris SUT = new KeplerEphemeris(SUN, J2000, List.of());

        List<Body> bodies = SUT.bodiesAt(J2000);

        assertThat(bodies).containsExactly(
                new Body(SUN.name(), SUN.kind(), SUN.mass(), SUN.radius(), Vector3.ZERO, Vector3.ZERO));
    }

    @Test
    void atTheEpochTheOrbitingBodyIsWhereItsElementsPutIt() {
        KeplerEphemeris SUT = new KeplerEphemeris(SUN, J2000, List.of(new KeplerOrbit(TWIN, CIRCLE_OF_RADIUS_TWO)));

        List<Body> bodies = SUT.bodiesAt(J2000);

        assertThat(secondRelativeToFirst(bodies)).usingRecursiveComparison(CLOSE_ENOUGH)
                .isEqualTo(new StateVector(new Vector3(2.0, 0.0, 0.0), new Vector3(0.0, CIRCULAR_SPEED, 0.0)));
    }

    @Test
    void changingTheListOfOrbitsAfterwardsDoesNotChangeTheEphemeris() {
        List<KeplerOrbit> orbits = new ArrayList<>(List.of(new KeplerOrbit(TWIN, CIRCLE_OF_RADIUS_TWO)));
        KeplerEphemeris SUT = new KeplerEphemeris(SUN, J2000, orbits);
        orbits.clear();

        List<Body> bodies = SUT.bodiesAt(J2000);

        assertThat(bodies).hasSize(2);
    }

    private static StateVector secondRelativeToFirst(List<Body> bodies) {
        Body first = bodies.get(0);
        Body second = bodies.get(1);
        return new StateVector(second.position().minus(first.position()),
                second.velocity().minus(first.velocity()));
    }
}

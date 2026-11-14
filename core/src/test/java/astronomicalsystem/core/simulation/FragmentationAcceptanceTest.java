package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.impact.Approach;
import astronomicalsystem.core.impact.FragmentSizes;
import astronomicalsystem.core.impact.Impactor;
import astronomicalsystem.core.math.Tolerances;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.physics.FragmentingCollisions;
import astronomicalsystem.core.physics.NewtonianGravity;
import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.physics.VelocityVerlet;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

/**
 * Acceptance test of a giant impact with the real gravity model, integrator and fragmentation: a
 * protoplanet a tenth of the mass of the Earth hits it as the Moon-forming impactor did.
 */
class FragmentationAcceptanceTest {

    private static final double G = 2.959122082855911e-4;
    private static final double NO_SOFTENING = 0.0;
    // The step of the application
    private static final TimeStep ONE_HOUR = new TimeStep(1.0 / 24.0);
    private static final int A_DAY_IN_HOURS = 24;
    private static final Body SUN = new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(4.65e-3),
            Vector3.ZERO, Vector3.ZERO);
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3.003e-6),
            new Radius(4.259e-5), new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.0172, 0.0));
    private static final Approach FROM_ABOVE_AIMING_ALONG_X = new Approach(new Vector3(0.0, 0.0, 1.0),
            new Vector3(1.0, 0.0, 0.0));
    private static final FragmentSizes SIZES = new FragmentSizes(new Mass(1e-12), 24);
    private static final double PRECISION_PERCENT = 1e-9;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = Tolerances.closeEnough(1e-15);

    @Test
    void aGiantImpactThrowsDebrisAndKeepsTheMassAndTheMomentum() {
        Body protoplanet = Impactor.PROTOPLANET.launchedAt(EARTH, FROM_ABOVE_AIMING_ALONG_X, new BodyName("Theia"));
        List<Body> initial = List.of(SUN, EARTH, protoplanet);
        Simulation SUT = new Simulation(new VelocityVerlet(new NewtonianGravity(G, NO_SOFTENING)),
                new FragmentingCollisions(SIZES), initial);

        SUT.advance(ONE_HOUR, A_DAY_IN_HOURS);

        assertThat(SUT.bodies()).extracting(Body::name).startsWith(SUN.name(), EARTH.name());
        assertThat(SUT.bodies()).extracting(Body::kind).contains(BodyKind.DEBRIS);
        assertThat(totalMassOf(SUT.bodies())).isCloseTo(totalMassOf(initial), withinPercentage(PRECISION_PERCENT));
        assertThat(momentumOf(SUT.bodies())).usingRecursiveComparison(CLOSE_ENOUGH).isEqualTo(momentumOf(initial));
    }

    private static double totalMassOf(List<Body> bodies) {
        return bodies.stream().mapToDouble(body -> body.mass().solarMasses()).sum();
    }

    private static Vector3 momentumOf(List<Body> bodies) {
        return bodies.stream().map(body -> body.velocity().times(body.mass().solarMasses()))
                .reduce(Vector3.ZERO, Vector3::plus);
    }
}

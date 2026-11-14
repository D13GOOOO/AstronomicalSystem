package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.physics.MergingCollisions;
import astronomicalsystem.core.physics.NewtonianGravity;
import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.physics.VelocityVerlet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

/**
 * Acceptance test of a simulation with the real gravity model, integrator and collision resolver.
 */
class CollisionAcceptanceTest {

    private static final double G = 2.959122082855911e-4;
    private static final double NO_SOFTENING = 0.0;
    private static final double IMPACTOR_DISTANCE = 1e-3;
    private static final double IMPACT_SPEED = 1e-2;
    // Small enough to follow the fall of the impactor closely
    private static final TimeStep A_THOUSANDTH_OF_A_DAY = new TimeStep(1e-3);
    private static final int STEPS_UNTIL_AFTER_THE_IMPACT = 150;
    private static final double MOMENTUM_TOLERANCE_PERCENT = 1e-9;
    // The step of the application
    private static final TimeStep ONE_HOUR = new TimeStep(1.0 / 24.0);
    private static final int A_DAY_IN_HOURS = 24;
    // About 20 km/s: the impactor covers more than eleven radii of the planet in an hour
    private static final double FAST_IMPACT_SPEED = 0.01155;
    // Far enough that no end of a step finds the impactor inside the planet
    private static final double FAST_IMPACTOR_DISTANCE = 1.1e-3;
    private static final Body EARTH_LIKE_PLANET = new Body(new BodyName("Planet"), BodyKind.PLANET, new Mass(3.003e-6),
            new Radius(4.259e-5), Vector3.ZERO, Vector3.ZERO);

    @Test
    void aHeadOnImpactLeavesOneBodyWithTheSameMomentum() {
        Body planet = new Body(new BodyName("Planet"), BodyKind.PLANET, new Mass(3.003e-6), new Radius(4.259e-5),
                Vector3.ZERO, Vector3.ZERO);
        Body impactor = new Body(new BodyName("Impactor"), BodyKind.PLANET, new Mass(3.003e-7), new Radius(2.3e-5),
                new Vector3(IMPACTOR_DISTANCE, 0.0, 0.0), new Vector3(-IMPACT_SPEED, 0.0, 0.0));
        List<Body> initial = List.of(planet, impactor);
        Simulation SUT = new Simulation(new VelocityVerlet(new NewtonianGravity(G, NO_SOFTENING)),
                new MergingCollisions(), initial);

        advanceUntilAfterTheImpact(SUT);

        assertThat(SUT.bodies()).hasSize(1);
        assertThat(totalMomentum(SUT.bodies()).x())
                .isCloseTo(totalMomentum(initial).x(), withinPercentage(MOMENTUM_TOLERANCE_PERCENT));
    }

    @Test
    void aFastImpactorHitsThePlanetItCrossesWithinAStep() {
        Body impactor = new Body(new BodyName("Impactor"), BodyKind.PLANET, new Mass(1e-18), new Radius(6.7e-9),
                new Vector3(FAST_IMPACTOR_DISTANCE, 0.0, 0.0), new Vector3(-FAST_IMPACT_SPEED, 0.0, 0.0));
        Simulation SUT = new Simulation(new VelocityVerlet(new NewtonianGravity(G, NO_SOFTENING)),
                new MergingCollisions(), List.of(EARTH_LIKE_PLANET, impactor));

        SUT.advance(ONE_HOUR, A_DAY_IN_HOURS);

        assertThat(SUT.bodies()).extracting(Body::name).containsExactly(EARTH_LIKE_PLANET.name());
    }

    private static void advanceUntilAfterTheImpact(Simulation simulation) {
        for (int step = 0; step < STEPS_UNTIL_AFTER_THE_IMPACT; step++) {
            simulation.advance(A_THOUSANDTH_OF_A_DAY);
        }
    }

    private static Vector3 totalMomentum(List<Body> bodies) {
        return bodies.stream()
                .map(body -> body.velocity().times(body.mass().solarMasses()))
                .reduce(Vector3.ZERO, Vector3::plus);
    }
}

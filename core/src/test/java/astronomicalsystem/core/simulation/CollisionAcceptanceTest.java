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
    // Small enough that the bodies cannot pass through each other within a single step
    private static final TimeStep A_THOUSANDTH_OF_A_DAY = new TimeStep(1e-3);
    private static final int STEPS_UNTIL_AFTER_THE_IMPACT = 150;
    private static final double MOMENTUM_TOLERANCE_PERCENT = 1e-9;

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

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

/**
 * Acceptance test of the prediction with the real gravity model, integrator and collision
 * resolver: the predicted path ends where the simulation goes.
 */
class TrajectoryPredictionAcceptanceTest {

    private static final double G = 2.959122082855911e-4;
    private static final double NO_SOFTENING = 0.0;
    private static final TimeStep ONE_DAY = new TimeStep(1.0);
    // Three points after the present one, ten days apart
    private static final Sampling EVERY_TEN_DAYS = new Sampling(10, 4);
    private static final int THIRTY_DAYS_IN_STEPS = 30;
    // The Earth on a circle of 1 AU around the Sun, at the speed sqrt(G M / r)
    private static final List<Body> SUN_AND_EARTH = List.of(
            new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(4.65e-3), Vector3.ZERO,
                    Vector3.ZERO),
            new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3.003e-6), new Radius(4.259e-5),
                    new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, Math.sqrt(G), 0.0)));

    // The same operations in the same order: equal to the last bit, with no tolerance
    @Test
    void thePredictedTrajectoryEndsWhereTheSimulationGoes() {
        TrajectoryPredictor SUT = new TrajectoryPredictor(gravityIntegrator(), new MergingCollisions(), ONE_DAY,
                EVERY_TEN_DAYS);
        Simulation simulation = new Simulation(gravityIntegrator(), new MergingCollisions(), SUN_AND_EARTH);

        List<Trajectory> trajectories = SUT.predict(SUN_AND_EARTH);
        advanceThirtyDays(simulation);

        assertThat(trajectories).extracting(trajectory -> trajectory.points().getLast())
                .containsExactlyElementsOf(simulation.bodies().stream().map(Body::position).toList());
    }

    private static VelocityVerlet gravityIntegrator() {
        return new VelocityVerlet(new NewtonianGravity(G, NO_SOFTENING));
    }

    private static void advanceThirtyDays(Simulation simulation) {
        for (int step = 0; step < THIRTY_DAYS_IN_STEPS; step++) {
            simulation.advance(ONE_DAY);
        }
    }
}

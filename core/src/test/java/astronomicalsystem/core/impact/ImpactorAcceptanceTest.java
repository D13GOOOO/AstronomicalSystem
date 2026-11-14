package astronomicalsystem.core.impact;

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
import astronomicalsystem.core.simulation.Simulation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Acceptance test of a launch with the real gravity model, integrator and collision resolver: the
 * impactor reaches its target.
 */
class ImpactorAcceptanceTest {

    private static final double G = 2.959122082855911e-4;
    private static final double NO_SOFTENING = 0.0;
    // The step of the application
    private static final TimeStep ONE_HOUR = new TimeStep(1.0 / 24.0);
    private static final int A_DAY_IN_HOURS = 24;
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3.003e-6),
            new Radius(4.259e-5), new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.0172, 0.0));
    private static final Approach FROM_ABOVE_AIMING_ALONG_X = new Approach(new Vector3(0.0, 0.0, 1.0),
            new Vector3(1.0, 0.0, 0.0));

    // Twenty radii of the Earth at 10 km/s, faster as it falls: a few hours
    @ParameterizedTest
    @EnumSource(Impactor.class)
    void aLaunchedImpactorHitsItsTargetWithinADay(Impactor SUT) {
        Body impactor = SUT.launchedAt(EARTH, FROM_ABOVE_AIMING_ALONG_X, new BodyName("Impactor"));
        Simulation simulation = new Simulation(new VelocityVerlet(new NewtonianGravity(G, NO_SOFTENING)),
                new MergingCollisions(), List.of(EARTH, impactor));

        simulation.advance(ONE_HOUR, A_DAY_IN_HOURS);

        assertThat(simulation.bodies()).extracting(Body::name).containsExactly(EARTH.name());
    }
}

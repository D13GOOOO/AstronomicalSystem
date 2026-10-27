package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.physics.Integrator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SimulationTest {

    private static final Body SUN = new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0),
            new Radius(4.65e-3), Vector3.ZERO, Vector3.ZERO);
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3.003e-6),
            new Radius(4.259e-5), new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.0172, 0.0));

    private final Integrator integrator = mock();

    @Test
    void aNewSimulationHoldsTheInitialBodies() {
        Simulation SUT = new Simulation(integrator, List.of(SUN, EARTH));

        List<Body> bodies = SUT.bodies();

        assertThat(bodies).containsExactly(SUN, EARTH);
    }
}

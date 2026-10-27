package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.physics.Integrator;
import astronomicalsystem.core.physics.TimeStep;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimulationTest {

    private static final TimeStep TWO_DAYS = new TimeStep(2.0);

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

    @Test
    void advancingReplacesTheBodiesWithTheIntegratedOnes() {
        Simulation SUT = new Simulation(integrator, List.of(SUN, EARTH));
        Body movedEarth = EARTH.withMotion(new Vector3(0.0, 1.0, 0.0), new Vector3(-0.0172, 0.0, 0.0));
        when(integrator.step(List.of(SUN, EARTH), TWO_DAYS)).thenReturn(List.of(SUN, movedEarth));

        SUT.advance(TWO_DAYS);

        assertThat(SUT.bodies()).containsExactly(SUN, movedEarth);
    }

    @Test
    void changingTheInitialListLaterDoesNotChangeTheSimulation() {
        List<Body> initialBodies = new ArrayList<>(List.of(SUN, EARTH));
        Simulation SUT = new Simulation(integrator, initialBodies);

        initialBodies.clear();

        assertThat(SUT.bodies()).containsExactly(SUN, EARTH);
    }
}

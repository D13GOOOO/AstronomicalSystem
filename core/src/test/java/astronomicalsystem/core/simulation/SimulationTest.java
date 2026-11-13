package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.physics.CollisionResolver;
import astronomicalsystem.core.physics.Integrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static astronomicalsystem.core.simulation.SolarSystemFixtures.EARTH;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.INITIAL_BODIES;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.INTEGRATED_BODIES;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.MERGED_BODIES;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.MOVED_EARTH;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.SUN;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.TWO_DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimulationTest {

    private static final int TWO_STEPS = 2;
    // A quarter of an orbit per step: after the y-axis, the Earth reaches the negative x-axis
    private static final List<Body> INTEGRATED_TWICE = List.of(SUN,
            MOVED_EARTH.withMotion(new Vector3(-1.0, 0.0, 0.0), new Vector3(0.0, -0.0172, 0.0)));

    private final Integrator integrator = mock();
    private final CollisionResolver collisions = mock();

    @BeforeEach
    void noCollisionsByDefault() {
        when(collisions.resolve(anyList())).thenAnswer(returnsFirstArg());
    }

    @Test
    void aNewSimulationHoldsTheInitialBodies() {
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);

        List<Body> bodies = SUT.bodies();

        assertThat(bodies).containsExactly(SUN, EARTH);
    }

    @Test
    void aNewSimulationResolvesTheCollisionsOfTheInitialBodies() {
        when(collisions.resolve(INITIAL_BODIES)).thenReturn(MERGED_BODIES);
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);

        List<Body> bodies = SUT.bodies();

        assertThat(bodies).containsExactlyElementsOf(MERGED_BODIES);
    }

    @Test
    void advancingReplacesTheBodiesWithTheIntegratedOnes() {
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_BODIES);

        SUT.advance(TWO_DAYS);

        assertThat(SUT.bodies()).containsExactly(SUN, MOVED_EARTH);
    }

    @Test
    void advancingResolvesTheCollisionsOfTheIntegratedBodies() {
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_BODIES);
        when(collisions.resolve(INTEGRATED_BODIES)).thenReturn(MERGED_BODIES);

        SUT.advance(TWO_DAYS);

        assertThat(SUT.bodies()).containsExactlyElementsOf(MERGED_BODIES);
    }

    @Test
    void severalStepsAreTakenOneAfterTheOther() {
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_BODIES);
        when(integrator.step(INTEGRATED_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_TWICE);

        SUT.advance(TWO_DAYS, TWO_STEPS);

        assertThat(SUT.bodies()).containsExactlyElementsOf(INTEGRATED_TWICE);
    }

    @Test
    void changingTheInitialListLaterDoesNotChangeTheSimulation() {
        List<Body> initialBodies = new ArrayList<>(INITIAL_BODIES);
        Simulation SUT = new Simulation(integrator, collisions, initialBodies);

        initialBodies.clear();

        assertThat(SUT.bodies()).containsExactly(SUN, EARTH);
    }

    @Test
    void theExposedBodiesCannotBeModified() {
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);

        List<Body> bodies = SUT.bodies();

        assertThatThrownBy(bodies::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void theExposedBodiesStayReadOnlyAfterAdvancing() {
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenReturn(new ArrayList<>(INTEGRATED_BODIES));
        SUT.advance(TWO_DAYS);

        List<Body> bodies = SUT.bodies();

        assertThatThrownBy(bodies::clear).isInstanceOf(UnsupportedOperationException.class);
    }
}

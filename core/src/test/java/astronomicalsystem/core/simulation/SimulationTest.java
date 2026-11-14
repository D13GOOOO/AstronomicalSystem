package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.physics.Collision;
import astronomicalsystem.core.physics.CollisionResolver;
import astronomicalsystem.core.physics.Integrator;
import astronomicalsystem.core.physics.Resolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static astronomicalsystem.core.simulation.SolarSystemFixtures.ASTEROID;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.EARTH;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.EARTH_INTO_THE_SUN;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.INITIAL_BODIES;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.INTEGRATED_BODIES;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.INTEGRATED_TWICE;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.MERGED_BODIES;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.MOVED_EARTH;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.SUN;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.TWO_DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimulationTest {

    private static final int TWO_STEPS = 2;
    private static final int ONE_STEP = 1;
    private static final List<Collision> NO_COLLISIONS = List.of();
    private static final Collision LATER_COLLISION = new Collision(SUN.name(), new Vector3(0.0, 1.0, 0.0),
            new Vector3(0.0, 1.0, 0.0), 0.2, 1e-8);

    private final Integrator integrator = mock();
    private final CollisionResolver collisions = mock();

    @BeforeEach
    void noCollisionsByDefault() {
        when(collisions.resolve(anyList(), anyList()))
                .thenAnswer(call -> new Resolution(call.getArgument(1), NO_COLLISIONS));
    }

    @Test
    void aNewSimulationHoldsTheInitialBodies() {
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);

        List<Body> bodies = SUT.bodies();

        assertThat(bodies).containsExactly(SUN, EARTH);
    }

    @Test
    void aNewSimulationResolvesTheCollisionsOfTheInitialBodies() {
        when(collisions.resolve(INITIAL_BODIES, INITIAL_BODIES)).thenReturn(new Resolution(MERGED_BODIES, NO_COLLISIONS));
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
        when(collisions.resolve(INITIAL_BODIES, INTEGRATED_BODIES))
                .thenReturn(new Resolution(MERGED_BODIES, NO_COLLISIONS));

        SUT.advance(TWO_DAYS);

        assertThat(SUT.bodies()).containsExactlyElementsOf(MERGED_BODIES);
    }

    @Test
    void anAdvanceTellsTheCollisionsOfItsStep() {
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_BODIES);
        when(collisions.resolve(INITIAL_BODIES, INTEGRATED_BODIES))
                .thenReturn(new Resolution(MERGED_BODIES, List.of(EARTH_INTO_THE_SUN)));

        SUT.advance(TWO_DAYS);

        assertThat(SUT.collisions()).containsExactly(EARTH_INTO_THE_SUN);
    }

    @Test
    void anAdvanceOfSeveralStepsTellsTheCollisionsOfAllOfThem() {
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_BODIES);
        when(integrator.step(INTEGRATED_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_TWICE);
        when(collisions.resolve(INITIAL_BODIES, INTEGRATED_BODIES))
                .thenReturn(new Resolution(INTEGRATED_BODIES, List.of(EARTH_INTO_THE_SUN)));
        when(collisions.resolve(INTEGRATED_BODIES, INTEGRATED_TWICE))
                .thenReturn(new Resolution(INTEGRATED_TWICE, List.of(LATER_COLLISION)));

        SUT.advance(TWO_DAYS, TWO_STEPS);

        assertThat(SUT.collisions()).containsExactly(EARTH_INTO_THE_SUN, LATER_COLLISION);
    }

    @Test
    void anAdvanceForgetsTheCollisionsOfTheOneBefore() {
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_BODIES);
        when(integrator.step(INTEGRATED_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_TWICE);
        when(collisions.resolve(INITIAL_BODIES, INTEGRATED_BODIES))
                .thenReturn(new Resolution(INTEGRATED_BODIES, List.of(EARTH_INTO_THE_SUN)));
        SUT.advance(TWO_DAYS, ONE_STEP);

        SUT.advance(TWO_DAYS, ONE_STEP);

        assertThat(SUT.collisions()).isEmpty();
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
    void anAddedBodyJoinsTheOthers() {
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);

        SUT.add(ASTEROID);

        assertThat(SUT.bodies()).containsExactly(SUN, EARTH, ASTEROID);
    }

    @Test
    void addingABodyResolvesItsCollisions() {
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);
        List<Body> joined = List.of(SUN, EARTH, ASTEROID);
        when(collisions.resolve(joined, joined)).thenReturn(new Resolution(MERGED_BODIES, NO_COLLISIONS));

        SUT.add(ASTEROID);

        assertThat(SUT.bodies()).containsExactlyElementsOf(MERGED_BODIES);
    }

    // Bodies are told apart by their names, for example by the paths and the camera
    @Test
    void aBodyCannotJoinWithTheNameOfAnotherOne() {
        Simulation SUT = new Simulation(integrator, collisions, INITIAL_BODIES);
        Body secondEarth = EARTH.withMotion(new Vector3(0.0, 3.0, 0.0), EARTH.velocity());

        assertThatThrownBy(() -> SUT.add(secondEarth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A body named Earth is already in the simulation");
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

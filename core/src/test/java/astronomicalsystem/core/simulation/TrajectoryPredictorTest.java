package astronomicalsystem.core.simulation;

import astronomicalsystem.core.physics.CollisionResolver;
import astronomicalsystem.core.physics.Integrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static astronomicalsystem.core.simulation.SolarSystemFixtures.EARTH;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.INITIAL_BODIES;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.INTEGRATED_BODIES;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.MOVED_EARTH;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.SUN;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.TWO_DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrajectoryPredictorTest {

    private static final Sampling EVERY_STEP_ONCE = new Sampling(1, 2);

    private final Integrator integrator = mock();
    private final CollisionResolver collisions = mock();

    @BeforeEach
    void noCollisionsByDefault() {
        when(collisions.resolve(anyList())).thenAnswer(returnsFirstArg());
    }

    @Test
    void everyTrajectoryStartsFromThePresentPositionAndFollowsTheIntegrator() {
        TrajectoryPredictor SUT = new TrajectoryPredictor(integrator, collisions, TWO_DAYS, EVERY_STEP_ONCE);
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_BODIES);

        List<Trajectory> trajectories = SUT.predict(INITIAL_BODIES);

        assertThat(trajectories).containsExactly(
                new Trajectory(SUN.name(), List.of(SUN.position(), SUN.position())),
                new Trajectory(EARTH.name(), List.of(EARTH.position(), MOVED_EARTH.position())));
    }
}

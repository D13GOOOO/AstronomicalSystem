package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
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
    private static final Body JUPITER = new Body(new BodyName("Jupiter"), BodyKind.PLANET, new Mass(9.5e-4),
            new Radius(4.67e-4), new Vector3(5.0, 0.0, 0.0), new Vector3(0.0, 0.0076, 0.0));
    private static final Body MOVED_JUPITER = JUPITER.withMotion(new Vector3(5.0, 0.0152, 0.0), JUPITER.velocity());
    // The present position, then the ones after two and four steps
    private static final Sampling EVERY_OTHER_STEP_TWICE = new Sampling(2, 3);

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

    @Test
    void aPointIsTakenEveryFewSteps() {
        TrajectoryPredictor SUT = new TrajectoryPredictor(integrator, collisions, TWO_DAYS, EVERY_OTHER_STEP_TWICE);
        when(integrator.step(List.of(SUN, EARTH), TWO_DAYS)).thenReturn(List.of(SUN, earthAt(1.0)));
        when(integrator.step(List.of(SUN, earthAt(1.0)), TWO_DAYS)).thenReturn(List.of(SUN, earthAt(2.0)));
        when(integrator.step(List.of(SUN, earthAt(2.0)), TWO_DAYS)).thenReturn(List.of(SUN, earthAt(3.0)));
        when(integrator.step(List.of(SUN, earthAt(3.0)), TWO_DAYS)).thenReturn(List.of(SUN, earthAt(4.0)));

        List<Trajectory> trajectories = SUT.predict(INITIAL_BODIES);

        assertThat(trajectories).containsExactly(
                new Trajectory(SUN.name(), List.of(SUN.position(), SUN.position(), SUN.position())),
                new Trajectory(EARTH.name(),
                        List.of(EARTH.position(), earthAt(2.0).position(), earthAt(4.0).position())));
    }

    // The Earth falls into the Sun, which keeps its name; Jupiter moves from the third place to the second
    @Test
    void theTrajectoryOfABodyThatMergesIntoAnotherOneEndsWhereItWasLastSeen() {
        TrajectoryPredictor SUT = new TrajectoryPredictor(integrator, collisions, TWO_DAYS, EVERY_STEP_ONCE);
        when(integrator.step(List.of(SUN, EARTH, JUPITER), TWO_DAYS))
                .thenReturn(List.of(SUN, MOVED_EARTH, MOVED_JUPITER));
        Body sunWithTheEarth = SUN.mergedWith(MOVED_EARTH);
        when(collisions.resolve(List.of(SUN, MOVED_EARTH, MOVED_JUPITER)))
                .thenReturn(List.of(sunWithTheEarth, MOVED_JUPITER));

        List<Trajectory> trajectories = SUT.predict(List.of(SUN, EARTH, JUPITER));

        assertThat(trajectories).containsExactly(
                new Trajectory(SUN.name(), List.of(SUN.position(), sunWithTheEarth.position())),
                new Trajectory(EARTH.name(), List.of(EARTH.position())),
                new Trajectory(JUPITER.name(), List.of(JUPITER.position(), MOVED_JUPITER.position())));
    }

    // The Earth after some steps, on the y-axis as far as the number of steps
    private static Body earthAt(double y) {
        return EARTH.withMotion(new Vector3(0.0, y, 0.0), EARTH.velocity());
    }
}

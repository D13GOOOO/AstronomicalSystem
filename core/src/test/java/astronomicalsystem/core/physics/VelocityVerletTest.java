package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VelocityVerletTest {

    private static final TimeStep TWO_DAYS = new TimeStep(2.0);
    private static final Body PROBE = new Body(new BodyName("Probe"), BodyKind.PLANET, new Mass(1e-3),
            new Radius(1e-3), new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.5, 0.0));
    private static final List<Vector3> NO_PULL = List.of(Vector3.ZERO);
    private static final List<Vector3> PULL_ALONG_X = List.of(new Vector3(0.25, 0.0, 0.0));
    private static final List<Vector3> PULL_ALONG_Y = List.of(new Vector3(0.0, 0.25, 0.0));

    private final GravityModel gravity = mock();
    private final VelocityVerlet SUT = new VelocityVerlet(gravity);

    @Test
    void withoutAccelerationABodyMovesInAStraightLineAtConstantVelocity() {
        when(gravity.accelerations(anyList())).thenReturn(NO_PULL);

        List<Body> stepped = SUT.step(List.of(PROBE), TWO_DAYS);

        assertThat(stepped).containsExactly(PROBE.withMotion(new Vector3(1.0, 1.0, 0.0), PROBE.velocity()));
    }

    @Test
    void underConstantAccelerationABodyFollowsAParabola() {
        when(gravity.accelerations(anyList())).thenReturn(PULL_ALONG_X);

        List<Body> stepped = SUT.step(List.of(PROBE), TWO_DAYS);

        assertThat(stepped).containsExactly(
                PROBE.withMotion(new Vector3(1.5, 1.0, 0.0), new Vector3(0.5, 0.5, 0.0)));
    }

    @Test
    void velocityChangesByTheAverageOfTheOldAndTheNewAcceleration() {
        when(gravity.accelerations(anyList())).thenReturn(PULL_ALONG_X).thenReturn(PULL_ALONG_Y);

        List<Body> stepped = SUT.step(List.of(PROBE), TWO_DAYS);

        assertThat(stepped).containsExactly(
                PROBE.withMotion(new Vector3(1.5, 1.0, 0.0), new Vector3(0.25, 0.75, 0.0)));
    }

    @Test
    void theNewAccelerationIsEvaluatedAtTheDriftedPositions() {
        when(gravity.accelerations(anyList())).thenReturn(PULL_ALONG_X);

        SUT.step(List.of(PROBE), TWO_DAYS);

        ArgumentCaptor<List<Body>> evaluated = ArgumentCaptor.captor();
        verify(gravity, times(2)).accelerations(evaluated.capture());
        assertThat(evaluated.getValue()).extracting(Body::position).containsExactly(new Vector3(1.5, 1.0, 0.0));
    }
}

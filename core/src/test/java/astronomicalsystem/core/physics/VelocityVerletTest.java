package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VelocityVerletTest {

    private static final TimeStep TWO_DAYS = new TimeStep(2.0);

    private final GravityModel gravity = mock();
    private final VelocityVerlet SUT = new VelocityVerlet(gravity);

    @Test
    void withoutAccelerationABodyMovesInAStraightLineAtConstantVelocity() {
        Vector3 velocity = new Vector3(0.0, 0.5, 0.0);
        Body body = body(new Vector3(1.0, 0.0, 0.0), velocity);
        when(gravity.accelerations(anyList())).thenReturn(List.of(Vector3.ZERO));

        List<Body> stepped = SUT.step(List.of(body), TWO_DAYS);

        assertThat(stepped).containsExactly(body.withMotion(new Vector3(1.0, 1.0, 0.0), velocity));
    }

    private static Body body(Vector3 position, Vector3 velocity) {
        return new Body(new BodyName("Probe"), BodyKind.PLANET, new Mass(1e-3), new Radius(1e-3),
                position, velocity);
    }
}

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

class NewtonianGravityTest {

    private static final double G = 2.959122082855911e-4;
    private static final double NO_SOFTENING = 0.0;

    private final NewtonianGravity SUT = new NewtonianGravity(G, NO_SOFTENING);

    @Test
    void lonelyBodyHasNoAcceleration() {
        List<Vector3> accelerations = SUT.accelerations(List.of(body("Sun", 1.0, Vector3.ZERO)));

        assertThat(accelerations).containsExactly(new Vector3(0.0, 0.0, 0.0));
    }

    private static Body body(String name, double solarMasses, Vector3 position) {
        return new Body(new BodyName(name), BodyKind.PLANET, new Mass(solarMasses), new Radius(1e-3),
                position, Vector3.ZERO);
    }
}

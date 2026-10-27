package astronomicalsystem.core.body;

import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BodyTest {

    private static final BodyName EARTH = new BodyName("Earth");
    private static final Mass EARTH_MASS = new Mass(3.003e-6);
    private static final Radius EARTH_RADIUS = new Radius(4.259e-5);

    @Test
    void withMotionReplacesOnlyPositionAndVelocity() {
        Body SUT = new Body(EARTH, BodyKind.PLANET, EARTH_MASS, EARTH_RADIUS,
                new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.0172, 0.0));

        Body moved = SUT.withMotion(new Vector3(0.0, 1.0, 0.0), new Vector3(-0.0172, 0.0, 0.0));

        assertThat(moved).isEqualTo(new Body(EARTH, BodyKind.PLANET, EARTH_MASS, EARTH_RADIUS,
                new Vector3(0.0, 1.0, 0.0), new Vector3(-0.0172, 0.0, 0.0)));
    }

    @Test
    void driftedMovesThePositionAlongTheVelocity() {
        Body SUT = new Body(EARTH, BodyKind.PLANET, EARTH_MASS, EARTH_RADIUS,
                new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.25, 0.0));

        Body drifted = SUT.drifted(2.0);

        assertThat(drifted).isEqualTo(new Body(EARTH, BodyKind.PLANET, EARTH_MASS, EARTH_RADIUS,
                new Vector3(1.0, 0.5, 0.0), new Vector3(0.0, 0.25, 0.0)));
    }

    @Test
    void kickedChangesTheVelocityAlongTheAcceleration() {
        Body SUT = new Body(EARTH, BodyKind.PLANET, EARTH_MASS, EARTH_RADIUS,
                new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.25, 0.0));

        Body kicked = SUT.kicked(new Vector3(-0.125, 0.0, 0.0), 2.0);

        assertThat(kicked).isEqualTo(new Body(EARTH, BodyKind.PLANET, EARTH_MASS, EARTH_RADIUS,
                new Vector3(1.0, 0.0, 0.0), new Vector3(-0.25, 0.25, 0.0)));
    }
}

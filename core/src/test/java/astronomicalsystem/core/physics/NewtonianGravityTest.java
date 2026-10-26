package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NewtonianGravityTest {

    private static final double G = 2.959122082855911e-4;
    private static final double NO_SOFTENING = 0.0;
    private static final double SUN_MASS = 1.0;
    private static final double PLANET_MASS = 1e-3;
    private static final double TOLERANCE = 1e-15;
    private static final Comparator<Vector3> CLOSE_ENOUGH =
            (actual, expected) -> actual.minus(expected).length() < TOLERANCE ? 0 : 1;

    private final NewtonianGravity SUT = new NewtonianGravity(G, NO_SOFTENING);

    @Test
    void lonelyBodyHasNoAcceleration() {
        List<Vector3> accelerations = SUT.accelerations(List.of(body("Sun", SUN_MASS, Vector3.ZERO)));

        assertThat(accelerations).containsExactly(new Vector3(0.0, 0.0, 0.0));
    }

    @Test
    void twoBodiesAttractEachOtherInverselyToTheSquareOfTheDistance() {
        Body sun = body("Sun", SUN_MASS, Vector3.ZERO);
        Body planet = body("Planet", PLANET_MASS, new Vector3(1.0, 0.0, 0.0));

        List<Vector3> accelerations = SUT.accelerations(List.of(sun, planet));

        assertThat(accelerations)
                .usingElementComparator(CLOSE_ENOUGH)
                .containsExactly(new Vector3(G * PLANET_MASS, 0.0, 0.0), new Vector3(-G * SUN_MASS, 0.0, 0.0));
    }

    @Test
    void pullsFromSeveralBodiesAddUp() {
        Body left = body("Left", PLANET_MASS, new Vector3(-1.0, 0.0, 0.0));
        Body sun = body("Sun", SUN_MASS, Vector3.ZERO);
        Body right = body("Right", PLANET_MASS, new Vector3(1.0, 0.0, 0.0));

        List<Vector3> accelerations = SUT.accelerations(List.of(left, sun, right));

        assertThat(accelerations)
                .usingElementComparator(CLOSE_ENOUGH)
                .containsExactly(
                        new Vector3(G * (SUN_MASS + PLANET_MASS / 4.0), 0.0, 0.0),
                        new Vector3(0.0, 0.0, 0.0),
                        new Vector3(-G * (SUN_MASS + PLANET_MASS / 4.0), 0.0, 0.0));
    }

    @Nested
    class WithSoftening {

        private static final double SOFTENING = 1.0;

        private final NewtonianGravity SUT = new NewtonianGravity(G, SOFTENING);

        @Test
        void softeningWeakensThePullAtShortDistances() {
            Body sun = body("Sun", SUN_MASS, Vector3.ZERO);
            Body planet = body("Planet", PLANET_MASS, new Vector3(1.0, 0.0, 0.0));

            List<Vector3> accelerations = SUT.accelerations(List.of(sun, planet));

            double softenedCube = 2.0 * Math.sqrt(2.0);
            assertThat(accelerations)
                    .usingElementComparator(CLOSE_ENOUGH)
                    .containsExactly(
                            new Vector3(G * PLANET_MASS / softenedCube, 0.0, 0.0),
                            new Vector3(-G * SUN_MASS / softenedCube, 0.0, 0.0));
        }
    }

    private static Body body(String name, double solarMasses, Vector3 position) {
        return new Body(new BodyName(name), BodyKind.PLANET, new Mass(solarMasses), new Radius(1e-3),
                position, Vector3.ZERO);
    }
}

package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Tolerances;
import astronomicalsystem.core.math.Vector3;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NewtonianGravityTest {

    private static final double G = 2.959122082855911e-4;
    private static final double NO_SOFTENING = 0.0;
    private static final double SUN_MASS = 1.0;
    private static final double PLANET_MASS = 1e-3;
    private static final double TOLERANCE = 1e-15;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = Tolerances.closeEnough(TOLERANCE);

    private final NewtonianGravity SUT = new NewtonianGravity(G, NO_SOFTENING);

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsInvalidGravitationalConstant(double gravitationalConstant) {
        assertThatThrownBy(() -> new NewtonianGravity(gravitationalConstant, NO_SOFTENING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Gravitational constant must be positive and finite");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsInvalidSofteningLength(double softeningLength) {
        assertThatThrownBy(() -> new NewtonianGravity(G, softeningLength))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Softening length must be non-negative and finite");
    }

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
                .usingRecursiveFieldByFieldElementComparator(CLOSE_ENOUGH)
                .containsExactly(new Vector3(G * PLANET_MASS, 0.0, 0.0), new Vector3(-G * SUN_MASS, 0.0, 0.0));
    }

    @Test
    void pullsFromSeveralBodiesAddUp() {
        Body left = body("Left", PLANET_MASS, new Vector3(-1.0, 0.0, 0.0));
        Body sun = body("Sun", SUN_MASS, Vector3.ZERO);
        Body right = body("Right", PLANET_MASS, new Vector3(1.0, 0.0, 0.0));

        List<Vector3> accelerations = SUT.accelerations(List.of(left, sun, right));

        assertThat(accelerations)
                .usingRecursiveFieldByFieldElementComparator(CLOSE_ENOUGH)
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
                    .usingRecursiveFieldByFieldElementComparator(CLOSE_ENOUGH)
                    .containsExactly(
                            new Vector3(G * PLANET_MASS / softenedCube, 0.0, 0.0),
                            new Vector3(-G * SUN_MASS / softenedCube, 0.0, 0.0));
        }

        @Test
        void coincidentBodiesDoNotPullEachOther() {
            Body sun = body("Sun", SUN_MASS, Vector3.ZERO);
            Body planet = body("Planet", PLANET_MASS, Vector3.ZERO);

            List<Vector3> accelerations = SUT.accelerations(List.of(sun, planet));

            assertThat(accelerations).containsExactly(new Vector3(0.0, 0.0, 0.0), new Vector3(0.0, 0.0, 0.0));
        }
    }

    private static Body body(String name, double solarMasses, Vector3 position) {
        return new Body(new BodyName(name), BodyKind.PLANET, new Mass(solarMasses), new Radius(1e-3),
                position, Vector3.ZERO);
    }
}

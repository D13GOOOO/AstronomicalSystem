package astronomicalsystem.core.impact;

import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ConeTest {

    private static final double HALF_A_RIGHT_ANGLE = Math.PI / 4.0;
    private static final double TOLERANCE = 1e-12;
    private static final Vector3 Z_AXIS = new Vector3(0.0, 0.0, 1.0);
    // Evenly spread directions each have a patch of the cap of solid angle 2 pi (1 - cos a) / n,
    // about the square of their distance from the nearest one; a good spread keeps most of it
    private static final double SPREAD = 0.8;

    // Along the z-axis, and along a slanting axis
    @ParameterizedTest(name = "{3} directions around ({0}, {1}, {2})")
    @CsvSource(textBlock = """
            0.0, 0.0, 1.0,  1
            0.0, 0.0, 1.0, 24
            1.0, 1.0, 0.0, 24
            """)
    void everyDirectionIsAUnitVectorInsideTheCone(double axisX, double axisY, double axisZ, int count) {
        Vector3 axis = new Vector3(axisX, axisY, axisZ);
        Cone SUT = new Cone(axis, HALF_A_RIGHT_ANGLE);

        List<Vector3> directions = SUT.directions(count);

        assertThat(directions).hasSize(count).allSatisfy(direction -> {
            assertThat(direction.length()).isCloseTo(1.0, within(TOLERANCE));
            assertThat(direction.dot(axis.normalized())).isGreaterThanOrEqualTo(Math.cos(HALF_A_RIGHT_ANGLE) - TOLERANCE);
        });
    }

    @ParameterizedTest(name = "{0} directions")
    @ValueSource(ints = {2, 8, 24})
    void theDirectionsAreSpreadEvenlyOverTheCone(int count) {
        Cone SUT = new Cone(Z_AXIS, HALF_A_RIGHT_ANGLE);

        List<Vector3> directions = SUT.directions(count);

        double patch = 2.0 * Math.PI * (1.0 - Math.cos(HALF_A_RIGHT_ANGLE)) / count;
        assertThat(smallestAngleBetween(directions)).isGreaterThanOrEqualTo(SPREAD * Math.sqrt(patch));
    }

    private static double smallestAngleBetween(List<Vector3> directions) {
        return IntStream.range(0, directions.size()).boxed()
                .flatMap(i -> IntStream.range(i + 1, directions.size()).mapToObj(j -> angle(directions.get(i), directions.get(j))))
                .min(Double::compare).orElseThrow();
    }

    private static double angle(Vector3 first, Vector3 second) {
        return Math.atan2(first.cross(second).length(), first.dot(second));
    }
}

package astronomicalsystem.core.impact;

import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ConeTest {

    private static final double HALF_A_RIGHT_ANGLE = Math.PI / 4.0;
    private static final double TOLERANCE = 1e-12;

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
}

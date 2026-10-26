package astronomicalsystem.core.math;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class Vector3Test {

    private static final double TOLERANCE = 1e-12;

    @Test
    void plusAddsEachComponent() {
        Vector3 SUT = new Vector3(1.0, 2.0, 3.0);

        Vector3 sum = SUT.plus(new Vector3(10.0, 20.0, 30.0));

        assertThat(sum).isEqualTo(new Vector3(11.0, 22.0, 33.0));
    }

    @Test
    void minusSubtractsEachComponent() {
        Vector3 SUT = new Vector3(11.0, 22.0, 33.0);

        Vector3 difference = SUT.minus(new Vector3(1.0, 2.0, 3.0));

        assertThat(difference).isEqualTo(new Vector3(10.0, 20.0, 30.0));
    }

    @Test
    void timesScalesEachComponent() {
        Vector3 SUT = new Vector3(1.0, -2.0, 3.0);

        Vector3 scaled = SUT.times(2.5);

        assertThat(scaled).isEqualTo(new Vector3(2.5, -5.0, 7.5));
    }

    @Test
    void dotSumsProductsOfComponents() {
        Vector3 SUT = new Vector3(1.0, 2.0, 3.0);

        double product = SUT.dot(new Vector3(4.0, -5.0, 6.0));

        assertThat(product).isEqualTo(12.0);
    }

    @Test
    void lengthSquaredSumsSquaresOfComponents() {
        Vector3 SUT = new Vector3(2.0, -3.0, 6.0);

        double lengthSquared = SUT.lengthSquared();

        assertThat(lengthSquared).isEqualTo(49.0);
    }

    @Test
    void lengthIsEuclideanNorm() {
        Vector3 SUT = new Vector3(2.0, -3.0, 6.0);

        double length = SUT.length();

        assertThat(length).isEqualTo(7.0);
    }

    @Test
    void normalizedKeepsDirectionWithUnitLength() {
        Vector3 SUT = new Vector3(0.0, 3.0, 4.0);

        Vector3 unit = SUT.normalized();

        assertThat(unit.x()).isCloseTo(0.0, within(TOLERANCE));
        assertThat(unit.y()).isCloseTo(0.6, within(TOLERANCE));
        assertThat(unit.z()).isCloseTo(0.8, within(TOLERANCE));
    }

    @Test
    void normalizedRejectsZeroVector() {
        Vector3 SUT = new Vector3(0.0, 0.0, 0.0);

        assertThatThrownBy(SUT::normalized)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot normalize the zero vector");
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            NaN,      0.0,       0.0
            0.0,      NaN,       0.0
            0.0,      0.0,       NaN
            Infinity, 0.0,       0.0
            0.0,      -Infinity, 0.0
            """)
    void constructorRejectsNonFiniteComponents(double x, double y, double z) {
        assertThatThrownBy(() -> new Vector3(x, y, z))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vector components must be finite");
    }
}

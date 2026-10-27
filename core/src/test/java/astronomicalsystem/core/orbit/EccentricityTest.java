package astronomicalsystem.core.orbit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EccentricityTest {

    private static final double CIRCLE = 0.0;
    private static final double EARTH = 0.0167;
    private static final double HALLEY = 0.967;
    // Math.nextDown(1.0): the largest double below 1
    private static final double LARGEST_BELOW_ONE = 0.9999999999999999;

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.0, 1.5, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsValuesOutsideTheEllipticRange(double value) {
        assertThatThrownBy(() -> new Eccentricity(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Eccentricity must be at least 0 and less than 1");
    }

    @ParameterizedTest
    @ValueSource(doubles = {CIRCLE, EARTH, HALLEY, LARGEST_BELOW_ONE})
    void constructorAcceptsEllipticValues(double value) {
        assertThatNoException().isThrownBy(() -> new Eccentricity(value));
    }
}

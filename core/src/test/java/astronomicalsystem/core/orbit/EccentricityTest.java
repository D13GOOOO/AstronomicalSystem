package astronomicalsystem.core.orbit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EccentricityTest {

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.0, 1.5, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsValuesOutsideTheEllipticRange(double value) {
        assertThatThrownBy(() -> new Eccentricity(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Eccentricity must be at least 0 and less than 1");
    }
}

package astronomicalsystem.core.body;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RadiusTest {

    private static final double EARTH_ASTRONOMICAL_UNITS = 4.259e-5;

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsNonPositiveOrNonFiniteValues(double astronomicalUnits) {
        assertThatThrownBy(() -> new Radius(astronomicalUnits))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Radius must be positive and finite");
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.MIN_VALUE, EARTH_ASTRONOMICAL_UNITS})
    void constructorAcceptsPositiveFiniteValues(double astronomicalUnits) {
        assertThatNoException().isThrownBy(() -> new Radius(astronomicalUnits));
    }
}

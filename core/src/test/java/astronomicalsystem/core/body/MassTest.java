package astronomicalsystem.core.body;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MassTest {

    private static final double EARTH_SOLAR_MASSES = 3.003e-6;

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsNonPositiveOrNonFiniteValues(double solarMasses) {
        assertThatThrownBy(() -> new Mass(solarMasses))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mass must be positive and finite");
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.MIN_VALUE, EARTH_SOLAR_MASSES})
    void constructorAcceptsPositiveFiniteValues(double solarMasses) {
        assertThatNoException().isThrownBy(() -> new Mass(solarMasses));
    }
}

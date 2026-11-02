package astronomicalsystem.core.orbit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GravitationalParameterTest {

    // k²(1 + m/M☉) for the Earth around the Sun, in AU³ per day squared
    private static final double SUN_AND_EARTH = 2.959122082855911e-4 * (1.0 + 3.003e-6);

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsNonPositiveOrNonFiniteValues(double auCubedPerDaySquared) {
        assertThatThrownBy(() -> new GravitationalParameter(auCubedPerDaySquared))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Gravitational parameter must be positive and finite");
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.MIN_VALUE, SUN_AND_EARTH})
    void constructorAcceptsPositiveFiniteValues(double auCubedPerDaySquared) {
        assertThatNoException().isThrownBy(() -> new GravitationalParameter(auCubedPerDaySquared));
    }
}

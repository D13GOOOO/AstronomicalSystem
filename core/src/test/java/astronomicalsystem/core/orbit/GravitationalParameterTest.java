package astronomicalsystem.core.orbit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GravitationalParameterTest {

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsNonPositiveOrNonFiniteValues(double auCubedPerDaySquared) {
        assertThatThrownBy(() -> new GravitationalParameter(auCubedPerDaySquared))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Gravitational parameter must be positive and finite");
    }
}

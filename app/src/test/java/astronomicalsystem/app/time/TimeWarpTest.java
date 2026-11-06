package astronomicalsystem.app.time;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeWarpTest {

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsNonPositiveOrNonFiniteValues(double daysPerSecond) {
        assertThatThrownBy(() -> new TimeWarp(daysPerSecond))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Time warp must be positive and finite");
    }
}

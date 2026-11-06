package astronomicalsystem.app.time;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeWarpTest {

    private static final double SLOW_MOTION = 0.5;
    private static final double A_YEAR_PER_SECOND = 365.25;

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsNonPositiveOrNonFiniteValues(double daysPerSecond) {
        assertThatThrownBy(() -> new TimeWarp(daysPerSecond))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Time warp must be positive and finite");
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.MIN_VALUE, SLOW_MOTION, A_YEAR_PER_SECOND})
    void constructorAcceptsPositiveFiniteValues(double daysPerSecond) {
        assertThatNoException().isThrownBy(() -> new TimeWarp(daysPerSecond));
    }
}

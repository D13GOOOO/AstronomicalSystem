package astronomicalsystem.core.physics;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeStepTest {

    private static final double ONE_HOUR_IN_DAYS = 1.0 / 24.0;

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsNonPositiveOrNonFiniteValues(double days) {
        assertThatThrownBy(() -> new TimeStep(days))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Time step must be positive and finite");
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.MIN_VALUE, ONE_HOUR_IN_DAYS})
    void constructorAcceptsPositiveFiniteValues(double days) {
        assertThatNoException().isThrownBy(() -> new TimeStep(days));
    }
}

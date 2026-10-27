package astronomicalsystem.core.physics;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeStepTest {

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsNonPositiveOrNonFiniteValues(double days) {
        assertThatThrownBy(() -> new TimeStep(days))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Time step must be positive and finite");
    }
}

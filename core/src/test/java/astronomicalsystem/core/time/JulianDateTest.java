package astronomicalsystem.core.time;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JulianDateTest {

    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void constructorRejectsNonFiniteValues(double days) {
        assertThatThrownBy(() -> new JulianDate(days))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Julian date must be finite");
    }
}

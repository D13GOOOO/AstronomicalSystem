package astronomicalsystem.core.time;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JulianDateTest {

    private static final double J2000 = 2451545.0;
    private static final double START_OF_2026 = 2461041.5;
    private static final double ORIGIN = 0.0;
    // Noon of 1 January 4714 BC, one Julian year before the origin
    private static final double BEFORE_THE_ORIGIN = -365.25;

    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void constructorRejectsNonFiniteValues(double days) {
        assertThatThrownBy(() -> new JulianDate(days))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Julian date must be finite");
    }

    @ParameterizedTest
    @ValueSource(doubles = {J2000, START_OF_2026, ORIGIN, BEFORE_THE_ORIGIN})
    void constructorAcceptsFiniteValues(double days) {
        assertThatNoException().isThrownBy(() -> new JulianDate(days));
    }
}

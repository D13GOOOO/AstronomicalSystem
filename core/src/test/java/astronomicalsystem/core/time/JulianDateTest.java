package astronomicalsystem.core.time;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class JulianDateTest {

    private static final double J2000 = 2451545.0;
    private static final double START_OF_2026 = 2461041.5;
    private static final double ORIGIN = 0.0;
    // Noon of 1 January 4714 BC, one Julian year before the origin
    private static final double BEFORE_THE_ORIGIN = -365.25;
    // In days, about 0.09 ms: a Julian date of this century has a unit in the last place of 0.04 ms
    private static final double TWO_UNITS_IN_THE_LAST_PLACE = 1e-9;

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

    // TDB is ahead of UTC by 69.184 s = 0.000800740741 d: 37 leap seconds plus the 32.184 s
    // between TAI and TT
    @ParameterizedTest
    @CsvSource(textBlock = """
            2026-01-01T00:00:00Z, 2461041.500800741
            2026-01-01T12:00:00Z, 2461042.000800741
            """)
    void atConvertsAUtcInstantToBarycentricDynamicalTime(String instant, double julianDate) {
        JulianDate date = JulianDate.at(Instant.parse(instant));

        assertThat(date.days()).isCloseTo(julianDate, within(TWO_UNITS_IN_THE_LAST_PLACE));
    }
}

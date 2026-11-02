package astronomicalsystem.core.orbit;

import astronomicalsystem.core.body.Mass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
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

    // Masses chosen so that their sums, 1 and 2 solar masses, give k² and 2k² exactly
    @ParameterizedTest
    @CsvSource(textBlock = """
            0.75, 0.25, 2.959122082855911e-4
            1.5,  0.5,  5.918244165711822e-4
            """)
    void theParameterOfTwoMassesIsTheGaussianConstantTimesTheirSum(double central, double orbiting,
                                                                   double expected) {
        GravitationalParameter SUT = GravitationalParameter.of(new Mass(central), new Mass(orbiting));

        assertThat(SUT).isEqualTo(new GravitationalParameter(expected));
    }
}

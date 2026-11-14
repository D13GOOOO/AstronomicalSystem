package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Mass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

class ImpactTest {

    private static final Mass EARTH = new Mass(3.003e-6);
    private static final Mass SMALL_ASTEROID = new Mass(1e-18);
    private static final double EARTH_RADIUS = 4.259e-5;
    // km/s to AU per day: 86 400 s a day over the kilometres in an AU
    private static final double KILOMETRES_PER_SECOND = 86_400.0 / 149_597_870.7;
    private static final Mass HALF_AN_EARTH = new Mass(1.5e-6);
    // The expected masses were worked out in SI units, independently of the code
    private static final double PRECISION_PERCENT = 0.01;

    // The escape speed of the Earth is 11.18 km/s
    @ParameterizedTest(name = "{0} km/s")
    @CsvSource(textBlock = """
            11.0, true
            11.4, false
            """)
    void bodiesMergeWholeWhenTheyMeetSlowerThanTheirEscapeSpeed(double kilometresPerSecond, boolean merges) {
        Impact SUT = new Impact(EARTH, SMALL_ASTEROID, EARTH_RADIUS, kilometresPerSecond * KILOMETRES_PER_SECOND);

        assertThat(SUT.merges()).isEqualTo(merges);
    }

    // Equal masses: Q_R = v² / 8 equals half of Q*_RD = 1.9 (4/5) pi rho1 G R_C1² = 4.034e7 J/kg at
    // 7.94 km/s, and the largest remnant keeps 1 - Q_R / (2 Q*_RD) = 3/4 of the total mass
    @Test
    void anImpactAtHalfTheDisruptionEnergyLeavesThreeQuartersOfTheMass() {
        Impact SUT = new Impact(HALF_AN_EARTH, HALF_AN_EARTH, EARTH_RADIUS, 0.007336643843930438);

        Mass remnant = SUT.largestRemnant();

        assertThat(remnant.solarMasses()).isCloseTo(2.25e-6, withinPercentage(PRECISION_PERCENT));
    }
}

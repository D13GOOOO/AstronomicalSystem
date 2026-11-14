package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Mass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ImpactTest {

    private static final Mass EARTH = new Mass(3.003e-6);
    private static final Mass SMALL_ASTEROID = new Mass(1e-18);
    private static final double EARTH_RADIUS = 4.259e-5;
    // km/s to AU per day: 86 400 s a day over the kilometres in an AU
    private static final double KILOMETRES_PER_SECOND = 86_400.0 / 149_597_870.7;

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
}

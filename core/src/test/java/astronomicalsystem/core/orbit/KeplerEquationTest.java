package astronomicalsystem.core.orbit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class KeplerEquationTest {

    private static final double TOLERANCE = 1e-12;
    private static final double NEARLY_PARABOLIC = 0.999999;
    private static final int GRID_STEPS_PER_HALF_TURN = 100_000;

    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            periapsis,                  0.0,                0.5
            apoapsis,                   3.141592653589793,  0.5
            quarter of the ellipse,     1.5707963267948966, 0.5
            Earth,                      1.0,                0.0167
            before periapsis,          -1.0,                0.5
            Halley near perihelion,     0.1,                0.967
            almost parabolic,           0.05,               0.99
            """)
    void solvesForTheEccentricAnomaly(String description, double eccentricAnomaly, double eccentricity) {
        double meanAnomaly = eccentricAnomaly - eccentricity * Math.sin(eccentricAnomaly);

        double solved = KeplerEquation.eccentricAnomaly(meanAnomaly, new Eccentricity(eccentricity));

        assertThat(solved).isCloseTo(eccentricAnomaly, within(TOLERANCE));
    }

    // A starting guess E = M makes Newton's method cycle or diverge for some of these mean anomalies
    @Test
    @Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void convergesForEveryMeanAnomalyOfANearlyParabolicOrbit() {
        List<Double> meanAnomalies = meanAnomaliesOverAFullTurn();

        List<Double> solved = meanAnomalies.stream()
                .map(meanAnomaly -> KeplerEquation.eccentricAnomaly(meanAnomaly, new Eccentricity(NEARLY_PARABOLIC)))
                .toList();

        assertThat(solved).zipSatisfy(meanAnomalies, (eccentricAnomaly, meanAnomaly) ->
                assertThat(eccentricAnomaly - NEARLY_PARABOLIC * Math.sin(eccentricAnomaly))
                        .isCloseTo(meanAnomaly, within(TOLERANCE)));
    }

    private static List<Double> meanAnomaliesOverAFullTurn() {
        return IntStream.rangeClosed(-GRID_STEPS_PER_HALF_TURN, GRID_STEPS_PER_HALF_TURN)
                .mapToObj(step -> Math.PI * step / GRID_STEPS_PER_HALF_TURN)
                .toList();
    }
}

package astronomicalsystem.core.orbit;

import astronomicalsystem.core.math.Vector3;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.assertj.core.util.DoubleComparator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrbitalElementsTest {

    private static final double SEMI_MAJOR_AXIS = 2.0;
    // With e = 0.6 the semi-minor axis is exactly 0.8 a, and with μ = 8 the mean motion √(μ/a³) is 1
    private static final double ECCENTRICITY = 0.6;
    private static final double GRAVITATIONAL_PARAMETER = 8.0;
    private static final double TOLERANCE = 1e-12;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = RecursiveComparisonConfiguration.builder()
            .withComparatorForType(new DoubleComparator(TOLERANCE), Double.class)
            .build();

    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            periapsis,           0.0,                 0.8, 0.0
            apoapsis,            3.141592653589793,  -3.2, 0.0
            end of minor axis,   1.5707963267948966, -1.2, 1.6
            """)
    void inTheEclipticPlaneThePeriapsisLiesAlongX(String description, double eccentricAnomaly,
                                                  double x, double y) {
        OrbitalElements SUT = inTheEclipticPlane(meanAnomalyAt(eccentricAnomaly));

        StateVector state = SUT.stateVector(GRAVITATIONAL_PARAMETER);

        assertThat(state.position()).usingRecursiveComparison(CLOSE_ENOUGH).isEqualTo(new Vector3(x, y, 0.0));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            periapsis,           0.0,                 0.0,  4.0
            apoapsis,            3.141592653589793,   0.0, -1.0
            end of minor axis,   1.5707963267948966, -2.0,  0.0
            """)
    void inTheEclipticPlaneTheOrbitIsTravelledCounterclockwise(String description, double eccentricAnomaly,
                                                               double vx, double vy) {
        OrbitalElements SUT = inTheEclipticPlane(meanAnomalyAt(eccentricAnomaly));

        StateVector state = SUT.stateVector(GRAVITATIONAL_PARAMETER);

        assertThat(state.velocity()).usingRecursiveComparison(CLOSE_ENOUGH).isEqualTo(new Vector3(vx, vy, 0.0));
    }

    private static OrbitalElements inTheEclipticPlane(double meanAnomaly) {
        return new OrbitalElements(SEMI_MAJOR_AXIS, new Eccentricity(ECCENTRICITY), 0.0, 0.0, 0.0, meanAnomaly);
    }

    private static double meanAnomalyAt(double eccentricAnomaly) {
        return eccentricAnomaly - ECCENTRICITY * Math.sin(eccentricAnomaly);
    }
}

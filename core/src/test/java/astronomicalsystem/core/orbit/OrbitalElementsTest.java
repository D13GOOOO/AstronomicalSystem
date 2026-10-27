package astronomicalsystem.core.orbit;

import astronomicalsystem.core.math.Vector3;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.assertj.core.util.DoubleComparator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrbitalElementsTest {

    private static final double SEMI_MAJOR_AXIS = 2.0;
    private static final double ECCENTRICITY = 0.5;
    private static final double GRAVITATIONAL_PARAMETER = 1.0;
    private static final double TOLERANCE = 1e-12;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = RecursiveComparisonConfiguration.builder()
            .withComparatorForType(new DoubleComparator(TOLERANCE), Double.class)
            .build();

    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            periapsis,           0.0,                 1.0, 0.0
            apoapsis,            3.141592653589793,  -3.0, 0.0
            end of minor axis,   1.5707963267948966, -1.0, 1.7320508075688772
            """)
    void inTheEclipticPlaneThePeriapsisLiesAlongX(String description, double eccentricAnomaly,
                                                  double x, double y) {
        OrbitalElements SUT = inTheEclipticPlane(meanAnomalyAt(eccentricAnomaly));

        StateVector state = SUT.stateVector(GRAVITATIONAL_PARAMETER);

        assertThat(state.position()).usingRecursiveComparison(CLOSE_ENOUGH).isEqualTo(new Vector3(x, y, 0.0));
    }

    private static OrbitalElements inTheEclipticPlane(double meanAnomaly) {
        return new OrbitalElements(SEMI_MAJOR_AXIS, new Eccentricity(ECCENTRICITY), 0.0, 0.0, 0.0, meanAnomaly);
    }

    private static double meanAnomalyAt(double eccentricAnomaly) {
        return eccentricAnomaly - ECCENTRICITY * Math.sin(eccentricAnomaly);
    }
}

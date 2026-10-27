package astronomicalsystem.core.orbit;

import astronomicalsystem.core.math.Tolerances;
import astronomicalsystem.core.math.Vector3;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class OrbitalElementsTest {

    private static final double SEMI_MAJOR_AXIS = 2.0;
    // With e = 0.6 the semi-minor axis is exactly 0.8 a, and with μ = 8 the mean motion √(μ/a³) is 1
    private static final double ECCENTRICITY = 0.6;
    private static final double GRAVITATIONAL_PARAMETER = 8.0;
    private static final double TOLERANCE = 1e-12;
    // Math.nextUp(Math.PI): the smallest double above π
    private static final double JUST_ABOVE_PI = 3.1415926535897936;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = Tolerances.closeEnough(TOLERANCE);

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

    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            periapsis turned,        0,   0,  90,  0.0, 0.8, 0.0,  -4.0,  0.0, 0.0
            node turned,             0,  90,   0,  0.0, 0.8, 0.0,  -4.0,  0.0, 0.0
            polar,                  90,   0,   0,  0.8, 0.0, 0.0,   0.0,  0.0, 4.0
            polar with node on y,   90,  90,   0,  0.0, 0.8, 0.0,   0.0,  0.0, 4.0
            periapsis above,        90,   0,  90,  0.0, 0.0, 0.8,  -4.0,  0.0, 0.0
            retrograde,            180,   0,   0,  0.8, 0.0, 0.0,   0.0, -4.0, 0.0
            """)
    void theOrientationAnglesRotateTheOrbitIntoTheEcliptic(String description, double inclinationDegrees,
                                                           double ascendingNodeDegrees, double periapsisDegrees,
                                                           double x, double y, double z,
                                                           double vx, double vy, double vz) {
        OrbitalElements SUT = atPeriapsisOriented(inclinationDegrees, ascendingNodeDegrees, periapsisDegrees);

        StateVector state = SUT.stateVector(GRAVITATIONAL_PARAMETER);

        assertThat(state).usingRecursiveComparison(CLOSE_ENOUGH)
                .isEqualTo(new StateVector(new Vector3(x, y, z), new Vector3(vx, vy, vz)));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsNonPositiveOrNonFiniteSemiMajorAxes(double semiMajorAxis) {
        assertThatThrownBy(() -> new OrbitalElements(semiMajorAxis, new Eccentricity(ECCENTRICITY), 0.0, 0.0, 0.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Semi-major axis must be positive and finite");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, JUST_ABOVE_PI, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsInclinationsOutsideZeroToPi(double inclination) {
        assertThatThrownBy(() -> new OrbitalElements(SEMI_MAJOR_AXIS, new Eccentricity(ECCENTRICITY), inclination,
                0.0, 0.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inclination must be between 0 and pi radians");
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            NaN,       0.0
            Infinity,  0.0
            -Infinity, 0.0
            0.0,       NaN
            0.0,       Infinity
            0.0,       -Infinity
            """)
    void constructorRejectsNonFiniteNodeOrPeriapsisAngles(double ascendingNode, double argumentOfPeriapsis) {
        assertThatThrownBy(() -> new OrbitalElements(SEMI_MAJOR_AXIS, new Eccentricity(ECCENTRICITY), 0.0,
                ascendingNode, argumentOfPeriapsis, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Orbital angles must be finite");
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
             0.5,                0.5
             4.71238898038469,  -1.5707963267948966
            -4.71238898038469,   1.5707963267948966
             7.283185307179586,  1.0
            """)
    void theMeanAnomalyIsReducedToWithinHalfATurn(double meanAnomaly, double reduced) {
        OrbitalElements SUT = inTheEclipticPlane(meanAnomaly);

        double stored = SUT.meanAnomaly();

        assertThat(stored).isCloseTo(reduced, within(TOLERANCE));
    }

    private static OrbitalElements atPeriapsisOriented(double inclinationDegrees, double ascendingNodeDegrees,
                                                       double periapsisDegrees) {
        return new OrbitalElements(SEMI_MAJOR_AXIS, new Eccentricity(ECCENTRICITY), Math.toRadians(inclinationDegrees),
                Math.toRadians(ascendingNodeDegrees), Math.toRadians(periapsisDegrees), 0.0);
    }

    private static OrbitalElements inTheEclipticPlane(double meanAnomaly) {
        return new OrbitalElements(SEMI_MAJOR_AXIS, new Eccentricity(ECCENTRICITY), 0.0, 0.0, 0.0, meanAnomaly);
    }

    private static double meanAnomalyAt(double eccentricAnomaly) {
        return eccentricAnomaly - ECCENTRICITY * Math.sin(eccentricAnomaly);
    }
}

package astronomicalsystem.core.orbit;

import astronomicalsystem.core.math.Tolerances;
import astronomicalsystem.core.math.Vector3;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;
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
    private static final GravitationalParameter GRAVITATIONAL_PARAMETER = new GravitationalParameter(8.0);
    // Mean motions √(μ/a³) with a = 2: 1 rad/day for μ = 8, 2 rad/day for μ = 32
    private static final double INITIAL_MEAN_ANOMALY = 0.5;
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
            NaN,       0.0,       0.0
            Infinity,  0.0,       0.0
            -Infinity, 0.0,       0.0
            0.0,       NaN,       0.0
            0.0,       Infinity,  0.0
            0.0,       -Infinity, 0.0
            0.0,       0.0,       NaN
            0.0,       0.0,       Infinity
            0.0,       0.0,       -Infinity
            """)
    void constructorRejectsNonFiniteOrbitalAngles(double ascendingNode, double argumentOfPeriapsis,
                                                  double meanAnomaly) {
        assertThatThrownBy(() -> new OrbitalElements(SEMI_MAJOR_AXIS, new Eccentricity(ECCENTRICITY), 0.0,
                ascendingNode, argumentOfPeriapsis, meanAnomaly))
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

    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            no time,           8.0,   0.0,                0.5
            forwards,          8.0,   1.0,                1.5
            backwards,         8.0,  -1.0,               -0.5
            faster orbit,     32.0,   1.0,                2.5
            turn and a half,   8.0,   9.42477796076938,  -2.641592653589793
            """)
    void theMeanAnomalyGrowsAtTheMeanMotion(String description, double gravitationalParameter, double days,
                                           double propagatedMeanAnomaly) {
        OrbitalElements SUT = inTheEclipticPlane(INITIAL_MEAN_ANOMALY);

        OrbitalElements propagated = SUT.propagated(days, new GravitationalParameter(gravitationalParameter));

        assertThat(propagated).usingRecursiveComparison(CLOSE_ENOUGH)
                .isEqualTo(inTheEclipticPlane(propagatedMeanAnomaly));
    }

    @Test
    void theLongitudesAreMeasuredFromTheReferenceDirection() {
        double inclination = 0.3;
        double ascendingNode = 1.0;
        double argumentOfPeriapsis = 0.5;
        double meanAnomaly = 0.75;

        OrbitalElements SUT = OrbitalElements.fromLongitudes(SEMI_MAJOR_AXIS, new Eccentricity(ECCENTRICITY),
                inclination, ascendingNode + argumentOfPeriapsis + meanAnomaly, ascendingNode + argumentOfPeriapsis,
                ascendingNode);

        assertThat(SUT).isEqualTo(new OrbitalElements(SEMI_MAJOR_AXIS, new Eccentricity(ECCENTRICITY), inclination,
                ascendingNode, argumentOfPeriapsis, meanAnomaly));
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

package astronomicalsystem.app.camera;

import astronomicalsystem.core.math.Vector3;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrbitTest {

    private static final double TOLERANCE = 1e-12;
    private static final double DISTANCE = 2.0;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = RecursiveComparisonConfiguration.builder()
            .withComparatorForType((a, b) -> Math.abs(a - b) < TOLERANCE ? 0 : Double.compare(a, b), Double.class)
            .build();

    // 89.5 degrees = 1.5620696805349264 rad, just beyond the limit of 89 degrees
    @ParameterizedTest
    @CsvSource(textBlock = """
            0.01,   0.0,       0.0,                 Orbit distance must be between 0.02 and 100 AU
            100.5,  0.0,       0.0,                 Orbit distance must be between 0.02 and 100 AU
            NaN,    0.0,       0.0,                 Orbit distance must be between 0.02 and 100 AU
            1.0,    Infinity,  0.0,                 Orbit azimuth must be finite
            1.0,    NaN,       0.0,                 Orbit azimuth must be finite
            1.0,    0.0,       1.5620696805349264,  Orbit elevation must be within 89 degrees of the ecliptic
            1.0,    0.0,      -1.5620696805349264,  Orbit elevation must be within 89 degrees of the ecliptic
            1.0,    0.0,       NaN,                 Orbit elevation must be within 89 degrees of the ecliptic
            """)
    void constructorRejectsPositionsOutsideTheLimits(double distance, double azimuth, double elevation,
                                                     String message) {
        assertThatThrownBy(() -> new Orbit(distance, azimuth, elevation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    // The limits themselves, any number of turns, and 89 degrees = 1.5533430342749532 rad
    @ParameterizedTest
    @CsvSource(textBlock = """
            0.02,   0.0,      0.0
            100.0,  0.0,      0.0
            1.0,    -100.0,   1.5533430342749532
            1.0,    100.0,   -1.5533430342749532
            """)
    void constructorAcceptsPositionsWithinTheLimits(double distance, double azimuth, double elevation) {
        assertThatNoException().isThrownBy(() -> new Orbit(distance, azimuth, elevation));
    }

    // eye = target + distance (cos e sin a, -cos e cos a, sin e): azimuth 0 from the negative y-axis,
    // a quarter turn (1.5707963267948966) from the positive x-axis, half a turn (3.141592653589793)
    // from the positive y-axis; 30 degrees (0.5235987755982988) above the ecliptic rises by half the
    // distance and comes closer by cos 30 = 0.8660254037844386
    @ParameterizedTest
    @CsvSource(textBlock = """
            0.0, 0.0, 0.0,   2.0, 0.0,                0.0,                  0.0, -2.0,                0.0
            0.0, 0.0, 0.0,   2.0, 1.5707963267948966, 0.0,                  2.0,  0.0,                0.0
            0.0, 0.0, 0.0,   2.0, 3.141592653589793,  0.0,                  0.0,  2.0,                0.0
            0.0, 0.0, 0.0,   2.0, 0.0,                0.5235987755982988,   0.0, -1.7320508075688772, 1.0
            1.0, 1.0, 1.0,   2.0, 0.0,                0.0,                  1.0, -1.0,                1.0
            """)
    void theEyeLiesOnTheSphereAroundTheTarget(double targetX, double targetY, double targetZ,
                                              double distance, double azimuth, double elevation,
                                              double eyeX, double eyeY, double eyeZ) {
        Orbit SUT = new Orbit(distance, azimuth, elevation);

        Vector3 eye = SUT.eye(new Vector3(targetX, targetY, targetZ));

        assertThat(eye).usingRecursiveComparison(CLOSE_ENOUGH).isEqualTo(new Vector3(eyeX, eyeY, eyeZ));
    }

    // Starting from azimuth 1 and elevation 0.5 rad; the elevation stops at +-89 degrees
    @ParameterizedTest
    @CsvSource(textBlock = """
             0.25,  0.25,   1.25,  0.75
            -2.0,  -1.0,   -1.0,  -0.5
             0.0,   1.5,    1.0,   1.5533430342749532
             0.0,  -3.0,    1.0,  -1.5533430342749532
            """)
    void rotatingTurnsTheAnglesUpToTheElevationLimit(double azimuthAngle, double elevationAngle,
                                                     double azimuth, double elevation) {
        Orbit SUT = new Orbit(DISTANCE, 1.0, 0.5);

        Orbit rotated = SUT.rotated(azimuthAngle, elevationAngle);

        assertThat(rotated).usingRecursiveComparison(CLOSE_ENOUGH).isEqualTo(new Orbit(DISTANCE, azimuth, elevation));
    }
}

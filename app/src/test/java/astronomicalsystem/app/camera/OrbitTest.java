package astronomicalsystem.app.camera;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrbitTest {

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
}

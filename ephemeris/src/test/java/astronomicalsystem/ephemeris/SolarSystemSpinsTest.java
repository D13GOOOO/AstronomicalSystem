package astronomicalsystem.ephemeris;

import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.rotation.Spin;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Checks the poles of the catalogue against a fact known independently of them: how much every
 * body is tilted to its own orbit.
 */
class SolarSystemSpinsTest {

    // Published tilts are given to the orbit of the date, and the pole of Neptune wobbles by half a
    // degree: enough to catch a pole copied wrongly
    private static final double HALF_A_DEGREE = 0.5;

    // The inclination and the ascending node of each orbit are those of Standish's table; the Sun is
    // compared with the ecliptic. Venus and Uranus turn backwards about the pole the IAU calls
    // north: their tilts of 177.36 and 97.77 degrees are 2.64 and 82.23 from that pole
    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            Sun,     0.0,          0.0,          7.25
            Mercury, 7.00497902,   48.33076593,  0.03
            Venus,   3.39467605,   76.67984255,  2.64
            Earth,   0.00001531,   180.0,        23.44
            Mars,    1.84969142,   49.55953891,  25.19
            Jupiter, 1.30439695,   100.47390909, 3.13
            Saturn,  2.48599187,   113.66242448, 26.73
            Uranus,  0.77263783,   74.01692503,  82.23
            Neptune, 1.77004347,   131.78422574, 28.32
            """)
    void everyBodyIsTiltedToItsOrbitAsObserved(String name, double inclination, double ascendingNode, double tilt) {
        Spin spin = SolarSystem.spins().get(new BodyName(name));

        Vector3 orbitNormal = normalOfTheOrbit(Math.toRadians(inclination), Math.toRadians(ascendingNode));
        double angle = Math.toDegrees(Math.acos(spin.pole().dot(orbitNormal)));
        assertThat(angle).isCloseTo(tilt, within(HALF_A_DEGREE));
    }

    // The unit vector perpendicular to an orbit inclined by i with its ascending node at longitude N
    private static Vector3 normalOfTheOrbit(double i, double node) {
        return new Vector3(Math.sin(i) * Math.sin(node), -Math.sin(i) * Math.cos(node), Math.cos(i));
    }
}

package astronomicalsystem.ephemeris;

import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.rotation.Spin;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RotationalElementsTest {

    // The pole of the Earth is the celestial pole; its prime meridian at J2000 and its rate are those
    // of the IAU report
    private static final RotationalElements EARTH = new RotationalElements(0.0, 90.0, 190.147, 360.9856235);
    // The obliquity of the ecliptic at J2000, 23.4392911 degrees: its sine and cosine
    private static final double SIN_OBLIQUITY = 0.3977771559;
    private static final double COS_OBLIQUITY = 0.9174820621;
    private static final double TOLERANCE = 1e-9;

    // Seen from the ecliptic, the celestial pole leans by the obliquity towards the y-axis, the
    // direction of the summer solstice of the north
    @Test
    void thePoleOfTheEarthLeansByTheObliquityOfTheEcliptic() {
        Spin spin = EARTH.spin();

        Vector3 pole = spin.pole();
        assertThat(pole.x()).isCloseTo(0.0, within(TOLERANCE));
        assertThat(pole.y()).isCloseTo(SIN_OBLIQUITY, within(TOLERANCE));
        assertThat(pole.z()).isCloseTo(COS_OBLIQUITY, within(TOLERANCE));
    }
}

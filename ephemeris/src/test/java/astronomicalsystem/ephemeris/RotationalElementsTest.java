package astronomicalsystem.ephemeris;

import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.rotation.Spin;
import astronomicalsystem.core.time.JulianDate;
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
    private static final double OBLIQUITY = Math.toRadians(23.4392911);
    private static final JulianDate J2000 = new JulianDate(2451545.0);
    // Greenwich mean sidereal time at J2000 in TDB, about 64 s before noon of 1 January 2000 in
    // UT1: 280.4606 degrees at noon, less the 0.267 degrees the Earth turns in 64 s
    private static final double GREENWICH_SIDEREAL_ANGLE = 280.19;
    // The report and the sidereal time define their frames slightly differently
    private static final double A_TENTH_OF_A_DEGREE = 0.1;
    // 23 h 56 min 4.09 s: the time the Earth takes to turn once with respect to the stars
    private static final double SIDEREAL_DAY = 86_164.09 / 86_400.0;
    private static final double NEARLY_OPPOSITE = 1e-5;

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

    @Test
    void afterHalfASiderealDayThePrimeMeridianOfTheEarthPointsTheOppositeWay() {
        Spin spin = EARTH.spin();

        Vector3 later = spin.axesAt(new JulianDate(J2000.days() + SIDEREAL_DAY / 2.0)).x();

        assertThat(later.plus(spin.axesAt(J2000).x()).length()).isLessThan(NEARLY_OPPOSITE);
    }

    // Back in the equatorial frame, the right ascension of the meridian of Greenwich is the
    // sidereal time there
    @Test
    void thePrimeMeridianOfTheEarthAtJ2000IsAtTheSiderealTimeOfGreenwich() {
        Spin spin = EARTH.spin();

        Vector3 greenwich = spin.axesAt(J2000).x().rotatedAboutX(OBLIQUITY);
        double rightAscension = Math.toDegrees(Math.atan2(greenwich.y(), greenwich.x())) + 360.0;
        assertThat(rightAscension).isCloseTo(GREENWICH_SIDEREAL_ANGLE, within(A_TENTH_OF_A_DEGREE));
    }
}

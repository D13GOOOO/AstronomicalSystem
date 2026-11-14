package astronomicalsystem.core.rotation;

import astronomicalsystem.core.math.Tolerances;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.time.JulianDate;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpinTest {

    private static final JulianDate J2000 = new JulianDate(2451545.0);
    private static final Vector3 X_AXIS = new Vector3(1.0, 0.0, 0.0);
    private static final Vector3 Y_AXIS = new Vector3(0.0, 1.0, 0.0);
    private static final Vector3 Z_AXIS = new Vector3(0.0, 0.0, 1.0);
    private static final double A_QUARTER_TURN_A_DAY = Math.PI / 2.0;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = Tolerances.closeEnough(1e-12);

    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            meridian along the pole,    0.0, 0.0, 1.0,   0.0, 0.0, -2.0,  1.0,  A spin needs a pole and a prime meridian across it
            no pole,                    0.0, 0.0, 0.0,   1.0, 0.0, 0.0,   1.0,  A spin needs a pole and a prime meridian across it
            no meridian,                0.0, 0.0, 1.0,   0.0, 0.0, 0.0,   1.0,  A spin needs a pole and a prime meridian across it
            endless rate,               0.0, 0.0, 1.0,   1.0, 0.0, 0.0,   Infinity,  Spin rate must be finite
            undefined rate,             0.0, 0.0, 1.0,   1.0, 0.0, 0.0,   NaN,  Spin rate must be finite
            """)
    void aSpinWithoutAnAxisOrASteadyRateIsRejected(String description, double poleX, double poleY, double poleZ,
                                                   double meridianX, double meridianY, double meridianZ,
                                                   double radiansPerDay, String message) {
        Vector3 pole = new Vector3(poleX, poleY, poleZ);
        Vector3 meridian = new Vector3(meridianX, meridianY, meridianZ);

        assertThatThrownBy(() -> new Spin(pole, meridian, J2000, radiansPerDay))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    // The pole twice as long as a unit vector, the meridian leaning towards it: at the epoch the
    // x-axis is the meridian without its part along the pole, and the y-axis is 90 degrees east
    @Test
    void atItsEpochTheBodyHasItsPrimeMeridianWhereGiven() {
        Spin SUT = new Spin(new Vector3(0.0, 0.0, 2.0), new Vector3(3.0, 0.0, 1.0), J2000, A_QUARTER_TURN_A_DAY);

        Axes axes = SUT.axesAt(J2000);

        assertThat(axes).usingRecursiveComparison(CLOSE_ENOUGH).isEqualTo(new Axes(X_AXIS, Y_AXIS, Z_AXIS));
    }

    // A quarter turn a day about z, eastwards: a day after the epoch the prime meridian is where
    // the y-axis was; a day before, with a retrograde rotation, as well
    @ParameterizedTest(name = "{0} rad/day, {1} days from the epoch")
    @CsvSource(textBlock = """
             1.5707963267948966,  1.0
            -1.5707963267948966, -1.0
            """)
    void theBodyTurnsAboutItsPoleAtASteadyRate(double radiansPerDay, double daysFromTheEpoch) {
        Spin SUT = new Spin(Z_AXIS, X_AXIS, J2000, radiansPerDay);

        Axes axes = SUT.axesAt(new JulianDate(J2000.days() + daysFromTheEpoch));

        assertThat(axes).usingRecursiveComparison(CLOSE_ENOUGH)
                .isEqualTo(new Axes(Y_AXIS, new Vector3(-1.0, 0.0, 0.0), Z_AXIS));
    }
}

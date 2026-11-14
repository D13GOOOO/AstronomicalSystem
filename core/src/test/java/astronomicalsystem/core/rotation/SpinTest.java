package astronomicalsystem.core.rotation;

import astronomicalsystem.core.math.Tolerances;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.time.JulianDate;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpinTest {

    private static final JulianDate J2000 = new JulianDate(2451545.0);
    private static final Vector3 X_AXIS = new Vector3(1.0, 0.0, 0.0);
    private static final Vector3 Y_AXIS = new Vector3(0.0, 1.0, 0.0);
    private static final Vector3 Z_AXIS = new Vector3(0.0, 0.0, 1.0);
    private static final double A_QUARTER_TURN_A_DAY = Math.PI / 2.0;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = Tolerances.closeEnough(1e-12);

    // The pole twice as long as a unit vector, the meridian leaning towards it: at the epoch the
    // x-axis is the meridian without its part along the pole, and the y-axis is 90 degrees east
    @Test
    void atItsEpochTheBodyHasItsPrimeMeridianWhereGiven() {
        Spin SUT = new Spin(new Vector3(0.0, 0.0, 2.0), new Vector3(3.0, 0.0, 1.0), J2000, A_QUARTER_TURN_A_DAY);

        Axes axes = SUT.axesAt(J2000);

        assertThat(axes).usingRecursiveComparison(CLOSE_ENOUGH).isEqualTo(new Axes(X_AXIS, Y_AXIS, Z_AXIS));
    }
}

package astronomicalsystem.app.sky;

import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StarTest {

    private static final Vector3 TWO_UNITS_TOWARDS_THE_POLE = new Vector3(0.0, 0.0, 2.0);
    private static final Vector3 TOWARDS_THE_POLE = new Vector3(0.0, 0.0, 1.0);
    private static final double FAINTEST = 1.0;

    @Test
    void theDirectionOfAStarIsAUnitVector() {
        Star SUT = new Star(TWO_UNITS_TOWARDS_THE_POLE, FAINTEST);

        assertThat(SUT.direction()).isEqualTo(TOWARDS_THE_POLE);
    }
}

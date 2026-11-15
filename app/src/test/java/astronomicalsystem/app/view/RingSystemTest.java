package astronomicalsystem.app.view;

import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RingSystemTest {

    private static final ClipPoint CENTRE = new ClipPoint(0.0, 0.0, 0.5, 2.0);
    private static final double DIAMETER = 100.0;
    private static final Vector3 AXIS = new Vector3(0.0, 1.0, 0.0);
    private static final Vector3 TOWARDS_THE_LIGHT = new Vector3(1.0, 0.0, 0.0);
    private static final RingSystem.Band BAND = new RingSystem.Band(1.5, 2.0, new Colour(0.8, 0.7, 0.6), 0.5);

    @Test
    void theRingsDoNotChangeWhenTheListTheyWereMadeFromDoes() {
        List<RingSystem.Band> bands = new ArrayList<>(List.of(BAND));
        RingSystem SUT = new RingSystem(CENTRE, DIAMETER, AXIS, TOWARDS_THE_LIGHT, bands);

        bands.clear();

        assertThat(SUT.bands()).containsExactly(BAND);
    }
}

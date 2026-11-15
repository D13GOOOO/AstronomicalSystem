package astronomicalsystem.core.rings;

import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RingsTest {

    private static final Vector3 POLE = new Vector3(0.0, 0.0, 1.0);
    private static final RingBand BAND = new RingBand(new Radius(5e-4), new Radius(8e-4), 0.5);

    @Test
    void theRingsDoNotChangeWhenTheListTheyWereMadeFromDoes() {
        List<RingBand> bands = new ArrayList<>(List.of(BAND));
        Rings SUT = new Rings(POLE, bands);

        bands.clear();

        assertThat(SUT.bands()).containsExactly(BAND);
    }
}

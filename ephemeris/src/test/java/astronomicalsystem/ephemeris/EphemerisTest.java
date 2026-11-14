package astronomicalsystem.ephemeris;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EphemerisTest {

    private static final Body SUN = new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(4.65e-3),
            Vector3.ZERO, Vector3.ZERO);

    @Test
    void theBodiesCannotBeChangedFromOutside() {
        List<Body> given = new ArrayList<>(List.of(SUN));
        Ephemeris SUT = new Ephemeris(given, Provenance.JPL_HORIZONS);

        given.clear();

        assertThat(SUT.bodies()).containsExactly(SUN);
        assertThatThrownBy(() -> SUT.bodies().add(SUN)).isInstanceOf(UnsupportedOperationException.class);
    }
}

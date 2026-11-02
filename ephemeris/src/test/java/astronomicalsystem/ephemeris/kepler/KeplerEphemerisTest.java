package astronomicalsystem.ephemeris.kepler;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.BodyProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeplerEphemerisTest {

    private static final BodyProperties SUN =
            new BodyProperties(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(4.65e-3));
    private static final JulianDate J2000 = new JulianDate(2451545.0);

    @Test
    void theCentralBodyAloneRestsAtTheOrigin() {
        KeplerEphemeris SUT = new KeplerEphemeris(SUN, J2000, List.of());

        List<Body> bodies = SUT.bodiesAt(J2000);

        assertThat(bodies).containsExactly(
                new Body(SUN.name(), SUN.kind(), SUN.mass(), SUN.radius(), Vector3.ZERO, Vector3.ZERO));
    }
}

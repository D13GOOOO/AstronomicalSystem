package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class EjectionTest {

    // The Earth and a protoplanet a tenth of its mass, merged
    private static final Body MERGED = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3.3e-6),
            new Radius(4.6e-5), new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.0172, 0.0));
    private static final Mass REMNANT = new Mass(3.2e-6);
    private static final List<Mass> FRAGMENTS = List.of(new Mass(6e-8), new Mass(4e-8));
    private static final Cone UPWARDS = new Cone(new Vector3(0.0, 0.0, 1.0), Math.PI / 4.0);
    private static final BodyName PROTOPLANET = new BodyName("Protoplanet 1");

    private final Ejection SUT = new Ejection(MERGED, REMNANT, FRAGMENTS, UPWARDS, PROTOPLANET);

    @Test
    void theLargestRemnantKeepsTheNameOfTheLargerBodyAndTheFragmentsAreDebris() {
        List<Body> bodies = SUT.bodies();

        assertThat(bodies).extracting(Body::name, Body::kind, Body::mass).containsExactly(
                tuple(MERGED.name(), BodyKind.PLANET, REMNANT),
                tuple(new BodyName("Protoplanet 1 fragment 1"), BodyKind.DEBRIS, FRAGMENTS.get(0)),
                tuple(new BodyName("Protoplanet 1 fragment 2"), BodyKind.DEBRIS, FRAGMENTS.get(1)));
    }
}

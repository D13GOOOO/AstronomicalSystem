package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.impact.FragmentSizes;
import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FragmentingCollisionsTest {

    private static final double EARTH_RADIUS = 4.259e-5;
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3.003e-6),
            new Radius(EARTH_RADIUS), Vector3.ZERO, Vector3.ZERO);
    // A tenth of the mass of the Earth and as dense: 0.464 of its radius
    private static final double PROTOPLANET_RADIUS = 1.977e-5;
    private static final double CONTACT = EARTH_RADIUS + PROTOPLANET_RADIUS;
    // Up to 24 fragments, none lighter than about 2e18 kg, a body some 100 km across
    private static final FragmentSizes SIZES = new FragmentSizes(new Mass(1e-12), 24);

    private final FragmentingCollisions SUT = new FragmentingCollisions(SIZES);

    // Falling from rest half a contact distance away, it hits at sqrt(1/3) of the escape speed
    @Test
    void bodiesMeetingSlowerThanTheirEscapeSpeedMergeWhole() {
        Body before = protoplanet(new Vector3(1.5 * CONTACT, 0.0, 0.0), Vector3.ZERO);
        Body after = protoplanet(new Vector3(0.5 * CONTACT, 0.0, 0.0), Vector3.ZERO);

        List<Body> resolved = SUT.resolve(List.of(EARTH, before), List.of(EARTH, after));

        assertThat(resolved).containsExactly(EARTH.mergedWith(after));
    }

    private static Body protoplanet(Vector3 position, Vector3 velocity) {
        return new Body(new BodyName("Protoplanet 1"), BodyKind.PLANET, new Mass(3.003e-7),
                new Radius(PROTOPLANET_RADIUS), position, velocity);
    }
}

package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MergingCollisionsTest {

    private static final Mass UNIT_MASS = new Mass(1.0);
    private static final Radius UNIT_RADIUS = new Radius(1.0);

    private final MergingCollisions SUT = new MergingCollisions();

    @Test
    void twoOverlappingBodiesMergeIntoOne() {
        Body first = sphereAt("First", 0.0);
        Body second = sphereAt("Second", 1.0);

        List<Body> resolved = SUT.resolve(List.of(first, second));

        assertThat(resolved).containsExactly(first.mergedWith(second));
    }

    @Test
    void separateBodiesAreLeftUnchanged() {
        Body first = sphereAt("First", 0.0);
        Body second = sphereAt("Second", 3.0);
        Body third = sphereAt("Third", 6.0);

        List<Body> resolved = SUT.resolve(List.of(first, second, third));

        assertThat(resolved).containsExactly(first, second, third);
    }

    @Test
    void theMergedBodyTakesThePlaceOfTheFirstOfThePair() {
        Body first = sphereAt("First", 0.0);
        Body middle = sphereAt("Middle", 5.0);
        Body last = sphereAt("Last", 1.0);

        List<Body> resolved = SUT.resolve(List.of(first, middle, last));

        assertThat(resolved).containsExactly(first.mergedWith(last), middle);
    }

    @Test
    void aChainOfOverlapsMergesIntoASingleBody() {
        Body first = sphereAt("First", 0.0);
        Body second = sphereAt("Second", 1.0);
        Body third = sphereAt("Third", 2.5);

        List<Body> resolved = SUT.resolve(List.of(first, second, third));

        assertThat(resolved).containsExactly(first.mergedWith(second).mergedWith(third));
    }

    private static Body sphereAt(String name, double x) {
        return new Body(new BodyName(name), BodyKind.PLANET, UNIT_MASS, UNIT_RADIUS,
                new Vector3(x, 0.0, 0.0), Vector3.ZERO);
    }
}

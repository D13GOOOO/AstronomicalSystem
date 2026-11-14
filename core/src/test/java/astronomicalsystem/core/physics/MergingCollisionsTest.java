package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Tolerances;
import astronomicalsystem.core.math.Vector3;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MergingCollisionsTest {

    private static final Mass UNIT_MASS = new Mass(1.0);
    private static final Radius UNIT_RADIUS = new Radius(1.0);

    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = Tolerances.closeEnough(1e-15);

    private final MergingCollisions SUT = new MergingCollisions();

    @Test
    void twoOverlappingBodiesMergeIntoOne() {
        Body first = sphereAt("First", 0.0);
        Body second = sphereAt("Second", 1.0);

        List<Body> atRest = List.of(first, second);

        List<Body> resolved = SUT.resolve(atRest, atRest).bodies();

        assertThat(resolved).containsExactly(first.mergedWith(second));
    }

    @Test
    void separateBodiesAreLeftUnchanged() {
        Body first = sphereAt("First", 0.0);
        Body second = sphereAt("Second", 3.0);
        Body third = sphereAt("Third", 6.0);

        List<Body> atRest = List.of(first, second, third);

        List<Body> resolved = SUT.resolve(atRest, atRest).bodies();

        assertThat(resolved).containsExactly(first, second, third);
    }

    @Test
    void theMergedBodyTakesThePlaceOfTheFirstOfThePair() {
        Body first = sphereAt("First", 0.0);
        Body middle = sphereAt("Middle", 5.0);
        Body last = sphereAt("Last", 1.0);

        List<Body> atRest = List.of(first, middle, last);

        List<Body> resolved = SUT.resolve(atRest, atRest).bodies();

        assertThat(resolved).containsExactly(first.mergedWith(last), middle);
    }

    @Test
    void aChainOfOverlapsMergesIntoASingleBody() {
        Body first = sphereAt("First", 0.0);
        Body second = sphereAt("Second", 1.0);
        Body third = sphereAt("Third", 2.5);

        List<Body> atRest = List.of(first, second, third);

        List<Body> resolved = SUT.resolve(atRest, atRest).bodies();

        assertThat(resolved).containsExactly(first.mergedWith(second).mergedWith(third));
    }

    // The second body runs through the first one within the step and ends on its other side
    @Test
    void bodiesThatPassThroughEachOtherDuringTheStepMerge() {
        Body first = sphereAt("First", 0.0);
        Body secondBefore = sphereAt("Second", 10.0);
        Body secondAfter = sphereAt("Second", -10.0);

        List<Body> resolved = SUT.resolve(List.of(first, secondBefore), List.of(first, secondAfter)).bodies();

        assertThat(resolved).containsExactly(first.mergedWith(secondAfter));
    }

    // The third body runs into the second one before reaching the first one, and their merged body
    // stops short of the first one; in index order the first pair would be the first and the third
    @Test
    void theEarliestMeetingOfTheStepIsResolvedFirst() {
        Body first = sphereAt("First", 0.0);
        Body secondBefore = sphereAt("Second", 10.0);
        Body secondAfter = sphereAt("Second", 30.0);
        Body thirdBefore = sphereAt("Third", 20.0);
        Body thirdAfter = sphereAt("Third", -20.0);

        List<Body> resolved = SUT.resolve(List.of(first, secondBefore, thirdBefore),
                List.of(first, secondAfter, thirdAfter)).bodies();

        assertThat(resolved).containsExactly(first, secondAfter.mergedWith(thirdAfter));
    }

    // The second body moves along a line parallel to the x-axis, at a distance y from it
    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            moving apart,            3.0,  10.0, 0.0
            passing beside,         10.0, -10.0, 2.5
            touching at the closest, 10.0, -10.0, 2.0
            touching at the end,    10.0,   2.0, 0.0
            meeting after the step, 10.0,   4.0, 0.0
            """)
    void bodiesThatDoNotOverlapWithinTheStepAreLeftUnchanged(String description, double startX, double endX,
                                                             double y) {
        Body first = sphereAt("First", 0.0);
        Body secondBefore = sphereAt("Second", startX, y);
        Body secondAfter = sphereAt("Second", endX, y);

        List<Body> resolved = SUT.resolve(List.of(first, secondBefore), List.of(first, secondAfter)).bodies();

        assertThat(resolved).containsExactly(first, secondAfter);
    }

    // Falling from rest 10 AU apart, two suns meet 2 AU apart: v² = 2 G M (1/2 - 1/10) = 1.6 G, and
    // the energy in the frame of their centre of mass is (1/2) mu v² with mu = 1/2. Head-on, the
    // debris flies straight back out of the site, on the side of the second body
    @Test
    void everyCollisionIsToldWithTheSurvivorTheSiteAndHowHardTheBodiesMet() {
        Body first = sphereAt("First", 0.0);
        Body secondBefore = sphereAt("Second", 10.0);
        Body secondAfter = sphereAt("Second", -10.0);

        List<Collision> collisions = SUT.resolve(List.of(first, secondBefore), List.of(first, secondAfter))
                .collisions();

        Vector3 towardsTheSecond = new Vector3(1.0, 0.0, 0.0);
        assertThat(collisions).usingRecursiveFieldByFieldElementComparator(CLOSE_ENOUGH).containsExactly(
                new Collision(first.name(), towardsTheSecond, towardsTheSecond, 0.021759125287036375,
                        1.1836488331423645e-4));
    }

    private static Body sphereAt(String name, double x) {
        return sphereAt(name, x, 0.0);
    }

    private static Body sphereAt(String name, double x, double y) {
        return new Body(new BodyName(name), BodyKind.PLANET, UNIT_MASS, UNIT_RADIUS,
                new Vector3(x, y, 0.0), Vector3.ZERO);
    }
}

package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Tolerances;
import astronomicalsystem.core.math.Vector3;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.withinPercentage;

class EjectionTest {

    // The Earth and a protoplanet a tenth of its mass, merged
    private static final Body MERGED = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3.3e-6),
            new Radius(4.6e-5), new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.0172, 0.0));
    private static final Mass REMNANT = new Mass(3.2e-6);
    private static final List<Mass> FRAGMENTS = List.of(new Mass(6e-8), new Mass(4e-8));
    private static final Cone UPWARDS = new Cone(new Vector3(0.0, 0.0, 1.0), Math.PI / 4.0);
    private static final BodyName PROTOPLANET = new BodyName("Protoplanet 1");

    private static final double PRECISION_PERCENT = 1e-9;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = Tolerances.closeEnough(1e-14);

    private final Ejection SUT = new Ejection(MERGED, REMNANT, FRAGMENTS, UPWARDS, PROTOPLANET);

    @Test
    void theLargestRemnantKeepsTheNameOfTheLargerBodyAndTheFragmentsAreDebris() {
        List<Body> bodies = SUT.bodies();

        assertThat(bodies).extracting(Body::name, Body::kind, Body::mass).containsExactly(
                tuple(MERGED.name(), BodyKind.PLANET, REMNANT),
                tuple(new BodyName("Protoplanet 1 fragment 1"), BodyKind.DEBRIS, FRAGMENTS.get(0)),
                tuple(new BodyName("Protoplanet 1 fragment 2"), BodyKind.DEBRIS, FRAGMENTS.get(1)));
    }

    // Mass over the cube of the radius, proportional to the density
    @Test
    void theRemnantAndTheFragmentsKeepTheDensityOfTheBodies() {
        List<Body> bodies = SUT.bodies();

        assertThat(bodies).allSatisfy(body -> assertThat(densityOf(body))
                .isCloseTo(densityOf(MERGED), withinPercentage(PRECISION_PERCENT)));
    }

    // Seen from the remnant, every fragment lies inside the cone, and no two bodies overlap
    @Test
    void theFragmentsStartInsideTheConeClearOfEachOtherAndOfTheRemnant() {
        List<Body> bodies = SUT.bodies();

        Vector3 remnant = bodies.getFirst().position();
        assertThat(bodies.subList(1, bodies.size())).allSatisfy(fragment -> {
            Vector3 offset = fragment.position().minus(remnant);
            assertThat(offset.normalized().dot(UPWARDS.axis())).isGreaterThanOrEqualTo(Math.cos(UPWARDS.halfAngle()));
        });
        assertThat(smallestGapBetween(bodies)).isPositive();
    }

    // No external force acts in the impact: the bodies move on as a whole
    @Test
    void theCentreOfMassStaysWhereTheBodiesWere() {
        List<Body> bodies = SUT.bodies();

        assertThat(totalMassOf(bodies)).isCloseTo(MERGED.mass().solarMasses(), withinPercentage(PRECISION_PERCENT));
        assertThat(centreOfMassOf(bodies)).usingRecursiveComparison(CLOSE_ENOUGH).isEqualTo(MERGED.position());
    }

    private static double totalMassOf(List<Body> bodies) {
        return bodies.stream().mapToDouble(body -> body.mass().solarMasses()).sum();
    }

    private static Vector3 centreOfMassOf(List<Body> bodies) {
        return bodies.stream().map(body -> body.position().times(body.mass().solarMasses()))
                .reduce(Vector3.ZERO, Vector3::plus).times(1.0 / totalMassOf(bodies));
    }

    // The least distance between the surfaces of two of the bodies
    private static double smallestGapBetween(List<Body> bodies) {
        return IntStream.range(0, bodies.size()).boxed()
                .flatMap(i -> IntStream.range(i + 1, bodies.size()).mapToObj(j -> gap(bodies.get(i), bodies.get(j))))
                .min(Double::compare).orElseThrow();
    }

    private static double gap(Body first, Body second) {
        return second.position().minus(first.position()).length() - first.radius().astronomicalUnits()
                - second.radius().astronomicalUnits();
    }

    private static double densityOf(Body body) {
        double radius = body.radius().astronomicalUnits();
        return body.mass().solarMasses() / (radius * radius * radius);
    }
}

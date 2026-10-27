package astronomicalsystem.core.body;

import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class BodyTest {

    private static final BodyName EARTH = new BodyName("Earth");
    private static final BodyName MOON = new BodyName("Moon");
    private static final Mass EARTH_MASS = new Mass(3.003e-6);
    private static final Radius EARTH_RADIUS = new Radius(4.259e-5);

    @Test
    void withMotionReplacesOnlyPositionAndVelocity() {
        Body SUT = new Body(EARTH, BodyKind.PLANET, EARTH_MASS, EARTH_RADIUS,
                new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.0172, 0.0));

        Body moved = SUT.withMotion(new Vector3(0.0, 1.0, 0.0), new Vector3(-0.0172, 0.0, 0.0));

        assertThat(moved).isEqualTo(new Body(EARTH, BodyKind.PLANET, EARTH_MASS, EARTH_RADIUS,
                new Vector3(0.0, 1.0, 0.0), new Vector3(-0.0172, 0.0, 0.0)));
    }

    @Test
    void driftedMovesThePositionAlongTheVelocity() {
        Body SUT = new Body(EARTH, BodyKind.PLANET, EARTH_MASS, EARTH_RADIUS,
                new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.25, 0.0));

        Body drifted = SUT.drifted(2.0);

        assertThat(drifted).isEqualTo(new Body(EARTH, BodyKind.PLANET, EARTH_MASS, EARTH_RADIUS,
                new Vector3(1.0, 0.5, 0.0), new Vector3(0.0, 0.25, 0.0)));
    }

    @Test
    void kickedChangesTheVelocityAlongTheAcceleration() {
        Body SUT = new Body(EARTH, BodyKind.PLANET, EARTH_MASS, EARTH_RADIUS,
                new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.25, 0.0));

        Body kicked = SUT.kicked(new Vector3(-0.125, 0.0, 0.0), 2.0);

        assertThat(kicked).isEqualTo(new Body(EARTH, BodyKind.PLANET, EARTH_MASS, EARTH_RADIUS,
                new Vector3(1.0, 0.0, 0.0), new Vector3(-0.25, 0.25, 0.0)));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            apart,                       1.0, 0.5,  2.0,  0.0, false
            touching,                    1.0, 0.5,  1.5,  0.0, false
            overlapping,                 1.0, 0.5,  1.25, 0.0, true
            inside,                      1.0, 0.5,  0.25, 0.0, true
            coincident,                  1.0, 0.5,  0.0,  0.0, true
            overlapping on the left,     1.0, 0.5, -1.25, 0.0, true
            overlapping off axis,        1.0, 0.5,  0.75, 1.0, true
            apart off axis,              1.0, 0.5,  1.2,  1.6, false
            overlapping (radii swapped), 0.5, 1.0,  1.25, 0.0, true
            touching (radii swapped),    0.5, 1.0,  1.5,  0.0, false
            """)
    void overlapsWhenTheDistanceIsLessThanTheSumOfTheRadii(String description,
                                                           double ownRadius, double otherRadius,
                                                           double otherX, double otherY,
                                                           boolean expected) {
        Body SUT = sphere(EARTH, ownRadius, Vector3.ZERO);
        Body other = sphere(MOON, otherRadius, new Vector3(otherX, otherY, 0.0));

        boolean overlaps = SUT.overlaps(other);

        assertThat(overlaps).isEqualTo(expected);
    }

    @Test
    void mergedWithAddsTheMasses() {
        Body SUT = withMass(EARTH, 0.75);
        Body other = withMass(MOON, 0.25);

        Body merged = SUT.mergedWith(other);

        assertThat(merged.mass()).isEqualTo(new Mass(1.0));
    }

    @Test
    void mergedWithIsPlacedAtTheCentreOfMass() {
        Body SUT = body(EARTH, 0.75, new Vector3(1.0, 2.0, 0.0), Vector3.ZERO);
        Body other = body(MOON, 0.25, new Vector3(5.0, -2.0, 4.0), Vector3.ZERO);

        Body merged = SUT.mergedWith(other);

        assertThat(merged.position()).isEqualTo(new Vector3(2.0, 1.0, 1.0));
    }

    private static Body withMass(BodyName name, double solarMasses) {
        return body(name, solarMasses, Vector3.ZERO, Vector3.ZERO);
    }

    private static Body body(BodyName name, double solarMasses, Vector3 position, Vector3 velocity) {
        return new Body(name, BodyKind.PLANET, new Mass(solarMasses), EARTH_RADIUS, position, velocity);
    }

    private static Body sphere(BodyName name, double radius, Vector3 position) {
        return new Body(name, BodyKind.PLANET, EARTH_MASS, new Radius(radius), position, Vector3.ZERO);
    }
}

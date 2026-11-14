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
import static org.assertj.core.api.Assertions.withinPercentage;

class FragmentingCollisionsTest {

    private static final double EARTH_RADIUS = 4.259e-5;
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3.003e-6),
            new Radius(EARTH_RADIUS), Vector3.ZERO, Vector3.ZERO);
    // A tenth of the mass of the Earth and as dense: 0.464 of its radius
    private static final double PROTOPLANET_RADIUS = 1.977e-5;
    private static final double CONTACT = EARTH_RADIUS + PROTOPLANET_RADIUS;
    private static final int MOST_FRAGMENTS = 24;
    // Up to 24 fragments, none lighter than about 2e18 kg, a body some 100 km across
    private static final FragmentSizes SIZES = new FragmentSizes(new Mass(1e-12), MOST_FRAGMENTS);

    // About 20 km/s, twice the escape speed: a giant impact
    private static final double FAST = 0.01155;
    private static final double PRECISION_PERCENT = 1e-9;
    private static final double ORBITAL_SPEED = 0.0172;
    private static final double AN_HOUR = 1.0 / 24.0;
    // Well away from the normal, where a head-on impact sends the debris
    private static final double CLEARLY_DOWNRANGE = 0.2;

    private final FragmentingCollisions SUT = new FragmentingCollisions(SIZES);

    // Falling from rest half a contact distance away, it hits at sqrt(1/3) of the escape speed
    @Test
    void bodiesMeetingSlowerThanTheirEscapeSpeedMergeWhole() {
        Body before = protoplanet(new Vector3(1.5 * CONTACT, 0.0, 0.0), Vector3.ZERO);
        Body after = protoplanet(new Vector3(0.5 * CONTACT, 0.0, 0.0), Vector3.ZERO);

        List<Body> resolved = SUT.resolve(List.of(EARTH, before), List.of(EARTH, after));

        assertThat(resolved).containsExactly(EARTH.mergedWith(after));
    }

    // Q_R is about a sixth of Q*_RD: some 9% of the mass flies off, enough for every fragment
    @Test
    void aGiantImpactLeavesTheLargestRemnantAndFragments() {
        Body before = protoplanet(new Vector3(3.0 * CONTACT, 0.0, 0.0), new Vector3(-FAST, 0.0, 0.0));
        Body after = protoplanet(new Vector3(-3.0 * CONTACT, 0.0, 0.0), new Vector3(-FAST, 0.0, 0.0));

        List<Body> resolved = SUT.resolve(List.of(EARTH, before), List.of(EARTH, after));

        assertThat(resolved).hasSize(1 + MOST_FRAGMENTS);
        assertThat(resolved.getFirst().name()).isEqualTo(EARTH.name());
        assertThat(resolved.subList(1, resolved.size())).extracting(Body::kind).containsOnly(BodyKind.DEBRIS);
        assertThat(totalMassOf(resolved))
                .isCloseTo(EARTH.mass().solarMasses() + after.mass().solarMasses(), withinPercentage(PRECISION_PERCENT));
    }

    // The Earth along its orbit covers 17 radii in the hour of a step; hit from behind, it throws the
    // debris back along its path, where it would run into it again were the fragments left behind
    @Test
    void theFragmentsKeepTheirPlaceAroundAMovingRemnant() {
        Vector3 orbiting = new Vector3(0.0, ORBITAL_SPEED, 0.0);
        Body earthBefore = EARTH.withMotion(Vector3.ZERO, orbiting);
        Body earthAfter = EARTH.withMotion(orbiting.times(AN_HOUR), orbiting);
        Vector3 chasing = new Vector3(0.0, ORBITAL_SPEED + FAST, 0.0);
        Body before = protoplanet(new Vector3(0.0, -3.0 * CONTACT, 0.0), chasing);
        Body after = protoplanet(new Vector3(0.0, -3.0 * CONTACT + (ORBITAL_SPEED + FAST) * AN_HOUR, 0.0), chasing);

        List<Body> resolved = SUT.resolve(List.of(earthBefore, before), List.of(earthAfter, after));

        assertThat(resolved).hasSize(1 + MOST_FRAGMENTS);
    }

    // Half a contact distance off the line of the centres, the projectile touches where the normal is
    // (0.866, 0.5, 0), and moves along the surface towards (-0.5, 0.866, 0): the debris leans that way
    @Test
    void theDebrisOfAnObliqueImpactLeansDownrange() {
        Body before = protoplanet(new Vector3(3.0 * CONTACT, 0.5 * CONTACT, 0.0), new Vector3(-FAST, 0.0, 0.0));
        Body after = protoplanet(new Vector3(-3.0 * CONTACT, 0.5 * CONTACT, 0.0), new Vector3(-FAST, 0.0, 0.0));

        List<Body> resolved = SUT.resolve(List.of(EARTH, before), List.of(EARTH, after));

        Vector3 downrange = new Vector3(-0.5, Math.sqrt(0.75), 0.0);
        assertThat(meanDirectionFromTheFirst(resolved).dot(downrange)).isGreaterThan(CLEARLY_DOWNRANGE);
    }

    // Bodies that start overlapping, at rest, meet at no speed at all
    @Test
    void bodiesAlreadyOverlappingAtRestMergeWhole() {
        Body overlapping = protoplanet(new Vector3(0.5 * CONTACT, 0.0, 0.0), Vector3.ZERO);

        List<Body> resolved = SUT.resolve(List.of(EARTH, overlapping), List.of(EARTH, overlapping));

        assertThat(resolved).containsExactly(EARTH.mergedWith(overlapping));
    }

    // The projectile first in the system: the target is the larger body, wherever it is
    @Test
    void theFragmentsAreNamedAfterTheSmallerBody() {
        Body before = protoplanet(new Vector3(3.0 * CONTACT, 0.0, 0.0), new Vector3(-FAST, 0.0, 0.0));
        Body after = protoplanet(new Vector3(-3.0 * CONTACT, 0.0, 0.0), new Vector3(-FAST, 0.0, 0.0));

        List<Body> resolved = SUT.resolve(List.of(before, EARTH), List.of(after, EARTH));

        assertThat(resolved).extracting(Body::name).startsWith(EARTH.name(), new BodyName("Protoplanet 1 fragment 1"));
    }

    // An asteroid a millionth of the mass of the Earth at the same speed throws out less than the
    // smallest fragment: the dust falls back, and the bodies merge
    @Test
    void anImpactThatThrowsOutOnlyDustMergesTheBodies() {
        Body before = asteroid(new Vector3(3.0 * CONTACT, 0.0, 0.0));
        Body after = asteroid(new Vector3(-3.0 * CONTACT, 0.0, 0.0));

        List<Body> resolved = SUT.resolve(List.of(EARTH, before), List.of(EARTH, after));

        assertThat(resolved).containsExactly(EARTH.mergedWith(after));
    }

    private static Body asteroid(Vector3 position) {
        return new Body(new BodyName("Asteroid 1"), BodyKind.ASTEROID, new Mass(3.003e-12), new Radius(4.259e-7),
                position, new Vector3(-FAST, 0.0, 0.0));
    }

    // The average of the directions of the other bodies seen from the first one
    private static Vector3 meanDirectionFromTheFirst(List<Body> bodies) {
        Vector3 centre = bodies.getFirst().position();
        return bodies.subList(1, bodies.size()).stream()
                .map(body -> body.position().minus(centre).normalized())
                .reduce(Vector3.ZERO, Vector3::plus).times(1.0 / (bodies.size() - 1));
    }

    private static double totalMassOf(List<Body> bodies) {
        return bodies.stream().mapToDouble(body -> body.mass().solarMasses()).sum();
    }

    private static Body protoplanet(Vector3 position, Vector3 velocity) {
        return new Body(new BodyName("Protoplanet 1"), BodyKind.PLANET, new Mass(3.003e-7),
                new Radius(PROTOPLANET_RADIUS), position, velocity);
    }
}

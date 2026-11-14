package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.impact.Cone;
import astronomicalsystem.core.impact.Ejection;
import astronomicalsystem.core.impact.FragmentSizes;
import astronomicalsystem.core.impact.Impact;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.GravitationalParameter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * Resolves the collisions by the energy of the impact: bodies that meet slower than their escape
 * speed merge whole, harder impacts break off fragments.
 */
public class FragmentingCollisions extends SweptCollisions {

    private static final double G = GravitationalParameter.GRAVITATIONAL_CONSTANT;
    // Ejecta leave in a cone of 45 degrees around the direction of the debris
    private static final double EJECTA_HALF_ANGLE = Math.PI / 4.0;

    private final FragmentSizes sizes;

    public FragmentingCollisions(@NotNull FragmentSizes sizes) {
        this.sizes = sizes;
    }

    @Override
    protected @NotNull List<Sweep> outcomeOf(@NotNull Sweep first, @NotNull Sweep second, double fraction) {
        Sweep merged = first.mergedWith(second);
        // The larger body is the target; on a tie the first, as in the merge
        boolean firstIsTarget = !second.end().mass().isGreaterThan(first.end().mass());
        Sweep target = firstIsTarget ? first : second;
        Sweep projectile = firstIsTarget ? second : first;
        Impact impact = impactOf(target, projectile);
        if (impact.merges()) {
            return List.of(merged);
        }
        Mass remnant = impact.largestRemnant();
        List<Mass> fragments = sizes.split(merged.end().mass().solarMasses() - remnant.solarMasses());
        if (fragments.isEmpty()) {
            return List.of(merged);
        }
        Cone cone = new Cone(debrisAxis(target, projectile, fraction), EJECTA_HALF_ANGLE);
        List<Body> bodies = new Ejection(merged.end(), remnant, fragments, cone, projectile.end().name()).bodies();
        return sweepsOf(bodies, merged.start());
    }

    /**
     * The impact speed comes from what the two bodies keep while they fall towards each other, at
     * the start of the step, rather than from their velocities at its end, which the step computed
     * as if they had gone through each other: the energy v² / 2 - G M / r gives
     * v² = v0² + 2 G M (1 / d - 1 / r0) at the contact distance d.
     */
    private static @NotNull Impact impactOf(@NotNull Sweep target, @NotNull Sweep projectile) {
        double contact = target.end().radius().astronomicalUnits() + projectile.end().radius().astronomicalUnits();
        // Bodies that already overlap meet at their own speed
        double distance = Math.max(separationAtStart(target, projectile).length(), contact);
        double totalMass = target.end().mass().solarMasses() + projectile.end().mass().solarMasses();
        double speedSquared = relativeVelocityAtStart(target, projectile).lengthSquared()
                + 2.0 * G * totalMass * (1.0 / contact - 1.0 / distance);
        return new Impact(target.end().mass(), projectile.end().mass(), contact, Math.sqrt(speedSquared));
    }

    /**
     * Straight back out of the crater in a head-on impact, leaning downrange in an oblique one:
     * the normal n plus the tangential velocity at contact over the impact speed. With the angular
     * momentum |r0 x v0| kept, the tangential speed at the contact distance d is |r0 x v0| / d.
     */
    private @NotNull Vector3 debrisAxis(@NotNull Sweep target, @NotNull Sweep projectile, double fraction) {
        Vector3 normal = positionAt(projectile, fraction).minus(positionAt(target, fraction)).normalized();
        Vector3 relativeVelocity = relativeVelocityAtStart(target, projectile);
        Vector3 tangential = relativeVelocity.minus(normal.times(relativeVelocity.dot(normal)));
        if (tangential.lengthSquared() == 0.0) {
            return normal;
        }
        Impact impact = impactOf(target, projectile);
        double tangentialSpeed = separationAtStart(target, projectile).cross(relativeVelocity).length()
                / impact.contactDistance();
        return normal.plus(tangential.normalized().times(Math.min(1.0, tangentialSpeed / impact.speed())));
    }

    // The fragments keep their place around the remnant during the step, so that they do not meet
    // it or each other again before the next one
    private static @NotNull List<Sweep> sweepsOf(@NotNull List<Body> bodies, @NotNull Body mergedStart) {
        Body remnant = bodies.getFirst();
        Vector3 displacement = remnant.position().minus(mergedStart.position());
        Stream<Sweep> fragments = bodies.subList(1, bodies.size()).stream().map(fragment -> new Sweep(
                fragment.withMotion(fragment.position().minus(displacement), fragment.velocity()), fragment));
        return Stream.concat(Stream.of(new Sweep(mergedStart, remnant)), fragments).toList();
    }

    private static @NotNull Vector3 separationAtStart(@NotNull Sweep target, @NotNull Sweep projectile) {
        return projectile.start().position().minus(target.start().position());
    }

    private static @NotNull Vector3 relativeVelocityAtStart(@NotNull Sweep target, @NotNull Sweep projectile) {
        return projectile.start().velocity().minus(target.start().velocity());
    }

    private static @NotNull Vector3 positionAt(@NotNull Sweep sweep, double fraction) {
        return sweep.start().position().plus(sweep.end().position().minus(sweep.start().position()).times(fraction));
    }
}

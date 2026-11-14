package astronomicalsystem.core.physics;

import astronomicalsystem.core.impact.Impact;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.GravitationalParameter;
import org.jetbrains.annotations.NotNull;

/**
 * Two bodies touching during a step: which one is hit, how hard, where and in which direction the
 * debris flies.
 *
 * @param target     the larger body
 * @param projectile the smaller body
 * @param fraction   of the step at which they touch, in [0, 1)
 */
public record Contact(@NotNull Sweep target, @NotNull Sweep projectile, double fraction) {

    private static final double G = GravitationalParameter.GRAVITATIONAL_CONSTANT;

    /**
     * @return the contact of the two bodies, the larger as the target; on a tie the first, as in
     *         a merge
     */
    static @NotNull Contact between(@NotNull Sweep first, @NotNull Sweep second, double fraction) {
        boolean firstIsTarget = !second.end().mass().isGreaterThan(first.end().mass());
        return firstIsTarget ? new Contact(first, second, fraction) : new Contact(second, first, fraction);
    }

    /**
     * The impact speed comes from what the two bodies keep while they fall towards each other, at
     * the start of the step, rather than from their velocities at its end, which the step computed
     * as if they had gone through each other: the energy v² / 2 - G M / r gives
     * v² = v0² + 2 G M (1 / d - 1 / r0) at the contact distance d.
     */
    @NotNull Impact impact() {
        double contact = target.end().radius().astronomicalUnits() + projectile.end().radius().astronomicalUnits();
        // Bodies that already overlap meet at their own speed
        double distance = Math.max(separationAtStart().length(), contact);
        double totalMass = target.end().mass().solarMasses() + projectile.end().mass().solarMasses();
        double speedSquared = relativeVelocityAtStart().lengthSquared()
                + 2.0 * G * totalMass * (1.0 / contact - 1.0 / distance);
        return new Impact(target.end().mass(), projectile.end().mass(), contact, Math.sqrt(speedSquared));
    }

    /**
     * @return the unit vector from the centre of the target towards the point where it is hit
     */
    @NotNull Vector3 normal() {
        return positionAt(projectile).minus(positionAt(target)).normalized();
    }

    /**
     * Straight back out of the crater in a head-on impact, leaning downrange in an oblique one:
     * the normal n plus the tangential velocity at contact over the impact speed. With the angular
     * momentum |r0 x v0| kept, the tangential speed at the contact distance d is |r0 x v0| / d.
     *
     * @return the direction in which the debris flies, not necessarily of unit length
     */
    @NotNull Vector3 debrisAxis() {
        Vector3 normal = normal();
        Vector3 relativeVelocity = relativeVelocityAtStart();
        Vector3 tangential = relativeVelocity.minus(normal.times(relativeVelocity.dot(normal)));
        if (tangential.lengthSquared() == 0.0) {
            return normal;
        }
        Impact impact = impact();
        double tangentialSpeed = separationAtStart().cross(relativeVelocity).length() / impact.contactDistance();
        return normal.plus(tangential.normalized().times(Math.min(1.0, tangentialSpeed / impact.speed())));
    }

    private @NotNull Vector3 separationAtStart() {
        return projectile.start().position().minus(target.start().position());
    }

    private @NotNull Vector3 relativeVelocityAtStart() {
        return projectile.start().velocity().minus(target.start().velocity());
    }

    private @NotNull Vector3 positionAt(@NotNull Sweep sweep) {
        return sweep.start().position().plus(sweep.end().position().minus(sweep.start().position()).times(fraction));
    }
}

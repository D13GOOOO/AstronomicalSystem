package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Bodies that can be thrown at another one, from a small asteroid to a body that changes the
 * fate of a planet.
 */
public enum Impactor {
    ASTEROID(1e-12, BodyKind.ASTEROID),
    PLANETESIMAL(1e-3, BodyKind.ASTEROID),
    PROTOPLANET(0.1, BodyKind.PLANET);

    // Far enough to see the impactor coming, near enough that it reaches a planet in a few hours
    private static final double START_DISTANCE_IN_RADII = 20.0;
    // Aiming off the centre makes the impact oblique, as most impacts are
    private static final double AIM_OFFSET_IN_RADII = 0.5;
    // 10 km/s in AU per day, a typical speed of encounter in the inner solar system before the
    // target pulls the impactor in
    private static final double APPROACH_SPEED = 864_000.0 / 149_597_870.7;

    private final double massRatio;
    private final BodyKind kind;

    /**
     * @param massRatio mass of the impactor over the mass of its target
     */
    Impactor(double massRatio, @NotNull BodyKind kind) {
        this.massRatio = massRatio;
        this.kind = kind;
    }

    /**
     * @return the impactor on its way towards the target: twenty radii of the target away in the
     *         direction the approach comes from, aiming half a radius aside of the centre, at 10 km/s
     *         relative to the target
     */
    public @NotNull Body launchedAt(@NotNull Body target, @NotNull Approach approach, @NotNull BodyName name) {
        double targetRadius = target.radius().astronomicalUnits();
        // The same density: the volume, and so the cube of the radius, scales as the mass
        Radius radius = new Radius(targetRadius * Math.cbrt(massRatio));
        Vector3 position = target.position()
                .plus(approach.from().times(START_DISTANCE_IN_RADII * targetRadius))
                .plus(approach.aside().times(AIM_OFFSET_IN_RADII * targetRadius));
        Vector3 velocity = target.velocity().minus(approach.from().times(APPROACH_SPEED));
        return new Body(name, kind, new Mass(massRatio * target.mass().solarMasses()), radius, position, velocity);
    }
}

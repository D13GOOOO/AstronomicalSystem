package astronomicalsystem.core.body;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable snapshot of a body: its identity, physical properties and motion.
 */
public record Body(@NotNull BodyName name,
                   @NotNull BodyKind kind,
                   @NotNull Mass mass,
                   @NotNull Radius radius,
                   @NotNull Vector3 position,
                   @NotNull Vector3 velocity) {

    public @NotNull Body withMotion(@NotNull Vector3 newPosition, @NotNull Vector3 newVelocity) {
        return new Body(name, kind, mass, radius, newPosition, newVelocity);
    }

    /**
     * @param days a finite duration
     * @return this body moved at its current velocity for {@code days}
     */
    public @NotNull Body drifted(double days) {
        return withMotion(position.plus(velocity.times(days)), velocity);
    }

    /**
     * @param acceleration the acceleration, in AU per day squared
     * @param days         a finite duration
     * @return this body with its velocity changed by {@code acceleration} acting for {@code days}
     */
    public @NotNull Body kicked(@NotNull Vector3 acceleration, double days) {
        return withMotion(position, velocity.plus(acceleration.times(days)));
    }

    /**
     * @return whether the spheres of the two bodies intersect; bodies that only touch do not
     */
    public boolean overlaps(@NotNull Body other) {
        double reach = radius.astronomicalUnits() + other.radius.astronomicalUnits();
        return other.position.minus(position).lengthSquared() < reach * reach;
    }

    /**
     * @return the body formed by a perfectly inelastic collision of this body with {@code other}
     */
    public @NotNull Body mergedWith(@NotNull Body other) {
        Mass mergedMass = mass.plus(other.mass);
        return new Body(name, kind, mergedMass, radius.withVolumeAdded(other.radius),
                massWeightedAverage(position, other, other.position, mergedMass),
                massWeightedAverage(velocity, other, other.velocity, mergedMass));
    }

    /**
     * @return {@code (m·own + m'·others) / total}, where {@code m'} is the mass of {@code other}
     *         and {@code total} is {@code m + m'}
     */
    private @NotNull Vector3 massWeightedAverage(@NotNull Vector3 own, @NotNull Body other,
                                                 @NotNull Vector3 others, @NotNull Mass total) {
        return own.times(mass.solarMasses())
                .plus(others.times(other.mass.solarMasses()))
                .times(1.0 / total.solarMasses());
    }
}

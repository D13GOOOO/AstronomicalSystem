package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalDouble;

/**
 * A body over a step, moving in a straight line from where it starts to where it ends.
 */
public record Sweep(@NotNull Body start, @NotNull Body end) {

    /**
     * The separation d + w s, where d is the separation at the start and w its change over the
     * step, reaches the sum of the radii R where |w|² s² + 2 (d·w) s + |d|² - R² = 0. The smaller
     * root, written as c / (-b + √(b² - a c)) so that nothing cancels, is the moment of contact.
     *
     * @return the fraction of the step, in [0, 1), at which the two spheres start to overlap; 0 if
     *         they already do; empty if they do not within the step
     */
    public @NotNull OptionalDouble firstContactWith(@NotNull Sweep other) {
        Vector3 startSeparation = other.start.position().minus(start.position());
        Vector3 change = other.end.position().minus(end.position()).minus(startSeparation);
        double reach = end.radius().astronomicalUnits() + other.end.radius().astronomicalUnits();
        double c = startSeparation.lengthSquared() - reach * reach;
        if (c < 0.0) {
            return OptionalDouble.of(0.0);
        }
        double b = startSeparation.dot(change);
        double discriminant = b * b - change.lengthSquared() * c;
        // Moving apart, or passing at the sum of the radii at the closest
        if (b >= 0.0 || discriminant <= 0.0) {
            return OptionalDouble.empty();
        }
        double fraction = c / (-b + Math.sqrt(discriminant));
        return fraction < 1.0 ? OptionalDouble.of(fraction) : OptionalDouble.empty();
    }

    /**
     * @return the two bodies merged at the start and at the end of the step: since their mutual
     *         forces cancel, the merged body moves as their centre of mass
     */
    public @NotNull Sweep mergedWith(@NotNull Sweep other) {
        return new Sweep(start.mergedWith(other.start), end.mergedWith(other.end));
    }
}

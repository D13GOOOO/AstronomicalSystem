package astronomicalsystem.core.math;

import org.jetbrains.annotations.NotNull;

/**
 * Immutable three-dimensional vector used for positions, velocities and accelerations.
 */
public record Vector3(double x, double y, double z) {

    public @NotNull Vector3 plus(@NotNull Vector3 other) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}

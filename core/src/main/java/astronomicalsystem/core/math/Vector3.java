package astronomicalsystem.core.math;

import org.jetbrains.annotations.NotNull;

/**
 * Immutable three-dimensional vector used for positions, velocities and accelerations.
 */
public record Vector3(double x, double y, double z) {

    public @NotNull Vector3 plus(@NotNull Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    public @NotNull Vector3 minus(@NotNull Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }

    public @NotNull Vector3 times(double scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }
}

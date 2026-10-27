package astronomicalsystem.core.math;

import org.jetbrains.annotations.NotNull;

/**
 * Immutable three-dimensional vector used for positions, velocities and accelerations.
 */
public record Vector3(double x, double y, double z) {

    public static final Vector3 ZERO = new Vector3(0.0, 0.0, 0.0);

    public Vector3 {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("Vector components must be finite");
        }
    }

    public @NotNull Vector3 plus(@NotNull Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    public @NotNull Vector3 minus(@NotNull Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }

    public @NotNull Vector3 times(double scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }

    public double dot(@NotNull Vector3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public double lengthSquared() {
        return dot(this);
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public @NotNull Vector3 normalized() {
        double length = length();
        if (length == 0.0) {
            throw new IllegalStateException("Cannot normalize the zero vector");
        }
        return times(1.0 / length);
    }

    /**
     * @param angle in radians, counterclockwise when seen from the positive z axis
     */
    public @NotNull Vector3 rotatedAboutZ(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector3(x * cos - y * sin, x * sin + y * cos, z);
    }

    /**
     * @param angle in radians, counterclockwise when seen from the positive x axis
     */
    public @NotNull Vector3 rotatedAboutX(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector3(x, y * cos - z * sin, y * sin + z * cos);
    }
}

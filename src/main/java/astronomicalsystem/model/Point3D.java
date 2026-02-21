package astronomicalsystem.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents an immutable vector or point in 3-dimensional space.
 * <p>
 * Implemented as a Java Record to guarantee thread safety during parallel
 * stream computations. All vector operations are side-effect free and
 * yield new instances. Provides optimized squared-magnitude operations
 * to bypass expensive square root calculations during high-frequency physics ticks.
 * </p>
 *
 * @param x the X coordinate.
 * @param y the Y coordinate.
 * @param z the Z coordinate.
 */
public record Point3D(double x, double y, double z) implements Serializable {

    /**
     * The serialization identifier ensuring version compatibility during file I/O operations.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Computes the vector addition of this vector and another.
     *
     * @param other the vector to add.
     * @return a new {@code Point3D} representing the vector sum.
     */
    public Point3D add(Point3D other) {
        return new Point3D(this.x + other.x(), this.y + other.y(), this.z + other.z());
    }

    /**
     * Computes the vector subtraction of another vector from this vector.
     *
     * @param other the vector to subtract.
     * @return a new {@code Point3D} representing the vector difference.
     */
    public Point3D subtract(Point3D other) {
        return new Point3D(this.x - other.x(), this.y - other.y(), this.z - other.z());
    }

    /**
     * Scales this vector by a scalar multiplier.
     *
     * @param factor the scalar multiplier.
     * @return a new {@code Point3D} representing the scaled vector.
     */
    public Point3D scale(double factor) {
        return new Point3D(this.x * factor, this.y * factor, this.z * factor);
    }

    /**
     * Calculates the exact Euclidean distance between this point and another.
     * <p>
     * Note: For simple proximity checks, prefer {@link #distanceSq(Point3D)}
     * to avoid the computational overhead of {@code Math.sqrt()}.
     * </p>
     *
     * @param other the target point.
     * @return the Euclidean distance.
     */
    public double distance(Point3D other) {
        return Math.sqrt(distanceSq(other));
    }

    /**
     * Calculates the squared Euclidean distance between this point and another.
     * Highly efficient for collision detection thresholds.
     *
     * @param other the target point.
     * @return the squared distance.
     */
    public double distanceSq(Point3D other) {
        double dx = this.x - other.x();
        double dy = this.y - other.y();
        double dz = this.z - other.z();
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Calculates the Euclidean magnitude (L2 norm) or length of this vector.
     *
     * @return the vector magnitude.
     */
    public double magnitude() {
        return Math.sqrt(magnitudeSq());
    }

    /**
     * Calculates the squared magnitude of this vector.
     *
     * @return the sum of the squared coordinates.
     */
    public double magnitudeSq() {
        return x * x + y * y + z * z;
    }

    /**
     * Returns a formatted string representation of this 3D point.
     *
     * @return a string containing the X, Y, and Z coordinates formatted to two decimal places.
     */
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}
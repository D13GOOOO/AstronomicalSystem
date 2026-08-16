package astronomicalsystem.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * A highly optimized, read-only structure representing either a specific location or a directional force 
 * (like velocity or acceleration) within the three dimensional simulated universe.
 * 
 * By being completely immutable, this object ensures absolute thread safety. When the physics engine calculates 
 * that a planet has moved, it does not alter the existing X, Y, or Z numbers inside this object. Instead, 
 * it calculates the new position and creates an entirely new point. This guarantees that the graphical 
 * renderer will never read a coordinate while it is only halfway through being updated, preventing visual 
 * stuttering and memory corruption.
 * 
 * @param x the horizontal coordinate or magnitude of force along the horizontal axis.
 * @param y the vertical coordinate or magnitude of force along the vertical axis.
 * @param z the depth coordinate or magnitude of force along the depth axis.
 */
public record Point3D(double x, double y, double z) implements Serializable {

    /**
     * The serialization identifier ensuring that saved simulation files containing coordinate data 
     * remain fully compatible with the current version of the application code.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * A permanently available, pre-built point representing exactly zero on all three axes.
     * This is heavily used by the physics engine to set starting velocities or to reset accumulated 
     * forces. By reusing this exact object instead of building a new one full of zeros every single 
     * time, the simulation saves a massive amount of memory and relieves the system from having to 
     * clean up thousands of discarded objects every second.
     */
    public static final Point3D ZERO = new Point3D(0.0, 0.0, 0.0);

    /**
     * Combines this current point or vector with another one, adding their respective X, Y, and Z 
     * values together to create a brand new point. This is heavily used when applying an acceleration 
     * force to an existing velocity, or when applying a velocity to a physical position to move an object.
     *
     * @param other the second vector or point to add to this one.
     * @return a completely new point containing the mathematical sum of both inputs.
     */
    public Point3D add(Point3D other) {
        return new Point3D(this.x + other.x(), this.y + other.y(), this.z + other.z());
    }

    /**
     * Subtracts the X, Y, and Z values of another point or vector from this current one, generating 
     * a brand new point. This is primarily used to find the exact directional line stretching from 
     * one planet to another when calculating gravitational pull.
     *
     * @param other the vector or point to mathematically subtract from this one.
     * @return a completely new point containing the mathematical difference.
     */
    public Point3D subtract(Point3D other) {
        return new Point3D(this.x - other.x(), this.y - other.y(), this.z - other.z());
    }

    /**
     * Multiplies the X, Y, and Z values of this point by a specific number, lengthening or shrinking 
     * the vector while maintaining its exact direction. This is used when a physical force needs to 
     * be amplified or reduced based on the mass of a planet.
     *
     * @param factor the numerical multiplier to apply to the coordinates.
     * @return a completely new point representing the amplified or reduced vector.
     */
    public Point3D scale(double factor) {
        return new Point3D(this.x * factor, this.y * factor, this.z * factor);
    }

    /**
     * Calculates the dot product between this vector and another. This is a mathematical operation 
     * that helps determine how similar the directions of two different forces are.
     *
     * @param other the target vector to compare against this one.
     * @return a single number representing the mathematical dot product of the two vectors.
     */
    public double dotProduct(Point3D other) {
        return this.x * other.x() + this.y * other.y() + this.z * other.z();
    }

    /**
     * Calculates the exact, real straight line physical distance between this point and another point 
     * in the 3D space. Because this requires calculating a square root, it is a heavy mathematical 
     * operation and is only used when the exact real distance is absolutely necessary.
     *
     * @param other the target point to measure the distance to.
     * @return the exact distance separating the two points.
     */
    public double distance(Point3D other) {
        return Math.sqrt(distanceSq(other));
    }

    /**
     * Calculates the squared physical distance between this point and another. This skips the final 
     * square root calculation, making it incredibly fast. The physics engine uses this constantly when 
     * checking if two planets are close enough to collide, as comparing squared distances is much faster 
     * than comparing exact distances and yields the same true or false result.
     *
     * @param other the target point to measure the squared distance to.
     * @return the squared distance separating the two points.
     */
    public double distanceSq(Point3D other) {
        double dx = this.x - other.x();
        double dy = this.y - other.y();
        double dz = this.z - other.z();
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Calculates the exact physical length (or magnitude) of this vector from the center of the universe 
     * to its coordinates. Like the exact distance calculation, this requires a heavy square root operation.
     *
     * @return the exact length or strength of the vector.
     */
    public double magnitude() {
        return Math.sqrt(magnitudeSq());
    }

    /**
     * Calculates the squared physical length (or magnitude) of this vector. By skipping the square root, 
     * it provides a lightning fast way to compare the relative strength of two different forces or velocities 
     * without bogging down the processor.
     *
     * @return the squared length or strength of the vector.
     */
    public double magnitudeSq() {
        return x * x + y * y + z * z;
    }

    /**
     * Calculates and returns a new directional vector that points in the exact same direction as this one, 
     * but has its length mathematically reduced to exactly one. This is used when the physics engine needs 
     * to know the pure direction a planet is heading without factoring in how fast it is going. If the current 
     * vector has no length, it safely returns the zero constant to prevent the computer from attempting to 
     * divide by zero.
     *
     * @return a new, purely directional vector with a length of exactly one, or the zero vector if it had no length.
     */
    public Point3D normalize() {
        double mag = magnitude();
        if (mag == 0.0) {
            return ZERO;
        }
        return new Point3D(x / mag, y / mag, z / mag);
    }

    /**
     * Generates a clean, readable text representation of the three coordinates contained within this point, 
     * rounding the numbers to two decimal places so they can be easily read on the user interface panels.
     *
     * @return a formatted text string displaying the X, Y, and Z coordinates.
     */
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}
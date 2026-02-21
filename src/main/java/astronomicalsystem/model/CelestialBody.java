package astronomicalsystem.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the fundamental physical properties of a celestial entity.
 * <p>
 * This class adheres to the Single Responsibility Principle (SRP) by isolating
 * core physical attributes (mass, radius, position) from UI-specific metadata.
 * It strictly adheres to the Liskov Substitution Principle (LSP) by omitting
 * kinetic behaviors (velocity, positional updates), ensuring that both static
 * and dynamic subclasses can inherit without violating contracts.
 * </p>
 */
public abstract class CelestialBody implements Comparable<CelestialBody>, Serializable {

    /**
     * The serialization identifier ensuring version compatibility during file I/O operations.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final double mass;
    private final double radius;

    private Point3D position;
    private BodyMetadata metadata;

    /**
     * Constructs the base physical representation of a celestial body.
     *
     * @param name     the unique identifier of the body.
     * @param mass     the mass in simulation units.
     * @param radius   the radius in simulation units.
     * @param position the initial spatial coordinates as an immutable point.
     */
    public CelestialBody(String name, double mass, double radius, Point3D position) {
        this.name = name;
        this.mass = mass;
        this.radius = radius;
        this.position = position;
        this.metadata = new BodyMetadata("N/A", "N/A", "N/A", "N/A", "N/A");
    }

    /**
     * Updates the spatial coordinates of this body.
     *
     * @param newPosition the new immutable 3D coordinates.
     */
    public void setPosition(Point3D newPosition) {
        this.position = newPosition;
    }

    /**
     * Assigns non-physical, descriptive data for UI representation.
     *
     * @param metadata the record containing descriptive strings.
     */
    public void setMetadata(BodyMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Retrieves the unique identifier of this celestial body.
     *
     * @return the name string.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the mass of this body.
     *
     * @return the mass in simulation units.
     */
    public double getMass() {
        return mass;
    }

    /**
     * Retrieves the physical/visual radius of this body.
     *
     * @return the radius in simulation units.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Retrieves the current spatial position of this body.
     *
     * @return the immutable {@link Point3D} representing the coordinates.
     */
    public Point3D getPosition() {
        return position;
    }

    /**
     * Retrieves the descriptive metadata associated with this body.
     *
     * @return the immutable {@link BodyMetadata} record.
     */
    public BodyMetadata getMetadata() {
        return metadata;
    }

    /**
     * Compares this celestial body with another based on their unique names.
     *
     * @param other the object to be compared.
     * @return a negative integer, zero, or a positive integer as this body's name
     * is lexicographically less than, equal to, or greater than the specified body's name.
     */
    @Override
    public int compareTo(CelestialBody other) {
        return this.name.compareTo(other.name);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Equality is strictly determined by the unique {@code name} identifier.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CelestialBody that = (CelestialBody) o;
        return name.equals(that.name);
    }

    /**
     * Returns a hash code value for this body, derived from its unique name.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Encapsulates purely descriptive UI data, fulfilling the Single Responsibility Principle.
     * @param displayMass      the formatted string representation of the real-world mass.
     * @param displayRadius    the formatted string representation of the real-world radius.
     * @param temperature      the formatted string representation of the surface temperature.
     * @param rotationPeriod   the formatted string representation of the axial rotation time.
     * @param revolutionPeriod the formatted string representation of the orbital period.
     */
    public record BodyMetadata(
            String displayMass,
            String displayRadius,
            String temperature,
            String rotationPeriod,
            String revolutionPeriod
    ) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}
package astronomicalsystem.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the fundamental physical properties of a celestial entity within the simulation.
 * This abstract base class forms the foundation of all simulated objects, establishing their most basic 
 * characteristics: a unique name, a mass, a physical radius, and a coordinate position in 3D space.
 * 
 * It strictly adheres to architectural principles by isolating these core physical attributes from 
 * user interface metadata and from complex physics behaviors. By specifically omitting moving or 
 * kinetic behaviors (like tracking velocity or updating positions), it allows both stationary objects 
 * (like stars) and moving objects (like planets) to inherit from this same base class without causing 
 * logical contradictions in the physics engine.
 * 
 * The system relies entirely on the unique name of the celestial body to identify it. Therefore, 
 * mathematical equality and hash codes are evaluated using only this name string.
 */
public abstract class CelestialBody implements Comparable<CelestialBody>, Serializable {

    /**
     * The serialization identifier ensuring that saved simulation files remain compatible 
     * with the current version of the application code.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The unique, human-readable identifier for this celestial body.
     */
    private final String name;

    /**
     * The physical mass of the celestial body, represented in internal simulation units.
     */
    private final double mass;

    /**
     * The physical and visual radius of the celestial body, represented in internal simulation units.
     */
    private final double radius;

    /**
     * The current spatial coordinates of the celestial body within the simulated 3D universe.
     */
    private Point3D position;

    /**
     * A structured container holding optional, human-readable text data used strictly for displaying 
     * information in the graphical user interface.
     */
    private BodyMetadata metadata;

    /**
     * Constructs the base physical representation of a celestial body with all required foundational attributes.
     *
     * @param name     the unique, human-readable identifier of the body. Must not be null.
     * @param mass     the mass of the object in internal simulation units. Must be a positive value.
     * @param radius   the size of the object in internal simulation units. Must be a positive value.
     * @param position the initial starting coordinates represented by an immutable 3D point. Must not be null.
     * @throws NullPointerException     if either the name or the initial position is null.
     * @throws IllegalArgumentException if the provided mass or radius is a negative number.
     */
    public CelestialBody(String name, double mass, double radius, Point3D position) {
        this.name = Objects.requireNonNull(name, "Celestial body name cannot be null");
        
        if (mass < 0.0) {
            throw new IllegalArgumentException("Mass cannot be negative: " + mass);
        }
        if (radius < 0.0) {
            throw new IllegalArgumentException("Radius cannot be negative: " + radius);
        }
        
        this.mass = mass;
        this.radius = radius;
        this.position = Objects.requireNonNull(position, "Initial position cannot be null");
        
        this.metadata = BodyMetadata.EMPTY;
    }

    /**
     * Updates the spatial coordinates of this celestial body to a new location.
     *
     * @param newPosition the new immutable 3D coordinates representing the target location. Must not be null.
     * @throws NullPointerException if the provided position object is null.
     */
    public void setPosition(Point3D newPosition) {
        this.position = Objects.requireNonNull(newPosition, "New position cannot be null");
    }

    /**
     * Attaches descriptive, non-physical text data to this body strictly for presentation purposes 
     * in the graphical user interface.
     *
     * @param metadata the data record containing formatted descriptive strings. Must not be null.
     * @throws NullPointerException if the provided metadata record is null.
     */
    public void setMetadata(BodyMetadata metadata) {
        this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
    }

    /**
     * Retrieves the unique string identifier assigned to this celestial body.
     *
     * @return the unique name string.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the current mass of this celestial body.
     *
     * @return the mass value in internal simulation units.
     */
    public double getMass() {
        return mass;
    }

    /**
     * Retrieves the physical and visual radius of this celestial body.
     *
     * @return the radius value in internal simulation units.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Retrieves the exact current spatial coordinates of this celestial body.
     *
     * @return an immutable 3D point representing the current location in the simulated universe.
     */
    public Point3D getPosition() {
        return position;
    }

    /**
     * Retrieves the descriptive text metadata associated with this celestial body.
     *
     * @return the metadata record containing human-readable statistics, or an empty placeholder 
     *         if no data was explicitly assigned.
     */
    public BodyMetadata getMetadata() {
        return metadata;
    }

    /**
     * Compares this celestial body with another celestial body strictly based on their unique names.
     * This sorting mechanism allows collections of bodies to be organized alphabetically.
     *
     * @param other the other celestial body object to compare against.
     * @return a negative integer, zero, or a positive integer as the name of this body is 
     *         lexicographically less than, equal to, or greater than the name of the other body.
     */
    @Override
    public int compareTo(CelestialBody other) {
        return this.name.compareTo(other.name);
    }

    /**
     * Determines whether another object is considered mathematically equal to this celestial body.
     * Because celestial bodies are uniquely identified by their names within the simulation, 
     * this equality check ignores physical properties and strictly compares the name strings.
     *
     * @param o the reference object to check for equality.
     * @return true if the provided object is a celestial body possessing the exact same name; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CelestialBody that = (CelestialBody) o;
        return name.equals(that.name);
    }

    /**
     * Generates a mathematical hash code for this celestial body to optimize storage and retrieval 
     * in hash-based collections. This value is derived exclusively from the unique name of the body.
     *
     * @return an integer hash code value generated from the body's name.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * A structured data record that encapsulates purely descriptive text data meant for the user interface.
     * It completely separates presentation concerns from the mathematics of the physics engine.
     * 
     * @param displayMass      a pre-formatted string representing the estimated real-world mass for display.
     * @param displayRadius    a pre-formatted string representing the estimated real-world radius for display.
     * @param temperature      a pre-formatted string indicating the average surface temperature.
     * @param rotationPeriod   a pre-formatted string indicating the time required for one full axial rotation.
     * @param revolutionPeriod a pre-formatted string indicating the time required to complete one orbital year.
     */
    public record BodyMetadata(
            String displayMass,
            String displayRadius,
            String temperature,
            String rotationPeriod,
            String revolutionPeriod
    ) implements Serializable {
        
        /**
         * The serialization identifier ensuring that saved simulation files remain compatible.
         */
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * A reusable, memory-efficient constant representing a completely empty metadata record.
         * It is utilized as a safe default for generated objects to avoid null pointer errors 
         * in the graphical interface.
         */
        public static final BodyMetadata EMPTY = new BodyMetadata("N/A", "N/A", "N/A", "N/A", "N/A");
    }
}
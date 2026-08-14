package astronomicalsystem.model;

import java.io.Serial;

/**
 * Represents a static, luminous celestial body, typically acting as the primary
 * gravitational anchor of a planetary system.
 * <p>
 * <b>Architecture Note:</b> This class purposefully omits the {@link Kinematic} interface. 
 * By actively rejecting kinetic behaviors (such as numerical integration and spatial translation), 
 * it strictly adheres to the Liskov Substitution Principle (LSP). It eliminates the anti-pattern
 * of implementing empty or dummy physics methods that violate architectural contracts.
 * </p>
 * <p>
 * By implementing the {@link AnchoredBody} marker interface, the physics engine recognizes 
 * this entity as a stationary gravity well. While its {@link #setPosition(Point3D)} method 
 * remains technically accessible via the {@link CelestialBody} base class, the engine will 
 * intrinsically bypass this body during velocity and position update phases.
 * </p>
 */
public class Star extends CelestialBody implements AnchoredBody {

    /**
     * The serialization identifier ensuring version compatibility during file I/O operations.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new stationary stellar body.
     *
     * @param name     the unique identifier of the star (must not be null).
     * @param mass     the mass in simulation units (must be >= 0).
     * @param radius   the visual and physical simulation radius (must be >= 0).
     * @param position the fixed spatial coordinates represented by an immutable {@link Point3D} (must not be null).
     * @throws NullPointerException     if {@code name} or {@code position} is null.
     * @throws IllegalArgumentException if {@code mass} or {@code radius} is strictly negative.
     */
    public Star(String name, double mass, double radius, Point3D position) {
        super(name, mass, radius, position);
    }

    /**
     * Returns a formatted string representation of this stellar body.
     *
     * @return a string containing the star's name, mass, radius, and fixed coordinates.
     */
    @Override
    public String toString() {
        return String.format("Star: %s [M:%.2f R:%.2f] Pos:%s",
                getName(), getMass(), getRadius(), getPosition().toString());
    }
}
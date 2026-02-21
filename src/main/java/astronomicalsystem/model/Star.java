package astronomicalsystem.model;

import java.io.Serial;

/**
 * Represents a static, luminous celestial body, typically acting as the primary
 * gravitational anchor of a planetary system.
 * <p>
 * This class purposefully omits the {@link Kinematic} interface. By actively rejecting
 * kinetic behaviors (such as velocity updates and spatial translation), it strictly
 * adheres to the Liskov Substitution Principle (LSP). It eliminates the anti-pattern
 * of implementing empty or dummy methods that violate architectural contracts.
 * </p>
 */
public class Star extends CelestialBody {

    /**
     * The serialization identifier ensuring version compatibility during file I/O operations.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new stationary stellar body.
     *
     * @param name     the unique identifier of the star.
     * @param mass     the mass in simulation units.
     * @param radius   the visual and physical simulation radius.
     * @param position the fixed spatial coordinates represented by an immutable {@link Point3D}.
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
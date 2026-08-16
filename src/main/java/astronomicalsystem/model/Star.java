package astronomicalsystem.model;

import java.io.Serial;

/**
 * A celestial body representing a massive, stationary star that serves as the central gravitational 
 * anchor for a planetary system. 
 * 
 * Unlike dynamic planets, a star does not move through space in this specific simulation. By intentionally 
 * refusing to implement the standard kinematic movement interface, this class strictly follows clean 
 * software architecture rules. It actively avoids the bad programming practice of writing empty or fake 
 * movement methods for an object that is meant to remain perfectly still. 
 * 
 * Because it implements the anchored body interface, the physics engine immediately recognizes this 
 * object as a fixed gravitational well. The engine will calculate how fiercely this star pulls on every 
 * other planet in the universe, but it will completely skip this star when calculating movement and 
 * updating spatial coordinates, guaranteeing that it remains locked in its exact starting position.
 */
public class Star extends CelestialBody implements AnchoredBody {

    /**
     * The serialization identifier ensuring that saved simulation files containing star objects 
     * remain fully compatible with the current version of the application code when they are loaded 
     * back into memory.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new stationary star and locks it into a permanent position within the simulated universe. 
     * As the heavy anchor of the system, its massive weight will immediately begin exerting a gravitational 
     * pull on all other dynamic bodies around it.
     *
     * @param name     the unique, human readable identifier assigned to this star.
     * @param mass     the immense physical mass of the star in internal simulation units. Must be zero or greater.
     * @param radius   the size of the star used for graphical rendering and collision detection. Must be zero or greater.
     * @param position the exact, permanent starting coordinates represented by an immutable three dimensional point.
     * @throws NullPointerException     if either the given name or the coordinate position is missing or null.
     * @throws IllegalArgumentException if the provided mass or radius is a negative number, as physical properties cannot be negative.
     */
    public Star(String name, double mass, double radius, Point3D position) {
        super(name, mass, radius, position);
    }

    /**
     * Creates and returns a readable text summary of this stellar body, which includes its unique name, 
     * its mass weight, its physical size, and its exact permanent coordinates in the three dimensional space.
     *
     * @return a descriptive string detailing the identity, weight, size, and location of the star.
     */
    @Override
    public String toString() {
        return String.format("Star: %s [M:%.2f R:%.2f] Pos:%s",
                getName(), getMass(), getRadius(), getPosition().toString());
    }
}
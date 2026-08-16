package astronomicalsystem.model;

import java.io.Serial;
import java.util.Objects;

/**
 * Represents a natural satellite that is permanently bound to a host planet by gravitational forces.
 * Unlike free roaming planets, a moon follows a strict, predictable mathematical path around its central 
 * host rather than calculating complex gravitational pulls from every other body in the universe. 
 * By separating this fixed orbital behavior from the general physics engine, the simulation avoids 
 * unnecessary calculations and guarantees that the moon will remain perfectly stable in its orbit 
 * without gradually drifting away or crashing into its host.
 */
public class Moon extends CelestialBody implements OrbitingBody {

    /**
     * The serialization identifier ensuring that saved simulation files containing moon objects 
     * remain fully compatible with the current version of the application code when they are loaded 
     * back into memory.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * A precalculated mathematical constant representing a complete full circle. This is used 
     * internally to quickly reset the moon's orbital angle once it completes a full revolution, 
     * saving the processor from having to calculate the multiplication repeatedly during every 
     * single frame of the simulation.
     */
    private static final double TWO_PI = 2.0 * Math.PI;

    /**
     * The central planetary body that this moon orbits around. The moon will continuously track 
     * this host planet's position and update its own location to stay nearby.
     */
    private final Planet host;

    /**
     * The fixed spatial distance maintained between the center of the moon and the center 
     * of its host planet.
     */
    private final double orbitalDistance;

    /**
     * The speed at which the moon travels along its circular path around the host planet, 
     * measured as an angle change per simulation step. A positive value means it travels in a 
     * standard forward direction, while a negative value makes it travel backward in a retrograde orbit.
     */
    private final double orbitalSpeed;

    /**
     * The current position of the moon along its circular orbital path, tracked as an angle. 
     * This value steadily increases or decreases over time to create the smooth circular motion 
     * around the host planet.
     */
    private double currentAngle;

    /**
     * Constructs a new moon object and locks it into a stable orbit around a specific host planet. 
     * The starting position of the moon is automatically calculated by taking the host planet's 
     * current location and shifting it outward by the specified orbital distance along the horizontal axis.
     *
     * @param name            the unique, human readable identifier assigned to this moon.
     * @param mass            the physical mass of the moon in internal simulation units. Must be zero or greater.
     * @param visualRadius    the size of the moon used for graphical rendering in the simulation. Must be zero or greater.
     * @param host            the specific planet that acts as the gravitational center for this moon's orbit. Must not be null.
     * @param orbitalDistance the exact radius of the circular path this moon will carve around its host planet. Must be zero or greater.
     * @param orbitalSpeed    the rate at which the moon rotates around the host planet at every step of time.
     * @throws NullPointerException     if either the given name or the target host planet is missing or null.
     * @throws IllegalArgumentException if the provided orbital distance is a negative number, as physical distances cannot be negative.
     */
    public Moon(String name, double mass, double visualRadius, Planet host, double orbitalDistance, double orbitalSpeed) {
        super(name, mass, visualRadius, new Point3D(
                Objects.requireNonNull(host, "Host planet cannot be null").getPosition().x() + orbitalDistance,
                host.getPosition().y(),
                host.getPosition().z()
        ));
        
        if (orbitalDistance < 0.0) {
            throw new IllegalArgumentException("Orbital distance cannot be negative: " + orbitalDistance);
        }
        
        this.host = host;
        this.orbitalDistance = orbitalDistance;
        this.orbitalSpeed = orbitalSpeed;
        this.currentAngle = 0.0;
    }

    /**
     * Calculates the next physical position of the moon along its orbital path and physically moves it there. 
     * This method updates the current tracking angle by adding the orbital speed, ensuring the angle stays 
     * within the boundaries of a single circle to prevent mathematical precision issues over very long simulations. 
     * It then determines the exact horizontal and depth coordinates by calculating the circular path relative to 
     * the host planet's ever changing position, essentially dragging the moon along wherever the host planet travels.
     */
    @Override
    public void updateOrbit() {
        this.currentAngle += this.orbitalSpeed;
        
        if (this.currentAngle > TWO_PI) {
            this.currentAngle -= TWO_PI;
        } else if (this.currentAngle < 0.0) {
            this.currentAngle += TWO_PI;
        }

        double newX = this.host.getPosition().x() + (this.orbitalDistance * Math.cos(this.currentAngle));
        double newZ = this.host.getPosition().z() + (this.orbitalDistance * Math.sin(this.currentAngle));

        this.setPosition(new Point3D(newX, this.host.getPosition().y(), newZ));
    }
    
    /**
     * Creates and returns a readable text summary of this moon, which includes its unique name, 
     * the name of the host planet it orbits, and its current exact coordinates in the three dimensional space.
     *
     * @return a descriptive string detailing the identity and location of the moon.
     */
    @Override
    public String toString() {
        return String.format("Moon: %s [Host:%s] Pos:%s",
                getName(), host.getName(), getPosition().toString());
    }
}
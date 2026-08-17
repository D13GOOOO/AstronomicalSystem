package astronomicalsystem.model;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

/**
 * Represents a dynamic celestial body that freely roams the simulated universe based on the 
 * physical forces of gravity. By adhering to the moving object contract, this entity actively 
 * participates in the complex physics engine calculations step by step.
 * 
 * It utilizes a strict immutable structure for tracking its physical coordinates and movement vectors. 
 * This means every time the planet moves, it generates entirely new mathematical coordinate data 
 * rather than altering the old ones. This design completely eliminates the risk of memory corruption 
 * or reading errors when the graphical interface tries to draw the planet at the exact same moment 
 * the physics engine is moving it.
 */
public class Planet extends CelestialBody implements Kinematic {

    /**
     * The serialization identifier ensuring that saved simulation files containing planetary objects 
     * remain fully compatible with the current version of the application code when they are loaded 
     * back into memory.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The current speed and directional heading of the planet traveling through the three dimensional 
     * space, represented by an immutable point structure.
     */
    private Point3D velocity;

    /**
     * Constructs a new dynamic planetary body and places it into the simulated universe. 
     * The planet begins entirely stationary by default, possessing zero velocity, until gravitational 
     * forces begin to act upon it or an initial push is explicitly applied by the system.
     *
     * @param name     the unique, human readable identifier assigned to this planet. Must not be null.
     * @param mass     the physical mass of the planet in internal simulation units. Must be zero or greater.
     * @param radius   the size of the planet used for graphical rendering and collision detection. Must be zero or greater.
     * @param position the exact starting coordinates represented by an immutable three dimensional point. Must not be null.
     */
    public Planet(String name, double mass, double radius, Point3D position) {
        super(name, mass, radius, position);
        this.velocity = Point3D.ZERO;
    }

    /**
     * Explicitly overwrites the current speed and directional heading of the planet. 
     * This is primarily utilized when the planet is initially created by the user and requires 
     * a starting push to establish a stable orbit, or during physical collisions when the physics 
     * engine calculates the resulting shared momentum of two recently merged bodies.
     *
     * @param velocity the new immutable three dimensional point dictating the exact speed and direction.
     * @throws NullPointerException if the provided velocity object is null.
     */
    @Override
    public void setVelocity(Point3D velocity) {
        this.velocity = Objects.requireNonNull(velocity, "Velocity vector cannot be null");
    }

    /**
     * Retrieves the current speed and directional heading of the planet.
     *
     * @return an immutable three dimensional point representing the exact velocity vector.
     */
    @Override
    public Point3D getVelocity() {
        return this.velocity;
    }

    /**
     * Calculates the immense gravitational pull exerted on this planet by every other celestial body 
     * in the universe and alters its current speed and trajectory accordingly. This method hands over 
     * the complex gravitational mathematics to the dedicated physical interactions utility to ensure 
     * calculation safety, mathematical stability, and crash prevention.
     *
     * @param systemBodies the complete list containing every celestial body currently existing 
     *                     in the simulation universe.
     * @param dt           the specific slice of time, or Delta Time, being simulated. This guarantees 
     *                     that the gravitational acceleration is properly scaled before permanently altering 
     *                     the planet's velocity vector.
     */
    @Override
    public void updateVelocity(List<CelestialBody> systemBodies, double dt) {
        Interactions.updateGravitationalVelocity(this, systemBodies, dt);
    }

    /**
     * Takes the newly calculated velocity and applies it to the current physical location of the planet, 
     * effectively moving it forward through space for the current simulation frame. 
     * 
     * By multiplying the velocity by the slice of time (Delta Time), it ensures the physical distance 
     * traveled is perfectly accurate regardless of whether the simulation is running in slow motion or 
     * fast forward. This operation generates a brand new coordinate object to replace the old one, 
     * guaranteeing that the graphical renderer will never accidentally read partially updated coordinates.
     * 
     * @param dt the precise fraction of time, or Delta Time, that acts as a multiplier to determine exactly 
     *           how far the planet physically traveled during this specific calculation step.
     */
    @Override
    public void updatePosition(double dt) {
        this.setPosition(this.getPosition().add(this.velocity.scale(dt)));
    }

    /**
     * Calculates the raw kinetic energy of this specific planetary body. Because the planet itself 
     * does not hold the entire system data in memory, it cannot calculate the gravitational potential 
     * energy shared with other planets. Therefore, it calculates only the pure movement energy derived 
     * from its own mass and speed. 
     * 
     * It highly optimizes this process by using squared magnitude calculations to completely bypass 
     * the heavy processing cost of calculating square roots.
     *
     * @return a single number representing the kinetic movement energy of the planet.
     */
    @Override
    public double totalEnergy() {
        return 0.5 * this.getMass() * this.velocity.magnitudeSq();
    }

    /**
     * Creates and returns a readable text summary of this planetary body, which includes its unique name, 
     * its mass weight, and its exact current coordinates in the three dimensional space.
     *
     * @return a descriptive string detailing the identity, weight, and location of the planet.
     */
    @Override
    public String toString() {
        return String.format("Planet: %s [M:%.2f] Pos:%s",
                getName(), getMass(), getPosition().toString());
    }
}
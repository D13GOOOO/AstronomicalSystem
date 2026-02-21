package astronomicalsystem.model;

import java.io.Serial;
import java.util.List;

/**
 * Represents a dynamic celestial body that orbits within the astronomical system.
 * <p>
 * Implements the {@link Kinematic} interface to participate in the physics
 * engine's integration steps. By strictly utilizing the immutable {@link Point3D}
 * record for spatial vectors, position and velocity updates are executed without
 * side effects, guaranteeing thread safety during parallel physics ticks.
 * </p>
 */
public class Planet extends CelestialBody implements Kinematic {

    /**
     * The serialization identifier ensuring version compatibility during file I/O operations.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private Point3D velocity;

    /**
     * Constructs a new dynamic planetary body.
     *
     * @param name     the unique identifier of the planet.
     * @param mass     the mass in simulation units.
     * @param radius   the visual and physical simulation radius.
     * @param position the initial spatial coordinates represented by an immutable {@link Point3D}.
     */
    public Planet(String name, double mass, double radius, Point3D position) {
        super(name, mass, radius, position);
        this.velocity = new Point3D(0.0, 0.0, 0.0);
    }

    /**
     * Explicitly overwrites the current velocity vector.
     *
     * @param velocity the new immutable {@link Point3D} velocity vector.
     */
    @Override
    public void setVelocity(Point3D velocity) {
        this.velocity = velocity;
    }

    /**
     * Retrieves the current velocity vector of the planet.
     *
     * @return the immutable {@link Point3D} representing the velocity vector.
     */
    @Override
    public Point3D getVelocity() {
        return this.velocity;
    }

    /**
     * Calculates the net gravitational acceleration exerted by all other bodies
     * in the system and updates the internal velocity vector.
     * <p>
     * Delegates the complex $N$-body gravitational force calculations to the
     * {@link Interactions} utility class.
     * </p>
     *
     * @param systemBodies the complete list of celestial bodies in the simulation.
     */
    @Override
    public void updateVelocity(List<CelestialBody> systemBodies) {
        Interactions.updateGravitationalVelocity(this, systemBodies);
    }

    /**
     * Integrates the current velocity vector into the spatial position.
     * <p>
     * Generates a new coordinate vector to replace the current state,
     * entirely eliminating race conditions in parallel execution contexts.
     * </p>
     */
    @Override
    public void updatePosition() {
        this.setPosition(this.getPosition().add(this.velocity));
    }

    /**
     * Calculates the total kinetic energy of this planetary body.
     * <p>
     * Utilizes the highly optimized {@link Point3D#magnitudeSq()} to bypass
     * expensive square root calculations during the $E_k = \frac{1}{2}mv^2$ derivation.
     * </p>
     *
     * @return the kinetic energy scalar.
     */
    @Override
    public double totalEnergy() {
        return 0.5 * this.getMass() * this.velocity.magnitudeSq();
    }

    /**
     * Returns a formatted string representation of this planetary body.
     *
     * @return a string containing the planet's name, mass, and coordinates.
     */
    @Override
    public String toString() {
        return String.format("Planet: %s [M:%.2f] Pos:%s",
                getName(), getMass(), getPosition().toString());
    }
}
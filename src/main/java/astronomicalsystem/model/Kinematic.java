package astronomicalsystem.model;

import java.util.List;

/**
 * Defines the contract for celestial entities subject to physical forces, kinematics, and orbital motion.
 * <p>
 * Segregating this behavior from the base {@link CelestialBody} class ensures strict adherence
 * to the Liskov Substitution Principle (LSP) and the Interface Segregation Principle (ISP).
 * Static bodies (e.g., central stars) are no longer forced to implement or mock kinetic behaviors,
 * maintaining clean inheritance hierarchies.
 * </p>
 */
public interface Kinematic {

    /**
     * Retrieves the current velocity vector of the entity.
     *
     * @return the immutable {@link Point3D} representing the velocity vector.
     */
    Point3D getVelocity();

    /**
     * Explicitly overwrites the current velocity vector.
     * <p>
     * Typically used during initialization or immediate inelastic collisions.
     * </p>
     *
     * @param velocity the new immutable {@link Point3D} velocity vector.
     */
    void setVelocity(Point3D velocity);

    /**
     * Calculates the net gravitational acceleration exerted by all other bodies in the system
     * and updates the internal velocity vector accordingly.
     * <p>
     * This method represents the first half of the discrete integration step (Euler/Verlet integration).
     * </p>
     *
     * @param systemBodies the complete list of celestial bodies in the simulation exerting gravitational pull.
     */
    void updateVelocity(List<CelestialBody> systemBodies);

    /**
     * Integrates the current velocity vector into the spatial position.
     * <p>
     * This method represents the second half of the discrete integration step. It must guarantee
     * thread safety, typically by yielding a new immutable position instance rather than mutating state.
     * </p>
     */
    void updatePosition();

    /**
     * Calculates the total energy (kinetic + potential) of the kinematic body relative to the system.
     *
     * @return the total energy scalar value.
     */
    double totalEnergy();
}
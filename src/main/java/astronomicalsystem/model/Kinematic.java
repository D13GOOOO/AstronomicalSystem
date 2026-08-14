package astronomicalsystem.model;

import java.util.List;

/**
 * Defines the contract for celestial entities subject to physical forces, kinematics, and dynamic orbital motion.
 * <p>
 * <b>Architecture Note:</b> Segregating this behavior from the base {@link CelestialBody} class ensures strict 
 * adherence to the Liskov Substitution Principle (LSP) and the Interface Segregation Principle (ISP).
 * Static bodies (e.g., central stars) or bodies with parametric orbits (e.g., moons) are no longer forced 
 * to implement or mock kinetic behaviors, preventing fragile code and maintaining clean inheritance hierarchies.
 * </p>
 */
public interface Kinematic {

    /**
     * Retrieves the current velocity vector of the kinematic entity.
     *
     * @return the immutable {@link Point3D} representing the velocity vector in 3D space.
     */
    Point3D getVelocity();

    /**
     * Explicitly overwrites the current velocity vector.
     * <p>
     * Typically used during initial spawning (e.g., from the Creator Panel) or to resolve 
     * immediate physical reactions such as perfectly inelastic collisions where momentum is conserved.
     * </p>
     *
     * @param velocity the new immutable {@link Point3D} velocity vector.
     */
    void setVelocity(Point3D velocity);

    /**
     * Calculates the net gravitational acceleration exerted by all other bodies in the system
     * and updates the internal velocity vector accordingly.
     * <p>
     * <b>Physics Engine Detail:</b> This method represents the first half of a discrete symplectic 
     * integration step (e.g., Semi-implicit Euler integration). It must be invoked for all bodies 
     * <i>before</i> any body updates its position.
     * </p>
     *
     * @param systemBodies the complete {@link List} of celestial bodies in the simulation exerting gravitational pull. 
     *                     Using a {@code List} (specifically {@code ArrayList}) ensures optimal CPU cache locality 
     *                     during $O(N^2)$ N-body interactions.
     */
    void updateVelocity(List<CelestialBody> systemBodies);

    /**
     * Integrates the current velocity vector into the spatial position.
     * <p>
     * <b>Physics Engine Detail:</b> This represents the second half of the discrete integration step. 
     * It relies on the velocity previously calculated by {@link #updateVelocity(List)}. 
     * Implementations should guarantee thread safety, typically by yielding a new immutable position 
     * instance rather than mutating state directly if processed in parallel streams.
     * </p>
     */
    void updatePosition();

    /**
     * Calculates the total mechanical energy (Kinetic Energy + Gravitational Potential Energy) 
     * of the kinematic body relative to the system.
     * <p>
     * This metric is extremely useful for profiling the stability of the numerical integrator: 
     * in a closed system without external forces, the sum of total energies should remain constant.
     * </p>
     *
     * @return the total mechanical energy scalar value (Joules or simulation-scaled equivalent).
     */
    double totalEnergy();
}
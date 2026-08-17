package astronomicalsystem.model;

import java.util.List;

/**
 * Defines a strict architectural contract for any celestial entity that travels through space
 * dynamically by calculating physical forces.
 * 
 * By isolating moving, force-driven behaviors into this specific interface, the system adheres 
 * to modern software design principles. It ensures that stationary objects (like central stars) 
 * or objects that follow strict mathematical paths without calculating gravity (like moons) 
 * do not have to write fake or empty methods just to fit into the simulation. Only objects that 
 * actually calculate their own speed and respond to gravity will implement this interface.
 * 
 * The methods within this contract are explicitly designed to accept a time scalar, commonly 
 * known as Delta Time. This enables the physics engine to safely accelerate, decelerate, or 
 * partition the flow of time into smaller sub-steps without breaking the underlying mathematical 
 * integration.
 */
public interface Kinematic {

    /**
     * Retrieves the exact direction and speed at which the celestial entity is currently traveling.
     *
     * @return an immutable 3D point containing the current speed and direction of the entity.
     */
    Point3D getVelocity();

    /**
     * Instantly forces the celestial entity to adopt a new speed and direction of travel.
     * This is typically executed when an object is first created by the user and needs an 
     * initial push, or immediately after a physical collision when two bodies merge and 
     * share their combined momentum.
     *
     * @param velocity the new immutable 3D point dictating the exact speed and direction.
     */
    void setVelocity(Point3D velocity);

    /**
     * Calculates the immense gravitational pull exerted by every single celestial body in the 
     * universe and alters the current speed and trajectory of this specific entity accordingly.
     * 
     * To prevent mathematical chaos and ensure the simulation remains stable, this step is 
     * executed for every single moving object in the universe before any object is actually 
     * allowed to change its physical location. The resulting physical acceleration is multiplied 
     * by the provided slice of time to guarantee that the physics remain mathematically accurate 
     * whether the simulation is running in real-time or fast-forward.
     *
     * @param systemBodies the complete list containing every celestial body currently existing 
     *                     in the simulation universe.
     * @param dt           the exact slice of time, or Delta Time, being simulated during this 
     *                     specific calculation step. It acts as a strict multiplier to determine 
     *                     how much of the calculated acceleration is actually applied to the 
     *                     current velocity vector.
     */
    void updateVelocity(List<CelestialBody> systemBodies, double dt);

    /**
     * Takes the velocity that was previously calculated by the physics engine and uses it to 
     * physically move the celestial entity to its new coordinates in 3D space.
     * 
     * This represents the second and final phase of a single physics frame. It executes only 
     * after all the gravitational forces in the universe have been fully computed and applied 
     * to the velocities of the moving objects. The object's velocity is multiplied by the slice 
     * of time to determine the exact distance it physically traveled before arriving at its new 
     * coordinate position.
     * 
     * @param dt the precise fraction of time, or Delta Time, that has passed during this specific 
     *           calculation step, acting as a multiplier for the physical distance traveled.
     */
    void updatePosition(double dt);

    /**
     * Calculates the total amount of mechanical energy this moving body currently possesses.
     * This involves analyzing how fast the object is moving combined with how intensely it is 
     * being pulled by the gravity of the surrounding system.
     * 
     * This calculation serves as an essential diagnostic tool. In a perfect, isolated universe, 
     * the total combined energy of all moving objects should remain perfectly steady over time.
     * Tracking this number allows the system to verify that the physics engine is calculating 
     * the passage of time accurately without generating artificial energy or slowing down due 
     * to mathematical errors.
     *
     * @return a single number representing the overall energy value of the moving body.
     */
    double totalEnergy();
}
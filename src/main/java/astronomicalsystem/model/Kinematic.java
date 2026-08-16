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
     * allowed to change its physical location.
     *
     * @param systemBodies the complete list containing every celestial body currently existing 
     *                     in the simulation universe.
     */
    void updateVelocity(List<CelestialBody> systemBodies);

    /**
     * Takes the velocity that was previously calculated by the physics engine and uses it to 
     * physically move the celestial entity to its new coordinates in 3D space.
     * 
     * This represents the second and final phase of a single physics frame. It executes only 
     * after all the gravitational forces in the universe have been fully computed and applied 
     * to the velocities of the moving objects.
     */
    void updatePosition();

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
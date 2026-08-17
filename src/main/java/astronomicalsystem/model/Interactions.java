package astronomicalsystem.model;

import java.util.List;

import astronomicalsystem.config.SimulationSettings;

/**
 * A critical utility class that acts as the mathematical core of the physics engine.
 * This class is strictly responsible for processing the interactions between celestial bodies,
 * specifically calculating gravitational attraction across the N-body system and resolving
 * physical collisions when objects intersect.
 * 
 * To ensure maximum stability during the simulation, this class includes built-in protective
 * measures. It actively prevents "division by zero" errors when bodies get extremely close,
 * clamps extreme accelerations to stop bodies from being shot out of the universe at impossible
 * speeds (the slingshot effect), and sanitizes all coordinate outputs to prevent the graphical
 * rendering pipeline from crashing due to invalid numbers.
 */
public final class Interactions {

    /**
     * The universal gravitational constant retrieved from the central configuration file.
     * This multiplier dictates the overall strength of gravity within the simulated universe.
     */
    public static final double G = SimulationSettings.defaults().physics().gravitationalConstant();

    private static final double SAFE_SOFTENING_FACTOR = 0.5;
    private static final double SAFE_COLLISION_FACTOR = 0.8;
    private static final double MAX_ACCELERATION = 500.0;

    /**
     * Private constructor to prevent the instantiation of this utility class.
     * All physical interaction methods are static and should be accessed directly through the class name.
     */
    private Interactions() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated.");
    }

    /**
     * Calculates the total gravitational pull exerted on a specific moving body by every other
     * body currently existing in the system, and applies that acceleration to its velocity, scaled 
     * by the provided slice of simulated time (Delta Time).
     * 
     * The method calculates the exact distance between the target body and the interacting body.
     * If the distance is dangerously close to zero, a softening limit is enforced to prevent
     * mathematical underflow. Once the pure gravitational force is determined, it is distributed
     * across the X, Y, and Z axes. Finally, the total acceleration is capped at a maximum limit
     * to prevent physics glitches, multiplied by the exact amount of time that has passed, and 
     * applied to the target's current velocity.
     *
     * @param target       the moving celestial entity whose velocity is currently being updated.
     * @param systemBodies the complete list of all celestial bodies present in the simulation universe.
     * @param dt           the exact slice of time, or Delta Time, being simulated. This correctly 
     *                     scales the gravitational acceleration before it is applied to the velocity vector.
     */
    public static void updateGravitationalVelocity(Kinematic target, List<CelestialBody> systemBodies, double dt) {
        CelestialBody targetBody = (CelestialBody) target;

        double accX = 0.0;
        double accY = 0.0;
        double accZ = 0.0;

        double tX = targetBody.getPosition().x();
        double tY = targetBody.getPosition().y();
        double tZ = targetBody.getPosition().z();
        double tRadius = targetBody.getRadius();

        for (CelestialBody other : systemBodies) {
            if (targetBody == other) continue;

            double dx = other.getPosition().x() - tX;
            double dy = other.getPosition().y() - tY;
            double dz = other.getPosition().z() - tZ;

            double distSq = dx * dx + dy * dy + dz * dz;

            double minDistance = (tRadius + other.getRadius()) * SAFE_SOFTENING_FACTOR;
            if (minDistance < 1.0) minDistance = 1.0; 
            
            if (distSq < minDistance * minDistance) continue;

            double interactionFactor = (other instanceof Kinematic) ? 0.0001 : 1.0;
            double dist = Math.sqrt(distSq);
            
            double forceMag = G * other.getMass() * interactionFactor; 
            
            double denominator = distSq * dist;
            if (denominator <= 0.0001) continue; 
            
            double accMultiplier = forceMag / denominator;

            if (Double.isNaN(accMultiplier) || Double.isInfinite(accMultiplier)) continue;

            accX += accMultiplier * dx;
            accY += accMultiplier * dy;
            accZ += accMultiplier * dz;
        }

        accX = Math.max(-MAX_ACCELERATION, Math.min(MAX_ACCELERATION, accX));
        accY = Math.max(-MAX_ACCELERATION, Math.min(MAX_ACCELERATION, accY));
        accZ = Math.max(-MAX_ACCELERATION, Math.min(MAX_ACCELERATION, accZ));

        if (!Double.isNaN(accX) && !Double.isNaN(accY) && !Double.isNaN(accZ)) {
            Point3D totalAcceleration = new Point3D(accX, accY, accZ);
            
            // The raw acceleration is multiplied by Delta Time before being added to velocity
            target.setVelocity(target.getVelocity().add(totalAcceleration.scale(dt)));
        }
    }

    /**
     * Inspects the spatial distance between two celestial entities to determine if their physical
     * boundaries are overlapping. If an overlap is detected, it processes a perfectly inelastic
     * collision, destroying both original bodies and returning a single, newly formed merged entity.
     * 
     * To ensure stability, a slightly reduced collision threshold is used. This guarantees that
     * bodies register as collided right before they deeply intersect, preventing the physics engine
     * from executing extreme gravitational calculations on bodies that are essentially inside one another.
     *
     * @param b1 the first celestial body involved in the proximity check.
     * @param b2 the second celestial body involved in the proximity check.
     * @return a new merged celestial body if a collision occurred, or null if the bodies remain separated.
     */
    public static CelestialBody checkCollision(CelestialBody b1, CelestialBody b2) {
        double combinedRadius = b1.getRadius() + b2.getRadius();
        
        double collisionThreshold = combinedRadius * SAFE_COLLISION_FACTOR;

        if (b1.getPosition().distanceSq(b2.getPosition()) < collisionThreshold * collisionThreshold) {
            
            boolean hasStaticAnchor = (b1 instanceof AnchoredBody) || (b2 instanceof AnchoredBody);
            if (hasStaticAnchor) {
                System.out.println("HUNGRY! A static anchor consumed " + (b1 instanceof Kinematic ? b1.getName() : b2.getName()));
            } else {
                System.out.println("COLLISION! " + b1.getName() + " and " + b2.getName() + " have merged.");
            }
            
            return mergeBodies(b1, b2);
        }

        return null;
    }

    /**
     * Executes the mathematical integration necessary to combine two colliding celestial bodies into one.
     * 
     * The resulting entity absorbs the mass of both original bodies. Its physical radius is recalculated
     * based on the combined volume of the two spheres. To respect the conservation of momentum, the new
     * velocity and spatial coordinates are calculated using a weighted average based on the masses of the
     * original objects. If one of the bodies is a fixed anchor (like a star), the resulting merged body
     * will remain fixed exactly at the anchor's original coordinates.
     *
     * @param b1 the first celestial body involved in the collision.
     * @param b2 the second celestial body involved in the collision.
     * @return the newly formed composite celestial body containing the combined mass and momentum.
     */
    private static CelestialBody mergeBodies(CelestialBody b1, CelestialBody b2) {
        double newMass = b1.getMass() + b2.getMass();
        double mergedRadius = mergedRadius(b1, b2);
        boolean anchored = (b1 instanceof AnchoredBody) || (b2 instanceof AnchoredBody);

        Point3D wPos;
        if (anchored) {
            wPos = (b1 instanceof AnchoredBody) ? b1.getPosition() : b2.getPosition();
        } else {
            Point3D p1 = b1.getPosition().scale(b1.getMass());
            Point3D p2 = b2.getPosition().scale(b2.getMass());
            wPos = p1.add(p2).scale(1.0 / newMass);
        }

        Point3D v1 = (b1 instanceof Kinematic k1) ? k1.getVelocity() : Point3D.ZERO;
        Point3D v2 = (b2 instanceof Kinematic k2) ? k2.getVelocity() : Point3D.ZERO;

        Point3D wv1 = v1.scale(b1.getMass());
        Point3D wv2 = v2.scale(b2.getMass());
        Point3D wVel = wv1.add(wv2).scale(1.0 / newMass);

        String newName = b1.getMass() > b2.getMass() ? b1.getName() : b2.getName();

        CelestialBody merged = anchored
                ? new Star(newName + "+", newMass, mergedRadius, wPos)
                : new Planet(newName + "+", newMass, mergedRadius, wPos);

        if (merged instanceof Kinematic kinematicMerged) {
            kinematicMerged.setVelocity(wVel);
        }

        merged.setMetadata(b1.getMass() > b2.getMass() ? b1.getMetadata() : b2.getMetadata());

        return merged;
    }

    /**
     * Calculates the physical radius of a newly merged celestial body by adding the volumes of the two
     * original bodies and deriving the new radius from that total combined volume.
     *
     * @param b1 the first celestial body.
     * @param b2 the second celestial body.
     * @return the derived radius for the resulting merged sphere.
     */
    private static double mergedRadius(CelestialBody b1, CelestialBody b2) {
        double volume = Math.pow(b1.getRadius(), 3) + Math.pow(b2.getRadius(), 3);
        return Math.cbrt(volume);
    }
}
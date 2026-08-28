package astronomicalsystem.model;

import java.util.ArrayList;
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
    
    /**
     * A strict threshold requiring bodies to physically intersect heavily before triggering an impact.
     * This high value (0.85) allows planets to pass extremely close to one another, naturally 
     * executing orbital slingshot maneuvers via gravity without falsely triggering a collision.
     */
    private static final double SAFE_COLLISION_FACTOR = 0.85;
    
    private static final double MAX_ACCELERATION = 500.0;
    
    private static int debrisCounter = 1;

    /**
     * Private constructor to prevent the instantiation of this utility class.
     * All physical interaction methods are static and should be accessed directly through the class name.
     */
    private Interactions() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated.");
    }

    /**
     * Calculates the net gravitational acceleration vector exerted on a specific point in space 
     * by all celestial bodies in the given system, excluding the body located at that point.
     * <p>
     * <b>Selective Gravity Mechanics:</b> To maintain the aesthetic stability of the original 
     * solar system while permitting realistic physics for user-generated content, an interaction 
     * factor is applied. Original base planets pulling on other original base planets have their 
     * gravity reduced. However, stars and custom user-spawned entities always interact with the 
     * entire universe using 100% Newtonian gravity.
     * </p>
     *
     * @param targetPosition the exact 3D coordinates where the gravitational field is being measured.
     * @param targetRadius   the physical radius of the object at this position (used for softening).
     * @param targetBody     the actual body at this location (used strictly to ignore self-gravity 
     *                       and determine gravity scaling rules).
     * @param systemBodies   the complete list of all other mass-bearing bodies in the universe.
     * @return a Point3D representing the raw acceleration vector (ax, ay, az).
     */
    public static Point3D calculateAccelerationVector(Point3D targetPosition, double targetRadius, CelestialBody targetBody, List<CelestialBody> systemBodies) {
        double accX = 0.0;
        double accY = 0.0;
        double accZ = 0.0;

        double tX = targetPosition.x();
        double tY = targetPosition.y();
        double tZ = targetPosition.z();

        boolean isTargetStar = targetBody instanceof Star;
        boolean isTargetCustom = targetBody.getName().toLowerCase().contains("custom");

        for (CelestialBody other : systemBodies) {
            if (targetBody == other) continue;

            double dx = other.getPosition().x() - tX;
            double dy = other.getPosition().y() - tY;
            double dz = other.getPosition().z() - tZ;

            double distSq = dx * dx + dy * dy + dz * dz;

            double minDistance = (targetRadius + other.getRadius()) * SAFE_SOFTENING_FACTOR;
            if (minDistance < 1.0) minDistance = 1.0; 
            
            if (distSq < minDistance * minDistance) continue;

            boolean isOtherStar = other instanceof Star;
            boolean isOtherCustom = other.getName().toLowerCase().contains("custom");

            double interactionFactor = 1.0;
            if (!isTargetStar && !isOtherStar && !isTargetCustom && !isOtherCustom) {
                interactionFactor = 0.0001;
            }

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
            return new Point3D(accX, accY, accZ);
        }
        
        return Point3D.ZERO;
    }

    /**
     * Calculates the total gravitational pull exerted on a specific moving body by every other
     * body currently existing in the system, and applies that acceleration to its velocity, scaled 
     * by the provided slice of simulated time (Delta Time).
     * 
     * @param target       the moving celestial entity whose velocity is currently being updated.
     * @param systemBodies the complete list of all celestial bodies present in the simulation universe.
     * @param dt           the exact slice of time, or Delta Time, being simulated.
     */
    public static void updateGravitationalVelocity(Kinematic target, List<CelestialBody> systemBodies, double dt) {
        CelestialBody targetBody = (CelestialBody) target;
        
        Point3D acceleration = calculateAccelerationVector(
            targetBody.getPosition(), 
            targetBody.getRadius(), 
            targetBody, 
            systemBodies
        );
        
        target.setVelocity(target.getVelocity().add(acceleration.scale(dt)));
    }

    /**
     * Inspects the spatial distance between two celestial entities to determine if their physical
     * boundaries are overlapping. If an overlap is detected, it calculates the impact energy and angle 
     * to determine the thermodynamic result: a total annihilation (merge + debris) or a glancing blow (deflection + debris).
     *
     * @param b1 the first celestial body involved in the proximity check.
     * @param b2 the second celestial body involved in the proximity check.
     * @return a List of resulting celestial bodies (cores and fragments) if an impact occurred, or null if separated.
     */
    public static List<CelestialBody> checkCollision(CelestialBody b1, CelestialBody b2) {
        // I frammenti sono fantasmi fisici: si disperdono nello spazio ma non entrano in collisione, 
        // prevenendo l'attivazione di crash a catena e loop infiniti.
        if (b1 instanceof Fragment || b2 instanceof Fragment) {
            return null;
        }

        double combinedRadius = b1.getRadius() + b2.getRadius();
        double collisionThreshold = combinedRadius * SAFE_COLLISION_FACTOR;

        double dx = b1.getPosition().x() - b2.getPosition().x();
        double dy = b1.getPosition().y() - b2.getPosition().y();
        double dz = b1.getPosition().z() - b2.getPosition().z();
        
        double distSq = dx * dx + dy * dy + dz * dz;

        if (distSq < collisionThreshold * collisionThreshold) {
            return resolveImpact(b1, b2, dx, dy, dz, Math.sqrt(distSq));
        }

        return null;
    }

    /**
     * Calculates the exact physics of the collision, generating the appropriate physical response based 
     * on the angle of impact and the relative velocity of the objects.
     *
     * @param b1      the first colliding body.
     * @param b2      the second colliding body.
     * @param dx      the distance along the X axis.
     * @param dy      the distance along the Y axis.
     * @param dz      the distance along the Z axis.
     * @param dist    the absolute scalar distance between the two centers.
     * @return a List of resulting celestial entities.
     */
    private static List<CelestialBody> resolveImpact(CelestialBody b1, CelestialBody b2, double dx, double dy, double dz, double dist) {
        List<CelestialBody> results = new ArrayList<>();
        boolean hasStaticAnchor = (b1 instanceof AnchoredBody) || (b2 instanceof AnchoredBody);

        if (hasStaticAnchor) {
            System.out.println("ANNIHILATION: A massive anchor consumed " + (b1 instanceof Kinematic ? b1.getName() : b2.getName()));
            results.add(mergeBodies(b1, b2, 1.0));
            return results;
        }

        Point3D v1 = (b1 instanceof Kinematic k1) ? k1.getVelocity() : Point3D.ZERO;
        Point3D v2 = (b2 instanceof Kinematic k2) ? k2.getVelocity() : Point3D.ZERO;

        double vRelX = v1.x() - v2.x();
        double vRelY = v1.y() - v2.y();
        double vRelZ = v1.z() - v2.z();
        double vRelMag = Math.sqrt(vRelX * vRelX + vRelY * vRelY + vRelZ * vRelZ);

        double nx = (dist > 0) ? dx / dist : 1.0;
        double ny = (dist > 0) ? dy / dist : 0.0;
        double nz = (dist > 0) ? dz / dist : 0.0;

        double impactFactor = 1.0;
        if (vRelMag > 0.001) {
            double dot = (vRelX * nx + vRelY * ny + vRelZ * nz) / vRelMag;
            impactFactor = Math.abs(dot);
        }

        if (impactFactor > 0.6) {
            System.out.println("TOTAL ANNIHILATION: Direct impact between " + b1.getName() + " and " + b2.getName());
            CelestialBody mergedCore = mergeBodies(b1, b2, 0.85); 
            results.add(mergedCore);
            results.addAll(spawnDebris(b1, b2, 0.15, vRelMag, mergedCore.getRadius() * 2.0, nx, ny, nz, true)); 
        } else {
            System.out.println("GLANCING BLOW: " + b1.getName() + " and " + b2.getName() + " collided and shattered!");
            results.addAll(deflectAndShatter(b1, b2, nx, ny, nz, vRelMag));
        }

        return results;
    }

    /**
     * Executes a perfectly inelastic collision, forming a single new core entity.
     *
     * @param b1                  the first celestial body involved in the collision.
     * @param b2                  the second celestial body involved in the collision.
     * @param massRetentionFactor the percentage of total mass that survives to form the core (0.0 to 1.0).
     * @return the newly formed composite celestial body containing the combined mass and momentum.
     */
    private static CelestialBody mergeBodies(CelestialBody b1, CelestialBody b2, double massRetentionFactor) {
        double rawMass = b1.getMass() + b2.getMass();
        double newMass = rawMass * massRetentionFactor;
        
        double mergedRadius = Math.cbrt(Math.pow(b1.getRadius(), 3) + Math.pow(b2.getRadius(), 3)) * massRetentionFactor;
        
        boolean anchored = (b1 instanceof AnchoredBody) || (b2 instanceof AnchoredBody);

        Point3D wPos;
        if (anchored) {
            wPos = (b1 instanceof AnchoredBody) ? b1.getPosition() : b2.getPosition();
        } else {
            Point3D p1 = b1.getPosition().scale(b1.getMass());
            Point3D p2 = b2.getPosition().scale(b2.getMass());
            wPos = p1.add(p2).scale(1.0 / rawMass);
        }

        Point3D v1 = (b1 instanceof Kinematic k1) ? k1.getVelocity() : Point3D.ZERO;
        Point3D v2 = (b2 instanceof Kinematic k2) ? k2.getVelocity() : Point3D.ZERO;

        Point3D wv1 = v1.scale(b1.getMass());
        Point3D wv2 = v2.scale(b2.getMass());
        Point3D wVel = wv1.add(wv2).scale(1.0 / rawMass);

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
     * Deflects both bodies based on a grazing impact, shedding mass and spawning debris in the process.
     *
     * @param b1      the first colliding body.
     * @param b2      the second colliding body.
     * @param nx      the normal X vector of the impact.
     * @param ny      the normal Y vector of the impact.
     * @param nz      the normal Z vector of the impact.
     * @param vRelMag the magnitude of the relative velocity between the two bodies.
     * @return a List containing the two deflected, damaged cores and the generated debris fragments.
     */
    private static List<CelestialBody> deflectAndShatter(CelestialBody b1, CelestialBody b2, double nx, double ny, double nz, double vRelMag) {
        List<CelestialBody> results = new ArrayList<>();
        
        double m1 = b1.getMass();
        double m2 = b2.getMass();
        double totalMass = m1 + m2;
        
        double impulseMag = vRelMag * 0.4; 
        Point3D normal = new Point3D(nx, ny, nz);
        
        Point3D v1 = (b1 instanceof Kinematic k1) ? k1.getVelocity() : Point3D.ZERO;
        Point3D v2 = (b2 instanceof Kinematic k2) ? k2.getVelocity() : Point3D.ZERO;

        Point3D newV1 = v1.add(normal.scale(impulseMag * (m2 / totalMass)));
        Point3D newV2 = v2.subtract(normal.scale(impulseMag * (m1 / totalMass)));

        Planet core1 = new Planet(b1.getName() + " (Damaged)", m1 * 0.8, b1.getRadius() * 0.95, b1.getPosition());
        core1.setVelocity(newV1);
        core1.setMetadata(b1.getMetadata());
        results.add(core1);

        Planet core2 = new Planet(b2.getName() + " (Damaged)", m2 * 0.8, b2.getRadius() * 0.95, b2.getPosition());
        core2.setVelocity(newV2);
        core2.setMetadata(b2.getMetadata());
        results.add(core2);
        
        double maxRadius = Math.max(core1.getRadius(), core2.getRadius());
        results.addAll(spawnDebris(b1, b2, 0.20, vRelMag, maxRadius * 2.0, nx, ny, nz, false));

        return results;
    }

    /**
     * Generates a structural burst of Fragment objects utilizing impact normals and momentum conservation.
     * Employs a clearance radius to guarantee fragments spawn safely outside the boundaries of any 
     * surviving cores, preventing instantaneous and infinite collision loops in the physics engine.
     *
     * @param b1                      the first colliding body.
     * @param b2                      the second colliding body.
     * @param destroyedMassPercentage the percentage of the total mass converted into debris.
     * @param impactSpeed             the velocity magnitude at the moment of impact.
     * @param clearanceRadius         the minimum absolute distance from the center of mass required to spawn fragments safely.
     * @param nx                      the normal X vector of the impact.
     * @param ny                      the normal Y vector of the impact.
     * @param nz                      the normal Z vector of the impact.
     * @param isAnnihilation          if true, spawns an accretion disk; if false, spawns a deflection cone.
     * @return a List containing the newly generated debris fragments.
     */
    private static List<CelestialBody> spawnDebris(CelestialBody b1, CelestialBody b2, double destroyedMassPercentage, double impactSpeed, double clearanceRadius, double nx, double ny, double nz, boolean isAnnihilation) {
        List<CelestialBody> debris = new ArrayList<>();
        
        double totalMass = b1.getMass() + b2.getMass();
        double debrisMassAvailable = totalMass * destroyedMassPercentage;
        
        int numFragments = 8 + (int)(Math.random() * 8); 
        double massPerFragment = debrisMassAvailable / numFragments;
        double fragmentRadius = Math.cbrt(Math.pow(b1.getRadius(), 3) + Math.pow(b2.getRadius(), 3)) * 0.12;
        
        Point3D centerOfMass = b1.getPosition().add(b2.getPosition()).scale(0.5);
        
        Point3D v1 = (b1 instanceof Kinematic k1) ? k1.getVelocity() : Point3D.ZERO;
        Point3D v2 = (b2 instanceof Kinematic k2) ? k2.getVelocity() : Point3D.ZERO;
        Point3D vCM = v1.scale(b1.getMass()).add(v2.scale(b2.getMass())).scale(1.0 / totalMass);

        double tempX = (Math.abs(nx) > 0.9) ? 0.0 : 1.0;
        double tempY = (Math.abs(nx) > 0.9) ? 1.0 : 0.0;
        double tempZ = 0.0;

        double t1x = ny * tempZ - nz * tempY;
        double t1y = nz * tempX - nx * tempZ;
        double t1z = nx * tempY - ny * tempX;
        double t1mag = Math.sqrt(t1x * t1x + t1y * t1y + t1z * t1z);
        t1x /= t1mag; t1y /= t1mag; t1z /= t1mag;

        double t2x = ny * t1z - nz * t1y;
        double t2y = nz * t1x - nx * t1z;
        double t2z = nx * t1y - ny * t1x;

        for (int i = 0; i < numFragments; i++) {
            double angle = Math.random() * 2.0 * Math.PI;
            
            double radialScatter = impactSpeed * (isAnnihilation ? (0.2 + Math.random() * 0.4) : (0.1 + Math.random() * 0.2));
            double normalScatter = impactSpeed * (Math.random() - 0.5) * (isAnnihilation ? 0.1 : 0.3);

            if (!isAnnihilation) {
                normalScatter += impactSpeed * 0.2;
            }

            double vx = t1x * Math.cos(angle) * radialScatter + t2x * Math.sin(angle) * radialScatter + nx * normalScatter;
            double vy = t1y * Math.cos(angle) * radialScatter + t2y * Math.sin(angle) * radialScatter + ny * normalScatter;
            double vz = t1z * Math.cos(angle) * radialScatter + t2z * Math.sin(angle) * radialScatter + nz * normalScatter;

            Point3D ejectionVector = new Point3D(vx, vy, vz);
            Point3D fragVelocity = vCM.add(ejectionVector);
            
            Point3D spawnDirection = ejectionVector.normalize();
            double safeDistance = clearanceRadius + (fragmentRadius * 2.0) + (Math.random() * clearanceRadius * 0.5);
            Point3D offsetPos = centerOfMass.add(spawnDirection.scale(safeDistance));
            
            Fragment frag = new Fragment("Debris-" + (debrisCounter++), massPerFragment, fragmentRadius, offsetPos);
            frag.setVelocity(fragVelocity);
            debris.add(frag);
        }
        
        return debris;
    }
}
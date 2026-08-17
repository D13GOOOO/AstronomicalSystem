package astronomicalsystem.engine;

import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.Kinematic;
import astronomicalsystem.model.Point3D;
import astronomicalsystem.model.Star;
import astronomicalsystem.model.Interactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A highly optimized computational utility designed to calculate the future paths of all moving 
 * bodies in the universe without affecting the actual simulation state.
 * 
 * To achieve instantaneous prediction without causing interface lag, this class utilizes a 
 * "Ghost Universe" approach based on Data-Oriented Design. It strips away all object-oriented 
 * overhead, creating lightweight mathematical clones of the current entities. 
 * 
 * This advanced implementation features "Smart Orbital Culling." Instead of blindly calculating 
 * a massive, fixed number of steps for every planet, it mathematically monitors the swept angle 
 * of each body relative to the central star. Once a planet completes exactly one full orbit (360 degrees), 
 * the predictor stops recording its path. This completely eliminates the visual "spirograph" artifact 
 * caused by orbital precession while drastically reducing memory consumption.
 */
public final class TrajectoryPredictor {

    private static final double SAFE_SOFTENING_FACTOR = 0.5;
    private static final double MAX_ACCELERATION = 500.0;
    
    // 2 PI represents exactly one full circular rotation in radians (360 degrees)
    private static final double FULL_ORBIT_RADIANS = Math.PI * 2.0;

    /**
     * Private constructor to prevent instantiation of this computational utility class.
     */
    private TrajectoryPredictor() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated.");
    }

    /**
     * Executes a high-speed parallel timeline simulation to project the future orbital paths of all bodies.
     *
     * @param activeBodies   the list of actual celestial bodies currently existing in the main simulation.
     * @param maxFutureSteps the absolute maximum number of physics frames to compute into the future. 
     *                       Acts as a safety limit if a body escapes orbit and travels in a straight line forever.
     * @param dt             the specific slice of time (Delta Time) used per step.
     * @param recordInterval the interval at which to save a position point.
     * @return a map linking each original moving celestial body to an ordered list of its predicted future coordinates.
     */
    public static Map<CelestialBody, List<Point3D>> calculate(List<CelestialBody> activeBodies, int maxFutureSteps, double dt, int recordInterval) {
        
        List<GhostBody> ghosts = new ArrayList<>();
        Map<CelestialBody, List<Point3D>> trajectories = new HashMap<>();
        
        // Try to identify the central anchor of the solar system (usually the Sun)
        GhostBody centralStar = null;

        for (CelestialBody body : activeBodies) {
            GhostBody ghost = new GhostBody(body);
            ghosts.add(ghost);
            
            if (!ghost.isKinematic && body instanceof Star) {
                centralStar = ghost;
            }
            
            if (ghost.isKinematic) {
                // Initialize the orbital path tracking list
                List<Point3D> path = new ArrayList<>();
                path.add(body.getPosition());
                trajectories.put(body, path);
            }
        }

        int ghostCount = ghosts.size();
        if (ghostCount < 2) {
            return trajectories;
        }
        
        // Initialize the tracking variables for Smart Culling
        if (centralStar != null) {
            for (GhostBody ghost : ghosts) {
                if (ghost.isKinematic) {
                    ghost.lastAngle = calculateAngle(ghost, centralStar);
                    ghost.accumulatedAngle = 0.0;
                    ghost.orbitCompleted = false;
                }
            }
        }

        double[] accX = new double[ghostCount];
        double[] accY = new double[ghostCount];
        double[] accZ = new double[ghostCount];
        
        // To optimize performance, we track if ALL planets have finished their orbits.
        // If they have, we can exit the heavy loop early without waiting for maxFutureSteps.
        boolean allOrbitsCompleted = false;

        for (int step = 1; step <= maxFutureSteps && !allOrbitsCompleted; step++) {
            
            allOrbitsCompleted = true; // Assume true until proven otherwise

            for (int i = 0; i < ghostCount; i++) {
                GhostBody target = ghosts.get(i);
                
                if (!target.isKinematic) continue; 
                
                // If this specific planet hasn't finished its circle, we must keep the engine running
                if (!target.orbitCompleted) {
                    allOrbitsCompleted = false;
                }

                double ax = 0.0, ay = 0.0, az = 0.0;

                for (int j = 0; j < ghostCount; j++) {
                    if (i == j) continue;
                    GhostBody other = ghosts.get(j);

                    double dx = other.px - target.px;
                    double dy = other.py - target.py;
                    double dz = other.pz - target.pz;
                    double distSq = dx * dx + dy * dy + dz * dz;

                    double minDistance = (target.radius + other.radius) * SAFE_SOFTENING_FACTOR;
                    if (minDistance < 1.0) minDistance = 1.0;

                    if (distSq < minDistance * minDistance) continue;

                    double interactionFactor = other.isKinematic ? 0.0001 : 1.0;
                    double dist = Math.sqrt(distSq);
                    double forceMag = Interactions.G * other.mass * interactionFactor; 
                    double denominator = distSq * dist;
                    
                    if (denominator <= 0.0001) continue;
                    
                    double accMultiplier = forceMag / denominator;

                    if (Double.isNaN(accMultiplier) || Double.isInfinite(accMultiplier)) continue;

                    ax += accMultiplier * dx;
                    ay += accMultiplier * dy;
                    az += accMultiplier * dz;
                }

                accX[i] = Math.max(-MAX_ACCELERATION, Math.min(MAX_ACCELERATION, ax));
                accY[i] = Math.max(-MAX_ACCELERATION, Math.min(MAX_ACCELERATION, ay));
                accZ[i] = Math.max(-MAX_ACCELERATION, Math.min(MAX_ACCELERATION, az));
            }

            boolean timeToRecord = (step % recordInterval == 0);

            for (int i = 0; i < ghostCount; i++) {
                GhostBody ghost = ghosts.get(i);
                
                if (!ghost.isKinematic) continue;

                if (!Double.isNaN(accX[i])) ghost.vx += accX[i] * dt;
                if (!Double.isNaN(accY[i])) ghost.vy += accY[i] * dt;
                if (!Double.isNaN(accZ[i])) ghost.vz += accZ[i] * dt;

                ghost.px += ghost.vx * dt;
                ghost.py += ghost.vy * dt;
                ghost.pz += ghost.vz * dt;

                // Only record the position if the planet hasn't closed its loop yet
                if (timeToRecord && !ghost.orbitCompleted) {
                    trajectories.get(ghost.originalRef).add(new Point3D(ghost.px, ghost.py, ghost.pz));
                }
                
                // Smart Culling Math: Check if the planet has completed a 360 degree revolution
                if (centralStar != null && !ghost.orbitCompleted) {
                    double currentAngle = calculateAngle(ghost, centralStar);
                    
                    // Calculate the shortest angular distance traveled since the last frame
                    double deltaAngle = currentAngle - ghost.lastAngle;
                    
                    // Handle the mathematical "wrap around" when crossing from 180 to -180 degrees
                    if (deltaAngle > Math.PI) deltaAngle -= FULL_ORBIT_RADIANS;
                    else if (deltaAngle < -Math.PI) deltaAngle += FULL_ORBIT_RADIANS;
                    
                    ghost.accumulatedAngle += Math.abs(deltaAngle);
                    ghost.lastAngle = currentAngle;
                    
                    // If total swept angle is greater than 360 degrees, lock the orbit!
                    if (ghost.accumulatedAngle >= FULL_ORBIT_RADIANS) {
                        ghost.orbitCompleted = true;
                    }
                }
            }
        }

        return trajectories;
    }
    
    /**
     * Calculates the horizontal 2D angle (in the X-Z plane) between a moving body and the central star.
     * This is the mathematical key to detecting when a full orbit has been completed.
     */
    private static double calculateAngle(GhostBody body, GhostBody center) {
        return Math.atan2(body.pz - center.pz, body.px - center.px);
    }

    private static final class GhostBody {
        final CelestialBody originalRef;
        final double mass;
        final double radius;
        final boolean isKinematic;
        
        double px, py, pz;
        double vx, vy, vz;
        
        // Smart Culling properties
        double lastAngle;
        double accumulatedAngle;
        boolean orbitCompleted;

        GhostBody(CelestialBody originalRef) {
            this.originalRef = originalRef;
            this.mass = originalRef.getMass();
            this.radius = originalRef.getRadius();
            this.isKinematic = originalRef instanceof Kinematic;
            
            Point3D pos = originalRef.getPosition();
            this.px = pos.x();
            this.py = pos.y();
            this.pz = pos.z();
            
            if (this.isKinematic) {
                Point3D vel = ((Kinematic) originalRef).getVelocity();
                this.vx = vel.x();
                this.vy = vel.y();
                this.vz = vel.z();
            } else {
                this.vx = 0.0;
                this.vy = 0.0;
                this.vz = 0.0;
            }
        }
    }
}
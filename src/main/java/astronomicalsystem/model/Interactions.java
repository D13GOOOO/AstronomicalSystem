package astronomicalsystem.model;

import java.util.List;

import astronomicalsystem.config.SimulationSettings;

/**
 * A utility class handling all physical interactions between celestial entities,
 * including N-body gravitational integration and perfectly inelastic collisions.
 * <p>
 * <b>Bulletproof Optimization:</b> This version includes rigorous sanitization against
 * floating-point underflows (NaN/Infinity) which natively crash JavaFX's 3D rendering pipeline.
 * </p>
 */
public final class Interactions {

    public static final double G = SimulationSettings.defaults().physics().gravitationalConstant();

    // Limitatori di sicurezza fissi per evitare che le impostazioni errate rompano la fisica
    private static final double SAFE_SOFTENING_FACTOR = 0.5;
    private static final double SAFE_COLLISION_FACTOR = 0.8;
    
    // Accelerazione massima percorsa in un singolo tick (Anti-Slingshot)
    private static final double MAX_ACCELERATION = 500.0;

    private Interactions() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated.");
    }

    /**
     * Calculates and applies the net gravitational acceleration on a target kinematic body.
     * Contains failsafes to prevent the "NaN Virus" caused by division by near-zero.
     */
    public static void updateGravitationalVelocity(Kinematic target, List<CelestialBody> systemBodies) {
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

            // 1. LIMITATORE DI DISTANZA (Softening)
            // Assicuriamo una distanza minima invalicabile per i calcoli per evitare la divisione per zero
            double minDistance = (tRadius + other.getRadius()) * SAFE_SOFTENING_FACTOR;
            if (minDistance < 1.0) minDistance = 1.0; // Hard limit assoluto
            
            if (distSq < minDistance * minDistance) continue;

            double interactionFactor = (other instanceof Kinematic) ? 0.0001 : 1.0;
            double dist = Math.sqrt(distSq);
            
            double forceMag = G * other.getMass() * interactionFactor; 
            
            // 2. PREVENZIONE SOTTOFLUSSO
            double denominator = distSq * dist;
            if (denominator <= 0.0001) continue; 
            
            double accMultiplier = forceMag / denominator;

            // 3. SANIFICAZIONE NAN/INFINITY
            if (Double.isNaN(accMultiplier) || Double.isInfinite(accMultiplier)) continue;

            accX += accMultiplier * dx;
            accY += accMultiplier * dy;
            accZ += accMultiplier * dz;
        }

        // 4. CLAMP ANTI-FIONDA (Blocca le accelerazioni esagerate che sparano il pianeta a coordinate infinite)
        accX = Math.max(-MAX_ACCELERATION, Math.min(MAX_ACCELERATION, accX));
        accY = Math.max(-MAX_ACCELERATION, Math.min(MAX_ACCELERATION, accY));
        accZ = Math.max(-MAX_ACCELERATION, Math.min(MAX_ACCELERATION, accZ));

        // Applica la velocità finale solo se la matematica è intatta
        if (!Double.isNaN(accX) && !Double.isNaN(accY) && !Double.isNaN(accZ)) {
            Point3D totalAcceleration = new Point3D(accX, accY, accZ);
            target.setVelocity(target.getVelocity().add(totalAcceleration));
        }
    }

    /**
     * Evaluates spatial proximity between two bodies and processes an inelastic merger if they intersect.
     */
    public static CelestialBody checkCollision(CelestialBody b1, CelestialBody b2) {
        double combinedRadius = b1.getRadius() + b2.getRadius();
        
        // Uso un fattore di collisione sicuro per garantire che collidano PRIMA di compenetrarsi e causare bug matematici
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
     * Executes the mathematical integration of two bodies during a perfectly inelastic collision.
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

    private static double mergedRadius(CelestialBody b1, CelestialBody b2) {
        double volume = Math.pow(b1.getRadius(), 3) + Math.pow(b2.getRadius(), 3);
        return Math.cbrt(volume);
    }
}
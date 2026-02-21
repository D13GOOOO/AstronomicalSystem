package astronomicalsystem.model;

import java.util.List;

/**
 * A utility class handling all physical interactions between celestial entities,
 * including N-body gravitational integration and perfectly inelastic collisions.
 * <p>
 * By centralizing these physics calculations, this class enforces the Single
 * Responsibility Principle (SRP). It replaces concrete type checking with
 * capability checking (e.g., {@code instanceof Kinematic}) to comply with the
 * Open/Closed Principle (OCP).
 * </p>
 * <p>
 * Note: Mathematical operations are heavily optimized to avoid floating-point
 * divisions and expensive square root calculations where squared magnitudes suffice.
 * </p>
 */
public final class Interactions {

    /**
     * The universal gravitational constant customized for the simulation's scale.
     */
    public static final double G = 0.1;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Interactions() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated.");
    }

    /**
     * Calculates and applies the net gravitational acceleration on a target kinematic body.
     * <p>
     * Utilizes the optimized vector form of Newton's law of universal gravitation:
     * $\vec{a} = \frac{GM}{r^3} \Delta\vec{r}$, bypassing explicit unit vector derivations.
     * </p>
     *
     * @param target       the dynamic body subject to gravitational forces.
     * @param systemBodies the complete list of bodies exerting gravitational pull.
     */
    public static void updateGravitationalVelocity(Kinematic target, List<CelestialBody> systemBodies) {
        CelestialBody targetBody = (CelestialBody) target;

        double accX = 0.0;
        double accY = 0.0;
        double accZ = 0.0;

        for (CelestialBody other : systemBodies) {
            if (targetBody.equals(other)) continue;

            double dx = other.getPosition().x() - targetBody.getPosition().x();
            double dy = other.getPosition().y() - targetBody.getPosition().y();
            double dz = other.getPosition().z() - targetBody.getPosition().z();

            double distSq = dx * dx + dy * dy + dz * dz;

            // Softening radius to prevent singularity explosions at near-zero distances
            double softeningLimit = (targetBody.getRadius() + other.getRadius()) * 0.5;
            if (distSq < softeningLimit * softeningLimit) continue;

            // OCP: Differentiating gravitational authority by capability (Kinematic), not concrete class
            double interactionFactor = (other instanceof Kinematic) ? 0.0001 : 1.0;

            // Optimization: avoiding 3 separate divisions for the unit vector
            double dist = Math.sqrt(distSq);
            double forceMag = G * other.getMass() * interactionFactor;
            double accMultiplier = forceMag / (distSq * dist); // GM / r^3

            accX += accMultiplier * dx;
            accY += accMultiplier * dy;
            accZ += accMultiplier * dz;
        }

        Point3D totalAcceleration = new Point3D(accX, accY, accZ);
        target.setVelocity(target.getVelocity().add(totalAcceleration));
    }

    /**
     * Evaluates spatial proximity between two bodies and processes an inelastic merger if they intersect.
     * <p>
     * Optimization: Relies entirely on squared distances to completely bypass {@code Math.sqrt()} overhead.
     * </p>
     *
     * @param b1 the first celestial body.
     * @param b2 the second celestial body.
     * @return a new merged {@link CelestialBody} if a collision occurs, otherwise {@code null}.
     */
    public static CelestialBody checkCollision(CelestialBody b1, CelestialBody b2) {
        boolean hasStaticAnchor = !(b1 instanceof Kinematic) || !(b2 instanceof Kinematic);
        double thresholdFactor = hasStaticAnchor ? 0.8 : 0.01;

        double combinedRadius = b1.getRadius() + b2.getRadius();
        double collisionThreshold = combinedRadius * thresholdFactor;

        // Bypassing Math.sqrt() by comparing squared magnitudes
        if (b1.getPosition().distanceSq(b2.getPosition()) < collisionThreshold * collisionThreshold) {

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
     * <p>
     * Calculates the new weighted center of mass and strictly conserves momentum.
     * </p>
     *
     * @param b1 the first colliding body.
     * @param b2 the second colliding body.
     * @return the resulting merged {@link Planet}.
     */
    private static CelestialBody mergeBodies(CelestialBody b1, CelestialBody b2) {
        double newMass = b1.getMass() + b2.getMass();

        // Weighted Center of Mass
        Point3D p1 = b1.getPosition().scale(b1.getMass());
        Point3D p2 = b2.getPosition().scale(b2.getMass());
        Point3D wPos = p1.add(p2).scale(1.0 / newMass);

        // Conservation of Momentum (m1v1 + m2v2 = m_final * v_final)
        Point3D v1 = (b1 instanceof Kinematic k1) ? k1.getVelocity() : new Point3D(0, 0, 0);
        Point3D v2 = (b2 instanceof Kinematic k2) ? k2.getVelocity() : new Point3D(0, 0, 0);

        Point3D wv1 = v1.scale(b1.getMass());
        Point3D wv2 = v2.scale(b2.getMass());
        Point3D wVel = wv1.add(wv2).scale(1.0 / newMass);

        String newName = b1.getMass() > b2.getMass() ? b1.getName() : b2.getName();
        Planet merged = new Planet(newName + "+", newMass, 1.0, wPos);
        merged.setVelocity(wVel);

        // Inherit metadata from the most massive parent body
        merged.setMetadata(b1.getMass() > b2.getMass() ? b1.getMetadata() : b2.getMetadata());

        return merged;
    }
}
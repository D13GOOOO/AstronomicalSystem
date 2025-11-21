import java.util.List;

public class Interactions {

    // Universal Gravitational Constant for the simulation
    public static final double G = 0.1;

    /**
     * Calculates and applies gravitational force on a target body based on other bodies.
     */
    public static void updateGravitationalVelocity(CelestialBody target, List<CelestialBody> systemBodies) {
        Point3D totalAcceleration = new Point3D(0.0, 0.0, 0.0);

        for (CelestialBody other : systemBodies) {
            if (target == other) continue;

            double r = target.getPosition().distance(other.getPosition());

            // Softening (avoids explosions at 0 distance)
            if (r < (target.getRadius() + other.getRadius()) * 0.5) continue;

            // --- INTERACTION RULES ---
            double interactionFactor;

            if (other instanceof Star) {
                // The Sun commands everyone (100%)
                interactionFactor = 1.0;
            } else {
                // Planet vs Planet: minimal interaction (0.01%) for orbit stability
                interactionFactor = 0.0001;
            }

            // Calculate Force
            double force = G * other.getMass() * interactionFactor;
            double accMag = force / (r * r);

            double ux = (other.getPosition().x - target.getPosition().x) / r;
            double uy = (other.getPosition().y - target.getPosition().y) / r;
            double uz = (other.getPosition().z - target.getPosition().z) / r;

            totalAcceleration.x += accMag * ux;
            totalAcceleration.y += accMag * uy;
            totalAcceleration.z += accMag * uz;
        }

        // Apply acceleration to the target body
        target.getVelocity().add(totalAcceleration);
    }

    /**
     * Checks if two bodies collide and returns the merged body if they do.
     * @return The new merged body, or null if no collision occurred.
     */
    public static CelestialBody checkCollision(CelestialBody b1, CelestialBody b2) {
        double dist = b1.getPosition().distance(b2.getPosition());
        double combinedRadius = b1.getRadius() + b2.getRadius();

        // Rule: Easy to crash into the Sun (0.8), difficult between planets (0.01)
        double thresholdFactor = (b1 instanceof Star || b2 instanceof Star) ? 0.8 : 0.01;

        if (dist < combinedRadius * thresholdFactor) {
            // Debug logs
            if (b1 instanceof Star || b2 instanceof Star) {
                System.out.println("HUNGRY! The Sun consumed " + (b1 instanceof Star ? b2.getName() : b1.getName()));
            } else {
                System.out.println("COLLISION! " + b1.getName() + " and " + b2.getName() + " have merged.");
            }
            return b1.merge(b2);
        }

        return null;
    }
}
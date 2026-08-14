package astronomicalsystem.model;

import java.io.Serial;
import java.util.Objects;

/**
 * Represents a natural satellite strictly bound to a host planet.
 * <p>
 * Employs a deterministic parametric orbital model driven by the host body's
 * current position. The moon does not participate in the velocity-based N-body
 * contract, which keeps the API honest and avoids dummy physics methods.
 * By implementing {@link OrbitingBody}, it cleanly separates Keplerian kinematics
 * from Newtonian dynamics, guaranteeing mathematical stability.
 * </p>
 */
public class Moon extends CelestialBody implements OrbitingBody {

    /**
     * The serialization identifier ensuring version compatibility during file I/O operations.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Precomputed constant for a full circle in radians ($2\pi$), used to prevent 
     * multiplication overhead during normalization.
     */
    private static final double TWO_PI = 2.0 * Math.PI;

    private final Planet host;
    private final double orbitalDistance;
    private final double orbitalSpeed;
    private double currentAngle;

    /**
     * Initializes a host-locked satellite.
     *
     * @param name            the unique identifier of the moon (must not be null).
     * @param mass            the mass in simulation units (must be >= 0).
     * @param visualRadius    the scalar radius for rendering (must be >= 0).
     * @param host            the central planetary body to orbit (must not be null).
     * @param orbitalDistance the fixed scalar distance from the host (must be >= 0).
     * @param orbitalSpeed    the angular velocity in radians per tick. Can be negative for retrograde orbits.
     * @throws NullPointerException     if {@code name} or {@code host} is null.
     * @throws IllegalArgumentException if {@code orbitalDistance} is strictly negative.
     */
    public Moon(String name, double mass, double visualRadius, Planet host, double orbitalDistance, double orbitalSpeed) {
        super(name, mass, visualRadius, new Point3D(
                Objects.requireNonNull(host, "Host planet cannot be null").getPosition().x() + orbitalDistance,
                host.getPosition().y(),
                host.getPosition().z()
        ));
        
        if (orbitalDistance < 0.0) {
            throw new IllegalArgumentException("Orbital distance cannot be negative: " + orbitalDistance);
        }
        
        this.host = host;
        this.orbitalDistance = orbitalDistance;
        this.orbitalSpeed = orbitalSpeed;
        this.currentAngle = 0.0;
    }

    /**
     * Advances the orbital state of the moon by one discrete simulation step.
     * <p>
     * The position is calculated using standard 2D circular parametric equations 
     * aligned to the X-Z plane:
     * <ul>
     *   <li>$x = x_{host} + r \cos(\theta)$</li>
     *   <li>$z = z_{host} + r \sin(\theta)$</li>
     * </ul>
     * where $r$ is the {@code orbitalDistance} and $\theta$ is the {@code currentAngle}.
     * </p>
     */
    @Override
    public void updateOrbit() {
        this.currentAngle += this.orbitalSpeed;
        
        // Normalize the angle within [0, 2π] to prevent floating-point precision loss 
        // over long simulation runtimes, handling both prograde and retrograde orbits.
        if (this.currentAngle > TWO_PI) {
            this.currentAngle -= TWO_PI;
        } else if (this.currentAngle < 0.0) {
            this.currentAngle += TWO_PI;
        }

        double newX = this.host.getPosition().x() + (this.orbitalDistance * Math.cos(this.currentAngle));
        double newZ = this.host.getPosition().z() + (this.orbitalDistance * Math.sin(this.currentAngle));

        this.setPosition(new Point3D(newX, this.host.getPosition().y(), newZ));
    }
    
    /**
     * Returns a formatted string representation of this moon.
     *
     * @return a string containing the moon's name, host, and coordinates.
     */
    @Override
    public String toString() {
        return String.format("Moon: %s [Host:%s] Pos:%s",
                getName(), host.getName(), getPosition().toString());
    }
}
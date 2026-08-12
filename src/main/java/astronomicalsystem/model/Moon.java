package astronomicalsystem.model;

/**
 * Represents a natural satellite strictly bound to a host planet.
 * <p>
 * Employs a deterministic parametric orbital model driven by the host body's
 * current position. The moon does not participate in the velocity-based N-body
 * contract, which keeps the API honest and avoids dummy physics methods.
 * </p>
 */
public class Moon extends CelestialBody implements OrbitingBody {

    private final Planet host;
    private final double orbitalDistance;
    private final double orbitalSpeed;
    private double currentAngle;

    /**
     * Initializes a host-locked satellite.
     *
     * @param name the identifier of the moon.
     * @param mass the mass in simulation units.
     * @param visualRadius the scalar radius for rendering.
     * @param host the central planetary body to orbit.
     * @param orbitalDistance the fixed scalar distance from the host.
     * @param orbitalSpeed the angular velocity in radians per tick.
     */
    public Moon(String name, double mass, double visualRadius, Planet host, double orbitalDistance, double orbitalSpeed) {
        super(name, mass, visualRadius, new Point3D(
                host.getPosition().x() + orbitalDistance,
                host.getPosition().y(),
                host.getPosition().z()
        ));
        this.host = host;
        this.orbitalDistance = orbitalDistance;
        this.orbitalSpeed = orbitalSpeed;
        this.currentAngle = 0.0;
    }

    @Override
    public void updateOrbit() {
        this.currentAngle += this.orbitalSpeed;
        if (this.currentAngle > Math.PI * 2) {
            this.currentAngle -= Math.PI * 2;
        }

        double newX = this.host.getPosition().x() + (this.orbitalDistance * Math.cos(this.currentAngle));
        double newZ = this.host.getPosition().z() + (this.orbitalDistance * Math.sin(this.currentAngle));

        this.setPosition(new Point3D(newX, this.host.getPosition().y(), newZ));
    }
}

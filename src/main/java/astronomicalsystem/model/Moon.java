package astronomicalsystem.model;

import java.util.List;

/**
 * Represents a natural satellite strictly bound to a host planet.
 * <p>
 * Employs a deterministic parametric orbital model rather than discrete N-Body
 * integration. This explicitly bypasses global gravitational influences and
 * eliminates numerical drift, ensuring infinite stability around the moving host.
 * </p>
 */
public class Moon extends CelestialBody implements Kinematic {

    private final Planet host;
    private final double orbitalDistance;
    private final double orbitalSpeed;
    private double currentAngle;

    /**
     * Initializes a kinematically locked satellite.
     *
     * @param name            the identifier of the moon.
     * @param mass            the mass in simulation units.
     * @param visualRadius    the scalar radius for rendering.
     * @param host            the central planetary body to orbit.
     * @param orbitalDistance the fixed scalar distance from the host.
     * @param orbitalSpeed    the angular velocity in radians per tick.
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
    public void updateVelocity(List<CelestialBody> bodies) {
    }

    @Override
    public void updatePosition() {
        this.currentAngle += this.orbitalSpeed;
        if (this.currentAngle > Math.PI * 2) {
            this.currentAngle -= Math.PI * 2;
        }

        double newX = this.host.getPosition().x() + (this.orbitalDistance * Math.cos(this.currentAngle));
        double newZ = this.host.getPosition().z() + (this.orbitalDistance * Math.sin(this.currentAngle));

        this.setPosition(new Point3D(newX, this.host.getPosition().y(), newZ));
    }

    @Override
    public Point3D getVelocity() {
        return new Point3D(0, 0, 0);
    }

    @Override
    public void setVelocity(Point3D velocity) {
    }

    @Override
    public double totalEnergy() {
        return 0.0;
    }
}
import java.io.Serial;
import java.util.List;

public class Planet extends CelestialBody {
    @Serial
    private static final long serialVersionUID = 1L;

    private Point3D velocity;

    public Planet(String name, double mass, double radiusSim, double x, double y, double z) {
        super(name, mass, radiusSim, x, y, z);
        this.velocity = new Point3D(0.0, 0.0, 0.0);
    }

    public void setVelocity(Point3D v) {
        this.velocity = v;
    }

    @Override
    public Point3D getVelocity() {
        return this.velocity;
    }

    @Override
    public void updateVelocity(List<CelestialBody> systemBodies) {
        Interactions.updateGravitationalVelocity(this, systemBodies);
    }

    @Override
    public void updatePosition() {
        this.position.add(this.velocity);
    }

    @Override
    public double totalEnergy() {
        return position.norm() * velocity.norm();
    }

    @Override
    public String toString() {
        return String.format("Planet: %s [M:%.2f] Pos:%s", name, mass, position.toString());
    }
}
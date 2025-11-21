import java.io.Serial;
import java.util.List;

public class Star extends CelestialBody {
    @Serial
    private static final long serialVersionUID = 1L;

    public Star(String name, double mass, double radiusSim, double x, double y, double z) {
        super(name, mass, radiusSim, x, y, z);
    }

    @Override
    protected Point3D getVelocity() {
        return new Point3D(0, 0, 0); // Velocità sempre zero
    }

    @Override
    public void updateVelocity(List<CelestialBody> systemBodies) {}

    @Override
    public void updatePosition() {}

    @Override
    public double totalEnergy() { return 0.0; }

    @Override
    public String toString() {
        return String.format("Star: %s [M:%.2f R:%.2f] Pos:%s", name, mass, radius, position.toString());
    }
}
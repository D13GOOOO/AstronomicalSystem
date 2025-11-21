import java.io.Serial;
import java.io.Serializable;

public class Point3D implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public double x;
    public double y;
    public double z;

    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void add(Point3D other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
    }

    public void scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
    }

    public Point3D multiply(double factor) {
        return new Point3D(this.x * factor, this.y * factor, this.z * factor);
    }

    public double distance(Point3D other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double dz = other.z - this.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public double norm() {
        return Math.abs(x) + Math.abs(y) + Math.abs(z);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}
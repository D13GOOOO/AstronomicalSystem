import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public abstract class CelestialBody implements Comparable<CelestialBody>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    protected final String name;
    protected Point3D position;
    protected final double mass;
    protected final double radius;

    protected String tempInfo = "N/A";
    protected String rotPeriod = "N/A";
    protected String revPeriod = "N/A";

    protected String displayMass = null;
    protected String displayRadius = null;

    public CelestialBody(String name, double mass, double radiusSim, double x, double y, double z) {
        this.name = name;
        this.mass = mass;
        this.radius = radiusSim;
        this.position = new Point3D(x, y, z);
    }

    public void setRealData(String realMass, String realRadius, String temp, String rot, String rev) {
        this.displayMass = realMass;
        this.displayRadius = realRadius;
        this.tempInfo = temp;
        this.rotPeriod = rot;
        this.revPeriod = rev;
    }

    public void setExtraData(String temp, String rot, String rev) {
        this.tempInfo = temp;
        this.rotPeriod = rot;
        this.revPeriod = rev;
    }

    public CelestialBody merge(CelestialBody other) {
        double newMass = this.mass + other.mass;
        Point3D wPos1 = this.position.multiply(this.mass);
        Point3D wPos2 = other.position.multiply(other.mass);
        wPos1.add(wPos2);
        wPos1.scale(1.0 / newMass);

        Point3D v1 = this.getVelocity();
        Point3D v2 = other.getVelocity();
        Point3D wVel1 = v1.multiply(this.mass);
        Point3D wVel2 = v2.multiply(other.mass);
        wVel1.add(wVel2);
        wVel1.scale(1.0 / newMass);

        String newName = this.mass > other.mass ? this.name : other.name;
        Planet merged = new Planet(newName + "+", newMass, 1.0, wPos1.x, wPos1.y, wPos1.z);
        merged.setVelocity(wVel1);
        return merged;
    }

    protected abstract Point3D getVelocity();
    public abstract void updateVelocity(List<CelestialBody> systemBodies);
    public abstract void updatePosition();
    public abstract double totalEnergy();

    public double getMass() { return mass; }
    public double getRadius() { return radius; }
    public Point3D getPosition() { return position; }
    public String getName() { return name; }

    public String getTempInfo() { return tempInfo; }
    public String getRotPeriod() { return rotPeriod; }
    public String getRevPeriod() { return revPeriod; }

    public String getDisplayMass() {
        if (displayMass != null) return displayMass;
        return String.format("%.0f (Sim Units)", mass);
    }

    public String getDisplayRadius() {
        if (displayRadius != null) return displayRadius;
        return String.format("%.2f (Sim Units)", radius);
    }

    @Override
    public int compareTo(CelestialBody other) {
        return this.name.compareTo(other.name);
    }
}
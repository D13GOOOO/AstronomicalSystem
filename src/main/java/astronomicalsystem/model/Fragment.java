package astronomicalsystem.model;

/**
 * Represents a piece of physical debris created from the violent collision of larger celestial bodies.
 * 
 * Fragments inherit standard kinematic physics, allowing them to be pulled by gravity and participate 
 * in further collisions. They act as independent micro-planets that visually represent the kinetic 
 * energy dispersed during glancing blows or total annihilations.
 */
public class Fragment extends Planet {

    /**
     * Constructs a new piece of collision debris.
     *
     * @param name     the unique identifier for this specific fragment.
     * @param mass     the mass of the debris in simulation units.
     * @param radius   the physical radius of the debris.
     * @param position the 3D spatial coordinates where the fragment spawned.
     */
    public Fragment(String name, double mass, double radius, Point3D position) {
        super(name, mass, radius, position);
    }
}
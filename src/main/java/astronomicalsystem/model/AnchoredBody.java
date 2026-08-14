package astronomicalsystem.model;

/**
 * Marker interface for celestial bodies that remain strictly fixed in the 3D scene graph.
 * <p>
 * Anchored bodies (such as central Stars or Black Holes) participate dynamically in the simulation:
 * they exert gravitational pull and can be involved in collisions. However, their spatial position 
 * is immutable during the standard physics tick.
 * </p>
 * <p>
 * <b>Architecture Note:</b> This marker provides the physics engine with an explicit, Liskov-safe 
 * mechanism to recognize stationary anchors. By checking {@code instanceof AnchoredBody}, the engine 
 * avoids coupling to specific concrete classes (like {@code Star}), adhering to the Open/Closed Principle.
 * During collisions, an anchored body effectively behaves as an object with infinite mass, absorbing 
 * kinetic impacts without altering its own position.
 * </p>
 */
public interface AnchoredBody {
    // Marker interface: No methods required. 
    // Its presence alone defines the structural role of the implementing class.
}
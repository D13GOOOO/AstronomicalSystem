package astronomicalsystem.model;

/**
 * Marker interface for celestial bodies that remain fixed in the scene graph.
 * <p>
 * Anchored bodies still participate in gravity and collisions, but they do not
 * advance their own position during the simulation tick. The marker gives the
 * physics layer an explicit and Liskov-safe way to recognize stationary
 * anchors without overloading the kinematic contract.
 * </p>
 */
public interface AnchoredBody {
}
package astronomicalsystem.model;

/**
 * Represents a celestial entity whose motion is governed by a parametric or Keplerian orbital model,
 * rather than dynamic N-body gravitational integration.
 * <p>
 * <b>Architecture Note:</b> This contract is intentionally separated from {@link Kinematic}. 
 * Orbiting bodies (such as Moons) are typically tightly bound to a massive host (like a Planet). 
 * Simulating them using standard discrete N-body physics often leads to numerical instability or requires 
 * extremely small computational time-steps. By isolating them behind this interface, they can advance along 
 * mathematically perfect orbital paths without polluting the physics engine with artificial velocity 
 * semantics or violating the Liskov Substitution Principle (LSP).
 * </p>
 */
public interface OrbitingBody {

    /**
     * Advances the orbital state of the body by one discrete simulation step.
     * <p>
     * Implementations of this method should incrementally update the body's internal orbital phase 
     * (e.g., mean anomaly or orbital angle) and recalculate its absolute spatial {@link Point3D} position 
     * based on the current position of its host anchor.
     * </p>
     */
    void updateOrbit();
}
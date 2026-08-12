package astronomicalsystem.model;

/**
 * Represents a body whose motion is governed by a host-centric orbital model.
 * <p>
 * This contract is intentionally separate from {@link Kinematic}: orbiting
 * bodies can be advanced without exposing artificial velocity semantics or
 * pretending to support the full N-body integration contract.
 * </p>
 */
public interface OrbitingBody {

    /**
     * Advances the orbital state of the body by one discrete simulation step.
     */
    void updateOrbit();
}